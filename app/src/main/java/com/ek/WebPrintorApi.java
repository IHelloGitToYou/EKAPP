package com.ek;


import android.content.SharedPreferences;


public class WebPrintorApi {

    //public static String HOST = "http://192.168.2.103:8223/StarService";

//    public static String HOST_IP = "192.168.43.117";

    public static String HOST_IP = "192.168.1.188";

//    public static String HOST_IP = "192.168.2.110";
    //public static String HOST = "http://192.168.2.110:8223/StarService";
    public static String getRealUrl(String u) {
        return "http://" + HOST_IP + ":8223/StarService" + u;
    }


    public static void SetPrintHost_IP(String ip) {
        HOST_IP = ip;
    }

}