/* Copyright 2011 SpringSource
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
package org.grails.datastore.mapping.model;

import org.grails.datastore.mapping.reflect.FieldEntityAccess;

/**
 * Models an embedded entity
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EmbeddedPersistentEntity extends AbstractPersistentEntity {

    public EmbeddedPersistentEntity(Class type, MappingContext ctx) {
        super(type, ctx);
    }

    @Override
    protected PersistentProperty resolveIdentifier() {
        return null; // no identifiers in embedded entities
    }

    @Override
    public void initialize() {
        super.initialize();
        FieldEntityAccess.getOrIntializeReflector(this);
    }

}
