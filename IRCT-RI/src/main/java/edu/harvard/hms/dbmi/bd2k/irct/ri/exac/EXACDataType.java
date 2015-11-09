/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

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
	};
}
