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
package org.grails.datastore.mapping.engine.event;

import org.grails.datastore.mapping.core.Datastore;
import org.grails.datastore.mapping.engine.EntityAccess;
import org.grails.datastore.mapping.model.PersistentEntity;

/**
 * @author Burt Beckwith
 */
public class PostUpdateEvent extends AbstractPersistenceEvent {

    private static final long serialVersionUID = 1;

    public PostUpdateEvent(final Datastore source, final PersistentEntity entity,
            final EntityAccess entityAccess) {
        super(source, entity, entityAccess);
    }

    public PostUpdateEvent(final Datastore source, final Object entity) {
        super(source, entity);
    }

    public PostUpdateEvent(Object source, PersistentEntity entity, EntityAccess entityAccess) {
        super(source, entity, entityAccess);
    }

    @Override
    public EventType getEventType() {
        return EventType.PostUpdate;
    }

}
