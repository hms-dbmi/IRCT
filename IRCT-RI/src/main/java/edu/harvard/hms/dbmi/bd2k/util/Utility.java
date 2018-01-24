package edu.harvard.hms.dbmi.bd2k.util;

public class Utility {

    public static String getURLFromPui (String pui, String resourceName){
        String result = "";
        if (pui == null || pui.isEmpty())
            return result;

        if (!pui.startsWith("/"))
            pui = "/" + pui;

        String[] strings = pui.split("/");

        for (int i = 2; i<strings.length; i++){
            result += "/" + strings[i];
        }

        return result;
    }
}
