/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import edu.harvard.hms.dbmi.bd2k.irct.event.find.BeforeFind;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * The Capitilization Before Find Event Listener creates a set of find terms that conform to different capitalization strategies.
 * 
 * Configurable Database Parameters
 * allcaps (True|False) To create an all caps term (Default false)
 * allLowerCase (True|False) To create an all lower case term (Default false)
 * wordCapitalization (True|False) To capitalize the first word as defined by the first character after a space or first character (Default false)
 * 
 * Configurable Additional Parameters
 * capitalization (True|False) To capitalize the search term (Default true)
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class CapitalizationBeforeFind implements BeforeFind {
	private boolean allCaps = false;
	private boolean allLowerCase = false;
	private boolean wordCapitalization = false;

	@Override
	public void init(Map<String, String> parameters) {
		this.allCaps = parameters.get("allCaps").equalsIgnoreCase("true");
		this.allLowerCase = parameters.get("allLowerCase").equalsIgnoreCase(
				"true");
		this.wordCapitalization = parameters.get("wordCapitalization")
				.equalsIgnoreCase("true");
	}

	@Override
	public void fire(Resource resource, Entity resourcePath,
			List<FindInformationInterface> findInformation,
			SecureSession session) {
		List<FindInformationInterface> newFindInformation = new ArrayList<FindInformationInterface>();

		for (FindInformationInterface findInfo : findInformation) {
			if (findInfo instanceof FindByPath
					&& (findInfo.getValues().containsKey("capitalization") && !findInfo
							.getValues().get("capitalization").equalsIgnoreCase("false"))) {
				String term = findInfo.getValues().get("term");

				Set<String> newTerms = new HashSet<String>();
				if (allCaps) {
					newTerms.add(term.toUpperCase());
				}
				if (allLowerCase) {
					newTerms.add(term.toLowerCase());
				}
				if (wordCapitalization) {
					newTerms.add(WordUtils.capitalize(term));
				}

				for (String newTerm : newTerms) {
					if (!newTerm.equals(term)) {
						FindByPath allCapFind = findInfo.copy();
						allCapFind.setValue("term", newTerm);
						newFindInformation.add(allCapFind);
					}
				}
			}
		}

		findInformation.addAll(newFindInformation);
	}

}
