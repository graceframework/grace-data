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

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormInstanceApi
import org.grails.datastore.gorm.GormValidateable

/**
 * Created by graemerocher on 16/02/2017.
 */
class DeepValidateWithSaveSpec extends GormDatastoreSpec {

    void 'test deep validate parameter'() {
        given:
        def validateable = Mock(GormValidateable)
        validateable.hasErrors() >> true
        def args = [deepValidate: true]

        when:
        GormInstanceApi instanceApi = GormEnhancer.findInstanceApi(TestEntity)
        instanceApi.save(validateable, [deepValidate: true])

        then:
        1 * validateable.validate(args)
    }

}
