/* Copyright 2004-2005 the original author or authors.
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
package org.grails.datastore.mapping.model.types;

import java.beans.PropertyDescriptor;

import org.grails.datastore.mapping.config.Property;
import org.grails.datastore.mapping.model.AbstractPersistentProperty;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;

/**
 * Models a simple property type
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class Simple<T extends Property> extends AbstractPersistentProperty<T> {

    public Simple(PersistentEntity owner, MappingContext context, PropertyDescriptor descriptor) {
        super(owner, context, descriptor);
    }

    public Simple(PersistentEntity owner, MappingContext context, String name, @SuppressWarnings("rawtypes") Class type) {
        super(owner, context, name, type);
    }

}
