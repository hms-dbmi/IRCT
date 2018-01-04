package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.util.HashMap;
import java.util.Map;


public class RIClassPathTracking {

    // to keep track of the class path, and allow not entering the database to
    // change the implementation classpath in Resource class
    private static Map<String, String> riClassPathMap;
    private static Map initRiClassPathMap(){
        riClassPathMap = new HashMap<>();

        // notice: never delete any mapping
        // and always put the latest mapping in the front for easily maintenance
        // use case: when the implementation classpath changed,
        // just add one more mapping at the top
        riClassPathMap.put("older class path", "latest class path");
        riClassPathMap.put("much older class path","older class path");

        return riClassPathMap;
    }

    /**
     * to get the latest classpath based on the inner classpath tracking map
     * @param latest
     * @return the latest classpath, anything wrong return null
     */
    public static String getLatestClassPath(String latest){
        if (latest == null)
            return null;

        if (riClassPathMap == null)
            initRiClassPathMap();

        if (riClassPathMap.isEmpty())
            return latest;

        if (riClassPathMap.containsKey(latest)==false)
            return latest;
        else
            return getLatestClassPath(
                    riClassPathMap.get(latest));

    }

}
