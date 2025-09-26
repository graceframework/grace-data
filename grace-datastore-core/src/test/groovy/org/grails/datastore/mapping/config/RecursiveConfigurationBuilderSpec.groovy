package org.grails.datastore.mapping.config

import org.grails.datastore.mapping.core.DatastoreUtils
import org.springframework.core.env.PropertyResolver
import spock.lang.Specification

class RecursiveConfigurationBuilderSpec extends Specification {

    void "recursive builders don't cause stackoverflow"() {
        given:
        PropertyResolver config = DatastoreUtils.createPropertyResolver([:])

        when:
        MongoConnectionSourceSettingsBuilder builder = new MongoConnectionSourceSettingsBuilder(config, "grails.mongodb");
        MongoConnectionSourceSettings settings = builder.build()

        then:
        noExceptionThrown()
        !settings.options.autoEncryptionSettings.bypassAutoEncryption
    }

    void "recursive builder get configured correctly"() {
        given:
        PropertyResolver config = DatastoreUtils.createPropertyResolver(["grails.mongodb.options.autoEncryptionSettings.bypassAutoEncryption": true])

        when:
        MongoConnectionSourceSettingsBuilder builder = new MongoConnectionSourceSettingsBuilder(config, "grails.mongodb")
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
