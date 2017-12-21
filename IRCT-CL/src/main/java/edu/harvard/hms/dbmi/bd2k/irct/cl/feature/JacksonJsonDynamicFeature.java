package edu.harvard.hms.dbmi.bd2k.irct.cl.feature;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * This DynamicFeature registers the JacksonJsonProvider for resource methods which
 * have been updated to use it.
 *
 */
@Provider
public class JacksonJsonDynamicFeature implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		if(resourceInfo.getResourceMethod().isAnnotationPresent(JacksonSerialized.class)){
			context.register(JacksonJsonProvider.class);
		}
	}

}
