package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class DataTypeJsonConverter implements Converter<DataType, DataTypeForJson>{
	
	@Override
	public DataTypeForJson convert(DataType arg0) {
		DataType typeOf = arg0.typeOf();
		return new DataTypeForJson().
				setDescription(arg0.getDescription()).
				setName(arg0.getName()).
				setPattern(arg0.getPattern().pattern()).
				setTypeof(typeOf == null ? null : typeOf.getName());
	}

	@Override
	public JavaType getInputType(TypeFactory arg0) {
		return arg0.constructFromCanonical(DataType.class.getCanonicalName());
	}

	@Override
	public JavaType getOutputType(TypeFactory arg0) {
		return arg0.constructFromCanonical(DataTypeForJson.class.getCanonicalName());
	}
/*	
 * 
      {
        "name": "date",
        "pattern": "^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$",
        "description": "Date in yyyy-mm-dd format",
        "typeof": "dateTime"
      },
 */
}
