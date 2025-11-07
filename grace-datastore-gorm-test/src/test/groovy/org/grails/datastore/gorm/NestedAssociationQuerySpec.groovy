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

class NestedAssociationQuerySpec extends GormDatastoreSpec {

    void 'Test that a join query can be applied to a nested association'() {
        given: 'A domain multipe with nested asssociations'
        def qText = 'What is your gender?'
        def genderQuestion = createQuestion(qText)
        def femaleAnswer = createAnswer('Female', genderQuestion)
        def userOpinion = createUserOpinion(femaleAnswer)

        when: 'The association is queried'
        def c = UserOpinion.createCriteria()
        def femaleCnt = c.count {
            answers {
                and {
                    question {
                        eq('text', qText)
                    }
                    eq('text', 'Female')
                }
            }
        }

        then: 'The right result is returned'
        femaleCnt == 1
    }

    void 'test join query for 3-level to-one association'() {
        when: 'A domain model 3 levels deep is created'
        new Release(name: 'Grails',
                milestoneCycle: new MilestoneCycle(name: '1.0', department:
                        new Department(name: 'dep A').save()
                ).save()
        ).save(flush: true)

        session.clear()
        Release r = Release.get(1)

        then: 'The domain model is correctly populated'
        Release.count() == 1
        MilestoneCycle.count() == 1
        Department.count() == 1
        r != null
        r.milestoneCycle != null
        r.milestoneCycle.department != null

        when: 'The domain model is queried'

        r = Release.createCriteria().get {
            milestoneCycle {
                department {
                    eq('name', 'dep A')
                }
            }
        }

        then: 'The right result is returned'
        r != null
        r.milestoneCycle != null
        r.milestoneCycle.department != null
    }

    private Question createQuestion(String qText) {
        new Question(text: qText).save(flush: true)
    }

    private Answer createAnswer(String aText, Question q) {
        new Answer(text: aText, question: q).save(flush: true)
    }

    private UserOpinion createUserOpinion(Answer answer) {
        def userOpinion = new UserOpinion()
        userOpinion.answers << answer

        userOpinion.save(flush: true)
    }

    @Override
    List getDomainClasses() {
        [UserOpinion, Answer, Question, Release, Department, MilestoneCycle]
    }

}

@Entity
class UserOpinion {

    Long id
    Set answers = []
    static hasMany = [answers: Answer]

}

@Entity
class Answer {

    Long id
    String text
    Question question

    static constraints = {
        text(blank: false)
    }

}

@Entity
class Question {

    Long id
    String text

    static constraints = {
        text(blank: false)
    }

}

@Entity
class Release {

    Long id
    String name
    MilestoneCycle milestoneCycle

}

@Entity
class MilestoneCycle {

    Long id
    String name
    Department department

}

@Entity
class Department {

    Long id
    String name

}
