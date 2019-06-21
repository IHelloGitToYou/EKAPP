package com.ek.model;

import java.io.Serializable;

//注意要集成这个接口才能 在Activity之间传递对象
public class SoLineModel implements Serializable
{
    public String  Z_work_no;
    public String so_no;
    public Integer itm;

    public String cus_no;

    public String prd_no;
    public String wh_no;
    public Double FD_width;
    public Double FD_length;
    public Double FD_core;
    public Double Z_core_kg;

    public Double qty;
    public Double qty1;
    public Double qty_jk;
    public String Z_print;
    public String Z_bzinfo;

    //缴库区新建栏位
    public String print_jk;
    public String print_back;
    public String printor;

    public boolean isSelected;
}
