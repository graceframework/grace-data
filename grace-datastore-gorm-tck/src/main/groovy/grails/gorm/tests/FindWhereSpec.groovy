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

class FindWhereSpec extends GormDatastoreSpec {

    def 'Test findWhere returns a matching Instance'() {
        given:
        def entityId = new TestEntity(name: 'David', age: 27).save().id

        when:
        def entity = TestEntity.findWhere(name: 'David')

        then:
        entity.name == 'David'
        entity.age == 27
        entity.id == entityId
    }

    def 'Test findWhere with a GString property'() {
        given:
        def entityId = new TestEntity(name: 'David', age: 27).save().id
        def property = 'name'

        when:
        def entity = TestEntity.findWhere("${property}": 'David')

        then:
        entity.name == 'David'
        entity.age == 27
        entity.id == entityId
    }

    def 'Test findAllWhere returns a matching Instance'() {
        given:
        def entityId = new TestEntity(name: 'David', age: 27).save().id

        when:
        def entity = TestEntity.findAllWhere(name: 'David')
        then:
        entity[0].name == 'David'
        entity[0].age == 27
        entity[0].id == entityId
    }

    def 'Test findAllWhere with a GString property'() {
        given:
        def entityId = new TestEntity(name: 'David', age: 27).save().id
        def property = 'name'

        when:
        def entity = TestEntity.findAllWhere("${property}": 'David')

        then:
        entity[0].name == 'David'
        entity[0].age == 27
        entity[0].id == entityId
    }

}
