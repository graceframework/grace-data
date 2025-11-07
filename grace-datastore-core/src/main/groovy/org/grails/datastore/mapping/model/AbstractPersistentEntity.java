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
package org.grails.datastore.mapping.model;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import org.grails.datastore.mapping.config.Entity;
import org.grails.datastore.mapping.core.EntityCreationException;
import org.grails.datastore.mapping.core.exceptions.ConfigurationException;
import org.grails.datastore.mapping.model.config.GormProperties;
import org.grails.datastore.mapping.model.types.Association;
import org.grails.datastore.mapping.model.types.Embedded;
import org.grails.datastore.mapping.model.types.Identity;
import org.grails.datastore.mapping.model.types.OneToMany;
import org.grails.datastore.mapping.model.types.TenantId;
import org.grails.datastore.mapping.multitenancy.MultiTenancySettings;
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher;
import org.grails.datastore.mapping.reflect.EntityReflector;

/**
 * Abstract implementation to be subclasses on a per datastore basis
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractPersistentEntity<T extends Entity> implements PersistentEntity {

    protected final Class javaClass;

    protected final MappingContext context;

    protected List<PersistentProperty> persistentProperties;

    protected List<Association> associations;

    protected List<Embedded> embedded;

    protected Map<String, PersistentProperty> propertiesByName = new HashMap<String, PersistentProperty>();

    protected Map<String, PersistentProperty> mappedPropertiesByName = new HashMap<String, PersistentProperty>();

    protected PersistentProperty identity;

    protected PersistentProperty version;

    protected List<String> persistentPropertyNames;

    private final String decapitalizedName;

    protected Set owners;

    private PersistentEntity parentEntity;

    private boolean external;

    private boolean initialized = false;

    private boolean propertiesInitialized = false;

    private boolean versionCompatibleType;

    private boolean versioned = true;

    private PersistentProperty[] compositeIdentity;

    private final String mappingStrategy;

    private final boolean isAbstract;

    private EntityReflector entityReflector;

    private final boolean isMultiTenant;

    private TenantId tenantId;

    public AbstractPersistentEntity(Class javaClass, MappingContext context) {
        Assert.notNull(javaClass, "The argument [javaClass] cannot be null");
        this.javaClass = javaClass;
        this.context = context;
        this.isAbstract = Modifier.isAbstract(javaClass.getModifiers());
        this.isMultiTenant = org.grails.datastore.mapping.reflect.ClassUtils.isMultiTenant(javaClass);
        this.decapitalizedName = Introspector.decapitalize(javaClass.getSimpleName());
        String classSpecified = ClassPropertyFetcher.getStaticPropertyValue(javaClass, GormProperties.MAPPING_STRATEGY, String.class);
        this.mappingStrategy = classSpecified != null ? classSpecified : GormProperties.DEFAULT_MAPPING_STRATEGY;
    }

    @Override
    public PersistentProperty[] getCompositeIdentity() {
        return this.compositeIdentity;
    }

    @Override
    public TenantId getTenantId() {
        return this.tenantId;
    }

    @Override
    public boolean isMultiTenant() {
        return this.isMultiTenant;
    }

    @Override
    public boolean isExternal() {
        return this.external;
    }

    public boolean isAbstract() {
        return this.isAbstract;
    }

    public String getMappingStrategy() {
        return this.mappingStrategy;
    }

    @Override
    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public MappingContext getMappingContext() {
        return this.context;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void initialize() {
        ClassMapping<T> mapping = getMapping();
        if (!this.initialized) {
            this.initialized = true;

            final MappingConfigurationStrategy mappingSyntaxStrategy = this.context.getMappingSyntaxStrategy();
            this.owners = mappingSyntaxStrategy.getOwningEntities(this.javaClass, this.context);
            Class superClass = this.javaClass.getSuperclass();
            if (superClass != null &&
                    !superClass.equals(Object.class)) {

                if (mappingSyntaxStrategy.isPersistentEntity(superClass)) {
                    this.parentEntity = this.context.addPersistentEntity(superClass);
                }
            }

            this.persistentProperties = mappingSyntaxStrategy.getPersistentProperties(this, this.context, mapping, includeIdentifiers());

            this.persistentPropertyNames = new ArrayList<>();
            this.associations = new ArrayList();
            this.embedded = new ArrayList();


            boolean multiTenancyEnabled = this.isMultiTenant &&
                    this.context.getMultiTenancyMode() == MultiTenancySettings.MultiTenancyMode.DISCRIMINATOR;
            for (PersistentProperty persistentProperty : this.persistentProperties) {
                if (multiTenancyEnabled && persistentProperty instanceof TenantId) {
                    this.tenantId = (TenantId) persistentProperty;
                }
                if (persistentProperty instanceof Identity) {
                    if (this.compositeIdentity != null) {
                        int l = this.compositeIdentity.length;
                        this.compositeIdentity = Arrays.copyOf(this.compositeIdentity, l + 1);
                        this.compositeIdentity[l] = this.identity;
                    }
                    else if (this.identity != null) {
                        this.compositeIdentity = new PersistentProperty[] { this.identity, persistentProperty };
                        this.identity = null;
                    }
                    else {
                        this.identity = persistentProperty;
                    }
                }

                if (!(persistentProperty instanceof OneToMany)) {
                    this.persistentPropertyNames.add(persistentProperty.getName());
                }

                if (persistentProperty instanceof Association) {
                    this.associations.add((Association) persistentProperty);
                }
                if (persistentProperty instanceof Embedded) {
                    this.embedded.add((Embedded) persistentProperty);
                }
                this.propertiesByName.put(persistentProperty.getName(), persistentProperty);
                final String targetName = persistentProperty.getMapping().getMappedForm().getTargetName();
                if (targetName != null) {
                    this.mappedPropertiesByName.put(targetName, persistentProperty);
                }
            }
            if (this.associations.isEmpty()) {
                this.associations = Collections.emptyList();
            }
            if (this.embedded.isEmpty()) {
                this.embedded = Collections.emptyList();
            }

            if (this.identity == null && this.compositeIdentity == null) {
                this.identity = resolveIdentifier();
            }

            if (multiTenancyEnabled && this.tenantId == null) {
                throw new ConfigurationException("Class [" + this.javaClass.getName() +
                        "] is multi tenant but does not specify a tenant identifier property");
            }

            if (!isExternal()) {
                final T mappedForm = mapping.getMappedForm(); // initialize mapping

                if (mappedForm.isVersioned()) {
                    this.version = this.propertiesByName.get(GormProperties.VERSION);
                    if (this.version == null) {
                        this.versioned = false;
                    }
                }
                else {
                    this.versioned = false;
                }
            }

            final PersistentProperty v = getVersion();
            if (v != null) {
                final Class type = v.getType();
                this.versionCompatibleType = Number.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type);
            }

            if (this.identity != null) {
                String idName = this.identity.getName();
                PersistentProperty idProp = this.propertiesByName.get(idName);
                if (idProp == null) {
                    this.propertiesByName.put(idName, this.identity);
                }
                else {
                    this.persistentProperties.remove(idProp);
                    this.persistentPropertyNames.remove(idProp.getName());
                    if (!idProp.getName().equals(GormProperties.IDENTITY)) {
                        disableDefaultId();
                    }
                }
            }
            IdentityMapping identifier = mapping != null ? mapping.getIdentifier() : null;
            if (this.identity == null && identifier != null) {

                final String[] identifierName = identifier.getIdentifierName();
                final MappingContext mappingContext = getMappingContext();
                if (identifierName.length > 1) {
                    this.compositeIdentity = mappingContext.getMappingSyntaxStrategy().getCompositeIdentity(this.javaClass, mappingContext);
                }
                for (String in : identifierName) {
                    final PersistentProperty p = this.propertiesByName.get(in);
                    if (p != null) {
                        this.persistentProperties.remove(p);
                    }
                    this.persistentPropertyNames.remove(in);
                }
                disableDefaultId();
            }
        }

        this.propertiesInitialized = true;
        this.entityReflector = getMappingContext().getEntityReflector(this);
    }

    private void disableDefaultId() {
        PersistentProperty otherId = getPropertyByName(GormProperties.IDENTITY);
        if (otherId != null) {
            this.persistentProperties.remove(otherId);
            this.persistentPropertyNames.remove(GormProperties.IDENTITY);
        }
    }

    @Override
    public EntityReflector getReflector() {
        return this.entityReflector;
    }

    protected boolean isAnnotatedSuperClass(MappingConfigurationStrategy mappingSyntaxStrategy, Class superClass) {
        Annotation[] annotations = superClass.getAnnotations();
        for (Annotation annotation : annotations) {
            String name = annotation.annotationType().getName();
            if (name.equals("grails.persistence.Entity") || name.equals("grails.gorm.annotation.Entity")) {
                return true;
            }
        }
        return false;
    }

    protected boolean includeIdentifiers() {
        return true;
    }

    protected PersistentProperty resolveIdentifier() {
        return this.context.getMappingSyntaxStrategy().getIdentity(this.javaClass, this.context);
    }

    @Override
    public boolean hasProperty(String name, Class type) {
        final PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(getJavaClass(), name);
        return pd != null && pd.getPropertyType().equals(type);
    }

    @Override
    public boolean isIdentityName(String propertyName) {
        return getIdentity().getName().equals(propertyName);
    }

    @Override
    public PersistentEntity getParentEntity() {
        return this.parentEntity;
    }

    @Override
    public String getDiscriminator() {
        return getJavaClass().getSimpleName();
    }

    @Override
    public PersistentEntity getRootEntity() {
        PersistentEntity root = this;
        PersistentEntity parent = getParentEntity();
        while (parent != null) {
            if (!parent.isInitialized()) {
                parent.initialize();
            }
            root = parent;
            parent = parent.getParentEntity();
        }
        return root;
    }

    @Override
    public boolean isRoot() {
        return getParentEntity() == null;
    }

    @Override
    public boolean isOwningEntity(PersistentEntity owner) {
        return owner != null && this.owners.contains(owner.getJavaClass());
    }

    @Override
    public String getDecapitalizedName() {
        return this.decapitalizedName;
    }

    @Override
    public List<String> getPersistentPropertyNames() {
        return this.persistentPropertyNames;
    }

    @Override
    public ClassMapping<T> getMapping() {
        return new AbstractClassMapping<Entity>(this, this.context) {

            @Override
            public Entity getMappedForm() {
                return new Entity();
            }

        };
    }

    @Override
    public Object newInstance() {
        try {
            return getJavaClass().newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new EntityCreationException("Unable to create entity of type [" + getJavaClass().getName() +
                    "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return this.javaClass.getName();
    }

    @Override
    public PersistentProperty getIdentity() {
        return this.identity;
    }

    @Override
    public PersistentProperty getVersion() {
        return this.version;
    }

    @Override
    public boolean isVersioned() {
        return (this.versionCompatibleType || !this.propertiesInitialized) && this.versioned;
    }

    @Override
    public Class getJavaClass() {
        return this.javaClass;
    }

    @Override
    public boolean isInstance(Object obj) {
        return getJavaClass().isInstance(obj);
    }

    @Override
    public List<PersistentProperty> getPersistentProperties() {
        return this.persistentProperties;
    }

    @Override
    public List<Association> getAssociations() {
        return this.associations;
    }

    @Override
    public List<Embedded> getEmbedded() {
        return this.embedded;
    }

    @Override
    public PersistentProperty getPropertyByName(String name) {
        PersistentProperty pp = this.propertiesByName.get(name);
        if (pp != null) {
            return pp;
        }
        return this.mappedPropertiesByName.get(name);
    }

    @Override
    public int hashCode() {
        return this.javaClass.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PersistentEntity)) {
            return false;
        }
        if (this == o) {
            return true;
        }

        PersistentEntity other = (PersistentEntity) o;
        return this.javaClass.equals(other.getJavaClass());
    }

    @Override
    public String toString() {
        return this.javaClass.getName();
    }

    @Override
    public boolean addOwner(Class type) {
        return this.owners.add(type);
    }

    private static final class MappingProperties {

        private Boolean version = true;

        private boolean intialized = false;

        public boolean isIntialized() {
            return this.intialized;
        }

        public void setIntialized(boolean intialized) {
            this.intialized = intialized;
        }

        public void setVersion(final boolean version) {
            this.version = version;
        }

        public boolean isVersioned() {
            return this.version == null ? true : this.version;
        }

    }

}
