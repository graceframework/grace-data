/* Copyright (C) 2011 SpringSource
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

import org.grails.datastore.mapping.engine.types.CustomTypeMarshaller;
import org.grails.datastore.mapping.model.AbstractPersistentProperty;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;

/**
 * Represents a custom type ie. a type whose database read/write semantics are specified by the user.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class Custom<T> extends AbstractPersistentProperty {

    private CustomTypeMarshaller<?, ?, ?> customTypeMarshaller;

    public Custom(PersistentEntity owner, MappingContext context, PropertyDescriptor descriptor,
            CustomTypeMarshaller<?, ?, ?> customTypeMarshaller) {
        super(owner, context, descriptor);
        this.customTypeMarshaller = customTypeMarshaller;
    }

    protected Custom(PersistentEntity owner, MappingContext context, String name, Class<?> type,
            CustomTypeMarshaller<?, ?, ?> customTypeMarshaller) {
        super(owner, context, name, type);
        this.customTypeMarshaller = customTypeMarshaller;
    }

    /**
     * @return The type converter for this custom type
     */
    @SuppressWarnings("rawtypes")
    public CustomTypeMarshaller<Object, T, T> getCustomTypeMarshaller() {
        return (CustomTypeMarshaller<Object, T, T>) customTypeMarshaller;
    }

}
