/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;

/**
 * A list of relationships that the i2b2 implementation can use
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum EXACOntologyRelationship implements OntologyRelationship {
	PARENT {
		public OntologyRelationship getInverse() {
			return CHILD;
		}
	},
	CHILD {
		public OntologyRelationship getInverse() {
			return PARENT;
		}
	}
}
