package edu.harvard.hms.dbmi.bd2k.irct.exception.mapper;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResourceInterfaceMapper implements ExceptionMapper<ResourceInterfaceException>{
    @Override
    public Response toResponse(ResourceInterfaceException exception) {
        return IRCTResponse.riError(exception.getMessage());
    }
}