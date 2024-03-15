package grails.gorm.tests

import grails.gorm.tests.GormDatastoreSpec
import spock.lang.Issue

class ReadOnlyCriteriaSpec extends GormDatastoreSpec {

    @Issue('GRAILS-11670')
    void 'Test invoking readOnly in a criteria query'() {
        when:
        def results = TestEntity.withCriteria {
            readOnly true
        }

        then:
        results == []
    }
}
