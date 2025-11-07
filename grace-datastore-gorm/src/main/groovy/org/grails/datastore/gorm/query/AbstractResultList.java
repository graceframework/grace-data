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
package org.grails.datastore.gorm.query;

import java.io.Closeable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An abstract result list for initializing objects lazily from a cursor
 *
 * @author Graeme Rocher
 * @since 5.0
 */
public abstract class AbstractResultList extends AbstractList implements Closeable {

    protected int offset = 0;

    protected final List initializedObjects;

    private int internalIndex;

    private Integer size;

    protected boolean initialized = false;

    protected Iterator<Object> cursor;

    public AbstractResultList(int offset, Iterator<Object> cursor) {
        this(offset, -1, cursor);
    }

    public AbstractResultList(int offset, Integer size, Iterator<Object> cursor) {
        this.offset = offset;
        boolean hasSize = size != null && size > -1;
        if (hasSize) {
            this.size = size;
        }
        this.cursor = cursor;
        this.initialized = !cursor.hasNext();
        this.initializedObjects = hasSize ? new ArrayList(size) : new ArrayList();
    }

    public Iterator<Object> getCursor() {
        return this.cursor;
    }

    protected void initializeFully() {
        if (this.initialized) {
            return;
        }

        while (this.cursor.hasNext()) {
            convertObject();
        }
        this.initialized = true;
    }


    @Override
    public boolean isEmpty() {
        if (this.initialized) {
            return this.initializedObjects.isEmpty();
        }
        else {
            return this.initializedObjects.isEmpty() && !this.cursor.hasNext();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(int index) {
        final List initializedObjects = this.initializedObjects;
        final int initializedSize = initializedObjects.size();
        if (initializedSize > index) {
            return initializedObjects.get(index);
        }
        else if (!this.initialized) {
            while (this.cursor.hasNext()) {
                Object o = convertObject();
                if (index == this.internalIndex) {
                    return o;
                }
                else if (index < initializedSize) {
                    return initializedObjects.get(index);
                }

            }
            this.initialized = true;
        }
        return initializedObjects.get(index);
    }

    protected Object convertObject() {
        final Object next = convertObject(nextDecoded());
        if (!this.cursor.hasNext()) {
            this.initialized = true;
        }
        this.initializedObjects.add(next);
        this.internalIndex = this.initializedObjects.size();
        return next;
    }

    protected Object convertObject(Object o) {
        return o;
    }

    protected abstract Object nextDecoded();

    @Override
    public Object set(int index, Object o) {
        Object previous = get(index);
        this.initializedObjects.set(index, o);
        return previous;
    }

    @Override
    public void add(int index, Object element) {
        initializeFully();
        this.initializedObjects.add(index, element);
    }

    @Override
    public Object remove(int index) {
        initializeFully();
        return this.initializedObjects.remove(index);
    }

    @Override
    public ListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator listIterator(int index) {
        initializeFully();
        return this.initializedObjects.listIterator(index);
    }

    /**
     * Override to transform elements if necessary during iteration.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator iterator() {
        if (this.initialized || !this.initializedObjects.isEmpty()) {
            if (!this.initialized) {
                initializeFully();
            }
            return this.initializedObjects.iterator();
        }

        return new Iterator() {
            int iteratorIndex = 0;

            Object current;

            public boolean hasNext() {
                if (this.iteratorIndex < AbstractResultList.this.internalIndex) {
                    return true;
                }
                else if (!AbstractResultList.this.initialized) {

                    boolean hasMore = AbstractResultList.this.cursor.hasNext();
                    if (!hasMore) {
                        AbstractResultList.this.initialized = true;
                    }
                    return hasMore;
                }
                return false;
            }

            @SuppressWarnings("unchecked")
            public Object next() {
                if (this.iteratorIndex < AbstractResultList.this.internalIndex) {
                    this.current = AbstractResultList.this.initializedObjects.get(this.iteratorIndex);
                }
                else {
                    this.current = convertObject();
                }
                try {
                    return this.current;
                }
                finally {
                    this.iteratorIndex++;
                }
            }

            public void remove() {
                if (this.current != null) {
                    AbstractResultList.this.initializedObjects.remove(this.current);
                }
            }
        };
    }

    @Override
    public int size() {
        if (this.initialized) {
            return this.initializedObjects.size();
        }
        else if (this.size == null) {
            initializeFully();
            this.size = this.initializedObjects.size();
        }
        return this.size;
    }

}
