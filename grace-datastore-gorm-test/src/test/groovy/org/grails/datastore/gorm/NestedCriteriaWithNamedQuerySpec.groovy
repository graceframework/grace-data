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
package org.grails.datastore.gorm

import spock.lang.Issue

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

/**
 * @author Graeme Rocher
 */
class NestedCriteriaWithNamedQuerySpec extends GormDatastoreSpec {

    @Issue('GRAILS-9497')
    void 'Test that nested criteria work with named queries'() {
        given: 'A domain model with 3 levels of association'
        Seller seller = new Seller()
        Ticket ticket = new Ticket()
        seller.addToTickets(ticket)
        seller.save(flush: true)
        Purchase purchase = new Purchase()
        ticket.addToPurchases(purchase)
        purchase.save(flush: true)
        ticket.save(flush: true)
        session.clear()

        when: 'The data is queried'
        def results = Purchase.myNamedQuery(seller).list()

        then: 'Results are returned'
        results != null
    }

    @Override
    List getDomainClasses() {
        [Seller, Ticket, Purchase]
    }

}

@Entity
class Seller {

    Long id
    Set tickets
    static hasMany = [tickets: Ticket]

}

@Entity
class Ticket {

    Long id
    Seller seller
    Set purchases
    static belongsTo = [seller: Seller]
    static hasMany = [purchases: Purchase]

}

@Entity
class Purchase {

    Long id
    Ticket ticket
    static belongsTo = [ticket: Ticket]
    static namedQueries = {
        myNamedQuery { Seller seller ->
            ticket {
                eq('seller', seller)
            }
        }
    }

}
