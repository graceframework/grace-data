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

/**
 * @author graemerocher
 */
class ListOrderBySpec extends GormDatastoreSpec {

    void 'Test listOrderBy property name method'() {
        given:
        def child = new ChildEntity(name: 'Child')
        new TestEntity(age: 30, name: 'Bob', child: child).save()
        new TestEntity(age: 55, name: 'Fred', child: child).save()
        new TestEntity(age: 17, name: 'Jack', child: child).save()

        when:
        def results = TestEntity.listOrderByAge()

        then:
        results.size() == 3
        results[0].name == 'Jack'
        results[1].name == 'Bob'
        results[2].name == 'Fred'

        when:
        results = TestEntity.listOrderByAge(order: 'desc')

        then:
        results.size() == 3
        results[2].name == 'Jack'
        results[1].name == 'Bob'
        results[0].name == 'Fred'
    }

}
