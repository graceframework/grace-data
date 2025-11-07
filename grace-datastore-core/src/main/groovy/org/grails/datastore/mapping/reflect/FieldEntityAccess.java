/*
 * Copyright 2015-2025 the original author or authors.
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
package org.grails.datastore.mapping.reflect;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.groovy.transform.trait.Traits;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;

import org.grails.datastore.mapping.dirty.checking.DirtyCheckable;
import org.grails.datastore.mapping.engine.EntityAccess;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.mapping.model.PersistentProperty;
import org.grails.datastore.mapping.proxy.ProxyHandler;

/**
 * Uses field reflection or CGlib to improve performance
 *
 * @author Graeme Rocher
 * @since 5.0
 */
public class FieldEntityAccess implements EntityAccess {

    private static Map<String, EntityReflector> REFLECTORS = new ConcurrentHashMap<>();

    private final PersistentEntity persistentEntity;

    private final Object entity;

    private final ConversionService conversionService;

    private final EntityReflector reflector;

    public FieldEntityAccess(PersistentEntity persistentEntity, Object entity, ConversionService conversionService) {
        this.persistentEntity = persistentEntity;
        this.entity = entity;
        this.conversionService = conversionService;
        this.reflector = getOrIntializeReflector(persistentEntity);
    }

    public static void clearReflectors() {
        REFLECTORS.clear();
    }

    public static EntityReflector getOrIntializeReflector(PersistentEntity persistentEntity) {
        String entityName = persistentEntity.getName();
        EntityReflector entityReflector = REFLECTORS.get(entityName);
        if (entityReflector == null) {
            entityReflector = new FieldEntityReflector(persistentEntity);
            REFLECTORS.put(entityName, entityReflector);
        }
        return entityReflector;
    }

    public static EntityReflector getReflector(String name) {
        return REFLECTORS.get(name);
    }

    @Override
    public Object getEntity() {
        return this.entity;
    }

    @Override
    public Object getProperty(String name) {
        Object object = unwrapIfProxy(this.persistentEntity, this.entity);
        return this.reflector.getProperty(object, name);
    }

    @Override
    public Object getPropertyValue(String name) {
        return getProperty(name);
    }

    @Override
    public Class getPropertyType(String name) {
        PersistentProperty property = this.persistentEntity.getPropertyByName(name);
        if (property != null) {
            return property.getType();
        }
        return null;
    }

