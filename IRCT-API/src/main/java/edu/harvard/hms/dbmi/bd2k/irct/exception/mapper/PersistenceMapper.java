package edu.harvard.hms.dbmi.bd2k.irct.exception.mapper;

import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;

import javax.persistence.PersistenceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PersistenceMapper implements ExceptionMapper<PersistenceException>{

    @Override
    public Response toResponse(PersistenceException exception) {
        return IRCTResponse.applicationError(exception.getMessage());
    }
}
