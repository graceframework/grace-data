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

import org.springframework.dao.DataIntegrityViolationException

import org.grails.datastore.gorm.proxy.GroovyProxyFactory

/**
 * @author graemerocher
 */
class GroovyProxySpec extends GormDatastoreSpec {

    void 'Test proxying of non-existent instance throws an exception'() {
        setup:
        if (useGroovyProxyFactory) {
            session.mappingContext.proxyFactory = new GroovyProxyFactory()
        }

        when: "A proxy is loaded for an instance that doesn't exist"
        def location = Location.proxy(123)

        then: 'The proxy is in a valid state'

        location != null
        location.id == 123
        location.isInitialized() == false
        location.initialized == false

        when: 'The proxy is loaded'
        location.code

        then: 'An exception is thrown'
        thrown DataIntegrityViolationException

        where:
        useGroovyProxyFactory << [true, false]
    }

    void 'Test creation and behavior of Groovy proxies'() {
        setup:
        if (useGroovyProxyFactory) {
            session.mappingContext.proxyFactory = new GroovyProxyFactory()
        }

        def id = new Location(name: 'United Kingdom', code: 'UK').save(flush: true)?.id
        session.clear()

        when:
        def location = Location.proxy(id)

        then:

        location != null
        location.id == id
        Location.isInstance(location) == true
        location.metaClass != null
        location.isInitialized() == false
        location.initialized == false

        location.code == 'UK'
        location.namedAndCode() == 'United Kingdom - UK'
        location.isInitialized() == true
        location.initialized == true
        location.target != null
        Location.isInstance(location) == true
        location.metaClass != null
        where:
        useGroovyProxyFactory << [true, false]
    }

    void 'Test setting metaClass property on proxy'() {
        setup:
        if (useGroovyProxyFactory) {
            session.mappingContext.proxyFactory = new GroovyProxyFactory()
        }

        when:
        def location = Location.proxy(123)
        location.metaClass = null
        then:
        location.metaClass != null
        where:
        useGroovyProxyFactory << [true, false]
    }

    void 'Test calling setMetaClass method on proxy'() {
        setup:
        if (useGroovyProxyFactory) {
            session.mappingContext.proxyFactory = new GroovyProxyFactory()
        }

        when:
        def location = Location.proxy(123)
        location.setMetaClass(null)
        then:
        location.metaClass != null
        where:
        useGroovyProxyFactory << [true, false]
    }

    void 'Test creation and behavior of Groovy proxies with method call'() {
        setup:
        if (useGroovyProxyFactory) {
            session.mappingContext.proxyFactory = new GroovyProxyFactory()
        }
        def id = new Location(name: 'United Kingdom', code: 'UK').save(flush: true)?.id
        session.clear()

        when:
        def location = Location.proxy(id)

        then:

        location != null
        location.id == id
        Location.isInstance(location) == true
        location.metaClass != null
        location.isInitialized() == false
        location.initialized == false

        location.namedAndCode() == 'United Kingdom - UK' // method first
        location.code == 'UK'
        location.isInitialized() == true
        location.initialized == true
        location.target != null
        Location.isInstance(location) == true
        location.metaClass != null
        where:
        useGroovyProxyFactory << [true, false]
    }

}
