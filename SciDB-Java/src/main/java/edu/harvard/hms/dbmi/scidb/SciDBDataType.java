/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

/**
 * An ENUM representation of the different data types that SciDB supports
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public enum SciDBDataType {
	BOOL {

		@Override
		public Object getDefaultValue() {
			return false;
		}

		@Override
		public String getSciDBName() {
			return "bool";
		}

	},
	CHAR {
		@Override
		public Object getDefaultValue() {
			return '\0';
		}

		@Override
		public String getSciDBName() {
			return "char";
		}
	},
	DATETIME {
		@Override
		public Object getDefaultValue() {
			return "1970-01-01 00:00:00";
		}

		@Override
		public String getSciDBName() {
			return "datetime";
		}
	},
	DATETIMEZ {
		@Override
		public Object getDefaultValue() {
			return "1970-01-01 00:00:00 -00:00";
		}

		@Override
		public String getSciDBName() {
			return "datetimez";
		}
	},
	DOUBLE {
		@Override
		public Object getDefaultValue() {
			return (double) 0;
		}

		@Override
		public String getSciDBName() {
			return "double";
		}
	},
	FLOAT {
		@Override
		public Object getDefaultValue() {
			return (float) 0;
		}

		@Override
		public String getSciDBName() {
			return "float";
		}
	},
	INT8 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "int8";
		}
	},
	INT16 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "int16";
		}
	},
	INT32 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "int32";
		}
	},
	INT64 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "int64";
		}
	},
	STRING {
		@Override
		public Object getDefaultValue() {
			return "";
		}

		@Override
		public String getSciDBName() {
			return "string";
		}
	},
	UINT8 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "uint8";
		}
	},
	UINT16 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "uint16";
		}
	},
	UINT32 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "uint32";
		}
	},
	UINT64 {
		@Override
		public Object getDefaultValue() {
			return (int) 0;
		}

		@Override
		public String getSciDBName() {
			return "uint64";
		}
	},
	DATE {
		@Override
		public Object getDefaultValue() {
			return "1970-01-01";
		}

		@Override
		public String getSciDBName() {
			return "date";
		}
	},
	TIME{
		@Override
		public Object getDefaultValue() {
			return "00:00:00";
		}

		@Override
		public String getSciDBName() {
			return "time";
		}
	},
	TIMESTAMP{
		@Override
		public Object getDefaultValue() {
			return "1970-01-01 00:00:00";
		}

		@Override
		public String getSciDBName() {
			return "timestamp";
		}
	},
	INTERVAL{
		@Override
		public Object getDefaultValue() {
			return "00:00:00.000000";
		}

		@Override
		public String getSciDBName() {
			return "interval";
		}
	};
	public abstract Object getDefaultValue();

	public abstract String getSciDBName();
}
