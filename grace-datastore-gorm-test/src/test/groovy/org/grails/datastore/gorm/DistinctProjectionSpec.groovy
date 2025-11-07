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

import grails.gorm.tests.GormDatastoreSpec
import grails.gorm.tests.Person

class DistinctProjectionSpec extends GormDatastoreSpec {

    def 'Test that using the distinct projection returns distinct results'() {
        given: 'Some people with the same last names'
        new Person(firstName: 'Homer', lastName: 'Simpson').save()
        new Person(firstName: 'Bart', lastName: 'Simpson').save()
        new Person(firstName: 'Barney', lastName: 'Rubble').save(flush: true)

        when: 'We query with criteria for distinct surnames'
        def results = Person.withCriteria {
            projections {
                distinct 'lastName'
            }
        }.sort()

        then: 'The correct results are returned'
        results == ['Rubble', 'Simpson']
    }

}
