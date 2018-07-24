package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.inner;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElementData{
    Map<String, Object> data;

    public ElementData(){
        super();
        data = new HashMap<>();
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

}
