package grails.gorm.time

import java.time.Period

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link java.time.Period} to and from a String
 *
 * @author James Kleeh
 */
@CompileStatic
trait PeriodConverter {

    String convert(Period value) {
        value.toString()
    }

    Period convert(String value) {
        Period.parse(value)
    }

}
