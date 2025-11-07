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

import org.grails.datastore.mapping.dirty.checking.DirtyCheckable

/**
 * Tests the unique constraint
 */

class UniqueConstraintSpec extends GormDatastoreSpec {

    @Override
    List getDomainClasses() {
        [UniqueGroup, GroupWithin]
    }

}

@Entity
class UniqueGroup implements Serializable, DirtyCheckable {

    Long id
    Long version
    String name
    String desc
    static constraints = {
        name unique: true, index: true
        desc nullable: true
    }

}

@Entity
class GroupWithin implements Serializable {

    Long id
    Long version
    String name
    String org

    static constraints = {
        name unique: 'org', index: true
        org index: true
    }

}
