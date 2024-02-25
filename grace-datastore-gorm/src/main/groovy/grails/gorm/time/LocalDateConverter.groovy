package grails.gorm.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

import groovy.transform.CompileStatic

/**
 * A trait to convert a {@link LocalDate} to and from a long
 *
 * @author James Kleeh
 */
@CompileStatic
trait LocalDateConverter extends TemporalConverter<LocalDate> {

    @Override
    Long convert(LocalDate value) {
        LocalDateTime localDateTime = LocalDateTime.of(value, LocalTime.MIN)
        localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @Override
    LocalDate convert(Long value) {
        Instant instant = Instant.ofEpochMilli(value)
        LocalDateTime.ofInstant(instant, ZoneId.of('UTC')).toLocalDate()
    }

}
