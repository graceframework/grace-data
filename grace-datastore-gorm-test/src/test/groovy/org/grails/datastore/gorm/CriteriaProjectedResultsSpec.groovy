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

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class CriteriaProjectedResultsSpec extends GormDatastoreSpec {

    void 'Test single projection'() {
        when: 'A instanced is saved'
        Check c = new Check(amount: 57).save()

        then: 'The count is 1'
        Check.count() == 1

        when: 'A sum projection is executed'
        def total = Check.withCriteria {
            projections {
                sum 'amount'
            }
        }

        then: 'A list is returned containing the sum'
        total == [57]
    }

    void 'Test multiple projections'() {
        given: 'A domain instance'
        Check c = new Check(amount: 57).save()

        when: 'A projection is used with two projected results'
        def model = Check.withCriteria {
            projections {
                rowCount()
                sum 'amount'
            }
        }

        then: 'A list containing another list is returned'
        model == [[1, 57]]
    }

    void 'Test single projection multiple rows'() {
        given: 'multiple records'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()

        when: 'A query is executed'
        def model = Check.withCriteria {
            projections {
                property('amount')
            }
            order('amount', 'asc')
        }

        then: 'A list of lists is returned'
        model == [29, 57, 83]
    }

    void 'Test multiple projections with multiple rows'() {
        given: 'Multiple recordes'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()

        when: 'A query with multiple projections is executed'
        def model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
        }

        then: 'A list of lists is returned'
        model == [['twenty-nine', 29], ['fifty-seven', 57], ['eighty-three', 83]]
    }

    void 'Test single order'() {
        given: 'A domain model'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()
        new Check(amount: 83, descr: 'seventy').save()

        when: 'The model is queried'
        def model = Check.withCriteria {
            order('amount', 'asc')
        }

        then: 'The order is correct'
        assert model
        assert model.size() == 4
        assert model[0].amount == 29
        assert model[1].amount == 57
        assert model[2].amount == 83
        assert model[3].amount == 83
    }

    void 'Test multiple orders'() {
        given: 'A domain model'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()
        new Check(amount: 83, descr: 'seventy').save()

        when: 'The domain model is queried with multiple order definitions'
        def model = Check.withCriteria {
            order('amount', 'asc')
            order('descr', 'desc')
        }

        then: 'The order is correct'
        assert model
        assert model.size() == 4
        assert model[0].amount == 29
        assert model[1].amount == 57
        assert model[2].amount == 83
        assert model[2].descr == 'seventy'
        assert model[3].amount == 83
        assert model[3].descr == 'eighty-three'
    }

    void 'Test multiple orders with projections'() {
        given: 'A domain model'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()
        new Check(amount: 83, descr: 'seventy').save()

        when: 'A query is executed with 2 projectsions and 2 orders in different directions'
        def model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
            order('descr', 'desc')
        }
        then: 'The results are correct'
        model == [['twenty-nine', 29], ['fifty-seven', 57], ['seventy', 83], ['eighty-three', 83]]

        when: 'A query is executed with 2 projections and 2 orders in the same direction'
        model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
            order('descr', 'asc')
        }

        then: 'The results are correct'
        model == [['twenty-nine', 29], ['fifty-seven', 57], ['eighty-three', 83], ['seventy', 83]]

        when: 'A query is executed with 2 projects and 2 orders on different properties'
        model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
            order('id', 'desc')
        }

        then: 'The results are correct'
        model == [['twenty-nine', 29], ['fifty-seven', 57], ['seventy', 83], ['eighty-three', 83]]
    }

    void testOrderAndOffset() {
        given: 'A domain model'
        new Check(amount: 57, descr: 'fifty-seven').save()
        new Check(amount: 83, descr: 'eighty-three').save()
        new Check(amount: 29, descr: 'twenty-nine').save()
        new Check(amount: 83, descr: 'seventy').save()

        expect: 'All items are present'
        Check.count() == 4

        when: 'A query is executed with 2 orderings in different directions, offset, and limit'
        def model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
            order('descr', 'desc')
            firstResult(1)
            maxResults(2)
        }

        then: 'The results are correct'
        model == [['fifty-seven', 57], ['seventy', 83]]

        when: 'A query is executed with 2 orderings in same direction, offset, and limit'
        model = Check.withCriteria {
            projections {
                property('descr')
                property('amount')
            }
            order('amount', 'asc')
            order('descr', 'asc')
            firstResult(2)
            maxResults(2)
        }

        then: 'The results are correct'
        model == [['eighty-three', 83], ['seventy', 83]]
    }

    @Override
    List getDomainClasses() {
        [Check]
    }

}

@Entity
class Check {

    Long id
    BigDecimal amount
    String descr

    static constraints = {
        amount nullable: false
        descr nullable: true
    }

}
