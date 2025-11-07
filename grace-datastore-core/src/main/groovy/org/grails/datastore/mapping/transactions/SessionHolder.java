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

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.transaction.support.ResourceHolderSupport;

import org.grails.datastore.mapping.core.Session;

/**
 * Holds a reference to one or more sessions.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class SessionHolder extends ResourceHolderSupport {

    private Deque<Session> sessions = new LinkedBlockingDeque<>();

    private Object creator = null;

    public SessionHolder(Session session) {
        this.sessions.add(session);
    }

    public SessionHolder(Session session, Object creator) {
        this(session);
        this.creator = creator;
    }

    public Object getCreator() {
        return this.creator;
    }

    public Transaction<?> getTransaction() {
        final Session session = getSession();
        if (session != null && session.hasTransaction()) {
            return session.getTransaction();
        }
        return null;
    }

    @Override
    public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction) {
        for (Session session : this.sessions) {
            session.setSynchronizedWithTransaction(synchronizedWithTransaction);
        }
        super.setSynchronizedWithTransaction(synchronizedWithTransaction);
    }

    @Deprecated
    public void setTransaction(Transaction<?> transaction) {
        // no-op. here for compatibility
    }

    public Session getSession() {
        return this.sessions.peekLast();
    }

    public boolean isEmpty() {
        return this.sessions.isEmpty();
    }

    public boolean doesNotHoldNonDefaultSession() {
        return isEmpty();
    }

    public void addSession(Session session) {
        this.sessions.add(session);
    }

    public void removeSession(Session session) {
        this.sessions.remove(session);
    }

    public boolean containsSession(Session session) {
        return this.sessions.contains(session);
    }

    public int size() {
        return this.sessions.size();
    }

    public Session getValidatedSession() {
        Session session = getSession();
        if (session != null && !session.isConnected()) {
            removeSession(session);
            session = null;
        }
        return session;
    }

}
