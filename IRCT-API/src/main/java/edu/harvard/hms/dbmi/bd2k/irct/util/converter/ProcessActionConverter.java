/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.util.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.action.ProcessAction;

/**
 * Converts a ProcessAction to a String representation of the class to allow for
 * storage inside a Relational Database
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Converter
public class ProcessActionConverter implements
		AttributeConverter<ProcessAction, String> {

	@Override
	public String convertToDatabaseColumn(ProcessAction joinAction) {
		if (joinAction != null) {
			return joinAction.getClass().getName();
		}
		return null;
	}

	@Override
	public ProcessAction convertToEntityAttribute(String className) {
		if (className != null) {
			ClassLoader cl = ProcessActionConverter.class.getClassLoader();
			try {
				return (ProcessAction) cl.loadClass(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
