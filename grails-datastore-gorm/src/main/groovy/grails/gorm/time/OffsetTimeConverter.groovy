package grails.gorm.time

import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneOffset

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link java.time.OffsetTime} to and from a long
 *
 * @author James Kleeh
 */
@CompileStatic
trait OffsetTimeConverter implements TemporalConverter<OffsetTime> {

    @Override
    Long convert(OffsetTime value) {
        value.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime().toNanoOfDay()
    }

    @Override
    OffsetTime convert(Long value) {
        OffsetTime.of(LocalTime.ofNanoOfDay(value), ZoneOffset.UTC).withOffsetSameInstant(systemOffset)
    }

}
