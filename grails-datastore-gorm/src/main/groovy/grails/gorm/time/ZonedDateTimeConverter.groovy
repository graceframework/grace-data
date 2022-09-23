package grails.gorm.time

import java.time.Instant
import java.time.ZonedDateTime

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link java.time.ZonedDateTime} to and from a long
 *
 * @author James Kleeh
 */
@CompileStatic
trait ZonedDateTimeConverter implements TemporalConverter<ZonedDateTime> {

    @Override
    Long convert(ZonedDateTime value) {
        value.toInstant().toEpochMilli()
    }

    @Override
    ZonedDateTime convert(Long value) {
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), systemOffset)
    }

}
