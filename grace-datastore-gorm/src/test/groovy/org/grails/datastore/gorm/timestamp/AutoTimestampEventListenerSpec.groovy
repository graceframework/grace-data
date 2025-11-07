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
package org.grails.datastore.gorm.timestamp

import groovy.transform.InheritConstructors
import org.grails.datastore.gorm.events.AutoTimestampEventListener
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.model.MappingContext
import spock.lang.Specification

class AutoTimestampEventListenerSpec extends Specification {

    TestEventListener listener
    Map<String, Boolean> lastUpdatedBaseState = [:]
    Map<String, Boolean> dateCreatedBaseState = [:]

    void setup() {
        listener = new TestEventListener(Stub(Datastore) {
            getMappingContext() >> null
        })
        [Foo, Bar, FooBar].each {
            lastUpdatedBaseState.put(it.getName(), true)
            dateCreatedBaseState.put(it.getName(), true)
        }
    }

    void updateBaseStates() {
        listener.dateCreated.entrySet().each {
            dateCreatedBaseState.put(it.key, it.value)
        }
        listener.lastUpdated.entrySet().each {
            lastUpdatedBaseState.put(it.key, it.value)
        }
    }

    void 'test withoutLastUpdated'() {
        when:
        listener.withoutLastUpdated {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == false
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == false
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutLastUpdated(Class)'() {
        when:
        listener.withoutLastUpdated(Bar) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutLastUpdated(Class[])'() {
        when:
        listener.withoutLastUpdated([Bar, FooBar]) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == false
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutDateCreated'() {
        when:
        listener.withoutDateCreated() {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == false
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == false

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutDateCreated(Class)'() {
        when:
        listener.withoutDateCreated(Bar) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == true

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutDateCreated(Class[])'() {
        when:
        listener.withoutDateCreated([Bar, FooBar]) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == false

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }


    void 'test withoutTimestamps'() {
        when:
        listener.withoutTimestamps() {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == false
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == false
        dateCreatedBaseState[Foo.getName()] == false
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == false

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutTimestamps(Class)'() {
        when:
        listener.withoutTimestamps(Bar) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == true

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }

    void 'test withoutTimestamps(Class[])'() {
        when:
        listener.withoutTimestamps([Bar, FooBar]) {
            updateBaseStates()
        }

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == false
        lastUpdatedBaseState[FooBar.getName()] == false
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == false
        dateCreatedBaseState[FooBar.getName()] == false

        when:
        updateBaseStates()

        then:
        lastUpdatedBaseState[Foo.getName()] == true
        lastUpdatedBaseState[Bar.getName()] == true
        lastUpdatedBaseState[FooBar.getName()] == true
        dateCreatedBaseState[Foo.getName()] == true
        dateCreatedBaseState[Bar.getName()] == true
        dateCreatedBaseState[FooBar.getName()] == true
    }
}

class Foo {

}

class Bar {

}

class FooBar {

}

@InheritConstructors
class TestEventListener extends AutoTimestampEventListener {

    Map<String, Boolean> getDateCreated() {
        this.entitiesWithDateCreated
    }
    Map<String, Boolean> getLastUpdated() {
        this.entitiesWithLastUpdated
    }

    protected void initForMappingContext(MappingContext mappingContext) {
        [Foo, Bar, FooBar].each {
            entitiesWithLastUpdated.put(it.getName(), true)
            entitiesWithDateCreated.put(it.getName(), true)
        }
    }
}