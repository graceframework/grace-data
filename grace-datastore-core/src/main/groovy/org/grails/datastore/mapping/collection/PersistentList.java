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
package org.grails.datastore.mapping.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.grails.datastore.mapping.core.Session;
import org.grails.datastore.mapping.engine.AssociationQueryExecutor;
import org.grails.datastore.mapping.model.types.Association;

/**
 * A lazy loaded list.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PersistentList extends AbstractPersistentCollection implements List {

    private final List list;

    public PersistentList(Class childType, Session session, List collection) {
        super(childType, session, collection);
        this.list = collection;
    }

    public PersistentList(Collection keys, Class childType, Session session) {
        super(keys, childType, session, new ArrayList());
        this.list = (List) collection;
    }

    public PersistentList(Serializable associationKey, Session session, AssociationQueryExecutor indexer) {
        super(associationKey, session, indexer, new ArrayList());
        this.list = (List) collection;
    }

    public PersistentList(Association association, Serializable associationKey, Session session) {
        super(association, associationKey, session, new ArrayList());
        this.list = (List) collection;
    }

    @Override
    public int indexOf(Object o) {
        initialize();
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        initialize();
        return this.list.lastIndexOf(o);
    }

    @Override
    public Object get(int index) {
        initialize();
        return this.list.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        initialize();
        Object replaced = this.list.set(index, element);
        if (replaced != element) {
            markDirty();
        }
        return replaced;
    }

    @Override
    public void add(int index, Object element) {
        initialize();
        this.list.add(index, element);
        markDirty();
    }

    @Override
    public Object remove(int index) {
        initialize();
        int size = size();
        Object removed = this.list.remove(index);
        if (size != size()) {
            markDirty();
        }
        return removed;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        initialize();
        boolean changed = this.list.addAll(index, c);
        if (changed) {
            markDirty();
        }
        return changed;
    }

    @Override
    public ListIterator listIterator() {
        initialize();
        return new PersistentListIterator(this.list.listIterator());
    }

    @Override
    public ListIterator listIterator(int index) {
        initialize();
        return new PersistentListIterator(this.list.listIterator(index));
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        initialize();
        return this.list.subList(fromIndex, toIndex); // not modification-aware
    }

    private final class PersistentListIterator implements ListIterator {

        private final ListIterator iterator;

        private PersistentListIterator(ListIterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Object next() {
            return this.iterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        @Override
        public Object previous() {
            return this.iterator.previous();
        }

        @Override
        public int nextIndex() {
            return this.iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return this.iterator.previousIndex();
        }

        @Override
        public void remove() {
            this.iterator.remove();
            markDirty();
        }

        @Override
        public void set(Object e) {
            this.iterator.set(e);
            markDirty(); // assume changed
        }

        @Override
        public void add(Object e) {
            this.iterator.add(e);
            markDirty();
        }

    }

}
