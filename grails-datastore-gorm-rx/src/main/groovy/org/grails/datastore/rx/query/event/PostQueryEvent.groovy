/*
 * Copyright 2016-2023 the original author or authors.
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
package org.grails.datastore.rx.query.event

import groovy.transform.CompileStatic
import rx.Observable

import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.query.event.AbstractQueryEvent
import org.grails.datastore.mapping.query.event.QueryEventType

/**
 * Post query event fired by RxGORM
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class PostQueryEvent extends AbstractQueryEvent {

    Observable observable

    PostQueryEvent(Object source, Query query, Observable observable) {
        super(source, query)
        this.observable = observable
    }

    @Override
    QueryEventType getEventType() {
        QueryEventType.PostExecution
    }

}
