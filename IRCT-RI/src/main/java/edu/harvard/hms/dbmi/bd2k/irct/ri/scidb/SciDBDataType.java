package edu.harvard.hms.dbmi.bd2k.irct.ri.scidb;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;

public enum SciDBDataType implements DataType {
	ARRAY {
		@Override
		public String getName() {
			return "array";
		}

		@Override
		public String getDescription() {
			return "Array";
		}

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
	},
	ATTRIBUTE {
		@Override
		public String getName() {
			return "attribute";
		}

		@Override
		public String getDescription() {
			return "Attribute";
		}

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
	},
	DIMENSION {
		@Override
		public String getName() {
			return "dimension";
		}

		@Override
		public String getDescription() {
			return "DIMENSION";
		}

		@Override
		public byte[] toBytes(Object value) {
			if (value == null) {
				return new byte[] { (byte) Character.MIN_VALUE };
			}

			String data = ((String) value).trim();
			return (data).getBytes(StandardCharsets.UTF_16);
		}

		@Override
		public Object fromBytes(byte[] bytes) {
			if ((bytes.length == 1)
					&& (bytes[0] == Character.reverseBytes(Character.MIN_VALUE))) {
				return null;
			}
			return new String(bytes, StandardCharsets.UTF_16).trim();
		}
	};

	@Override
	public Pattern getPattern() {
		return Pattern.compile("^.*$", Pattern.CASE_INSENSITIVE);
	}

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
