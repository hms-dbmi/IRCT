package edu.harvard.hms.dbmi.bd2k.irct.exception.mapper;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ProtocolException;
import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ProtocolMapper implements ExceptionMapper<ProtocolException>{

    @Override
    public Response toResponse(ProtocolException exception) {
        return IRCTResponse.protocolError(exception.getResponse().getStatus(), exception.getContent());
    }
}
