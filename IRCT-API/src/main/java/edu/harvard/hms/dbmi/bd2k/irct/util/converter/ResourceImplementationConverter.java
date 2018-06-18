/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.util.RIClassPathTracking;
import org.apache.log4j.Logger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts a Resource Implementation to a String representation of the class to
 * allow for storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class ResourceImplementationConverter implements
		AttributeConverter<ResourceImplementationInterface, String> {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public String convertToDatabaseColumn(
			ResourceImplementationInterface resourceImplementation) {
		if (resourceImplementation != null) {
			return resourceImplementation.getClass().getName().split("\\$")[0];
		}
		return null;
	}

	@Override
	public ResourceImplementationInterface convertToEntityAttribute(
			String className) {
		if (className != null) {
			try {
				return (ResourceImplementationInterface) Class.forName(className)
						.newInstance();
			} catch (ClassNotFoundException ex){
				try {
					String latestClassPath = RIClassPathTracking.getLatestClassPath(className);
					if (latestClassPath != null
                            && !className.equals(latestClassPath))
						return (ResourceImplementationInterface) Class.forName(latestClassPath)
								.newInstance();
					else
						logger.info("Resource Implementation: cannot find the latest classpath mapping");
				} catch (ClassNotFoundException e) {
					logger.error("Resource Implementation: class not found: " + className);
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error("Resource Implementation: Cannot convert to class by class name: " + className);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("Resource Implementation: Cannot convert to class by class name: " + className);
			}
			
		}

		return null;

	}

}
