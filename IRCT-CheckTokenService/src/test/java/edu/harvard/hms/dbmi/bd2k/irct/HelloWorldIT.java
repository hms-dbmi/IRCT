package edu.harvard.hms.dbmi.bd2k.irct;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

// TODO: create test that works with new docker deployment strategy
public class HelloWorldIT {
    private static String endpointUrl;

    @BeforeClass
    public static void beforeClass() {
        endpointUrl = System.getProperty("service.url");
    }

    @Ignore @Test
    public void testPing() throws Exception {
        WebClient client = WebClient.create(endpointUrl + "/hello/echo/SierraTangoNevada");
        Response r = client.accept("text/plain").get();
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        String value = IOUtils.toString((InputStream)r.getEntity());
        assertEquals("SierraTangoNevada", value);
    }

    @Ignore @Test
    public void testJsonRoundtrip() throws Exception {
        List<Object> providers = new ArrayList<Object>();
        providers.add(new JacksonJsonProvider());
        JsonBean inputBean = new JsonBean();
        inputBean.setVal1("Maple");
        WebClient client = WebClient.create(endpointUrl + "/hello/jsonBean", providers);
        Response r = client.accept("application/json")
            .type("application/json")
            .post(inputBean);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        MappingJsonFactory factory = new MappingJsonFactory();
        JsonParser parser = factory.createJsonParser((InputStream)r.getEntity());
        JsonBean output = parser.readValueAs(JsonBean.class);
        assertEquals("Maple", output.getVal2());
    }
}
