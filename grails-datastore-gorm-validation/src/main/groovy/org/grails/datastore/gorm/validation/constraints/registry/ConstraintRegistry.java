package org.grails.datastore.gorm.validation.constraints.registry;

import java.util.List;

import grails.gorm.validation.Constraint;

import org.grails.datastore.gorm.validation.constraints.factory.ConstraintFactory;

/**
 * A registry of Constraint factories
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public interface ConstraintRegistry {

    String DEFAULT_CONSTRAINTS = "grails.gorm.default.constraints";

    /**
     * Adds a constraint factory
     *
     * @param constraintFactory The constraint factory
     */
    <T extends Constraint> void addConstraintFactory(ConstraintFactory<T> constraintFactory);

    /**
     * Adds a constraint for the given class
     *
     * @param constraintClass     The constraint class
     * @param targetPropertyTypes the target types if any
     */
    void addConstraint(Class<? extends Constraint> constraintClass, List<Class> targetPropertyTypes);

    /**
     * Adds a constraint for the given class
     *
     * @param constraintClass The constraint class
     */
    void addConstraint(Class<? extends Constraint> constraintClass);

    /**
     * Finds a named constraint
     *
     * @param name The short name of the constraint
     * @return The constraint
     */
    <T extends Constraint> List<ConstraintFactory<T>> findConstraintFactories(String name);

    /**
     * Finds a constraint by class
     *
     * @param constraintType The class of the constraint
     * @return The constraint
     */
    <T extends Constraint> List<ConstraintFactory<T>> findConstraintFactories(Class<T> constraintType);

}
