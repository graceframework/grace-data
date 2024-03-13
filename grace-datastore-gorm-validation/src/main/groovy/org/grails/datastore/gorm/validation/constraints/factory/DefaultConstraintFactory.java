package org.grails.datastore.gorm.validation.constraints.factory;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.context.MessageSource;

import grails.gorm.validation.Constraint;
import grails.gorm.validation.exceptions.ValidationConfigurationException;

import org.grails.datastore.gorm.validation.constraints.NullableConstraint;
import org.grails.datastore.mapping.reflect.ClassUtils;

/**
 * A default factory for creating constraints
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public class DefaultConstraintFactory implements ConstraintFactory {

    private final Class<? extends Constraint> type;
    private final String name;
    private final MessageSource messageSource;
    private final List<Class> targetTypes;

    public DefaultConstraintFactory(Class<? extends Constraint> constraintClass, MessageSource messageSource, List<Class> targetTypes) {
        this.type = constraintClass;
        this.name = getConstraintName(constraintClass);
        this.messageSource = messageSource;
        this.targetTypes = targetTypes == null ? List.of(Object.class) : targetTypes;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean supports(Class targetType) {
        if (NullableConstraint.class.isAssignableFrom(this.type)) {
            return !targetType.isPrimitive();
        }
        else {
            return this.targetTypes.stream().anyMatch(type -> ClassUtils.isAssignableOrConvertibleFrom(type, targetType));
        }
    }

    @Override
    public Constraint build(Class owner, String property, Object constrainingValue) {
        Constraint constraint;
        try {
            Constructor<?> defaultConstructor = type.getConstructor(Class.class, String.class, Object.class, MessageSource.class);
            if (!defaultConstructor.isAccessible()) {
                defaultConstructor.setAccessible(true);
            }
            try {
                constraint = (Constraint) defaultConstructor.newInstance(owner, property, constrainingValue, this.messageSource);
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ValidationConfigurationException("Could not create a new instance of class [" +
                        this.type.getName() + "]!", e);
            }
        }
        catch (NoSuchMethodException e) {
            throw new ValidationConfigurationException("Invalid constraint type [" + this.type + "] must have a 4 argument constructor accepting " +
                    "the Class, propertyName, constrainedObject and MessageSource.", e);
        }
        return constraint;
    }

    private static String getConstraintName(final Class<? extends Constraint> constraintClass) {
        String simpleName = Introspector.decapitalize(constraintClass.getSimpleName());
        if (simpleName.endsWith(Constraint.CONSTRAINT_SUFFIX)) {
            return simpleName.substring(0, simpleName.length() - Constraint.CONSTRAINT_SUFFIX.length());
        }
        return simpleName;
    }
}
