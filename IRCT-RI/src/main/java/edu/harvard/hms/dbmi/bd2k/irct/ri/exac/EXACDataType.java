/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

import java.util.regex.Pattern;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;

public enum EXACDataType implements DataType {
	VARIANT {
		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getName() {
			return "variant";
		}
	}, GENE {
		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String getName() {
			return "gene";
		}
	}, TRANSCRIPT {
		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String getName() {
			return "transcript";
		}
	}, REGION {
		@Override
		public byte[] toBytes(Object value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String getName() {
			return "region";
		}
	};
	
	@Override
	public Pattern getPattern() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataType typeOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] fromString(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validate(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JsonObject toJson() {
		// TODO Auto-generated method stub
		return null;
	}
}
