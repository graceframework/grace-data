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
package org.grails.datastore.gorm.support;

import java.util.List;

import grails.persistence.support.PersistenceContextInterceptor;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
public class AggregatePersistenceContextInterceptor implements PersistenceContextInterceptor {

    private final List<PersistenceContextInterceptor> interceptors;

    /**
     * Constructor.
     * @param interceptors the real interceptors
     */
    public AggregatePersistenceContextInterceptor(final List<PersistenceContextInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public boolean isOpen() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            if (interceptor.isOpen()) {
                // true at least one is true
                return true;
            }
        }
        return false;
    }

    @Override
    public void reconnect() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.reconnect();
        }
    }

    @Override
    public void destroy() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            try {
                if (interceptor.isOpen()) {
                    interceptor.destroy();
                }
            }
            catch (Exception ignore) {
                // ignore exception
            }
        }
    }

    @Override
    public void clear() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.clear();
        }
    }

    @Override
    public void disconnect() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.disconnect();
        }
    }

    @Override
    public void flush() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.flush();
        }
    }

    @Override
    public void init() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.init();
        }
    }

    @Override
    public void setReadOnly() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.setReadOnly();
        }
    }

    @Override
    public void setReadWrite() {
        for (PersistenceContextInterceptor interceptor : this.interceptors) {
            interceptor.setReadWrite();
        }
    }

}
