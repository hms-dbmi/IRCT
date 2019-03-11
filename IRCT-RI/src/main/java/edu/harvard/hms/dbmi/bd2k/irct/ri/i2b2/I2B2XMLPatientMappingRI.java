package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

import java.util.Arrays;
import java.util.Map;

/**
 * The patient mapping RI work is handled by main i2b2xmlRI, the switch to turn it on is
 * the parameter: <code>returnFullSet</code>, when this parameter sets to false, the main i2b2xmlRI will work
 *
 * The returnFullSet parameter will finally affect creating getPdoFromInputlist function in CRCCell class
 * like patient mapping
 */
public class I2B2XMLPatientMappingRI extends I2B2XMLResourceImplementation {

    @Override
    public String getType(){
        return "i2b2XML_patientMapping";
    }

    public void setup(Map<String, String> parameters) throws ResourceInterfaceException {
        if (!parameters.keySet().contains("sourceWhiteList")) {
            throw new ResourceInterfaceException("Missing ```sourceWhiteList``` parameter. It is mandatory");
        }
        super.setup(parameters);
        this.sourceWhiteList = Arrays.asList(parameters.get("sourceWhiteList").split(","));
        this.returnFullSet = false;
    }

    @Override
    public Result runQuery(User user,Query query, Result result) throws ResourceInterfaceException {
        return super.runQuery(user, query, result);
    }
}
