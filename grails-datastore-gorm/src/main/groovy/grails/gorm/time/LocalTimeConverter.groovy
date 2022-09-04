package grails.gorm.time

import java.time.LocalTime

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link LocalTime} to and from a long
 *
 * @author James Kleeh
 */
@CompileStatic
trait LocalTimeConverter implements TemporalConverter<LocalTime> {

    @Override
    Long convert(LocalTime value) {
        value.toNanoOfDay()
    }

    @Override
    LocalTime convert(Long value) {
        LocalTime.ofNanoOfDay(value)
    }

}
