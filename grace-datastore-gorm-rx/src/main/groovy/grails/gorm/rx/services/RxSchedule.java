package grails.gorm.rx.services;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import rx.schedulers.Schedulers;

import org.grails.datastore.gorm.transform.GormASTTransformationClass;

/**
 * A transformation that transforms the body of a method to return an Observable that runs on the IO Scheduler
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
@Inherited
@Documented
@GroovyASTTransformationClass("org.grails.datastore.gorm.transform.OrderedGormTransformation")
@GormASTTransformationClass("org.grails.gorm.rx.transform.RxScheduleIOTransformation")
public @interface RxSchedule {

    /**
     * @return A closure that returns the scheduler to run on. Default is {@link Schedulers#io()}
     */
    Class scheduler() default Object.class;

    /**
     * @return Whether the underlying query method returns a single result of an iterable
     */
    boolean singleResult() default false;

}
