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

/**
 * @author graemerocher
 */
class FindByMethodSpec extends GormDatastoreSpec {

    void 'Test Using AND Multiple Times In A Dynamic Finder'() {
        given:
        new Person(firstName: 'Jake', lastName: 'Brown', age: 11).save()
        new Person(firstName: 'Zack', lastName: 'Brown', age: 14).save()
        new Person(firstName: 'Jeff', lastName: 'Brown', age: 41).save()
        new Person(firstName: 'Zack', lastName: 'Galifianakis', age: 41).save()

        when:
        def people = Person.findAllByFirstNameAndLastNameAndAge('Jeff', 'Brown', 1)

        then:
        people?.size() == 0

        when:
        people = Person.findAllByFirstNameAndLastNameAndAgeGreaterThan('Zack', 'Brown', 20)

        then:
        people?.size() == 0

        when:
        people = Person.findAllByFirstNameAndLastNameAndAgeGreaterThan('Zack', 'Brown', 8)

        then:
        people?.size() == 1
        people[0].age == 14

        when:
        def cnt = Person.countByFirstNameAndLastNameAndAge('Jake', 'Brown', 11)

        then:
        cnt == 1

        when:
        cnt = Person.countByFirstNameAndLastNameAndAgeInList('Zack', 'Brown', [12, 13, 14, 15])

        then:
        cnt == 1
    }

    void 'Test Using OR Multiple Times In A Dynamic Finder'() {
        given:
        new Person(firstName: 'Jake', lastName: 'Brown', age: 11).save()
        new Person(firstName: 'Zack', lastName: 'Brown', age: 14).save()
        new Person(firstName: 'Jeff', lastName: 'Brown', age: 41).save()
        new Person(firstName: 'Zack', lastName: 'Galifianakis', age: 41).save()

        when:
        def people = Person.findAllByFirstNameOrLastNameOrAge('Zack', 'Tyler', 125)

        then:
        people?.size() == 2

        when:
        people = Person.findAllByFirstNameOrLastNameOrAge('Zack', 'Brown', 125)

        then:
        people?.size() == 4

        when:
        def cnt = Person.countByFirstNameOrLastNameOrAgeInList('Jeff', 'Wilson', [11, 41])

        then:
        cnt == 3
    }

    void testBooleanPropertyQuery() {
        given:
        new Highway(bypassed: true, name: 'Bypassed Highway').save()
        new Highway(bypassed: true, name: 'Bypassed Highway').save()
        new Highway(bypassed: false, name: 'Not Bypassed Highway').save()
        new Highway(bypassed: false, name: 'Not Bypassed Highway').save()

        when:
        def highways = Highway.findAllBypassedByName('Not Bypassed Highway')

        then:
        highways.size() == 0

        when:
        highways = Highway.findAllNotBypassedByName('Not Bypassed Highway')

        then:
        highways?.size() == 2
        highways[0].name == 'Not Bypassed Highway'
        highways[1].name == 'Not Bypassed Highway'

        when:
        highways = Highway.findAllBypassedByName('Bypassed Highway')

        then:
        highways?.size() == 2
        highways[0].name == 'Bypassed Highway'
        highways[1].name == 'Bypassed Highway'

        when:
        highways = Highway.findAllNotBypassedByName('Bypassed Highway')
        then:
        highways?.size() == 0

        when:
        highways = Highway.findAllBypassed()
        then:
        highways?.size() == 2
        highways[0].name == 'Bypassed Highway'
        highways[1].name == 'Bypassed Highway'

        when:
        highways = Highway.findAllNotBypassed()
        then:
        highways?.size() == 2
        highways[0].name == 'Not Bypassed Highway'
        highways[1].name == 'Not Bypassed Highway'

        when:
        def highway = Highway.findNotBypassed()
        then:
        highway?.name == 'Not Bypassed Highway'

        when:
        highway = Highway.findBypassed()
        then:
        highway?.name == 'Bypassed Highway'

        when:
        highway = Highway.findNotBypassedByName('Not Bypassed Highway')
        then:
        highway?.name == 'Not Bypassed Highway'

        when:
        highway = Highway.findBypassedByName('Bypassed Highway')
        then:
        highway?.name == 'Bypassed Highway'

        when:
        Book.newInstance(author: 'Jeff', title: 'Fly Fishing For Everyone', published: false).save()
        Book.newInstance(author: 'Jeff', title: 'DGGv2', published: true).save()
        Book.newInstance(author: 'Graeme', title: 'DGGv2', published: true).save()
        Book.newInstance(author: 'Dierk', title: 'GINA', published: true).save()

        def book = Book.findPublishedByAuthor('Jeff')
        then:
        book.author == 'Jeff'
        book.title == 'DGGv2'

        when:
        book = Book.findPublishedByAuthor('Graeme')
        then:
        book.author == 'Graeme'
        book.title == 'DGGv2'

        when:
        book = Book.findPublishedByTitleAndAuthor('DGGv2', 'Jeff')
        then:
        book.author == 'Jeff'
        book.title == 'DGGv2'

        when:
        book = Book.findNotPublishedByAuthor('Jeff')
        then:
        book.title == 'Fly Fishing For Everyone'

        when:
        book = Book.findPublishedByTitleOrAuthor('Fly Fishing For Everyone', 'Dierk')
        then:
        book.title == 'GINA'
        Book.findPublished() != null

        when:
        book = Book.findNotPublished()
        then:
        book?.title == 'Fly Fishing For Everyone'

        when:
        def books = Book.findAllPublishedByTitle('DGGv2')
        then:
        books?.size() == 2

        when:
        books = Book.findAllPublished()
        then:
        books?.size() == 3

        when:
        books = Book.findAllNotPublished()
        then:
        books?.size() == 1

        when:
        books = Book.findAllPublishedByTitleAndAuthor('DGGv2', 'Graeme')
        then:
        books?.size() == 1

        when:
        books = Book.findAllPublishedByAuthorOrTitle('Graeme', 'GINA')
        then:
        books?.size() == 2

        when:
        books = Book.findAllNotPublishedByAuthor('Jeff')
        then:
        books?.size() == 1

        when:
        books = Book.findAllNotPublishedByAuthor('Graeme')
        then:
        books?.size() == 0
    }

