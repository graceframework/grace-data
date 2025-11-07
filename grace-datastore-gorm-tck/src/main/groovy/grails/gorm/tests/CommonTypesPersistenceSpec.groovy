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
package grails.gorm.tests

import grails.gorm.annotation.Entity

/**
 * @author graemerocher
 */
class CommonTypesPersistenceSpec extends GormDatastoreSpec {

    def testPersistBasicTypes() {
        given:
        def now = new Date()
        def cal = new GregorianCalendar()
        def ct = new CommonTypes(
                l: 10L,
                b: 10 as byte,
                s: 10 as short,
                bool: true,
                i: 10,
                url: new URL('http://google.com'),
                date: now,
                c: cal,
                bd: 1.0,
                bi: 10 as BigInteger,
                d: 1.0 as Double,
                f: 1.0 as Float,
                tz: TimeZone.getTimeZone('GMT'),
                loc: Locale.UK,
                cur: Currency.getInstance('USD'),
                ba: 'hello'.bytes
        )

        when:
        ct.save(flush: true)
        ct.discard()
        ct = CommonTypes.get(ct.id)

        then:
        ct
        ct.l == 10L
        ct.b == (10 as byte)
        ct.s == (10 as short)
        ct.bool == true
        ct.i == 10
        ct.url == new URL('http://google.com')
        ct.date.time == now.time
        ct.c == cal
        ct.bd == 1.0
        ct.bi == 10 as BigInteger
        ct.d == (1.0 as Double)
        ct.f == (1.0 as Float)
        ct.tz == TimeZone.getTimeZone('GMT')
        ct.loc == Locale.UK
        ct.cur == Currency.getInstance('USD')
        ct.ba == 'hello'.bytes
    }

}

@Entity
class CommonTypes implements Serializable {

    Long id
    Long version
    Long l
    Byte b
    Short s
    Boolean bool
    Integer i
    URL url
    Date date
    Calendar c
    BigDecimal bd
    BigInteger bi
    Double d
    Float f
    TimeZone tz
    Locale loc
    Currency cur
    byte[] ba

}
