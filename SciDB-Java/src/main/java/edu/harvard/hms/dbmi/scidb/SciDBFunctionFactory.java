/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

public class SciDBFunctionFactory {

	public static SciDBFunction remainder(double first, double second) {
		return create("%", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction remainder(int first, int second) {
		return create("%", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction multiply(double first, double second) {
		return create("*", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction multiply(float first, float second) {
		return create("*", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction multiply(int first, int second) {
		return create("*", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction add(String first, String second) {
		return create("+", first, second);
	}

	public static SciDBFunction add(String first, int second) {
		return create("+", first, Integer.toString(second));
	}

	public static SciDBFunction add(double first, double second) {
		return create("+", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction add(float first, float second) {
		return create("+", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction add(int first, int second) {
		return create("+", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction subtract(String first) {
		return create("-", first);
	}

	public static SciDBFunction subtract(String first, String second) {
		return create("-", first, second);
	}

	public static SciDBFunction subtract(String first, int second) {
		return create("-", first, Integer.toString(second));
	}

	public static SciDBFunction subtract(double first, double second) {
		return create("-", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction subtract(double first) {
		return create("-", Double.toString(first));
	}

	public static SciDBFunction subtract(float first, float second) {
		return create("-", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction subtract(float first) {
		return create("-", Float.toString(first));
	}

	public static SciDBFunction subtract(int first, int second) {
		return create("-", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction subtract(int first) {
		return create("-", Integer.toString(first));
	}

	public static SciDBFunction divide(double first, double second) {
		return create("-", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction divide(float first, float second) {
		return create("-", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction divide(int first, int second) {
		return create("-", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction lessThan(boolean first, boolean second) {
		return create("<", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction lessThan(char first, char second) {
		return create("<", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction lessThan(String first, String second) {
		return create("<", first, second);
	}

	public static SciDBFunction lessThan(double first, double second) {
		return create("<", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction lessThan(int first, int second) {
		return create("<", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction lessThan(float first, float second) {
		return create("<", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction lessThanEqual(boolean first, boolean second) {
		return create("<=", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction lessThanEqual(char first, char second) {
		return create("<=", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction lessThanEqual(String first, String second) {
		return create("<=", first, second);
	}

	public static SciDBFunction lessThanEqual(double first, double second) {
		return create("<=", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction lessThanEqual(int first, int second) {
		return create("<=", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction lessThanEqual(float first, float second) {
		return create("<=", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction notEqual(boolean first, boolean second) {
		return create("<>", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction notEqual(char first, char second) {
		return create("<>", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction notEqual(String first, String second) {
		return create("<>", first, second);
	}

	public static SciDBFunction notEqual(double first, double second) {
		return create("<>", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction notEqual(int first, int second) {
		return create("<>", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction notEqual(float first, float second) {
		return create("<>", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction equal(boolean first, boolean second) {
		return create("=", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction equal(char first, char second) {
		return create("=", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction equal(String first, String second) {
		return create("=", first, second);
	}

	public static SciDBFunction equal(double first, double second) {
		return create("=", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction equal(int first, int second) {
		return create("=", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction equal(float first, float second) {
		return create("=", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction greaterThan(boolean first, boolean second) {
		return create(">", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction greaterThan(char first, char second) {
		return create(">", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction greaterThan(String first, String second) {
		return create(">", first, second);
	}

	public static SciDBFunction greaterThan(double first, double second) {
		return create(">", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction greaterThan(int first, int second) {
		return create(">", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction greaterThan(float first, float second) {
		return create(">", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction greaterThanEqual(boolean first, boolean second) {
		return create(">=", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction greaterThanEqual(char first, char second) {
		return create(">=", Character.toString(first),
				Character.toString(second));
	}

	public static SciDBFunction greaterThanEqual(String first, String second) {
		return create(">=", first, second);
	}

	public static SciDBFunction greaterThanEqual(double first, double second) {
		return create(">=", Double.toString(first), Double.toString(second));
	}

	public static SciDBFunction greaterThanEqual(int first, int second) {
		return create(">=", Integer.toString(first), Integer.toString(second));
	}

	public static SciDBFunction greaterThanEqual(float first, float second) {
		return create(">=", Float.toString(first), Float.toString(second));
	}

	public static SciDBFunction abs(double variable) {
		return create("abs", Double.toString(variable));
	}

	public static SciDBFunction abs(int variable) {
		return create("abs", Integer.toString(variable));
	}

	public static SciDBFunction acos(double variable) {
		return create("acos", Double.toString(variable));
	}

	public static SciDBFunction acos(float variable) {
		return create("acos", Float.toString(variable));
	}

	public static SciDBFunction and(boolean first, boolean second) {
		return create("and", Boolean.toString(first), Boolean.toString(second));
	}

	public static SciDBFunction appendOffset(String appendOffset, int offset) {
		return create("append_offset", appendOffset, Integer.toString(offset));
	}

	public static SciDBFunction applyOffset(String applyOffset, int offset) {
		return create("apply_offset", applyOffset, Integer.toString(offset));
	}

	public static SciDBFunction asin(double variable) {
		return create("asin", Double.toString(variable));
	}

	public static SciDBFunction asin(float variable) {
		return create("asin", Float.toString(variable));
	}

	public static SciDBFunction atan(double variable) {
		return create("atan", Double.toString(variable));
	}

	public static SciDBFunction atan(float variable) {
		return create("atan", Float.toString(variable));
	}

	public static SciDBFunction book(String var1, String var2, int var3) {
		return create("book", var1, var2, Integer.toString(var3));
	}

	public static SciDBFunction ceil(double variable) {
		return create("ceil", Double.toString(variable));
	}

	public static SciDBFunction charCount(String var1, String var2) {
		return create("char_count", var1, var2);
	}

	public static SciDBFunction charToInt(char var) {
		return create("char_to_int", Character.toString(var));
	}

	public static SciDBFunction codify(String var) {
		return create("codify", var);
	}

	public static SciDBFunction cos(double variable) {
		return create("cos", Double.toString(variable));
	}

	public static SciDBFunction cos(float variable) {
		return create("cos", Float.toString(variable));
	}

	public static SciDBFunction dayOfWeek(String dateTime) {
		return create("day_of_week", dateTime);
	}

	public static SciDBFunction dcast(String var1, boolean var2) {
		return create("dcast", var1, Boolean.toString(var2));
	}

	public static SciDBFunction dcast(String var1, double var2) {
		return create("dcast", var1, Double.toString(var2));
	}

	public static SciDBFunction dcast(String var1, float var2) {
		return create("dcast", var1, Float.toString(var2));
	}

	public static SciDBFunction dcast(String var1, int var2) {
		return create("dcast", var1, Integer.toString(var2));
	}

	public static SciDBFunction dhyper(double var1, double var2, double var3,
			double var4) {
		return create("dhyper", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4));
	}

	public static SciDBFunction dumb_hash(String var) {
		return create("dumb_hash", var);
	}

	public static SciDBFunction dumb_unhash(int var) {
		return create("dumb_unhash", Integer.toString(var));
	}

	public static SciDBFunction exp(double var) {
		return create("exp", Double.toString(var));
	}

	public static SciDBFunction exp(float var) {
		return create("exp", Float.toString(var));
	}

	public static SciDBFunction firstIndex(String var) {
		return create("first_index", var);
	}

	public static SciDBFunction firstIndex(String var1, String var2) {
		return create("first_index", var1, var2);
	}

	public static SciDBFunction fisherTestOddsRatio(double var1, double var2,
			double var3, double var4) {
		return create("fishertest_odds_ratio", Double.toString(var1),
				Double.toString(var2), Double.toString(var3),
				Double.toString(var4));
	}

	public static SciDBFunction fisherTestPValue(double var1, double var2,
			double var3, double var4, String var5) {
		return create("fishertest_p_value", Double.toString(var1),
				Double.toString(var2), Double.toString(var3),
				Double.toString(var4), var5);
	}

	public static SciDBFunction floor(double var) {
		return create("floor", Double.toString(var));
	}

	public static SciDBFunction format(double var1, String var2) {
		return create("format", Double.toString(var1), var2);
	}

	public static SciDBFunction formatExtract(String var1, String var2,
			String var3) {
		return create("format_extract", var1, var2, var3);
	}

	public static SciDBFunction getOffset(String dateTimez) {
		return create("get_offset", dateTimez);
	}

	public static SciDBFunction high(String var) {
		return create("high", var);
	}

	public static SciDBFunction high(String var1, String var2) {
		return create("high", var1, var2);
	}

	public static SciDBFunction hourOfDay(String var) {
		return create("hour_of_day", var);
	}

	public static SciDBFunction hygecdf(double var1, double var2, double var3,
			double var4) {
		return create("hygecdf", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4));
	}

	public static SciDBFunction hygecdf(double var1, double var2, double var3,
			double var4, boolean var5) {
		return create("hygecdf", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4),
				Boolean.toString(var5));
	}

	public static SciDBFunction hygepmf(double var1, double var2, double var3,
			double var4) {
		return create("hygepmf", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4));
	}

	public static SciDBFunction hygequant(double var1, double var2,
			double var3, double var4) {
		return create("hygequant", Double.toString(var1),
				Double.toString(var2), Double.toString(var3),
				Double.toString(var4));
	}

	public static SciDBFunction instanceId() {
		return create("instanceid");
	}

	public static SciDBFunction intToChar(int var) {
		return create("int_to_char", Integer.toString(var));
	}

	public static SciDBFunction isNan(double var) {
		return create("is_nan", Double.toString(var));
	}

	public static SciDBFunction keyedValue(String var1, String var2,
			String var3, String var4) {
		return create("keyed_value", var1, var2, var3, var4);
	}

	public static SciDBFunction lastIndex(String var) {
		return create("last_index", var);
	}

	public static SciDBFunction lastIndex(String var1, String var2) {
		return create("last_index", var1, var2);
	}

	public static SciDBFunction length(String var) {
		return create("length", var);
	}

	public static SciDBFunction length(String var1, String var2) {
		return create("length", var1, var2);
	}

	public static SciDBFunction log(double var) {
		return create("log", Double.toString(var));
	}

	public static SciDBFunction log(float var) {
		return create("log", Float.toString(var));
	}

	public static SciDBFunction log10(double var) {
		return create("log10", Double.toString(var));
	}

	public static SciDBFunction log10(float var) {
		return create("log10", Float.toString(var));
	}

	public static SciDBFunction low(String var) {
		return create("low", var);
	}

	public static SciDBFunction low(String var1, String var2) {
		return create("low", var1, var2);
	}

	public static SciDBFunction max(boolean var1, boolean var2) {
		return create("max", Boolean.toString(var1), Boolean.toString(var2));
	}

	public static SciDBFunction max(String var1, String var2) {
		return create("max", var1, var2);
	}

	public static SciDBFunction maxlenCSV(String var) {
		return create("maxlen_csv", var);
	}

	public static SciDBFunction maxlenTDV(String var) {
		return create("maxlen_tdv", var);
	}

	public static SciDBFunction min(boolean var1, boolean var2) {
		return create("min", Boolean.toString(var1), Boolean.toString(var2));
	}

	public static SciDBFunction min(String var1, String var2) {
		return create("min", var1, var2);
	}

	public static SciDBFunction missing(int var) {
		return create("missing", Integer.toString(var));
	}

	public static SciDBFunction not(boolean var) {
		return create("not", Boolean.toString(var));
	}

	public static SciDBFunction now() {
		return create("now");
	}

	public static SciDBFunction nthCSV(String var1, int var2) {
		return create("nth_csv", var1, Integer.toString(var2));
	}

	public static SciDBFunction nthTDV(String var1, int var2, String var3) {
		return create("nth_tdv", var1, Integer.toString(var2), var3);
	}

	public static SciDBFunction or(boolean var1, boolean var2) {
		return create("or", Boolean.toString(var1), Boolean.toString(var2));
	}

	public static SciDBFunction phyper(double var1, double var2, double var3,
			double var4, boolean var5) {
		return create("phyper", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4),
				Boolean.toString(var5));
	}

	public static SciDBFunction pow(double var1, double var2) {
		return create("double", Double.toString(var1), Double.toString(var2));
	}

	public static SciDBFunction qhyper(double var1, double var2, double var3,
			double var4, boolean var5) {
		return create("qhyper", Double.toString(var1), Double.toString(var2),
				Double.toString(var3), Double.toString(var4),
				Boolean.toString(var5));
	}

	public static SciDBFunction random() {
		return create("random");
	}

	public static SciDBFunction regex(String var1, String var2) {
		return create("regex", var1, "\'" + var2 + "\'");
	}

	public static SciDBFunction rsub(String var1, String var2) {
		return create("rsub", var1, var2);
	}

	public static SciDBFunction sin(double var) {
		return create("sin", Double.toString(var));
	}

	public static SciDBFunction sin(float var) {
		return create("sin", Float.toString(var));
	}

	public static SciDBFunction sleep(int var) {
		return create("sleep", Integer.toString(var));
	}

	public static SciDBFunction sqrt(double var) {
		return create("sqrt", Double.toString(var));
	}

	public static SciDBFunction sqrt(float var) {
		return create("sqrt", Float.toString(var));
	}

	public static SciDBFunction strchar(String var) {
		return create("strchar", var);
	}

	public static SciDBFunction strftime(String var1, String var2) {
		return create("strftime", var1, var2);
	}

	public static SciDBFunction stripOffset(String datetime) {
		return create("strip_offset", datetime);
	}

	public static SciDBFunction strlen(String var) {
		return create("strlen", var);
	}

	public static SciDBFunction stripftime(String var1, String var2, String var3) {
		return create("strpftime", var1, var2, var3);
	}

	public static SciDBFunction substr(String var1, int var2, int var3) {
		return create("substr", var1, Integer.toString(var2),
				Integer.toString(var3));
	}

	public static SciDBFunction tan(double var) {
		return create("tan", Double.toString(var));
	}

	public static SciDBFunction tan(float var) {
		return create("tan", Float.toString(var));
	}

	public static SciDBFunction toGMT(String dateTime) {
		return create("togmt", dateTime);
	}

	public static SciDBFunction trim(String var) {
		return create("trim", var);
	}

	public static SciDBFunction trim(String var1, String var2) {
		return create("trim", var1, var2);
	}

	public static SciDBFunction tzNow() {
		return create("tznow");
	}

	public static SciDBFunction iif(boolean var1, String var2, String var3) {
		return create("iif", Boolean.toString(var1), var2, var3);
	}

	public static SciDBFunction missingReason(String var) {
		return create("missing_reason", var);
	}

	private static SciDBFunction create(String command, String first,
			String second, String third, String fourth, String fifth) {
		return new SciDBFunction(command + "(" + first + "," + second + ","
				+ third + "," + fourth + "," + fifth + ")");
	}

	private static SciDBFunction create(String command, String first,
			String second, String third, String fourth) {
		return new SciDBFunction(command + "(" + first + "," + second + ","
				+ third + "," + fourth + ")");
	}

	private static SciDBFunction create(String command, String first,
			String second, String third) {
		return new SciDBFunction(command + "(" + first + "," + second + ","
				+ third + ")");
	}

	private static SciDBFunction create(String command, String first,
			String second) {
		return new SciDBFunction(command + "(" + first + "," + second + ")");
	}

	private static SciDBFunction create(String command, String first) {
		return new SciDBFunction(command + "(" + first + ")");
	}

	private static SciDBFunction create(String command) {
		return new SciDBFunction(command + "()");
	}

	// Attribute calls
	public static SciDBFunction remainder(String attribute, Double second) {
		return create("%", attribute, Double.toString(second));
	}

	public static SciDBFunction remainder(String attribute, Integer second) {
		return create("%", attribute, Integer.toString(second));
	}

	public static SciDBFunction multiply(String attribute, Double second) {
		return create("*", attribute, Double.toString(second));
	}

	public static SciDBFunction multiply(String attribute, Float second) {
		return create("*", attribute, Float.toString(second));
	}

	public static SciDBFunction multiply(String attribute, Integer second) {
		return create("*", attribute, Integer.toString(second));
	}

	public static SciDBFunction add(String attribute, Double second) {
		return create("+", attribute, Double.toString(second));
	}

	public static SciDBFunction add(String attribute, Float second) {
		return create("+", attribute, Float.toString(second));
	}

	public static SciDBFunction add(String attribute, Integer second) {
		return create("+", attribute, Integer.toString(second));
	}

	public static SciDBFunction subtract(String attribute, Double second) {
		return create("-", attribute, Double.toString(second));
	}

	public static SciDBFunction subtract(String attribute, Float second) {
		return create("-", attribute, Float.toString(second));
	}

	public static SciDBFunction subtract(String attribute, Integer second) {
		return create("-", attribute, Integer.toString(second));
	}

	public static SciDBFunction divide(String attribute, Double second) {
		return create("/", attribute, Double.toString(second));
	}

	public static SciDBFunction divide(String attribute, Float second) {
		return create("/", attribute, Float.toString(second));
	}

	public static SciDBFunction divide(String attribute, Integer second) {
		return create("/", attribute, Integer.toString(second));
	}

	public static SciDBFunction lessThan(String attribute, Boolean second) {
		return create("<", attribute, Boolean.toString(second));
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

	public static SciDBFunction abs(String attribute) {
		return create("abs", attribute);
	}

	public static SciDBFunction acos(String attribute) {
		return create("acos", attribute);
	}

	public static SciDBFunction and(String attribute, Boolean second) {
		return create("and", attribute, Boolean.toString(second));
	}

	public static SciDBFunction appendOffset(String attribute, Integer second) {
		return create("append_offset", attribute, Integer.toString(second));
	}

	public static SciDBFunction applyOffset(String attribute, Integer second) {
		return create("apply_offset", attribute, Integer.toString(second));
	}

	public static SciDBFunction asin(String attribute) {
		return create("asin", attribute);
	}

	public static SciDBFunction atan(String attribute) {
		return create("atan", attribute);
	}

	public static SciDBFunction ceil(String attribute) {
		return create("ceil", attribute);
	}

	public static SciDBFunction chaToInt(String attribute) {
		return create("char_to_int", attribute);
	}

	public static SciDBFunction cos(String attribute) {
		return create("cos", attribute);
	}

	public static SciDBFunction dcast(String attribute, Boolean second) {
		return create("dcast", attribute, Boolean.toString(second));
	}

	public static SciDBFunction dcast(String attribute, Double second) {
		return create("dcast", attribute, Double.toString(second));
	}

	public static SciDBFunction dcast(String attribute, Float second) {
		return create("dcast", attribute, Float.toString(second));
	}

	public static SciDBFunction dcast(String attribute, Integer second) {
		return create("dcast", attribute, Integer.toString(second));
	}

	public static SciDBFunction exp(String attribute) {
		return create("exp", attribute);
	}

	public static SciDBFunction attributeIndex(String attribute) {
		return create("attribute_index", attribute);
	}

	public static SciDBFunction attributeIndex(String attribute, String second) {
		return create("attribute_index", attribute, second);
	}

	public static SciDBFunction floor(String attribute) {
		return create("floor", attribute);
	}

	public static SciDBFunction format(String attribute, String second) {
		return create("format", attribute, second);
	}

	public static SciDBFunction hygecdf(String attribute, Double second,
			Double third, Double fourth, Boolean fifth) {
		return create("hygecdf", attribute, Double.toString(second),
				Double.toString(third), Double.toString(fourth),
				Boolean.toString(fifth));
	}

	public static SciDBFunction hygepmf(String attribute, Double second,
			Double third, Double fourth) {
		return create("hygepmf", attribute, Double.toString(second),
				Double.toString(third), Double.toString(fourth));
	}

	public static SciDBFunction hygequant(String attribute, Double second,
			Double third, Double fourth, Boolean fifth) {
		return create("hygequant", attribute, Double.toString(second),
				Double.toString(third), Double.toString(fourth),
				Boolean.toString(fifth));
	}

	public static SciDBFunction instanceid() {
		return create("instanceid");
	}

	public static SciDBFunction intToChar(String attribute) {
		return create("int_to_char", attribute);
	}

	public static SciDBFunction isNAN(String attribute) {
		return create("is_nan", attribute);
	}

	public static SciDBFunction isNull(String attribute) {
		return create("is_null", attribute);
	}

	public static SciDBFunction keyedValue(String attribute, String second,
			String third) {
		return create("keyed_value", attribute, second, third);
	}

	public static SciDBFunction log(String attribute) {
		return create("log", attribute);
	}

	public static SciDBFunction log10(String attribute) {
		return create("log10", attribute);
	}

	public static SciDBFunction max(String attribute, Boolean second) {
		return create("max", attribute, Boolean.toString(second));
	}

	public static SciDBFunction maxlenTDV(String attribute, String second) {
		return create("maxlen_tdv", attribute, second);
	}

	public static SciDBFunction min(String attribute, Boolean second) {
		return create("min", attribute, Boolean.toString(second));
	}

	public static SciDBFunction missing(String attribute) {
		return create("missing", attribute);
	}

	public static SciDBFunction not(String attribute) {
		return create("not", attribute);
	}

	public static SciDBFunction nthCSV(String attribute, Integer second) {
		return create("nth_csv", attribute, Integer.toString(second));
	}

	public static SciDBFunction nthTDV(String attribute, Integer second,
			String third) {
		return create("nth_tdv", attribute, Integer.toString(second), third);
	}

	public static SciDBFunction or(String attribute, Boolean second) {
		return create("or", attribute, Boolean.toString(second));
	}

	public static SciDBFunction pow(String attribute, Double second) {
		return create("pow", attribute, Double.toString(second));
	}

	public static SciDBFunction sin(String attribute) {
		return create("sin", attribute);
	}

	public static SciDBFunction sqrt(String attribute) {
		return create("sqrt", attribute);
	}

	public static SciDBFunction substr(String attribute, Integer second,
			Integer third) {
		return create("substr", attribute, Integer.toString(second),
				Integer.toString(third));
	}

	public static SciDBFunction tan(String attribute) {
		return create("tan", attribute);
	}

	public static SciDBFunction togmt(String attribute) {
		return create("togmt", attribute);
	}

	public static SciDBFunction iif(String attribute, String second,
			String third) {
		return create("iif", attribute, second, third);
	}

	public static SciDBFunction sizeOf(String attribute) {
		return create("sizeof", attribute);
	}
}
