/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;

public enum EXACDataType implements DataType {
	VARIANT {
		@Override
		public byte[] toBytes(Object value) {
			if(value == null) {
				return new byte[] {(byte) Character.MIN_VALUE};
			}
			
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if((bytes.length == 1) && (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
		
		@Override
		public String getName() {
			return "variant";
		}
		
		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}
		
		@Override
		public String getDescription() {
			return "Variant";
		}
		
	}, GENE {
		@Override
		public byte[] toBytes(Object value) {
			if(value == null) {
				return new byte[] {(byte) Character.MIN_VALUE};
			}
			
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if((bytes.length == 1) && (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
		@Override
		public String getName() {
			return "gene";
		}
		
		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}
		
		@Override
		public String getDescription() {
			return "Gene";
		}
	}, TRANSCRIPT {
		@Override
		public byte[] toBytes(Object value) {
			if(value == null) {
				return new byte[] {(byte) Character.MIN_VALUE};
			}
			
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if((bytes.length == 1) && (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
		
		@Override
		public String getName() {
			return "transcript";
		}
		
		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}
		
		@Override
		public String getDescription() {
			return "Transcript";
		}
	}, REGION {
		@Override
		public byte[] toBytes(Object value) {
			if(value == null) {
				return new byte[] {(byte) Character.MIN_VALUE};
			}
			
			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if((bytes.length == 1) && (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
		
		@Override
		public String getName() {
			return "region";
		}
		
		@Override
		public Pattern getPattern() {
			return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
		}
		
		@Override
		public String getDescription() {
			return "Region";
		}
	};
	
	@Override
	public DataType typeOf() {
		return PrimitiveDataType.STRING;
	}

	@Override
	public byte[] fromString(String value) {
		return value.getBytes();
	}

	@Override
	public String toString(byte[] bytes) {
		return new String(bytes);
	}

	@Override
	public boolean validate(String value) {
		return getPattern().matcher(value).matches();
	}


	@Override
	public JsonObject toJson() {
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("name", getName());
		build.add("pattern", getPattern().toString());
		build.add("description", getDescription());
		if (typeOf() != null) {
			build.add("typeof", typeOf().getName());
		}
		return build.build();
	}
}
