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

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class UUIDTypeIdentifierSpec extends GormDatastoreSpec {

    void 'Test that an id with type of java.util.UUID is correctly generated'() {
        when: 'A domain with a UUID is saved'
        def dm = new SimpleUUIDModel(name: 'My Doc').save()

        then: 'The UUID is correctly generated'
        dm != null
        dm.id != null
        SimpleUUIDModel.count() == 1

        when: 'Another entity is saved'
        new SimpleUUIDModel(name: 'Another').save()
        then: 'There are 2'
        SimpleUUIDModel.count() == 2
    }

    @Override
    List getDomainClasses() {
        [SimpleUUIDModel]
    }

}

@Entity
class SimpleUUIDModel {

    UUID id
    String name

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name blank: false
    }

}
