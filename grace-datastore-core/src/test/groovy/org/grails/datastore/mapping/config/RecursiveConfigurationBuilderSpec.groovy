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
package org.grails.datastore.mapping.config

import org.grails.datastore.mapping.core.DatastoreUtils
import org.springframework.core.env.PropertyResolver
import spock.lang.Specification

class RecursiveConfigurationBuilderSpec extends Specification {

    void "recursive builders don't cause stackoverflow"() {
        given:
        PropertyResolver config = DatastoreUtils.createPropertyResolver([:])

        when:
        MongoConnectionSourceSettingsBuilder builder = new MongoConnectionSourceSettingsBuilder(config, 'grails.mongodb');
        MongoConnectionSourceSettings settings = builder.build()

        then:
        noExceptionThrown()
        !settings.options.autoEncryptionSettings.bypassAutoEncryption
    }

    void 'recursive builder get configured correctly'() {
        given:
        PropertyResolver config = DatastoreUtils.createPropertyResolver(['grails.mongodb.options.autoEncryptionSettings.bypassAutoEncryption': true])

        when:
        MongoConnectionSourceSettingsBuilder builder = new MongoConnectionSourceSettingsBuilder(config, 'grails.mongodb')
        MongoConnectionSourceSettings settings = builder.build()

        then:
        noExceptionThrown()
        settings.options.autoEncryptionSettings.bypassAutoEncryption
    }

    class MongoConnectionSourceSettingsBuilder extends ConfigurationBuilder<MongoConnectionSourceSettings, MongoConnectionSourceSettings>{

        MongoConnectionSourceSettingsBuilder(PropertyResolver propertyResolver, String configurationPrefix, Object fallBackConfiguration, String builderMethodPrefix) {
            super(propertyResolver, configurationPrefix, fallBackConfiguration, builderMethodPrefix)
        }

        MongoConnectionSourceSettingsBuilder(PropertyResolver propertyResolver, String configurationPrefix, Object fallBackConfiguration) {
            super(propertyResolver, configurationPrefix, fallBackConfiguration)
        }

        MongoConnectionSourceSettingsBuilder(PropertyResolver propertyResolver, String configurationPrefix) {
            super(propertyResolver, configurationPrefix)
        }

        MongoConnectionSourceSettingsBuilder(PropertyResolver propertyResolver, String configurationPrefix, String builderMethodPrefix) {
            super(propertyResolver, configurationPrefix, builderMethodPrefix)
        }

        @Override
        protected MongoConnectionSourceSettings createBuilder() {
            new MongoConnectionSourceSettings()
        }

        @Override
        protected MongoConnectionSourceSettings toConfiguration(MongoConnectionSourceSettings builder) {
            builder
        }
    }

    @SettingsBuilder
    static class MongoConnectionSourceSettings {

        MongoClientOptions options = new MongoClientOptions()

    }

    @SettingsBuilder
    static class MongoClientOptions {

        AutoEncryptionSettings autoEncryptionSettings = new AutoEncryptionSettings()

    }

    @SettingsBuilder
    static class MongoClientSettings {

        AutoEncryptionSettings autoEncryptionSettings = new AutoEncryptionSettings()

    }

    @SettingsBuilder
    static class AutoEncryptionSettings {
        boolean bypassAutoEncryption
    }
}