    void 'Test findOrCreateBy For A Record That Does Not Exist In The Database'() {
        when:
        def book = Book.findOrCreateByAuthor('Someone')

        then:
        book.author == 'Someone'
        book.title == null
        book.id == null
    }

    void 'Test findOrCreateBy With An AND Clause'() {
        when:
        def book = Book.findOrCreateByAuthorAndTitle('Someone', 'Something')

        then:
        book.author == 'Someone'
        book.title == 'Something'
        book.id == null
    }

    void 'Test findOrCreateBy Throws Exception If An OR Clause Is Used'() {
        when:
        Book.findOrCreateByAuthorOrTitle('Someone', 'Something')

        then:
        thrown(MissingMethodException)
    }

    void 'Test findOrSaveBy For A Record That Does Not Exist In The Database'() {
        when:
        def book = Book.findOrSaveByAuthorAndTitle('Some New Author', 'Some New Title')

        then:
        book.author == 'Some New Author'
        book.title == 'Some New Title'
        book.id != null
    }

    void 'Test findOrSaveBy For A Record That Does Exist In The Database'() {
        given:
        def originalId = new Book(author: 'Some Author', title: 'Some Title').save().id

        when:
        def book = Book.findOrSaveByAuthor('Some Author')

        then:
        book.author == 'Some Author'
        book.title == 'Some Title'
        book.id == originalId
    }

    void 'Test patterns which shold throw MissingMethodException'() {
        // Redis doesn't like Like queries...
//        when:
//            Book.findOrCreateByAuthorLike('B%')
//
//        then:
//            thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorInList(['Jeff'])

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorOrTitle('Jim', 'Title')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorNotEqual('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorGreaterThan('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorLessThan('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorBetween('A', 'B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorGreaterThanEquals('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrCreateByAuthorLessThanEquals('B')

        then:
        thrown MissingMethodException

        // GemFire doesn't like these...
//        when:
//            Book.findOrCreateByAuthorIlike('B%')
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrCreateByAuthorRlike('B%')
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrCreateByAuthorIsNull()
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrCreateByAuthorIsNotNull()
//
//        then:
//            thrown MissingMethodException

        // Redis doesn't like Like queries...
//        when:
//            Book.findOrSaveByAuthorLike('B%')
//
//        then:
//            thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorInList(['Jeff'])

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorOrTitle('Jim', 'Title')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorNotEqual('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorGreaterThan('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorLessThan('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorBetween('A', 'B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorGreaterThanEquals('B')

        then:
        thrown MissingMethodException

        when:
        Book.findOrSaveByAuthorLessThanEquals('B')

        then:
        thrown MissingMethodException

        // GemFire doesn't like these...
//        when:
//            Book.findOrSaveByAuthorIlike('B%')
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrSaveByAuthorRlike('B%')
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrSaveByAuthorIsNull()
//
//        then:
//            thrown MissingMethodException

//        when:
//            Book.findOrSaveByAuthorIsNotNull()
//
//        then:
//            thrown MissingMethodException
    }

}

@Entity
class Highway implements Serializable {

    Long id
    Long version
    Boolean bypassed
    String name

    static mapping = {
        bypassed index: true
        name index: true
    }

}

@Entity
class Book implements Serializable {

    Long id
    Long version
    String author
    String title
    Boolean published = false

    static mapping = {
        published index: true
        title index: true
        author index: true
    }

}
