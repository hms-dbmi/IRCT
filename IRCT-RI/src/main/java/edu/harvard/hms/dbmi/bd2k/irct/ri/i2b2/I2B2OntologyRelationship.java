package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;

/**
 * A list of relationships that the i2b2 implementation can use
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum I2B2OntologyRelationship implements OntologyRelationship {
	PARENT {
		public OntologyRelationship getInverse() {
			return CHILD;
		}
	},
	CHILD {
		public OntologyRelationship getInverse() {
			return PARENT;
		}
	},
	SIBLING {
		public OntologyRelationship getInverse() {
			return SIBLING;
		}
	},
	MODIFIER {
		public OntologyRelationship getInverse() {
			return MODIFIER;
		}
	},
	TERM {
		public OntologyRelationship getInverse() {
			return TERM;
		}
	};

}
