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
package org.grails.datastore.mapping.proxy

import spock.lang.Specification

import grails.gorm.annotation.Entity

import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.keyvalue.mapping.config.KeyValueMappingContext

/**
 * Created by graemerocher on 04/10/2016.
 */
class JavassistProxyFactorySpec extends Specification {

    void 'test is association initialized'() {
        given:
        JavassistProxyFactory proxyFactory = new JavassistProxyFactory()
        def session = Mock(Session)
        def mappingContext = new KeyValueMappingContext('test')
        mappingContext.addPersistentEntities(Book, Author)
        session.getMappingContext() >> mappingContext
        Book book = proxyFactory.createProxy(session, Book, 1L)
        Author a = new Author(book: book)
        a.id == 2L
        expect:
        proxyFactory.isProxy(book)
        !proxyFactory.isProxy(a)
        proxyFactory.getIdentifier(book) == 1L
        proxyFactory.getIdentifier(a) == null // not a proxy
        !proxyFactory.isInitialized(book)
        !proxyFactory.isInitialized(a, 'book')
    }

}

@Entity
class Book {

    Long id
    String title

}

@Entity
class Author {

    String name
    Book book

}
