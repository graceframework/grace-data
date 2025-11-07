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

import spock.lang.Issue

/**
 * Created by graemerocher on 16/02/2017.
 */
class SingleResultSpec extends GormDatastoreSpec {

    @Issue('https://github.com/grails/grails-data-mapping/issues/872')
    void 'test single result state'() {
        when:
        def query = session.createQuery(TestEntity)

        then:
        query.uniqueResult == false

        when:
        def result = query.singleResult()
        then:
        query.uniqueResult == true
    }

}
