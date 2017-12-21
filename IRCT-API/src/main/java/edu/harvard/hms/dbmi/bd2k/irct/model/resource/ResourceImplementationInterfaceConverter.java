package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ResourceImplementationInterface;

public class ResourceImplementationInterfaceConverter implements Converter<ResourceImplementationInterface, String> {

	@Override
	public String convert(ResourceImplementationInterface arg0) {
		return arg0.getType();
	}

	@Override
	public JavaType getInputType(TypeFactory arg0) {
		return arg0.constructFromCanonical(ResourceImplementationInterface.class.getCanonicalName());
	}

	@Override
	public JavaType getOutputType(TypeFactory arg0) {
		return arg0.constructFromCanonical(String.class.getCanonicalName());
	}

}
