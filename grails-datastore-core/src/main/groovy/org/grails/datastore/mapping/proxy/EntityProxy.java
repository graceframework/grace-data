/* Copyright (C) 2010 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.datastore.mapping.proxy;

import java.io.Serializable;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
public interface EntityProxy<T> {

    /**
     * Initializes the proxy if it hasn't been initialized already
     */
    void initialize();

    /**
     * Obtains the target performing initialization if necessary
     * @return The target
     */
    T getTarget();

    /**
     * Checks whether the proxy has been initialized
     * @return True if it has
     */
    boolean isInitialized();

    /**
     * @return The identifier
     */
    Serializable getProxyKey();

}
