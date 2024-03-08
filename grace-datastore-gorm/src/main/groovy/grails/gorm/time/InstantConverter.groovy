package grails.gorm.time

import java.time.Instant

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link java.time.Instant} to and from a long
 *
 * @author James Kleeh
 */
@CompileStatic
trait InstantConverter {

    Long convert(Instant value) {
        value.toEpochMilli()
    }

    Instant convert(Long value) {
        Instant.ofEpochMilli(value)
    }

}
