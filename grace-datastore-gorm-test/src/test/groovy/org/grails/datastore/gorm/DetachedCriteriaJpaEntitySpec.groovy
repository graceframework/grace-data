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

import org.grails.datastore.gorm.query.transform.ApplyDetachedCriteriaTransform

/**
 * ensure that detached criteria ast transformations work on annotated jpa entities
 */
@ApplyDetachedCriteriaTransform
@Issue('GRAILS-9750')
class DetachedCriteriaJpaEntitySpec extends GormDatastoreSpec {

    @Override
    List getDomainClasses() {
        return [Todo]
    }

    def 'test a where query on a GORM entity'() {
        given: 'a todo'
        new Todo(title: 'todo').save(flush: true)
        session.clear()

        when: 'query without restrictions'
        def results = Todo.findAll { }

        then: 'one todo'
        results.size() == 1

        when: 'query with matching restrictions'
        results = Todo.findAll {
            title == 'todo'
        }

        then: 'one todo'
        results.size() == 1

        when: 'query with not matching restrictions'
        results = Todo.findAll {
            title == 'no match'
        }

        then: 'no todo'
        results.size() == 0
    }

}

@Entity
class Todo {

    Long id
    String title

}
