package org.grails.datastore.mapping.model;

import org.grails.datastore.mapping.config.Property;

/**
 * Interface for a property mapping which specifies what or where a particular property is mapped to.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public interface PropertyMapping<T extends Property> {

    /**
     * Retrieves the ClassMapping instance of the owning class
     *
     * @return The ClassMapping instance
     */
    @SuppressWarnings("rawtypes")
    ClassMapping getClassMapping();

    /**
     * Returns the mapped form of the property such as a Column, a Key/Value pair, attribute etc.
     * @return The mapped representation
     */
    T getMappedForm();

}
