package com.ek.model;
// Response.Write("{success: true, result: false, msg:'登录出错'}");
public class NormalResult {
    public Boolean success;
    public Boolean result;
    public String msg;
    public Integer data; ///返回卷流水数字 使用

    public Integer qty; ///生成卷流水数字 使用
    public Double qty1; ///生成卷流水数字 使用
    public String tz_no; //出库是返回 使用

    public String old_Z_path; //上架是返回 使用
}
