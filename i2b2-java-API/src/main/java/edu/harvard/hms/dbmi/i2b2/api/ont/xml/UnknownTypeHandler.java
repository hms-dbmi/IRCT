package edu.harvard.hms.dbmi.i2b2.api.ont.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class UnknownTypeHandler implements DomHandler<String, StreamResult>{

	@Override
	public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
		return new StreamResult(new StringWriter());
	}

	@Override
	public String getElement(StreamResult rt) {
		return rt.getWriter().toString();
	}

	@Override
	public Source marshal(String n, ValidationEventHandler errorHandler) {
		return new StreamSource(new StringReader(n));
	}

}
