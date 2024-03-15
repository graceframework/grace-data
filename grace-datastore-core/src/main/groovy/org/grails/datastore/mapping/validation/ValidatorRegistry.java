package org.grails.datastore.mapping.validation;

import org.springframework.context.MessageSource;
import org.springframework.validation.Validator;

import org.grails.datastore.mapping.model.PersistentEntity;

/**
 * Strategy interface for looking up validators
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public interface ValidatorRegistry {

    /**
     * Looks up a validator for the given entity
     *
     * @param entity The entity
     * @return The validator
     */
    Validator getValidator(PersistentEntity entity);

    /**
     * @return The message source used by this registry
     */
    MessageSource getMessageSource();

}
