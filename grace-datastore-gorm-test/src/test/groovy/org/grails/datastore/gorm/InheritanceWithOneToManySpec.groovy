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

import spock.lang.Issue

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class InheritanceWithOneToManySpec extends GormDatastoreSpec {

    @Issue('GRAILS-9010')
    void 'Test that a one-to-many cascades to an association featuring inheritance'() {
        when: 'A domain model with an association featuring inheritance is saved'
        def group = new Group(name: 'my group')
        def subMember = new SubMember(name: 'my name', extraName: 'extra name', externalId: 'blah')
        group.addToMembers subMember
        group.save(failOnError: true, flush: true)
        session.clear()

        then: 'The association is correctly saved'
        Group.count() == 1
        SubMember.count() == 1
    }

    @Override
    List getDomainClasses() {
        [Group, Member, SubMember]
    }

}

@Entity
class Group {

    Long id
    String name
    static hasMany = [members: Member]
    Collection members

}

@Entity
class Member {

    Long id
    String name
    String externalId

}

@Entity
class SubMember extends Member {

    String extraName

}
