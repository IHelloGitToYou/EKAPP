package com.ek;

import java.net.PortUnreachableException;


public class WebApi {
    public static String HOST = "http://192.168.43.117:8099";//http://192.168.2.108:8099";
    public static String URL_LOGIN = "/ASHX/Core/FD/Ashx_DBInfo.ashx";
    public static String URL_PRDTONLY = "/ASHX/Core/FD/Ashx_PrdtOnly.ashx";
    public static String URL_VONLINE = "/ASHX/Core/FD/Ashx_Vonline.ashx";
    public static String URL_SALM = "/ASHX/Base/SYSBase/Ashx_Salm.ashx";
    public static String URL_JL= "/ASHX/EK/CK/Ashx_JL.ashx";
    public static String URL_SO= "/Ashx/EK/SO/Ashx_EK_SO.ashx";
    public static String URL_HostPrint= "/Ashx/Core/FD/Ashx_Print.ashx";
    public static String URL_EKJK= "/Ashx/EK/SC/EK_JK.ashx";
    public static String URL_EKJOB= "/Ashx/EK/SC/Ashx_JOB.ashx";

    public static String getRealUrl(String u)
    {
        return HOST + u;
    }


    //加载打印模版

}

