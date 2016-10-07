package edu.harvard.hms.dbmi.scidb;

public class SciDBFilterFactory {
	private static SciDBFunction create(String command, String first,
			String second) {
		return new SciDBFunction(first + command + second);
	}

	public static SciDBFunction lessThan(String attribute, Boolean second) {
		return create("<", attribute, Boolean.toString(second));
	}

	public static SciDBFunction lessThan(String attribute, String second) {
		return create("<", attribute, second);
	}

	public static SciDBFunction lessThan(String attribute, char second) {
		return create("<", attribute, Character.toString(second));
	}

	public static SciDBFunction lessThan(String attribute, Double second) {
		return create("<", attribute, Double.toString(second));
	}

	public static SciDBFunction lessThan(String attribute, Float second) {
		return create("<", attribute, Float.toString(second));
	}

	public static SciDBFunction lessThan(String attribute, Integer second) {
		return create("<", attribute, Integer.toString(second));
	}

	public static SciDBFunction lessThanEqual(String attribute, Boolean second) {
		return create("<=", attribute, Boolean.toString(second));
	}

	public static SciDBFunction lessThanEqual(String attribute, String second) {
		return create("<=", attribute, second);
	}

	public static SciDBFunction lessThanEqual(String attribute, char second) {
		return create("<=", attribute, Character.toString(second));
	}

	public static SciDBFunction lessThanEqual(String attribute, Double second) {
		return create("<=", attribute, Double.toString(second));
	}

	public static SciDBFunction lessThanEqual(String attribute, Float second) {
		return create("<=", attribute, Float.toString(second));
	}

	public static SciDBFunction lessThanEqual(String attribute, Integer second) {
		return create("<=", attribute, Integer.toString(second));
	}

	public static SciDBFunction notEqual(String attribute, Boolean second) {
		return create("<>", attribute, Boolean.toString(second));
	}

	public static SciDBFunction notEqual(String attribute, String second) {
		return create("<>", attribute, second);
	}

	public static SciDBFunction notEqual(String attribute, char second) {
		return create("<>", attribute, Character.toString(second));
	}

	public static SciDBFunction notEqual(String attribute, Double second) {
		return create("<>", attribute, Double.toString(second));
	}

	public static SciDBFunction notEqual(String attribute, Float second) {
		return create("<>", attribute, Float.toString(second));
	}

	public static SciDBFunction notEqual(String attribute, Integer second) {
		return create("<>", attribute, Integer.toString(second));
	}

	public static SciDBFunction equal(String attribute, Boolean second) {
		return create("=", attribute, Boolean.toString(second));
	}

	public static SciDBFunction equal(String attribute, char second) {
		return create("=", attribute, Character.toString(second));
	}

	public static SciDBFunction equal(String attribute, String second) {
		return create("=", attribute, second);
	}

	public static SciDBFunction equal(String attribute, Double second) {
		return create("=", attribute, Double.toString(second));
	}

	public static SciDBFunction equal(String attribute, Float second) {
		return create("=", attribute, Float.toString(second));
	}

	public static SciDBFunction equal(String attribute, Integer second) {
		return create("=", attribute, Integer.toString(second));
	}

	public static SciDBFunction greaterThan(String attribute, Boolean second) {
		return create(">", attribute, Boolean.toString(second));
	}

	public static SciDBFunction greaterThan(String attribute, String second) {
		return create(">", attribute, second);
	}

	public static SciDBFunction greaterThan(String attribute, char second) {
		return create(">", attribute, Character.toString(second));
	}

	public static SciDBFunction greaterThan(String attribute, Double second) {
		return create(">", attribute, Double.toString(second));
	}

	public static SciDBFunction greaterThan(String attribute, Float second) {
		return create(">", attribute, Float.toString(second));
	}

	public static SciDBFunction greaterThan(String attribute, Integer second) {
		return create(">", attribute, Integer.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String attribute,
			Boolean second) {
		return create(">=", attribute, Boolean.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String attribute, String second) {
		return create(">=", attribute, second);
	}

	public static SciDBFunction greaterThanEqual(String attribute, char second) {
		return create(">=", attribute, Character.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String attribute, Double second) {
		return create(">=", attribute, Double.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String attribute, Float second) {
		return create(">=", attribute, Float.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String attribute,
			Integer second) {
		return create(">=", attribute, Integer.toString(second));
	}
}
