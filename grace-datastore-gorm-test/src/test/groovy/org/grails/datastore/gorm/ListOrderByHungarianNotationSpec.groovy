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

import grails.gorm.tests.ClassWithHungarianNotation
import grails.gorm.tests.GormDatastoreSpec

/**
 * Created by sdelamo on 12/10/2017.
 */
class ListOrderByHungarianNotationSpec extends GormDatastoreSpec {

    void 'test dynamic finder of properties with hungarian notation'() {
        when:
        new ClassWithHungarianNotation(iSize: 2).save()
        new ClassWithHungarianNotation(iSize: 3).save()

        then:
        ClassWithHungarianNotation.listOrderByISize(order: 'desc')*.iSize == [3, 2]
    }

    List getDomainClasses() {
        [ClassWithHungarianNotation]
    }

}
