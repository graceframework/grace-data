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

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ConfigurableApplicationContext
import spock.util.concurrent.PollingConditions

import grails.gorm.annotation.Entity

import org.grails.datastore.gorm.events.ConfigurableApplicationEventPublisher
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

class DirtyCheckingAfterListenerSpec extends GormDatastoreSpec {

    TestSaveOrUpdateEventListener listener
    def datastore

    @Override
    List getDomainClasses() {
        return [TestPlayer]
    }

    def setup() {
        datastore = session.datastore
        listener = new TestSaveOrUpdateEventListener(datastore)
        ApplicationEventPublisher publisher = datastore.applicationEventPublisher
        if (publisher instanceof ConfigurableApplicationEventPublisher) {
            ((ConfigurableApplicationEventPublisher) publisher).addApplicationListener(listener)
        }
        else if (publisher instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) publisher).addApplicationListener(listener)
        }
    }

    void 'test state change from listener update the object'() {
        when:
        TestPlayer john = new TestPlayer(name: 'John').save(flush: true)

        then:
        new PollingConditions().eventually { listener.isExecuted && TestPlayer.count() }

        when:
        session.flush()
        session.clear()
        john = TestPlayer.get(john.id)

        then:
        john.attributes
        john.attributes.size() == 3
    }

}

@Entity
class TestPlayer implements Serializable {

    Long id
    String name
    List<String> attributes

}

class TestSaveOrUpdateEventListener extends AbstractPersistenceEventListener {

    boolean isExecuted = false

    TestSaveOrUpdateEventListener(Datastore datastore) {
        super(datastore)
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        TestPlayer player = (TestPlayer) event.entityObject
        player.attributes = ['test0', 'test1', 'test2']
        isExecuted = true
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType == PreUpdateEvent || eventType == PreInsertEvent
    }

}
