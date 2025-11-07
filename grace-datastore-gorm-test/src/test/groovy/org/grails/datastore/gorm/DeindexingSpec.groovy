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

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

/**
 * @author Daniel Wiell
 */
class DeindexingSpec extends GormDatastoreSpec {

    def 'Null is de-indexed'() {
        def author = new AuthorWithPseudonym(name: 'Samuel Clemens').save(failOnError: true)
        author.pseudonym = 'Mark Twain'
        author.save(failOnError: true)

        expect:
        !AuthorWithPseudonym.findByPseudonymIsNull()
    }

    @Override
    List getDomainClasses() {
        [AuthorWithPseudonym]
    }

}

@Entity
class AuthorWithPseudonym {

    Long id
    Integer version
    String name
    String pseudonym

    static constraints = {
        pseudonym nullable: true
    }

}
