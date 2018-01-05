package edu.harvard.hms.dbmi.bd2k.irct.cl.feature;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Add this annotation to methods which should use the Jackson serialization provider.
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface JacksonSerialized {

}
