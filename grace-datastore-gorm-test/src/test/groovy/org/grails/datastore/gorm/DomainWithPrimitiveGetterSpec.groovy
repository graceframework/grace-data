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

class DomainWithPrimitiveGetterSpec extends GormDatastoreSpec {

    @Issue('GRAILS-8788')
    void 'Test that a domain that contains a primitive getter maps correctly'() {
        when: 'The domain model is saved'
        def author = new DomainWithPrimitiveGetterAuthor(name: 'Stephen King')
        author.save()
        def book = new DomainWithPrimitiveGetterBook(title: 'The Stand', author: author)
        book.save flush: true
        then: 'The save executes correctly'
        DomainWithPrimitiveGetterBook.count() == 1
        DomainWithPrimitiveGetterAuthor.count() == 1
    }

    @Override
    List getDomainClasses() {
        [DomainWithPrimitiveGetterAuthor, DomainWithPrimitiveGetterBook]
    }

}

@Entity
class DomainWithPrimitiveGetterBook {

    Long id
    String title
    DomainWithPrimitiveGetterAuthor author

    int getValue(int param) {
        return 0
    }

}

@Entity
class DomainWithPrimitiveGetterAuthor {

    Long id
    String name
    static hasMany = [books: DomainWithPrimitiveGetterBook]

}
