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
package org.grails.datastore.mapping.core;

/**
 * Thrown when there was an error creating an entity.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class EntityCreationException extends RuntimeException {

    private static final long serialVersionUID = 1;

    public EntityCreationException(String msg) {
        super(msg);
    }

    public EntityCreationException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
