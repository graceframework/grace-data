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

class FindOrSaveWhereSpec extends GormDatastoreSpec {

    def "Test findOrSaveWhere returns a new instance if it doesn't exist in the database"() {
        when:
        def entity = TestEntity.findOrSaveWhere(name: 'Lake', age: 63)

        then:
        entity.name == 'Lake'
        entity.age == 63
        entity.id != null
    }

    def 'Test findOrSaveWhere returns a persistent instance if it exists in the database'() {
        given:
        def entityId = new TestEntity(name: 'Levin', age: 64).save().id

        when:
        def entity = TestEntity.findOrSaveWhere(name: 'Levin', age: 64)

        then:
        entity.id != null
        entity.id == entityId
        entity.name == 'Levin'
        entity.age == 64
    }

}
