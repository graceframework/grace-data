/*
 * Copyright 2010-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.gorm.tests

import grails.gorm.annotation.Entity

/**
 * @author graemerocher
 */
class InheritanceSpec extends GormDatastoreSpec {

    @Override
    List getDomainClasses() {
        return super.getDomainClasses() + [Practice]
    }

    void 'Test inheritance with dynamic finder'() {
        given:
        def city = new City([code: 'UK', name: 'London', longitude: 49.1, latitude: 53.1])
        def country = new Country([code: 'UK', name: 'United Kingdom', population: 10000000])

        city.save()
        country.save(flush: true)
        session.clear()

        when:
        def locations = Location.findAllByCode('UK')
        def cities = City.findAllByCode('UK')
        def countries = Country.findAllByCode('UK')

        then:
        locations.size() == 2
        cities.size() == 1
        countries.size() == 1
        cities[0].name == 'London'
        countries[0].name == 'United Kingdom'
    }

    void 'Test querying with inheritance'() {
        given:
        def city = new City([code: 'LON', name: 'London', longitude: 49.1, latitude: 53.1])
        def location = new Location([code: 'XX', name: 'The World'])
        def country = new Country([code: 'UK', name: 'United Kingdom', population: 10000000])

        country.save()
        city.save()
        location.save()

        session.flush()

        when:
        city = City.get(city.id)
        def london = Location.get(city.id)
        country = Location.findByName('United Kingdom')
        def london2 = Location.findByName('London')

        then:
        City.count() == 1
        Country.count() == 1
        Location.count() == 3

        city != null
        city instanceof City
        london instanceof City
        london2 instanceof City
        london2.name == 'London'
        london2.longitude == 49.1
        london2.code == 'LON'

        country instanceof Country
        country.code == 'UK'
        country.population == 10000000
    }

    void 'Test hasMany with inheritance should return appropriate class'() {
        given: 'a practice with two locations'
        Practice practice = new Practice(name: 'Test practice')
        practice.addToLocations(new City(name: 'Austin', latitude: 30.2672, longitude: 97.7431))
        practice.addToLocations(new Country(name: 'United States'))
        practice.save()
        session.flush()

        expect:
        Location.findByName('Austin').class == City
    }

    def clearSession() {
        City.withSession { session -> session.flush() }
    }

}

@Entity
class Practice implements Serializable {

//    Long id
    Long version
    String name
    static hasMany = [locations: Location]

}

@Entity
class Location implements Serializable {

//    Long id
    Long version
    String name
    String code = 'DEFAULT'

    def namedAndCode() {
        "$name - $code"
    }

    static mapping = {
        name index: true
        code index: true
    }

}

@Entity
class City extends Location {

    BigDecimal latitude
    BigDecimal longitude

}

@Entity
class Country extends Location {

    Integer population = 0

    static hasMany = [residents: Person]
    Set residents

}
