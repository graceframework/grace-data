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
package org.grails.datastore.gorm

import spock.lang.Shared

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class EmbeddedAssociationSpec extends GormDatastoreSpec {

    @Shared
    Date now = new Date()

    void 'Test persistence of embedded entities'() {
        given:
        def i = new Individual(name: 'Bob', address: new Address(postCode: '30483'), bio: new Bio(birthday: new Birthday(now)))

        i.save(flush: true)
        session.clear()

        when:
        i = Individual.findByName('Bob')

        then:
        i != null
        i.name == 'Bob'
        i.address != null
        i.address.postCode == '30483'
        i.bio.birthday.date == now
    }

    @Override
    List getDomainClasses() {
        [Individual, Address]
    }

}

@Entity
class Individual {

    Long id
    String name
    Address address
    Bio bio

    static embedded = ['address', 'bio']

    static mapping = {
        name index: true
    }

}

@Entity
class Address {

    Long id
    String postCode

}

// Test embedded associations with custom types
class Bio {

    Birthday birthday

}
