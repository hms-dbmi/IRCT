package edu.harvard.hms.dbmi.bd2k.irct.ri.gnome;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class GNomeTesting {
    static String token = "";
    public static void main(String[] args) {
//
//        String a = null;
//
//        System.out.println("abc" + a);
//        retrieveToken();

//        System.out.println(isTokenExists());

        printingIPAddress();

    }

    private static boolean isTokenExists(){
        return token!=null && !token.isEmpty();
    }

    private static void retrieveToken(){
        try {
            URL url = new URL("http://gnome.tchlab.org/auth/auth.cgi");



            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");



            con.setRequestProperty("Authorization", "Basic " +
                    Base64.encodeBase64String(("bchgrin:bchgrin").getBytes()));


            String token = (String)new ObjectMapper().readValue(con.getInputStream(), Map.class).get("token");




            System.out.println(token);
        } catch (IOException ex){
            ex.printStackTrace();

        }

    }


    static void printingIPAddress(){

        double max = Math.pow(256, 4);
        for (double i = 0d; i<max; i++){

        }
    }
}
