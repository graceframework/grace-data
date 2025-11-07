/*
 * Copyright 2011-2025 the original author or authors.
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
package org.grails.datastore.mapping.engine.event;

import org.springframework.context.ApplicationEvent;

import org.grails.datastore.mapping.core.Datastore;

/**
 * @author Burt Beckwith
 */
public abstract class AbstractPersistenceEventListener implements PersistenceEventListener {

    protected Datastore datastore;

    protected AbstractPersistenceEventListener(final Datastore datastore) {
        this.datastore = datastore;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(
     *org.springframework.context.ApplicationEvent)
     */
    @Override
    public final void onApplicationEvent(ApplicationEvent e) {
        if (e instanceof AbstractPersistenceEvent) {

            AbstractPersistenceEvent event = (AbstractPersistenceEvent) e;
            if (!isValidSource(event)) {
                return;
            }

            if (event.isCancelled()) {
                return;
            }

            if (event.isListenerExcluded(getClass().getName())) {
                return;
            }
            onPersistenceEvent(event);
        }

    }

    protected boolean isValidSource(AbstractPersistenceEvent event) {
        Object source = event.getSource();
        return (source instanceof Datastore) && source.equals(this.datastore);
    }

    protected abstract void onPersistenceEvent(AbstractPersistenceEvent event);

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean supportsSourceType(final Class<?> sourceType) {
        // ensure that this listener only handles its events (e.g. if Mongo and Redis are both installed)
        return this.datastore.getClass().isAssignableFrom(sourceType);
    }

}
