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
package org.grails.datastore.mapping.dirty.checking

import groovy.transform.CompileStatic

/**
 * Wrapper list to dirty check a list and mark a parent as dirty
 *
 * @author Graeme Rocher
 * @since 4.1
 */
@CompileStatic
class DirtyCheckingList extends DirtyCheckingCollection implements List {

    @Delegate
    List target

    DirtyCheckingList(List target, DirtyCheckable parent, String property) {
        super(target, parent, property)
        this.target = target
    }

    @Override
    boolean addAll(int index, Collection c) {
        this.parent.markDirty(this.property)
        this.target.addAll(index, c)
    }

    @Override
    Object set(int index, Object element) {
        this.parent.markDirty(this.property)
        this.target.set(index, element)
    }

    @Override
    void add(int index, Object element) {
        this.parent.markDirty(this.property)
        this.target.add(index, element)
    }

    @Override
    Object remove(int index) {
        this.parent.markDirty(this.property)
        this.target.remove((int) index)
    }

}
