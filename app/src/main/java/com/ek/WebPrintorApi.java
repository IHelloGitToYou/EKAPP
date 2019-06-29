package com.ek;


public class WebPrintorApi {
    public static String HOST = "http://192.168.43.117:8223/StarService";
    //http://192.168.43.117:8223/StarService?action=GetPrinter
    public static String getRealUrl(String u)
    {
        return HOST + u;
    }


}