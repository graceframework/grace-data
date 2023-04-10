package org.grails.datastore.gorm.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import grails.gorm.services.Service;

/**
 * Annotation added by the {@link Service} transformation to know which class implemented
 * a method
 *
 * @author Graeme Rocher
 * @since 6.1.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Implemented {

    Class by();

}
