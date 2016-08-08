/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import edu.harvard.hms.dbmi.bd2k.irct.event.find.BeforeFind;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class UMLSSynonymBeforeFind implements BeforeFind {
	
	private DataSource dataSource;
	
	@Override
	public void init(Map<String, String> parameters) {
		System.out.println("UMLSSynonymBeforeFind Loaded");
		
		try {
			Context ctx = new InitialContext();
			dataSource = (DataSource) ctx.lookup(parameters.get("jndi"));
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void fire(Resource resource, Entity resourcePath,
			List<FindInformationInterface> findInformation,
			SecureSession session) {
		List<FindInformationInterface> newFindInformation = new ArrayList<FindInformationInterface>();

		for (FindInformationInterface findInfo : findInformation) {
			if (findInfo instanceof FindByPath) {
				String term = findInfo.getValues().get("term");

				Set<String> newTerms = new HashSet<String>();
				
				
//				try {
//					ResultSet testMe = dataSource.getConnection().prepareStatement("").executeQuery();
//				} catch (SQLException e) {
//					 TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				

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
