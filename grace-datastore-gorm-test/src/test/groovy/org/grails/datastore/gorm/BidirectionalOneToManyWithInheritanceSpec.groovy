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
 * @author graemerocher
 */
class BidirectionalOneToManyWithInheritanceSpec extends GormDatastoreSpec {

    void 'Test a bidirectional one-to-many association with inheritance'() {
        given:
        def doc = new Documentation()

        doc.addToConfigurationItems(new ChangeRequest())
           .addToConfigurationItems(new Documentation())

        when:
        doc.save(flush: true)
        session.clear()
        doc = Documentation.get(1)

        then:
        doc.configurationItems.size() == 2
    }

    @Override
    List getDomainClasses() {
        [ConfigurationItem, Documentation, ChangeRequest]
    }

}

@Entity
class ConfigurationItem {

    Long id
    Long version
    ConfigurationItem parent

    Set configurationItems

    static hasMany = [configurationItems: ConfigurationItem]
    static mappedBy = [configurationItems: 'parent']
    static belongsTo = [ConfigurationItem]
    static constraints = {
        parent(nullable: true)
    }

}

@Entity
class Documentation extends ConfigurationItem {

    Long id
    Long version

}

@Entity
class ChangeRequest extends ConfigurationItem {

    Long id
    Long version

}
