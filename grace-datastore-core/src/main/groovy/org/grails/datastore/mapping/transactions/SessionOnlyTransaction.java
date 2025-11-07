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
package org.grails.datastore.mapping.transactions;

import org.grails.datastore.mapping.core.Session;

/**
 * <p>An implementation that provides Session only transaction management. Essentially when {@link #rollback()} is called
 * the {@link Session}'s clear() method is called and when {@link #commit()} is called the flush() method is called.
 * </p>
 *
 * <p>
 * No other resource level transaction management is provided.
 * </p>
 *
 * @param <T>
 * @author graemerocher
 */
public class SessionOnlyTransaction<T> implements Transaction<T> {

    private final T nativeInterface;

    private final Session session;

    private boolean active = true;

    public SessionOnlyTransaction(T nativeInterface, Session session) {
        this.nativeInterface = nativeInterface;
        this.session = session;
    }

    @Override
    public void commit() {
        if (this.active) {
            try {
                this.session.flush();
            }
            finally {
                this.active = false;
            }
        }
    }

    @Override
    public void rollback() {
        if (this.active) {
            try {
                this.session.clear();
            }
            finally {
                this.active = false;
            }
        }
    }

    @Override
    public T getNativeTransaction() {
        return this.nativeInterface;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setTimeout(int timeout) {
        // do nothing
    }

}
