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
package org.grails.datastore.mapping

import grails.gorm.tests.GormDatastoreSpec
import grails.gorm.annotation.Entity

/**
 * Created by Jim on 8/19/2016.
 */
class EntityReflectorSpec extends GormDatastoreSpec {

    void 'test getAssociationId with a null association'() {
        when:
        LibraryBook book = new LibraryBook()
        Serializable id = book.getAssociationId('library')

        then:
        noExceptionThrown()
        id == null
    }

    @Override
    List getDomainClasses() {
        [Library, LibraryBook]
    }

}

@Entity
class Library {

    Long id

}

@Entity
class LibraryBook {

    Long id
    Library library

}
