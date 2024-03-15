package grails.gorm.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.validation.Errors;

/**
 * An interface for a constrained property
 *
 * @author Graeme Rocher
 * @since 6.0
 */
public interface ConstrainedProperty extends Constrained {

    /**
     * @param constraintName The name of the constraint to check
     * @return Returns true if the specified constraint name is being applied to this property
     */
    boolean hasAppliedConstraint(String constraintName);

    /**
     * @return Returns the propertyType.
     */
    Class<?> getPropertyType();

    /**
     * @return The name of the property constrained
     */
    String getPropertyName();

    /**
     * Check whether a given constraint type is supported
     *
     * @param constraintName The name of the constraint
     * @return True if it is supported
     */
    boolean supportsContraint(String constraintName);

    /**
     * Apply a named constraint
     *
     * @param constraintName    The name of the constraint
     * @param constrainingValue The value to constrain by
     */
    void applyConstraint(String constraintName, Object constrainingValue);

    /**
     * @return Returns the appliedConstraints.
     */
    Collection<Constraint> getAppliedConstraints();

    /**
     * Obtain an applied constraint
     *
     * @param name The name
     * @return The constraint or null if it doesn't exist
     */
    Constraint getAppliedConstraint(String name);

    /**
     * @return The owner
     */
    Class getOwner();

    /**
     * Validate this constrainted property against specified property value
     *
     * @param target        The target object to validate
     * @param propertyValue The value of the property to validate
     * @param errors        The Errors instances to report errors to
     */
    void validate(Object target, Object propertyValue, Errors errors);

    ResourceBundle MESSAGE_BUNDLE = ResourceBundle.getBundle("grails.gorm.validation.DefaultErrorMessages");

    String DEFAULT_NULL_MESSAGE_CODE = "default.null.message";

    String DEFAULT_INVALID_MIN_SIZE_MESSAGE_CODE = "default.invalid.min.size.message";

    String DEFAULT_INVALID_MAX_SIZE_MESSAGE_CODE = "default.invalid.max.size.message";

    String DEFAULT_NOT_EQUAL_MESSAGE_CODE = "default.not.equal.message";

    String DEFAULT_INVALID_MIN_MESSAGE_CODE = "default.invalid.min.message";

    String DEFAULT_INVALID_MAX_MESSAGE_CODE = "default.invalid.max.message";

    String DEFAULT_INVALID_SIZE_MESSAGE_CODE = "default.invalid.size.message";

    String DEFAULT_NOT_INLIST_MESSAGE_CODE = "default.not.inlist.message";

    String DEFAULT_INVALID_RANGE_MESSAGE_CODE = "default.invalid.range.message";

    String DEFAULT_INVALID_EMAIL_MESSAGE_CODE = "default.invalid.email.message";

    String DEFAULT_INVALID_CREDIT_CARD_MESSAGE_CODE = "default.invalid.creditCard.message";

    String DEFAULT_INVALID_URL_MESSAGE_CODE = "default.invalid.url.message";

    String DEFAULT_INVALID_VALIDATOR_MESSAGE_CODE = "default.invalid.validator.message";

    String DEFAULT_DOESNT_MATCH_MESSAGE_CODE = "default.doesnt.match.message";

    String DEFAULT_BLANK_MESSAGE_CODE = "default.blank.message";

    String DEFAULT_BLANK_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_BLANK_MESSAGE_CODE);

    String DEFAULT_DOESNT_MATCH_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_DOESNT_MATCH_MESSAGE_CODE);

    String DEFAULT_INVALID_URL_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_URL_MESSAGE_CODE);

    String DEFAULT_INVALID_CREDIT_CARD_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_CREDIT_CARD_MESSAGE_CODE);

    String DEFAULT_INVALID_EMAIL_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_EMAIL_MESSAGE_CODE);

    String DEFAULT_INVALID_RANGE_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_RANGE_MESSAGE_CODE);

    String DEFAULT_NOT_IN_LIST_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_NOT_INLIST_MESSAGE_CODE);

    String DEFAULT_INVALID_SIZE_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_SIZE_MESSAGE_CODE);

    String DEFAULT_INVALID_MAX_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_MAX_MESSAGE_CODE);

    String DEFAULT_INVALID_MIN_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_MIN_MESSAGE_CODE);

    String DEFAULT_NOT_EQUAL_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_NOT_EQUAL_MESSAGE_CODE);

    String DEFAULT_INVALID_MAX_SIZE_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_MAX_SIZE_MESSAGE_CODE);

    String DEFAULT_INVALID_MIN_SIZE_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_MIN_SIZE_MESSAGE_CODE);

    String DEFAULT_NULL_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_NULL_MESSAGE_CODE);

    String DEFAULT_INVALID_VALIDATOR_MESSAGE = MESSAGE_BUNDLE.getString(DEFAULT_INVALID_VALIDATOR_MESSAGE_CODE);

    Map<String, String> DEFAULT_MESSAGES = new HashMap<String, String>() {
    };

    String CREDIT_CARD_CONSTRAINT = "creditCard";

    String EMAIL_CONSTRAINT = "email";

    String BLANK_CONSTRAINT = "blank";

    String RANGE_CONSTRAINT = "range";

    String IN_LIST_CONSTRAINT = "inList";

    String URL_CONSTRAINT = "url";

    String MATCHES_CONSTRAINT = "matches";

    String SIZE_CONSTRAINT = "size";

    String MIN_CONSTRAINT = "min";

    String MAX_CONSTRAINT = "max";

    String MAX_SIZE_CONSTRAINT = "maxSize";

    String MIN_SIZE_CONSTRAINT = "minSize";

    String SCALE_CONSTRAINT = "scale";

    String NOT_EQUAL_CONSTRAINT = "notEqual";

    String NULLABLE_CONSTRAINT = "nullable";

    String VALIDATOR_CONSTRAINT = "validator";

    String INVALID_SUFFIX = ".invalid";

    String EXCEEDED_SUFFIX = ".exceeded";

    String NOTMET_SUFFIX = ".notmet";

    String NOT_PREFIX = "not.";

    String TOOBIG_SUFFIX = ".toobig";

    String TOOLONG_SUFFIX = ".toolong";

    String TOOSMALL_SUFFIX = ".toosmall";

    String TOOSHORT_SUFFIX = ".tooshort";

}
