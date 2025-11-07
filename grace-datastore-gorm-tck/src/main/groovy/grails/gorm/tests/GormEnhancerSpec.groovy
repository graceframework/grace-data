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
 * @author graemerocher
 */
class GormEnhancerSpec extends GormDatastoreSpec {

    void 'Test basic CRUD operations'() {
        given:
        def t

        when:
        t = TestEntity.get(1)

        then:
        t == null

        when:
        t = new TestEntity(name: 'Bob', child: new ChildEntity(name: 'Child'))
        t.save()

        then:
        t.id != null

        when:
        def results = TestEntity.list()

        then:
        results.size() == 1
        results[0].name == 'Bob'

        when:
        t = TestEntity.get(t.id)

        then:
        t != null
        t.name == 'Bob'
    }

    void 'Test simple dynamic finder'() {
        given:
        def t = new TestEntity(name: 'Bob', child: new ChildEntity(name: 'Child'))
        t.save()

        t = new TestEntity(name: 'Fred', child: new ChildEntity(name: 'Child'))
        t.save()

        when:
        def results = TestEntity.list()
        def bob = TestEntity.findByName('Bob')

        then:
        results.size() == 2
        bob != null
        bob.name == 'Bob'
    }

    void 'Test dynamic finder with disjunction'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def results = TestEntity.findAllByNameOrAge('Barney', 40)
        def barney = results.find { it.name == 'Barney' }
        def bob = results.find {
            it.age == 40
        }

        then:
        TestEntity.count() == 3
        results.size() == 2
        barney != null
        barney.age == 42
        bob != null
        bob.name == 'Bob'
    }

    void 'Test getAll() method'() {
        given:
        def age = 40
        def ids = []
        ['Bob', 'Fred', 'Barney'].each {
            ids.add(new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save().id)
        }

        when:
        def results = TestEntity.getAll(ids[0], ids[1])

        then:
        results.size() == 2
    }

    void 'Test ident() method'() {
        given:
        def t

        when:
        t = new TestEntity(name: 'Bob', child: new ChildEntity(name: 'Child'))
        t.save()

        then:
        t.id != null
        t.id == t.ident()
    }

    void 'Test dynamic finder with pagination parameters'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def total = TestEntity.count()

        then:
        total == 4

        TestEntity.findAllByNameOrAge('Barney', 40).size() == 2
        TestEntity.findAllByNameOrAge('Barney', 40, [max: 1]).size() == 1
    }

    void 'Test in list query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def total = TestEntity.count()

        then:
        total == 4
        TestEntity.findAllByNameInList(['Fred', 'Frank']).size() == 2
        TestEntity.findAllByNameInList(['Joe', 'Frank']).size() == 1
        TestEntity.findAllByNameInList(['Jeff', 'Jack']).size() == 0
        TestEntity.findAllByNameInListOrName(['Joe', 'Frank'], 'Bob').size() == 2
    }

    void 'Test like query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank', 'frita'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def results = TestEntity.findAllByNameLike('Fr%')

        then:
        results.size() == 2
        results.find { it.name == 'Fred' } != null
        results.find { it.name == 'Frank' } != null
    }

    @Ignore
    void 'Test ilike query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney', 'Frank', 'frita'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def results = TestEntity.findAllByNameIlike('fr%')

        then:
        results.size() == 3
        results.find { it.name == 'Fred' } != null
        results.find { it.name == 'Frank' } != null
        results.find { it.name == 'frita' } != null
    }

    void 'Test count by query'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def total = TestEntity.count()

        then:
        total == 3
        TestEntity.list().size() == 3
        TestEntity.countByNameOrAge('Barney', 40) == 2
        TestEntity.countByNameAndAge('Bob', 40) == 1
    }

    void 'Test dynamic finder with conjunction'() {
        given:
        def age = 40
        ['Bob', 'Fred', 'Barney'].each {
            new TestEntity(name: it, age: age++, child: new ChildEntity(name: "$it Child")).save()
        }

        when:
        def total = TestEntity.count()

        then:
        total == 3
        TestEntity.list().size() == 3

        TestEntity.findByNameAndAge('Bob', 40)
        !TestEntity.findByNameAndAge('Bob', 41)
    }

    void 'Test count() method'() {
        given:
        def t

        when:
        t = new TestEntity(name: 'Bob', child: new ChildEntity(name: 'Child'))
        t.save()

        then:
        TestEntity.count() == 1

        when:
        t = new TestEntity(name: 'Fred', child: new ChildEntity(name: 'Child'))
        t.save()

        then:
        TestEntity.count() == 2
    }

}
