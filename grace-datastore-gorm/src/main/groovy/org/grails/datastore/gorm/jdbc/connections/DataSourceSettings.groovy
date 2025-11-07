/*
 * Copyright 2016-2024 the original author or authors.
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
package org.grails.datastore.gorm.jdbc.connections

import javax.sql.DataSource

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.jdbc.schema.DefaultSchemaHandler
import org.grails.datastore.gorm.jdbc.schema.SchemaHandler
import org.grails.datastore.mapping.config.SettingsBuilder
import org.grails.datastore.mapping.core.connections.ConnectionSourceSettings

/**
 * DataSource settings
 *
 * @author Graeme Rocher
 * @author Michael Yan
 */
@CompileStatic
@SettingsBuilder
class DataSourceSettings extends ConnectionSourceSettings {

    /**
     * The data source URL
     */
    String url

    /**
     * The driver class name
     */
    String driverClassName

    /**
     * The username
     */
    String username

    /**
     * The password
     */
    String password

    /**
     * The JNDI name
     */
    String jndiName

    /**
     * Whether the data source is pooled
     */
    boolean pooled = true

    /**
     * Whether the data source is lazy
     */
    boolean lazy = true

    /**
     * Whether the data source is aware of an ongoing Spring transaction
     */
    boolean transactionAware = true

    /**
     * Whether the connection is readonly
     */
    boolean readOnly = false

    /**
     * The dialect to use
     */
    Class dialect

    /**
     * The schema handler to use
     */
    Class<? extends SchemaHandler> schemaHandler = DefaultSchemaHandler

    /**
     * Whether to log SQL
     */
    boolean logSql = false

    /**
     * Whether to format the SQL
     */
    boolean formatSql = false

    /**
     * The default value for `hibernate.hbm2ddl.auto`
     */
    String dbCreate = 'none'

    /**
     * The data source properties
     */
    Map properties = [:]

    /**
     * The connection pool to use
     */
    Class<? extends DataSource> type

    /**
     * Convert to Hibernate properties
     *
     * @return The hibernate properties
     */
    @CompileStatic
    Properties toHibernateProperties() {
        Properties props = new Properties()
        props.put('hibernate.hbm2ddl.auto', dbCreate)
        props.put('hibernate.show_sql', String.valueOf(logSql))
        props.put('hibernate.format_sql', String.valueOf(formatSql))
        if (dialect != null) {
            props.put('hibernate.dialect', dialect.name)
        }
        return props
    }

    /**
     * @return Convert to datasource properties
     */
    @CompileStatic
    Map<String, String> toProperties() {
        Map<String, String> properties = new LinkedHashMap<>()
        properties.putAll(this.properties)
        properties.put('url', url)
        if (driverClassName) {
            properties.put('driverClassName', driverClassName)
        }
        if (username) {
            properties.put('username', username)
        }
        if (password) {
            properties.put('username', password)
        }
        if (readOnly) {
            properties.put('defaultReadOnly', String.valueOf(readOnly))
        }
        return properties
    }

}
