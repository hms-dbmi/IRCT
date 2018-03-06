package edu.harvard.hms.dbmi.bd2k.irct.exception.mapper;

import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedMapper implements ExceptionMapper<NotAuthorizedException>{
    @Override
    public Response toResponse(NotAuthorizedException exception) {
        return IRCTResponse.protocolError(Response.Status.UNAUTHORIZED,
                exception.getChallenges().toString());
    }
}
