package edu.harvard.hms.dbmi.bd2k.irct.exception.mapper;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ApplicationException;
import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationMapper implements ExceptionMapper<ApplicationException>{

    @Override
    public Response toResponse(ApplicationException exception) {
        return IRCTResponse.applicationError(exception.getContent());
    }
}
