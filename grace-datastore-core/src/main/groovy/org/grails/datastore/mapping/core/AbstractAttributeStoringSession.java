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
package org.grails.datastore.mapping.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.grails.datastore.mapping.transactions.SessionHolder;

public abstract class AbstractAttributeStoringSession implements Session {

    protected Map<Integer, Map<String, Object>> attributes = new ConcurrentHashMap<>();

    protected Map<String, Object> sessionPropertyMap = new ConcurrentHashMap<>();

    private boolean connected = true;

    @Override
    public void setAttribute(Object entity, String attributeName, Object value) {
        if (entity == null) {
            return;
        }

        int id = System.identityHashCode(entity);
        Map<String, Object> attrs = this.attributes.get(id);
        if (attrs == null) {
            attrs = new ConcurrentHashMap<>();
            this.attributes.put(id, attrs);
        }

        if (attributeName != null && value != null) {
            attrs.put(attributeName, value);
        }
        if (attributeName != null && value == null) {
            attrs.remove(attributeName);
        }
    }

    @Override
    public Object getAttribute(Object entity, String attributeName) {
        if (entity == null) {
            return null;
        }

        final Map<String, Object> attrs = this.attributes.get(System.identityHashCode(entity));
        if (attrs == null || attributeName == null) {
            return null;
        }

        return attrs.get(attributeName);
    }

    protected void removeAttributesForEntity(Object entity) {
        if (entity == null) {
            return;
        }
        this.attributes.remove(System.identityHashCode(entity));
    }

    /**
     * Set a property on this session. Note that properties are not cleared out when a session is cleared.
     *
     * @param property The property name.
     * @param value    The property value.
     */
    @Override
    public Object setSessionProperty(String property, Object value) {
        return this.sessionPropertyMap.put(property, value);
    }

    /**
     * Get the value of a property of the session.
     *
     * @param property The name of the property.
     * @return The value.
     */
    @Override
    public Object getSessionProperty(String property) {
        return this.sessionPropertyMap.get(property);
    }

    /**
     * Clear a property in a session.
     *
     * @param property The property name.
     * @return The property value, if there was one (or null).
     */
    @Override
    public Object clearSessionProperty(String property) {
        return this.sessionPropertyMap.remove(property);
    }

    /**
     * Performs clear up. Subclasses should always call into this super
     * implementation.
     */
    @Override
    public void disconnect() {
        this.connected = false;
        try {
            clear();
            this.attributes.clear();
        }
        finally {
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(getDatastore());
            if (sessionHolder != null) {
                sessionHolder.removeSession(this);
                if (sessionHolder.isEmpty()) {
                    try {
                        TransactionSynchronizationManager.unbindResource(getDatastore());
                    }
                    catch (IllegalStateException ignored) {
                        // ignore session disconnected by another thread
                    }
                }
            }
        }
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

}
