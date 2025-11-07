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

class EmbeddedNonEntityAssociationSpec extends GormDatastoreSpec {

    void 'Test persistence of embedded entities'() {
        given:
        def i = new Being(name: 'Bob', address: new ResidentialAddress(postCode: '30483'))

        i.save(flush: true)
        session.clear()

        when:
        i = Being.findByName('Bob')

        then:
        i != null
        i.name == 'Bob'
        i.address != null
        i.address.postCode == '30483'
    }

    @Override
    List getDomainClasses() {
        [Being]
    }

}

@Entity
class Being {

    Long id
    String name
    ResidentialAddress address
    static embedded = ['address']

    static mapping = {
        name index: true
    }

}

class ResidentialAddress {

    String postCode

}
