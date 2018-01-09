package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class IRCTResponse {

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.APPLICATION_JSON_TYPE;
    private static final Response.Status DEFAULT_RESPONSE_ERROR_CODE = Response.Status.INTERNAL_SERVER_ERROR;

    public static Response success(){
        return Response.ok().build();
    }

    public static Response success(Object content){
        return Response.ok(content, DEFAULT_MEDIA_TYPE)
                .build();
    }

    public static Response success(Object content, MediaType type){
        return Response.ok(JsonNodeFactory.instance
                .objectNode()
                .putPOJO("details", content), type)
                .build();
    }

    public static Response error(Object content) {
        return error(content, MediaType.APPLICATION_JSON_TYPE);
    }

    public static Response error(Object content, MediaType type){
        return error(DEFAULT_RESPONSE_ERROR_CODE, content, type);
    }

    public static Response error(Response.Status status, Object content, MediaType type){
        return Response.status(status)
                .entity(new IRCTResponseError(content))
                .type(type)
                .build();
    }

}
