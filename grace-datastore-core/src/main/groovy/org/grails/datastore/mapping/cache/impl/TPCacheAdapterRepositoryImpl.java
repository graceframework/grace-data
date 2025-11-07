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
package org.grails.datastore.mapping.cache.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.grails.datastore.mapping.cache.TPCacheAdapter;
import org.grails.datastore.mapping.cache.TPCacheAdapterRepository;
import org.grails.datastore.mapping.model.PersistentEntity;

/**
 * Simple implementation of {@link TPCacheAdapterRepository}
 *
 * @author Roman Stepanenko
 */
public class TPCacheAdapterRepositoryImpl<T> implements TPCacheAdapterRepository<T> {

    private ConcurrentHashMap<String, TPCacheAdapter<T>> adapters = new ConcurrentHashMap<>();

    public TPCacheAdapter<T> getTPCacheAdapter(PersistentEntity entity) {
        if (entity == null) {
            return null;
        }

        return this.adapters.get(entity.getJavaClass().getName());
    }

    @Override
    public void setTPCacheAdapter(PersistentEntity entity, TPCacheAdapter<T> cacheAdapter) {
        setTPCacheAdapter(entity.getJavaClass(), cacheAdapter);
    }

    @Override
    public void setTPCacheAdapter(@SuppressWarnings("rawtypes") Class entityJavaClass, TPCacheAdapter<T> cacheAdapter) {
        setTPCacheAdapter(entityJavaClass.getName(), cacheAdapter);
    }

    @Override
    public void setTPCacheAdapter(String entityJavaClassFQN, TPCacheAdapter<T> cacheAdapter) {
        this.adapters.put(entityJavaClassFQN, cacheAdapter);
    }

}