    @Override
    public void setProperty(String name, Object value) {
        FieldEntityReflector.PropertyWriter writer = this.reflector.getPropertyWriter(name);
        Object converted;
        try {
            converted = this.conversionService.convert(value, writer.propertyType());
        }
        catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot assign value [" + value + "] to property [" + name + "] of type [" +
                    writer.propertyType().getName() + "] of class [" + this.persistentEntity.getName() +
                    "]. The value could not be converted to the appropriate type: " + e.getMessage(), e);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cannot assign value [" + value + "] to property [" + name + "] of type [" +
                    writer.propertyType().getName() + "] of class [" + this.persistentEntity.getName() +
                    "]. The value is not an acceptable type: " + e.getMessage(), e);
        }
        writer.write(this.entity, converted);
    }

    @Override
    public Object getIdentifier() {
        return this.reflector.getIdentifier(this.entity);
    }

    @Override
    public void setIdentifier(Object id) {
        Object converted;
        try {
            converted = this.conversionService.convert(id, this.reflector.identifierType());
        }
        catch (ConversionException e) {
            throw new IllegalArgumentException("Cannot assign identifier [" + id + "] to property [" + this.reflector.getIdentifierName() +
                    "] of type [" + this.reflector.identifierType().getName() + "] of class [" + this.persistentEntity.getName() +
                    "]. The value could not be converted to the appropriate type: " + e.getMessage(), e);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cannot assign identifier [" + id + "] to property [" + this.reflector.getIdentifierName() +
                    "] of type [" + this.reflector.identifierType().getName() + "] of class [" + this.persistentEntity.getName() +
                    "]. The identifier is not an compatible type: " + e.getMessage(), e);

        }
        this.reflector.setIdentifier(this.entity, converted);
    }

    @Override
    public void setIdentifierNoConversion(Object id) {
        try {
            this.reflector.setIdentifier(this.entity, id);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Cannot assign identifier [" + id + "] to property [" +
                    this.reflector.getIdentifierName() + "] of type [" + this.reflector.identifierType().getName() + "] of class [" +
                    this.persistentEntity.getName() + "]. The identifier is not an compatible type: " + e.getMessage(), e);

        }
    }

    @Override
    public String getIdentifierName() {
        return this.reflector.getIdentifierName();
    }

    @Override
    public PersistentEntity getPersistentEntity() {
        return this.persistentEntity;
    }

    @Override
    public void refresh() {
        // no-op
    }

    @Override
    public void setPropertyNoConversion(String name, Object value) {
        try {
            this.reflector.setProperty(this.entity, name, value);
        }
        catch (Exception e) {
            String valueType = value != null ? value.getClass().getName() : null;
            throw new IllegalArgumentException("Cannot assign value [" + value + "] with type [" + valueType + "] to property [" + name +
                    "] of class [" + this.persistentEntity.getName() + "]. The value is not an acceptable type: " + e.getMessage(), e);
        }
    }

    private static Object unwrapIfProxy(PersistentEntity entity, Object object) {
        if (entity != null) {
            final ProxyHandler proxyHandler = entity.getMappingContext().getProxyHandler();
            return proxyHandler.unwrap(object);
        }
        else {
            return object;
        }
    }

    static class FieldEntityReflector implements EntityReflector {

        final PersistentEntity entity;

        final PropertyReader[] readers;

        final PropertyWriter[] writers;

        final PropertyReader identifierReader;

        final PropertyWriter identifierWriter;

        final String identifierName;

        final Class identifierType;

        final Map<String, PropertyReader> readerMap = new HashMap<>();

        final Map<String, PropertyWriter> writerMap = new HashMap<>();

        final Field dirtyCheckingStateField;

        FastClass fastClass;

        public FieldEntityReflector(PersistentEntity entity) {
            this.entity = entity;
            PersistentProperty identity = entity.getIdentity();
            this.dirtyCheckingStateField = ReflectionUtils.findField(entity.getJavaClass(),
                    getTraitFieldName(DirtyCheckable.class, "$changedProperties"));
            if (this.dirtyCheckingStateField != null) {
                ReflectionUtils.makeAccessible(this.dirtyCheckingStateField);
            }
            ClassPropertyFetcher cpf = ClassPropertyFetcher.forClass(entity.getJavaClass());
            if (identity != null) {
                String identityName = identity.getName();
                this.identifierName = identityName;
                this.identifierType = identity.getType();

                ReaderAndWriterMaker readerAndWriterMaker = new ReaderAndWriterMaker(cpf, identityName).make();
                this.identifierReader = readerAndWriterMaker.getPropertyReader();
                this.identifierWriter = readerAndWriterMaker.getPropertyWriter();

                this.readerMap.put(this.identifierName, this.identifierReader);
                if (this.identifierWriter != null) {
                    this.writerMap.put(this.identifierName, this.identifierWriter);
                }
            }
            else {
                this.identifierName = null;
                this.identifierReader = null;
                this.identifierWriter = null;
                this.identifierType = null;
            }

            PersistentProperty[] composite = entity.getCompositeIdentity();
            if (composite != null) {
                for (PersistentProperty property : composite) {
                    String propertyName = property.getName();
                    ReaderAndWriterMaker readerAndWriterMaker = new ReaderAndWriterMaker(cpf, propertyName).make();
                    this.readerMap.put(propertyName, readerAndWriterMaker.getPropertyReader());
                    this.writerMap.put(propertyName, readerAndWriterMaker.getPropertyWriter());
                }
            }
            List<PersistentProperty> properties = entity.getPersistentProperties();
            this.readers = new PropertyReader[properties.size()];
            this.writers = new PropertyWriter[properties.size()];
            for (int i = 0; i < properties.size(); i++) {
                PersistentProperty property = properties.get(i);

                String propertyName = property.getName();
                ReaderAndWriterMaker readerAndWriterMaker = new ReaderAndWriterMaker(cpf, propertyName).make();
                PropertyReader reader = readerAndWriterMaker.getPropertyReader();
                PropertyWriter writer = readerAndWriterMaker.getPropertyWriter();

                this.readers[i] = reader;
                this.readerMap.put(propertyName, reader);
                this.writers[i] = writer;
                this.writerMap.put(propertyName, writer);
            }
        }

        protected String getTraitFieldName(Traits.TraitBridge traitBridge, String fieldName) {
            Class traitClass = traitBridge.traitClass();
            return getTraitFieldName(traitClass, fieldName);
        }

        private String getTraitFieldName(Class traitClass, String fieldName) {
            return traitClass.getName().replace('.', '_') + "__" + fieldName;
        }

        @Override
        public PersistentEntity getPersitentEntity() {
            return this.entity;
        }

        @Override
        public Map<String, Object> getDirtyCheckingState(Object entity) {
            if (this.dirtyCheckingStateField != null) {
                try {
                    return (Map<String, Object>) this.dirtyCheckingStateField.get(entity);
                }
                catch (Throwable e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public FastClass fastClass() {
            if (this.fastClass == null) {
                this.fastClass = FastClass.create(this.entity.getJavaClass());
            }
            return this.fastClass;
        }

        @Override
        public PropertyReader getPropertyReader(String name) {
            final PropertyReader reader = this.readerMap.get(name);
            if (reader != null) {
                return reader;
            }
            throw new IllegalArgumentException("Property [" + name + "] is not a valid property of " + this.entity.getJavaClass());
        }

        @Override
        public PropertyWriter getPropertyWriter(String name) {
            final PropertyWriter writer = this.writerMap.get(name);
            if (writer != null) {
                return writer;
            }
            else {
                throw new IllegalArgumentException("Property [" + name + "] is not a valid property of " + this.entity.getJavaClass());
            }
        }

        @Override
        public Object getProperty(Object object, String name) {
            object = unwrapIfProxy(getPersitentEntity(), object);
            return getPropertyReader(name).read(object);
        }

        @Override
        public void setProperty(Object object, String name, Object value) {
            getPropertyWriter(name).write(object, value);
        }

        @Override
        public Class identifierType() {
            return this.identifierType;
        }

        @Override
        public Serializable getIdentifier(Object object) {
            if (this.identifierReader != null && object != null) {
                return (Serializable) this.identifierReader.read(object);
            }
            return null;
        }

        @Override
        public void setIdentifier(Object object, Object value) {
            if (this.identifierWriter != null) {
                this.identifierWriter.write(object, value);
            }
        }

        @Override
        public String getIdentifierName() {
            return this.identifierName;
        }

        @Override
        public Iterable<String> getPropertyNames() {
            return this.readerMap.keySet();
        }

        @Override
        public Object getProperty(Object object, int index) {
            return this.readers[index].read(object);
        }

        @Override
        public void setProperty(Object object, int index, Object value) {
            this.writers[index].write(object, value);
        }

        static class ReflectMethodReader implements PropertyReader {

            final Method method;

            public ReflectMethodReader(Method method) {
                this.method = method;
                ReflectionUtils.makeAccessible(method);
            }

            @Override
            public Field field() {
                return null;
            }

            @Override
            public Method getter() {
                return this.method;
            }

            @Override
            public Class propertyType() {
                return this.method.getReturnType();
            }

            @Override
            public Object read(Object object) {
                return ReflectionUtils.invokeMethod(this.method, object);
            }

        }

        static class ReflectionMethodWriter implements PropertyWriter {

            final Method method;

            final Class propertyType;

            public ReflectionMethodWriter(Method method, Class propertyType) {
                this.method = method;
                ReflectionUtils.makeAccessible(method);
                this.propertyType = propertyType;
            }

            @Override
            public Field field() {
                return null;
            }

            @Override
            public Method setter() {
                return this.method;
            }

            @Override
            public Class propertyType() {
                return this.propertyType;
            }

            @Override
            public void write(Object object, Object value) {
                ReflectionUtils.invokeMethod(this.method, object, value);
            }

        }

        static class FieldReader implements PropertyReader {

            final Field field;

            final Method getter;

            public FieldReader(Field field, Method getter) {
                this.field = field;
                this.getter = getter;
                ReflectionUtils.makeAccessible(field);
            }

            @Override
            public Field field() {
                return this.field;
            }

            @Override
            public Method getter() {
                return this.getter;
            }

            @Override
            public Class propertyType() {
                return this.field.getType();
            }

            @Override
            public Object read(Object object) {
                try {
                    object = unwrapIfProxy(null, object);
                    return this.field.get(object);
                }
                catch (Throwable e) {
                    throw new IllegalArgumentException("Cannot read field [" + this.field + "] from object [" +
                            object + "] of type [" + object.getClass() + "]", e);
                }
            }

        }

        static class FieldWriter implements PropertyWriter {

            final Field field;

            final Method setter;

            public FieldWriter(Field field, Method setter) {
                this.field = field;
                this.setter = setter;
                ReflectionUtils.makeAccessible(field);
            }

            @Override
            public Field field() {
                return this.field;
            }

            @Override
            public Method setter() {
                return this.setter;
            }

            @Override
            public Class propertyType() {
                return this.field.getType();
            }

            @Override
            public void write(Object object, Object value) {
                try {
                    this.field.set(object, value);
                }
                catch (Throwable e) {
                    throw new IllegalArgumentException("Cannot set field [" + this.field.getName() + "] of object [" +
                            object + "] for value [" + value + "] of type [" + value.getClass().getName() + "]", e);
                }
            }

        }

        private class ReaderAndWriterMaker {

            private ClassPropertyFetcher cpf;

            private String propertyName;

            private PropertyReader propertyReader;

            private PropertyWriter propertyWriter;

            public ReaderAndWriterMaker(ClassPropertyFetcher cpf, String propertyName) {
                this.cpf = cpf;
                this.propertyName = propertyName;
            }

            public PropertyReader getPropertyReader() {
                return this.propertyReader;
            }

            public PropertyWriter getPropertyWriter() {
                return this.propertyWriter;
            }

            public ReaderAndWriterMaker make() {
                Class javaClass = this.cpf.getJavaClass();
                Field field = ReflectionUtils.findField(javaClass, this.propertyName);
                if (field != null) {
                    ReflectionUtils.makeAccessible(field);
                    this.propertyReader = new FieldReader(field, ReflectionUtils.findMethod(javaClass, NameUtils.getGetterName(this.propertyName)));
                    this.propertyWriter = new FieldWriter(field, ReflectionUtils.findMethod(javaClass, NameUtils.getSetterName(this.propertyName),
                            field.getType()));
                }
                else {
                    PropertyDescriptor descriptor = this.cpf.getPropertyDescriptor(this.propertyName);
                    Method readMethod = descriptor.getReadMethod();

                    Traits.TraitBridge traitBridge = readMethod.getAnnotation(Traits.TraitBridge.class);
                    String traitFieldName;
                    if (traitBridge != null) {
                        traitFieldName = getTraitFieldName(traitBridge, this.propertyName);
                    }
                    else {
                        Traits.Implemented traitImplemented = readMethod.getAnnotation(Traits.Implemented.class);
                        if (traitImplemented != null) {
                            traitFieldName = getTraitFieldName(readMethod.getDeclaringClass(), this.propertyName);
                        }
                        else {
                            traitFieldName = null;
                        }
                    }
                    if (traitFieldName != null) {
                        field = ReflectionUtils.findField(javaClass, traitFieldName);
                        if (field != null) {
                            ReflectionUtils.makeAccessible(field);
                            this.propertyReader = new FieldReader(field, readMethod);
                            this.propertyWriter = new FieldWriter(field, descriptor.getWriteMethod());
                        }
                        else {
                            Method writeMethod = descriptor.getWriteMethod();
                            this.propertyReader = new ReflectMethodReader(readMethod);
                            this.propertyWriter = new ReflectionMethodWriter(writeMethod, descriptor.getPropertyType());
                        }
                    }
                    else {
                        this.propertyReader = new ReflectMethodReader(readMethod);
                        Method writeMethod = descriptor.getWriteMethod();
                        if (writeMethod != null) {
                            this.propertyWriter = new ReflectionMethodWriter(writeMethod, descriptor.getPropertyType());
                        }
                    }
                }
                return this;
            }

        }

    }

}
