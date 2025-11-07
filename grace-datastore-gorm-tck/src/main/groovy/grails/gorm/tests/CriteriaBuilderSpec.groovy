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

import spock.lang.Ignore

/**
 * Abstract base test for criteria queries. Subclasses should do the necessary setup to configure GORM
 */
class CriteriaBuilderSpec extends GormDatastoreSpec {

    void 'Test count distinct projection'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        new TestEntity(name: 'Chuck', age: age - 1, child: new ChildEntity(name: 'Chuckie')).save()

        ChildEntity.count() == 5

        def criteria = TestEntity.createCriteria()

        when:
        def result = criteria.get {
            projections {
                countDistinct 'age'
            }
        }

        then:
        result == 4
    }

    @Ignore
    // ignored this test because the id() projection does not actually exist in GORM for Hibernate
    void 'Test id projection'() {
        given:
        def entity = new TestEntity(name: 'Bob', age: 44, child: new ChildEntity(name: 'Child')).save(flush: true)

        when:
        def result = TestEntity.createCriteria().get {
            projections { id() }
            idEq entity.id
        }

        then:
        result != null
        result == entity.id
    }

    void 'Test idEq method'() {
        given:
        def entity = new TestEntity(name: 'Bob', age: 44, child: new ChildEntity(name: 'Child')).save(flush: true)

        when:
        def result = TestEntity.createCriteria().get { idEq entity.id }

        then:
        result != null
        result.name == 'Bob'
    }

    void 'Test disjunction query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each { new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save() }
        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            or {
                like('name', 'B%')
                eq('age', 41)
            }
        }

        then:
        results.size() == 3
    }

    void 'Test conjunction query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each { new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save() }

        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            and {
                like('name', 'B%')
                eq('age', 40)
            }
        }

        then:
        results.size() == 1
    }

    void 'Test list() query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            like('name', 'B%')
        }

        then:
        results.size() == 2

        when:
        criteria = TestEntity.createCriteria()
        results = criteria.list {
            like('name', 'B%')
            maxResults 1
        }

        then:
        results.size() == 1
    }

    void 'Test count()'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        def criteria = TestEntity.createCriteria()

        when:
        def result = criteria.count {
            like('name', 'B%')
        }

        then:
        result == 2
    }

    void 'Test obtain a single result'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        def criteria = TestEntity.createCriteria()

        when:
        def result = criteria.get {
            eq('name', 'Bob')
        }

        then:
        result != null
        result.name == 'Bob'
    }

    void 'Test order by a property name'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            like('name', 'B%')
            order 'age'
        }

        then:
        results[0].name == 'Bob'
        results[1].name == 'Barney'

        when:
        criteria = TestEntity.createCriteria()
        results = criteria.list {
            like('name', 'B%')
            order 'age', 'desc'
        }

        then:
        results[0].name == 'Barney'
        results[1].name == 'Bob'
    }

    void 'Test get minimum value with projection'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }
        Thread.sleep 500

        def criteria = TestEntity.createCriteria()

        when:
        def result = criteria.get {
            projections {
                min 'age'
            }
        }

        then:
        result == 40

        when:
        criteria = TestEntity.createCriteria()
        result = criteria.get {
            projections {
                max 'age'
            }
        }

        then:
        result == 43

        when:
        criteria = TestEntity.createCriteria()
        def results = criteria.list {
            projections {
                max 'age'
                min 'age'
            }
        }.flatten()

        then:
        results.size() == 2
        results[0] == 43
        results[1] == 40
        results == [43, 40]
    }

    void 'Test obtain property value using projection'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            projections {
                property 'age'
            }
        }

        then:
        results.sort() == [40, 41, 42, 43]
    }

    void 'Test obtain association entity using property projection'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        ChildEntity.count() == 4

        def criteria = TestEntity.createCriteria()

        when:
        def results = criteria.list {
            projections {
                property 'child'
            }
        }

        then:
        results.find { it.name == 'Bob Child' }
        results.find { it.name == 'Fred Child' }
        results.find { it.name == 'Barney Child' }
        results.find { it.name == 'Frank Child' }
    }

}
