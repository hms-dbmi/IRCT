package edu.harvard.hms.dbmi.scidb;

public class SciDBAggregateFactory {
	private static SciDBAggregate create(String aggregate, String attribute) {
		return new SciDBAggregate(aggregate + "(" + attribute + ")");
	}
	
	public static SciDBAggregate count() {
		return count("*");
	}
	public static SciDBAggregate count(String attribute) {
		return create("count", attribute);
	}
}
