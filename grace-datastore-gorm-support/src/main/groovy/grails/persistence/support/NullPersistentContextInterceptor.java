/*
 * Copyright 2004-2025 the original author or authors.
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
package grails.persistence.support;

/**
 * A dummy persistence context interceptor that does nothing.
 *
 * @author Graeme Rocher
 * @author Michael Yan
 * @since 1.1.1
 */
public class NullPersistentContextInterceptor implements PersistenceContextInterceptor {

    @Override
    public void init() {
        // NOOP
    }

    @Override
    public void destroy() {
        // NOOP
    }

    @Override
    public void disconnect() {
        // NOOP
    }

    @Override
    public void reconnect() {
        // NOOP
    }

    @Override
    public void flush() {
        // NOOP
    }

    @Override
    public void clear() {
        // NOOP
    }

    @Override
    public void setReadOnly() {
        // NOOP
    }

    @Override
    public void setReadWrite() {
        // NOOP
    }

    @Override
    public boolean isOpen() {
        return false;
    }

}
