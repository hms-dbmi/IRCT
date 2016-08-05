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

public class CapitilizationBeforeFind implements BeforeFind {
	private boolean allCaps = false;
	private boolean allLowerCase = false;
	private boolean wordCapitilization = false;

	@Override
	public void init(Map<String, String> parameters) {
		this.allCaps = parameters.get("allCaps").equalsIgnoreCase("true");
		this.allLowerCase = parameters.get("allLowerCase").equalsIgnoreCase(
				"true");
		this.wordCapitilization = parameters.get("wordCapitilization")
				.equalsIgnoreCase("true");
	}

	@Override
	public void fire(Resource resource, Entity resourcePath,
			List<FindInformationInterface> findInformation,
			SecureSession session) {
		List<FindInformationInterface> newFindInformation = new ArrayList<FindInformationInterface>();

		for (FindInformationInterface findInfo : findInformation) {
			if (findInfo instanceof FindByPath
					&& (findInfo.getValues().containsKey("capitilization") && !findInfo
							.getValues().get("capitilization").equals("false"))) {
				String term = findInfo.getValues().get("term");

				Set<String> newTerms = new HashSet<String>();
				if (allCaps) {
					newTerms.add(term.toUpperCase());
				}
				if (allLowerCase) {
					newTerms.add(term.toLowerCase());
				}
				if (wordCapitilization) {
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
