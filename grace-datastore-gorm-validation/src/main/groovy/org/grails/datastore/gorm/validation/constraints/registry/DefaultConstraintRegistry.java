package org.grails.datastore.gorm.validation.constraints.registry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;

import grails.gorm.validation.Constraint;

import org.grails.datastore.gorm.validation.constraints.BlankConstraint;
import org.grails.datastore.gorm.validation.constraints.CreditCardConstraint;
import org.grails.datastore.gorm.validation.constraints.EmailConstraint;
import org.grails.datastore.gorm.validation.constraints.InListConstraint;
import org.grails.datastore.gorm.validation.constraints.MatchesConstraint;
import org.grails.datastore.gorm.validation.constraints.MaxConstraint;
import org.grails.datastore.gorm.validation.constraints.MaxSizeConstraint;
import org.grails.datastore.gorm.validation.constraints.MinConstraint;
import org.grails.datastore.gorm.validation.constraints.MinSizeConstraint;
import org.grails.datastore.gorm.validation.constraints.NotEqualConstraint;
import org.grails.datastore.gorm.validation.constraints.NullableConstraint;
import org.grails.datastore.gorm.validation.constraints.RangeConstraint;
import org.grails.datastore.gorm.validation.constraints.ScaleConstraint;
import org.grails.datastore.gorm.validation.constraints.SizeConstraint;
import org.grails.datastore.gorm.validation.constraints.UrlConstraint;
import org.grails.datastore.gorm.validation.constraints.ValidatorConstraint;
import org.grails.datastore.gorm.validation.constraints.factory.ConstraintFactory;
import org.grails.datastore.gorm.validation.constraints.factory.DefaultConstraintFactory;

/**
 * Default implementation of the {@link ConstraintRegistry} interface. Provides lookup and registration of constraints
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public class DefaultConstraintRegistry implements ConstraintRegistry {

    protected final Map<String, List<ConstraintFactory<? extends Constraint>>> factoriesByName = new ConcurrentHashMap<>();
    protected final Map<Class<? extends Constraint>, List<ConstraintFactory<? extends Constraint>>> factoriesByType = new ConcurrentHashMap<>();

    protected final MessageSource messageSource;

    public DefaultConstraintRegistry(MessageSource messageSource) {
        this.messageSource = messageSource;

        List<Class> charSequenceType = List.of(CharSequence.class);
        List<Class> comparableNumberType = List.of(Comparable.class, Number.class);
        List<Class> charSequenceIterableType = List.of(CharSequence.class, Iterable.class);

        addConstraint(BlankConstraint.class, charSequenceType);
        addConstraint(CreditCardConstraint.class, charSequenceType);
        addConstraint(EmailConstraint.class, charSequenceType);
        addConstraint(InListConstraint.class);
        addConstraint(MatchesConstraint.class, charSequenceType);
        addConstraint(MaxConstraint.class, comparableNumberType);
        addConstraint(MaxSizeConstraint.class, charSequenceIterableType);
        addConstraint(MinConstraint.class, comparableNumberType);
        addConstraint(MinSizeConstraint.class, charSequenceIterableType);
        addConstraint(NotEqualConstraint.class);
        addConstraint(NullableConstraint.class);
        addConstraint(RangeConstraint.class, comparableNumberType);
        addConstraint(ScaleConstraint.class, List.of(BigDecimal.class, Double.class, Float.class));
        addConstraint(SizeConstraint.class, charSequenceIterableType);
        addConstraint(UrlConstraint.class, charSequenceType);
        addConstraint(ValidatorConstraint.class);
    }

    public void setConstraintFactories(ConstraintFactory... constraintFactories) {
        Arrays.stream(constraintFactories).forEach(this::addConstraintFactory);
    }

    @Override
    public <T extends Constraint> void addConstraintFactory(ConstraintFactory<T> factory) {
        this.factoriesByType.computeIfAbsent(factory.getType(), k -> new ArrayList<>()).add(factory);
        this.factoriesByName.computeIfAbsent(factory.getName(), k -> new ArrayList<>()).add(factory);
    }

    @Override
    public void addConstraint(Class<? extends Constraint> constraintClass) {
        addConstraintFactory(new DefaultConstraintFactory(constraintClass, this.messageSource, List.of(Object.class)));
    }

    @Override
    public void addConstraint(Class<? extends Constraint> constraintClass, List<Class> targetPropertyTypes) {
        addConstraintFactory(new DefaultConstraintFactory(constraintClass, this.messageSource, targetPropertyTypes));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ConstraintFactory<? extends Constraint>> findConstraintFactories(String name) {
        return this.factoriesByName.getOrDefault(name, new ArrayList<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ConstraintFactory<? extends Constraint>> findConstraintFactories(Class constraintType) {
        return this.factoriesByType.getOrDefault(constraintType, new ArrayList<>());
    }

}
