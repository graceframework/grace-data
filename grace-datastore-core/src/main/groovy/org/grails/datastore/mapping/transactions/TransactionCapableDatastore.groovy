/* Copyright (C) 2017 original authors
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
package org.grails.datastore.mapping.transactions

import org.springframework.transaction.PlatformTransactionManager

import org.grails.datastore.mapping.core.Datastore

/**
 * An interface for {@link Datastore} implementations that support transaction management
 *
 * @author Graeme Rocher
 * @since 6.1
 */
interface TransactionCapableDatastore extends Datastore {

    /**
     * @return The transaction manager for this datastore
     */
    PlatformTransactionManager getTransactionManager()

}
