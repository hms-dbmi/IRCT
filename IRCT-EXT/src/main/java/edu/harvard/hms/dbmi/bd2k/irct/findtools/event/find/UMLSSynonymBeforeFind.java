/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

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

	private Connection con;
	private String storedSynByPTProcedure;
	private String storedSynByPTSABProcedure;
	private String newTermColumn;

	@Override
	public void init(Map<String, String> parameters) {
		try {

			Class.forName(parameters.get("jdbcDriverName"));

			con = DriverManager.getConnection(parameters.get("connectionString"), parameters.get("username"), parameters.get("password"));

			storedSynByPTProcedure = parameters.get("storedSynByPTProcedure");
			storedSynByPTSABProcedure = parameters.get("storedSynByPTSABProcedure");
			newTermColumn = parameters.get("newTermColumn");

		} catch (ClassNotFoundException | SQLException e) {
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
			if (findInfo instanceof FindByPath
					&& (findInfo.getValues().containsKey("umls") && findInfo
							.getValues().get("umls").equalsIgnoreCase("true"))) {
				String term = findInfo.getValues().get("term");

				Set<String> newTerms = new HashSet<String>();

				try {

					CallableStatement cs = null;
					ResultSet umlsSynonyms = null;
					if (findInfo.getValues().get("ontologies") != null) {
						String[] ontologies = findInfo.getValues()
								.get("ontologies").split(",");
						cs = con.prepareCall("{call "
								+ storedSynByPTSABProcedure + "}");

						cs.setString(2, term.toUpperCase());
						ArrayDescriptor des = ArrayDescriptor.createDescriptor(
								"UMLS.SAB_LIST", con);
						cs.setArray(1, new ARRAY(des, con, ontologies));
						cs.registerOutParameter(3, OracleTypes.CURSOR);
						cs.execute();
						umlsSynonyms = (ResultSet) cs.getObject(3);
					} else {
						cs = con.prepareCall("{call " + storedSynByPTProcedure
								+ "}");
						cs.setString(1, term.toUpperCase());
						cs.registerOutParameter(2, OracleTypes.CURSOR);
						cs.execute();
						umlsSynonyms = (ResultSet) cs.getObject(2);
					}

					while (umlsSynonyms.next()) {
						String tempTerm = umlsSynonyms.getString(newTermColumn);
						newTerms.add(tempTerm);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				for (String newTerm : newTerms) {
					if (!newTerm.equals(term)) {
						FindByPath synonymTerm = findInfo.copy();
						synonymTerm.setValue("term", newTerm);
						newFindInformation.add(synonymTerm);
					}
				}
			}
		}

		findInformation.addAll(newFindInformation);
	}

}
