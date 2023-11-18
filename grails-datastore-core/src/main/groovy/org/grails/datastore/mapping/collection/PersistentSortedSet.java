/*
 * Copyright 2012-2023 the original author or authors.
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
package org.grails.datastore.mapping.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.grails.datastore.mapping.core.Session;
import org.grails.datastore.mapping.engine.AssociationQueryExecutor;
import org.grails.datastore.mapping.model.types.Association;

/**
 * A lazy loaded sorted set.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PersistentSortedSet<E> extends AbstractPersistentCollection<E> implements SortedSet<E> {

    public PersistentSortedSet(Association association, Serializable associationKey, Session session) {
        super(association, associationKey, session, new TreeSet());
    }

    public PersistentSortedSet(Class<?> childType, Session session, SortedSet<E> collection) {
        super(childType, session, collection);
    }

    public PersistentSortedSet(Collection<E> keys, Class<?> childType, Session session) {
        super(keys, childType, session, new TreeSet());
    }

    public PersistentSortedSet(Serializable associationKey, Session session, AssociationQueryExecutor indexer) {
        super(associationKey, session, indexer, new TreeSet());
    }

    public Comparator comparator() {
        return getSortedSet().comparator();
    }

    private SortedSet<E> getSortedSet() {
        initialize();
        return ((SortedSet<E>) collection);
    }

    public SortedSet<E> subSet(E o, E o1) {
        return getSortedSet().subSet(o, o1);
    }

    public SortedSet<E> headSet(E o) {
        return getSortedSet().headSet(o);
    }

    public SortedSet<E> tailSet(E o) {
        return getSortedSet().tailSet(o);
    }

    public E first() {
        return getSortedSet().first();
    }

    public E last() {
        return getSortedSet().last();
    }

}

