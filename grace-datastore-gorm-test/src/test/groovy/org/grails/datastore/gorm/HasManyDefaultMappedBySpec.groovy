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

import spock.lang.Ignore

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

import org.grails.datastore.mapping.model.types.Association

class HasManyDefaultMappedBySpec extends GormDatastoreSpec {

    @Ignore
    void 'Test that has-many with multiple potential matches for the other side matches correctly'() {
        when: 'A has many with multiple potential matching sides is retrieved'
        def entity = session.datastore.mappingContext.getPersistentEntity(MyDomain.name)
        Association p = entity.getPropertyByName('childs')

        then: 'The other side is correctly mapped'
        p != null
        p.inverseSide != null
        p.inverseSide.name == 'parent'
    }

    @Override
    List getDomainClasses() {
        [MyDomain, ChildDomain]
    }

}

@Entity
class MyDomain {

    Long id
    Set childs

    static hasMany = [childs: ChildDomain]

}

@Entity
class ChildDomain {

    Long id
    static belongsTo = [parent: MyDomain]

    def getSomething() {
    }

    def myService

    MyDomain parent

}
