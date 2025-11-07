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
package org.grails.datastore.mapping.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import jakarta.persistence.FlushModeType;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

import org.grails.datastore.mapping.cache.TPCacheAdapterRepository;
import org.grails.datastore.mapping.config.Entity;
import org.grails.datastore.mapping.core.impl.PendingDelete;
import org.grails.datastore.mapping.core.impl.PendingInsert;
import org.grails.datastore.mapping.core.impl.PendingOperation;
import org.grails.datastore.mapping.core.impl.PendingOperationExecution;
import org.grails.datastore.mapping.core.impl.PendingUpdate;
import org.grails.datastore.mapping.dirty.checking.DirtyCheckable;
import org.grails.datastore.mapping.dirty.checking.DirtyCheckingSupport;
import org.grails.datastore.mapping.engine.EntityAccess;
import org.grails.datastore.mapping.engine.EntityPersister;
import org.grails.datastore.mapping.engine.NativeEntryEntityPersister;
import org.grails.datastore.mapping.engine.NonPersistentTypeException;
import org.grails.datastore.mapping.engine.Persister;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.mapping.model.PersistentProperty;
import org.grails.datastore.mapping.query.Query;
import org.grails.datastore.mapping.query.api.QueryableCriteria;
import org.grails.datastore.mapping.transactions.Transaction;

