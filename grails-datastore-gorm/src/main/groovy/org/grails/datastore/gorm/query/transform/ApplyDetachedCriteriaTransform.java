package org.grails.datastore.gorm.query.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * Used only for testing
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@GroovyASTTransformationClass("org.grails.datastore.gorm.query.transform.DetachedCriteriaASTTransformation")
public @interface ApplyDetachedCriteriaTransform {

}
