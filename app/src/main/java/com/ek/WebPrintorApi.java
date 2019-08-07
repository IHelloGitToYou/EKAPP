package com.ek;


public class WebPrintorApi {

    //public static String HOST = "http://192.168.2.103:8223/StarService";

    public static String HOST_IP = "192.168.43.118";
    public static String HOST = "http://192.168.43.118:8223/StarService";

//    public static String HOST_IP = "192.168.1.188";
//    public static String HOST = "http://192.168.1.188:8223/StarService";

//    public static String HOST_IP = "192.168.1.137";
//    public static String HOST = "http://192.168.1.137:8223/StarService";
    public static String getRealUrl(String u)
    {
        return HOST + u;
    }


}