/**
 * Abstract implementation of the {@link org.grails.datastore.mapping.core.Session} interface that uses
 * a list of {@link org.grails.datastore.mapping.engine.Persister} instances
 * to save, update and delete instances
 *
 * @param <N>
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractSession<N> extends AbstractAttributeStoringSession implements SessionImplementor {

    public static final String ENTITY_ACCESS = "org.grails.gorm.ENTITY_ACCESS";

    private static final RemovalListener<PersistentEntity, Collection<PendingInsert>> EXCEPTION_THROWING_INSERT_LISTENER =
            (key, value, cause) -> {
                if (cause.wasEvicted()) {
                    throw new DataAccessResourceFailureException("Maximum number (5000) of insert operations to flush() exceeded. " +
                            "Flush the session periodically to avoid this error for batch operations.");
                }
            };

    private static final RemovalListener<PersistentEntity, Collection<PendingUpdate>> EXCEPTION_THROWING_UPDATE_LISTENER =
            (key, value, cause) -> {
                if (cause.wasEvicted()) {
                    throw new DataAccessResourceFailureException("Maximum number (5000) of update operations to flush() exceeded. " +
                            "Flush the session periodically to avoid this error for batch operations.");
                }
            };

    private static final RemovalListener<PersistentEntity, Collection<PendingDelete>> EXCEPTION_THROWING_DELETE_LISTENER =
            (key, value, cause) -> {
                if (cause.wasEvicted()) {
                    throw new DataAccessResourceFailureException("Maximum number (5000) of delete operations to flush() exceeded. " +
                            "Flush the session periodically to avoid this error for batch operations.");
                }
            };

    private static final String NULL = "null";

    protected Map<Class, Persister> persisters = new ConcurrentHashMap<>();

    protected boolean isSynchronizedWithTransaction = false;

    private MappingContext mappingContext;

    protected ConcurrentLinkedQueue lockedObjects = new ConcurrentLinkedQueue();

    protected Transaction transaction;

    private Datastore datastore;

    private FlushModeType flushMode = FlushModeType.AUTO;

    protected Map<Class, Map<Serializable, Object>> firstLevelCache = new ConcurrentHashMap<>();

    protected Map<Class, Map<Serializable, Object>> firstLevelEntryCache = new ConcurrentHashMap<>();

    protected Map<Class, Map<Serializable, Object>> firstLevelEntryCacheDirtyCheck = new ConcurrentHashMap<>();

    protected Map<CollectionKey, Collection> firstLevelCollectionCache = new ConcurrentHashMap<>();

    protected TPCacheAdapterRepository cacheAdapterRepository;

    private Collection<Serializable> objectsPendingOperations = new ConcurrentLinkedQueue<>();

    private Map<PersistentEntity, Collection<PendingInsert>> pendingInserts =
            Caffeine.newBuilder()
                    .removalListener(EXCEPTION_THROWING_INSERT_LISTENER)
                    .executor(Runnable::run)
                    .maximumSize(5000).build().asMap();

    private Map<PersistentEntity, Collection<PendingUpdate>> pendingUpdates =
            Caffeine.newBuilder()
                    .removalListener(EXCEPTION_THROWING_UPDATE_LISTENER)
                    .executor(Runnable::run)
                    .maximumSize(5000).build().asMap();

    private Map<PersistentEntity, Collection<PendingDelete>> pendingDeletes =
            Caffeine.newBuilder()
                    .removalListener(EXCEPTION_THROWING_DELETE_LISTENER)
                    .executor(Runnable::run)
                    .maximumSize(5000).build().asMap();

    protected Collection<Runnable> postFlushOperations = new ConcurrentLinkedQueue<>();

    private boolean exceptionOccurred;

    protected ApplicationEventPublisher publisher;

    protected boolean stateless = false;

    protected boolean flushActive = false;


    public AbstractSession(Datastore datastore, MappingContext mappingContext,
            ApplicationEventPublisher publisher) {
        this(datastore, mappingContext, publisher, false);
    }

    public AbstractSession(Datastore datastore, MappingContext mappingContext,
            ApplicationEventPublisher publisher, boolean stateless) {
        this.mappingContext = mappingContext;
        this.datastore = datastore;
        this.publisher = publisher;
        this.stateless = stateless;
    }

    public AbstractSession(Datastore datastore, MappingContext mappingContext,
            ApplicationEventPublisher publisher, TPCacheAdapterRepository cacheAdapterRepository) {
        this(datastore, mappingContext, publisher, false);
        this.cacheAdapterRepository = cacheAdapterRepository;
    }

    public AbstractSession(Datastore datastore, MappingContext mappingContext,
            ApplicationEventPublisher publisher, TPCacheAdapterRepository cacheAdapterRepository, boolean stateless) {
        this(datastore, mappingContext, publisher, stateless);
        this.cacheAdapterRepository = cacheAdapterRepository;
    }

    @Override
    public boolean isSchemaless() {
        return this.datastore.isSchemaless();
    }

    @Override
    public boolean isStateless() {
        return this.stateless;
    }

    public void addPostFlushOperation(Runnable runnable) {
        if (runnable != null && !this.postFlushOperations.contains(runnable)) {
            this.postFlushOperations.add(runnable);
        }
    }

    public void addPendingInsert(PendingInsert insert) {
        final Object o = insert.getObject();
        if (o != null) {
            registerPending(o);
        }
        Collection<PendingInsert> inserts = this.pendingInserts.get(insert.getEntity());
        if (inserts == null) {
            inserts = new ConcurrentLinkedQueue<>();
            this.pendingInserts.put(insert.getEntity(), inserts);
        }

        inserts.add(insert);
    }

    @Override
    public boolean isPendingAlready(Object obj) {
        Serializable id = getPersister(obj).getObjectIdentifier(obj);
        if (id != null) {
            return this.objectsPendingOperations.contains(id);
        }
        else {
            return this.objectsPendingOperations.contains(System.identityHashCode(obj));
        }
    }

    @Override
    public void registerPending(Object obj) {
        if (obj != null) {
            Serializable id = getPersister(obj).getObjectIdentifier(obj);
            if (id != null) {
                if (!this.objectsPendingOperations.contains(id)) {
                    this.objectsPendingOperations.add(id);
                }
            }
            else {
                int identityHashCode = System.identityHashCode(obj);
                if (!this.objectsPendingOperations.contains(identityHashCode)) {
                    this.objectsPendingOperations.add(identityHashCode);
                }
            }
        }
    }

    @Override
    public void addPendingUpdate(PendingUpdate update) {
        final Object o = update.getObject();
        if (o != null) {
            registerPending(o);
        }

        Collection<PendingUpdate> inserts = this.pendingUpdates.get(update.getEntity());
        if (inserts == null) {
            inserts = new ConcurrentLinkedQueue<>();
            this.pendingUpdates.put(update.getEntity(), inserts);
        }

        inserts.add(update);
    }

    public void addPendingDelete(PendingDelete delete) {
        final Object o = delete.getObject();
        if (o != null) {
            registerPending(o);
        }

        Collection<PendingDelete> deletes = this.pendingDeletes.get(delete.getEntity());
        if (deletes == null) {
            deletes = new ConcurrentLinkedQueue<>();
            this.pendingDeletes.put(delete.getEntity(), deletes);
        }

        deletes.add(delete);
    }

    @Override
    public Object getCachedEntry(PersistentEntity entity, Serializable key) {
        if (isStateless(entity)) {
            return null;
        }
        return getCachedEntry(entity, key, false);
    }

    @Override
    public Object getCachedEntry(PersistentEntity entity, Serializable key, boolean forDirtyCheck) {
        if (isStateless(entity)) {
            return null;
        }
        if (key == null) {
            return null;
        }

        return getEntryCache(entity.getJavaClass(), forDirtyCheck).get(key);
    }

    @Override
    public void cacheEntry(PersistentEntity entity, Serializable key, Object entry) {
        if (isStateless(entity)) {
            return;
        }
        if (key == null || entry == null) {
            return;
        }

        cacheEntry(key, entry, getEntryCache(entity.getJavaClass(), true), true);
        cacheEntry(key, entry, getEntryCache(entity.getJavaClass(), false), false);
    }

    @Override
    public boolean isStateless(PersistentEntity entity) {
        Entity mappedForm = entity != null ? entity.getMapping().getMappedForm() : null;
        return isStateless() || (mappedForm != null && mappedForm.isStateless());
    }

    protected void cacheEntry(Serializable key, Object entry, Map<Serializable, Object> entryCache, boolean forDirtyCheck) {
        if (isStateless()) {
            return;
        }
        entryCache.put(key, entry);
    }

    @Override
    public Collection getCachedCollection(PersistentEntity entity, Serializable key, String name) {
        if (isStateless(entity)) {
            return null;
        }
        if (key == null || name == null) {
            return null;
        }

        return this.firstLevelCollectionCache.get(
                new CollectionKey(entity.getJavaClass(), key, name));
    }

    @Override
    public void cacheCollection(PersistentEntity entity, Serializable key, Collection collection, String name) {
        if (isStateless(entity)) {
            return;
        }
        if (key == null || collection == null || name == null) {
            return;
        }

        this.firstLevelCollectionCache.put(
                new CollectionKey(entity.getJavaClass(), key, name),
                collection);
    }

    @Override
    public Map<PersistentEntity, Collection<PendingInsert>> getPendingInserts() {
        return this.pendingInserts;
    }

    @Override
    public Map<PersistentEntity, Collection<PendingUpdate>> getPendingUpdates() {
        return this.pendingUpdates;
    }

    @Override
    public Map<PersistentEntity, Collection<PendingDelete>> getPendingDeletes() {
        return this.pendingDeletes;
    }

    @Override
    public FlushModeType getFlushMode() {
        return this.flushMode;
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
    }

    @Override
    public Datastore getDatastore() {
        return this.datastore;
    }

    @Override
    public MappingContext getMappingContext() {
        return this.mappingContext;
    }

    @Override
    public void flush() {
        if (this.flushActive) {
            return;
        }

        boolean hasInserts;
        try {
            if (this.exceptionOccurred) {
                throw new InvalidDataAccessResourceUsageException(
                        "Do not flush() the Session after an exception occurs");
            }

            this.flushActive = true;

            hasInserts = hasUpdates();
            if (hasInserts) {
                flushPendingInserts(this.pendingInserts);
                flushPendingUpdates(this.pendingUpdates);
                flushPendingDeletes(this.pendingDeletes);

                this.firstLevelCollectionCache.clear();

                executePendings(this.postFlushOperations);
            }

        }
        finally {
            clearPendingOperations();
            this.flushActive = false;
        }
        postFlush(hasInserts);
    }

    protected void flushPendingDeletes(Map<PersistentEntity, Collection<PendingDelete>> pendingDeletes) {
        final Collection<Collection<PendingDelete>> deletes = pendingDeletes.values();
        for (Collection<PendingDelete> delete : deletes) {
            flushPendingOperations(delete);
        }
    }

    @Override
    public boolean isDirty(Object instance) {
        if (instance == null) {
            return false;
        }

        EntityPersister persister = (EntityPersister) getPersister(instance);
        if (persister == null) {
            return false;
        }

        if (instance instanceof DirtyCheckable) {
            return ((DirtyCheckable) instance).hasChanged() ||
                    DirtyCheckingSupport.areAssociationsDirty(this, persister.getPersistentEntity(), instance);
        }

        if (!(persister instanceof NativeEntryEntityPersister)) {
            return false;
        }

        Serializable id = persister.getObjectIdentifier(instance);
        if (id == null) {
            // not persistent
            return false;
        }

        Object entry = getCachedEntry(persister.getPersistentEntity(), id, false);
        Object instance2 = getCachedInstance(instance.getClass(), id);
        return instance != instance2 || ((NativeEntryEntityPersister) persister).isDirty(instance, entry);
    }

    @Override
    public Serializable getObjectIdentifier(Object instance) {
        Persister persister = getPersister(instance);
        if (persister != null) {
            return persister.getObjectIdentifier(instance);
        }
        return null;
    }


    /**
     * The default implementation of flushPendingUpdates is to iterate over each update operation
     * and execute them one by one. This may be suboptimal for stores that support batch update
     * operations. Subclasses can override this method to implement batch update more efficiently.
     *
     * @param updates
     */
    protected void flushPendingUpdates(Map<PersistentEntity, Collection<PendingUpdate>> updates) {
        for (Collection<PendingUpdate> pending : updates.values()) {
            flushPendingOperations(pending);
        }
    }

    /**
     * The default implementation of flushPendingInserts is to iterate over each insert operations
     * and execute them one by one. This may be suboptimal for stores that support batch insert
     * operations. Subclasses can override this method to implement batch insert more efficiently.
     *
     * @param inserts The insert operations
     */
    protected void flushPendingInserts(Map<PersistentEntity, Collection<PendingInsert>> inserts) {
        for (Collection<PendingInsert> pending : inserts.values()) {
            flushPendingOperations(pending);
        }
    }

    private void flushPendingOperations(Collection operations) {
        for (Object o : operations) {
            PendingOperation pendingOperation = (PendingOperation) o;
            try {
                PendingOperationExecution.executePendingOperation(pendingOperation);
            }
            catch (RuntimeException e) {
                setFlushMode(FlushModeType.COMMIT);
                this.exceptionOccurred = true;
                throw e;
            }
        }
    }

    private boolean hasUpdates() {
        return !this.pendingInserts.isEmpty() || !this.pendingUpdates.isEmpty() ||
                !this.pendingDeletes.isEmpty() || !this.postFlushOperations.isEmpty();
    }

    protected void postFlush(boolean hasUpdates) {
        // do nothing
    }

    protected void executePendings(Collection<? extends Runnable> pendings) {
        try {
            for (Runnable pending : pendings) {
                pending.run();
            }
        }
        catch (RuntimeException e) {
            setFlushMode(FlushModeType.COMMIT);
            this.exceptionOccurred = true;
            throw e;
        }
    }

    @Override
    public void clear() {
        clearMaps(this.firstLevelCache);
        clearMaps(this.firstLevelEntryCache);
        clearMaps(this.firstLevelEntryCacheDirtyCheck);
        this.firstLevelCollectionCache.clear();
        clearPendingOperations();
        this.attributes.clear();
        this.exceptionOccurred = false;
    }

    protected void clearPendingOperations() {
        this.objectsPendingOperations.clear();
        this.pendingInserts.clear();
        this.pendingUpdates.clear();
        this.pendingDeletes.clear();
        this.postFlushOperations.clear();
    }

    private void clearMaps(Map<Class, Map<Serializable, Object>> mapOfMaps) {
        for (Map<Serializable, Object> cache : mapOfMaps.values()) {
            cache.clear();
        }
    }

    @Override
    public Persister getPersister(Object o) {
        if (o == null) {
            return null;
        }
        Class cls;
        if (o instanceof Class) {
            cls = (Class) o;
        }
        else if (o instanceof PersistentEntity) {
            cls = ((PersistentEntity) o).getJavaClass();
        }
        else {
            cls = o.getClass();
        }
        Persister p = this.persisters.get(cls);
        if (p == null) {
            p = createPersister(cls, getMappingContext());
            if (p != null) {
                if (!isStateless(((EntityPersister) p).getPersistentEntity())) {
                    this.firstLevelCache.put(cls, new ConcurrentHashMap<Serializable, Object>());
                }
                this.persisters.put(cls, p);
            }
        }
        return p;
    }

    protected abstract Persister createPersister(Class cls, MappingContext mappingContext);

    @Override
    public boolean contains(Object o) {
        if (o == null || isStateless()) {
            return false;
        }

        final Serializable identifier = getObjectIdentifier(o);
        if (identifier != null) {
            return getInstanceCache(o.getClass()).containsKey(identifier);
        }
        else {
            return getInstanceCache(o.getClass()).containsValue(o);
        }
    }

    @Override
    public boolean isCached(Class type, Serializable key) {
        PersistentEntity entity = getMappingContext().getPersistentEntity(type.getName());
        if (type == null || key == null || isStateless(entity)) {
            return false;
        }

        return getInstanceCache(type).containsKey(key);
    }

    @Override
    public void cacheInstance(Class type, Serializable key, Object instance) {
        if (type == null || key == null || instance == null) {
            return;
        }
        if (isStateless(getMappingContext().getPersistentEntity(type.getName()))) {
            return;
        }
        getInstanceCache(type).put(key, instance);
    }

    @Override
    public Object getCachedInstance(Class type, Serializable key) {
        if (isStateless()) {
            return null;
        }
        if (type == null || key == null) {
            return null;
        }
        if (isStateless(getMappingContext().getPersistentEntity(type.getName()))) {
            return null;
        }
        return getInstanceCache(type).get(key);
    }

    @Override
    public void clear(Object o) {
        if (o == null || isStateless()) {
            return;
        }

        final Map<Serializable, Object> cache = this.firstLevelCache.get(o.getClass());
        if (cache != null) {
            Persister persister = getPersister(o);
            Serializable key = persister.getObjectIdentifier(o);
            if (key != null) {
                cache.remove(key);
            }
        }
        removeAttributesForEntity(o);
    }

    @Override
    public void attach(Object o) {
        if (o == null) {
            return;
        }

        EntityPersister p = (EntityPersister) getPersister(o);
        if (p == null) {
            return;
        }

        Serializable identifier = p.getObjectIdentifier(o);
        if (identifier != null) {
            cacheObject(identifier, o);
        }
    }

    protected void cacheObject(Serializable identifier, Object o) {
        if (identifier == null || o == null) {
            return;
        }
        cacheInstance(o.getClass(), identifier, o);
    }

    @Override
    public Serializable persist(Object o) {
        Assert.notNull(o, "Cannot persist null object");
        Persister persister = getPersister(o);
        if (persister == null) {
            throw new NonPersistentTypeException("Object [" + o +
                    "] cannot be persisted. It is not a known persistent type.");
        }

        final Serializable key = persister.persist(o);
        cacheObject(key, o);
        return key;
    }

    @Override
    public Serializable insert(Object o) {
        Assert.notNull(o, "Cannot persist null object");
        Persister persister = getPersister(o);
        if (persister == null) {
            throw new NonPersistentTypeException("Object [" + o +
                    "] cannot be persisted. It is not a known persistent type.");
        }

        final Serializable key = persister.insert(o);
        cacheObject(key, o);
        return key;
    }

    @Override
    public void refresh(Object o) {
        Assert.notNull(o, "Cannot persist null object");
        Persister persister = getPersister(o);
        if (persister == null) {
            throw new NonPersistentTypeException("Object [" + o +
                    "] cannot be refreshed. It is not a known persistent type.");
        }

        final Serializable key = persister.refresh(o);
        cacheObject(key, o);
    }

    @Override
    public Object retrieve(Class type, Serializable key) {
        if (key == null || type == null || NULL.equals(key)) {
            return null;
        }

        Persister persister = getPersister(type);
        if (persister == null) {
            throw new NonPersistentTypeException("Cannot retrieve object with key [" + key +
                    "]. The class [" + type.getName() + "] is not a known persistent type.");
        }

        final PersistentEntity entity = ((EntityPersister) persister).getPersistentEntity();
        if (entity != null) {
            final PersistentProperty identity = entity.getIdentity();
            if (!identity.getType().isAssignableFrom(key.getClass())) {
                key = convertIdentityIfNecessasry(identity, key);
            }
        }

        if (key == null) {
            return null;
        }

        Object o = getInstanceCache(type).get(key);
        if (o == null) {
            o = persister.retrieve(key);
            if (o != null) {
                cacheObject(key, o);
            }
        }
        return o;
    }

    protected Serializable convertIdentityIfNecessasry(PersistentProperty identity, Serializable key) {
        ConversionService conversionService = getMappingContext().getConversionService();
        if (conversionService.canConvert(key.getClass(), identity.getType())) {
            try {
                key = (Serializable) conversionService.convert(key, identity.getType());
            }
            catch (ConversionFailedException conversionFailedException) {
                // ignore
            }
        }
        return key;
    }

    @Override
    public Object proxy(Class type, Serializable key) {
        if (key == null || type == null) {
            return null;
        }

        Persister persister = getPersister(type);
        if (persister == null) {
            throw new NonPersistentTypeException("Cannot retrieve object with key [" + key +
                    "]. The class [" + type.getName() + "] is not a known persistent type.");
        }

        // only return proxy if real instance is not available.
        Object o = getInstanceCache(type).get(key);
        if (o == null) {
            o = persister.proxy(key);
        }

        return o;
    }

    @Override
    public void lock(Object o) {
        throw new UnsupportedOperationException("Datastore [" + getClass().getName() + "] does not support locking.");
    }

    @Override
    public Object lock(Class type, Serializable key) {
        throw new UnsupportedOperationException("Datastore [" + getClass().getName() + "] does not support locking.");
    }

    @Override
    public void unlock(Object o) {
        if (o != null) {
            this.lockedObjects.remove(o);
        }
    }

    /**
     * This default implementation of the deleteAll method is unlikely to be optimal as it iterates and deletes each object.
     * <p>
     * Subclasses should override to optimize for the batch operation capability of the underlying datastore
     *
     * @param criteria The criteria
     */
    @Override
    public long deleteAll(QueryableCriteria criteria) {
        List list = criteria.list();
        delete(list);
        return list.size();
    }

    /**
     * This default implementation of updateAll is unlikely to be optimal as it iterates and updates each object one by one.
     * <p>
     * Subclasses should override to optimize for the batch operation capability of the underlying datastore
     *
     * @param criteria   The criteria
     * @param properties The properties
     */
    @Override
    public long updateAll(QueryableCriteria criteria, Map<String, Object> properties) {
        List list = criteria.list();
        for (Object o : list) {
            BeanWrapper bean = new BeanWrapperImpl(o);
            for (String property : properties.keySet()) {
                bean.setPropertyValue(property, properties.get(property));
            }
        }
        persist(list);
        return list.size();
    }

    @Override
    public void delete(final Object obj) {
        if (obj == null) {
            return;
        }

        final EntityPersister p = (EntityPersister) getPersister(obj);
        if (p == null) {
            return;
        }

        p.delete(obj);
        clear(obj);
    }

    @Override
    public void delete(final Iterable objects) {
        if (objects == null) {
            return;
        }

        // sort the objects into sets by Persister, in case the objects are of different types.
        Map<Persister, List> toDelete = new HashMap<>();
        for (Object object : objects) {
            if (object == null) {
                continue;
            }
            final Persister p = getPersister(object);
            if (p == null) {
                continue;
            }
            List listForPersister = toDelete.get(p);
            if (listForPersister == null) {
                toDelete.put(p, listForPersister = new ArrayList());
            }
            listForPersister.add(object);
        }
        // for each type (usually only 1 type), set up a pendingDelete of that type
        for (Map.Entry<Persister, List> entry : toDelete.entrySet()) {
            final EntityPersister p = (EntityPersister) entry.getKey();
            p.delete(entry.getValue());
        }
    }

    @Override
    public List<Serializable> persist(Iterable objects) {
        if (objects == null) {
            return Collections.emptyList();
        }

        final Iterator i = objects.iterator();
        if (!i.hasNext()) {
            return Collections.emptyList();
        }

        // peek at the first object to get the persister
        final Object obj = i.next();
        final Persister p = getPersister(obj);
        if (p == null) {
            throw new NonPersistentTypeException("Cannot persist objects. The class [" +
                    obj.getClass().getName() + "] is not a known persistent type.");
        }

        return p.persist(objects);
    }

    @Override
    public List retrieveAll(Class type, Iterable keys) {
        EntityPersister p = (EntityPersister) getPersister(type);
        if (p == null) {
            throw new NonPersistentTypeException("Cannot retrieve objects with keys [" + keys +
                    "]. The class [" + type.getName() + "] is not a known persistent type.");
        }

        List list = new ArrayList();
        List<Serializable> toRetrieve = new ArrayList<>();
        final Map<Serializable, Object> cache = getInstanceCache(type);
        for (Object key : keys) {
            Serializable serializable = (Serializable) key;
            Object cached = cache.get(serializable);
            list.add(cached);
            if (cached == null) {
                toRetrieve.add(serializable);
            }
        }
        List<Object> retrieved = p.retrieveAll(toRetrieve);
        Iterator<Serializable> keyIterator = toRetrieve.iterator();
        Map<Serializable, Object> retrievedMap = new HashMap<>();
        for (Object o : retrieved) {
            final Serializable identifier = p.getObjectIdentifier(o);
            if (identifier != null) {
                retrievedMap.put(identifier, o);
            }
        }
        // now fill in the null entries (possibly with more nulls)
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o == null) {
                if (keyIterator.hasNext()) {
                    Serializable key = keyIterator.next();
                    key = (Serializable) this.mappingContext.getConversionService().convert(key, p.getPersistentEntity().getIdentity().getType());
                    final Object next = retrievedMap.get(key);
                    list.set(i, next);
                    cacheInstance(type, key, next);
                }
            }
        }
        return list;
    }

    @Override
    public List retrieveAll(Class type, Serializable... keys) {
        Persister p = getPersister(type);
        if (p == null) {
            throw new NonPersistentTypeException("Cannot retrieve objects with keys [" + keys +
                    "]. The class [" + type.getName() + "] is not a known persistent type.");
        }
        return retrieveAll(type, Arrays.asList(keys));
    }

    @Override
    public Query createQuery(Class type) {
        Persister p = getPersister(type);
        if (p == null) {
            throw new NonPersistentTypeException("Cannot create query. The class [" + type +
                    "] is not a known persistent type.");
        }

        return p.createQuery();
    }

    @Override
    public final Transaction beginTransaction() {
        return beginTransaction(new DefaultTransactionDefinition());
    }

    @Override
    public Transaction beginTransaction(TransactionDefinition definition) {
        this.transaction = beginTransactionInternal();
        return this.transaction;
    }

    protected abstract Transaction beginTransactionInternal();

    @Override
    public Transaction getTransaction() {
        if (this.transaction == null) {
            throw new NoTransactionException("Transaction not started. Call beginTransaction() first");
        }
        return this.transaction;
    }

    @Override
    public boolean hasTransaction() {
        return this.transaction != null;
    }

    private Map<Serializable, Object> getInstanceCache(Class c) {
        Map<Serializable, Object> cache = this.firstLevelCache.get(c);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            this.firstLevelCache.put(c, cache);
        }
        return cache;
    }

    private Map<Serializable, Object> getEntryCache(Class c, boolean forDirtyCheck) {
        Map<Class, Map<Serializable, Object>> caches = forDirtyCheck ? this.firstLevelEntryCacheDirtyCheck : this.firstLevelEntryCache;
        Map<Serializable, Object> cache = caches.get(c);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            caches.put(c, cache);
        }
        return cache;
    }

    @Override
    public EntityAccess createEntityAccess(PersistentEntity entity, Object instance) {
        return getMappingContext().createEntityAccess(entity, instance);
    }

    /**
     * Whether the session is synchronized with an external transaction
     *
     * @param isSynchronizedWithTransaction True if it is
     */
    @Override
    public void setSynchronizedWithTransaction(boolean isSynchronizedWithTransaction) {
        this.isSynchronizedWithTransaction = isSynchronizedWithTransaction;
    }

    private static final class CollectionKey {

        final Class clazz;

        final Serializable key;

        final String collectionName;

        private CollectionKey(Class clazz, Serializable key, String collectionName) {
            this.clazz = clazz;
            this.key = key;
            this.collectionName = collectionName;
        }

        @Override
        public int hashCode() {
            int value = 17;
            value = value * 37 + this.clazz.getName().hashCode();
            value = value * 37 + this.key.hashCode();
            value = value * 37 + this.collectionName.hashCode();
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            CollectionKey other = (CollectionKey) obj;
            return other.clazz.getName() == this.clazz.getName() &&
                    other.key.equals(this.key) &&
                    other.collectionName.equals(this.collectionName);
        }

        @Override
        public String toString() {
            return this.clazz.getName() + ':' + this.key + ':' + this.collectionName;
        }

    }

}
