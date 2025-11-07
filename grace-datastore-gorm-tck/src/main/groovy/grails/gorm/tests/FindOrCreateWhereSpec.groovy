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

class FindOrCreateWhereSpec extends GormDatastoreSpec {

    def "Test findOrCreateWhere returns a new instance if it doesn't exist in the database"() {
        when:
        def entity = TestEntity.findOrCreateWhere(name: 'Fripp', age: 64)

        then:
        entity.name == 'Fripp'
        entity.age == 64
        entity.id == null
    }

    def 'Test findOrCreateWhere returns a persistent instance if it exists in the database'() {
        given:
        def entityId = new TestEntity(name: 'Belew', age: 61).save().id

        when:
        def entity = TestEntity.findOrCreateWhere(name: 'Belew', age: 61)

        then:
        entity.id != null
        entity.id == entityId
        entity.name == 'Belew'
        entity.age == 61
    }

}
