package com.ek.model;

import java.io.Serializable;

//注意要集成这个接口才能 在Activity之间传递对象
public class SelectSoLineModel implements Serializable
{
    public String  Z_work_no;
    public String so_no;
    public Integer itm;

    public String cus_no;

    public String prd_no;
    public String wh_no;
    public Integer FD_width;
    public Integer FD_length;
    public Double FD_core;
    public Integer Z_sale_hou3;


    public Integer qty;
    public Double qty1;
    public Integer qty_jk;
    public String Z_bzinfo;

    //缴库区新建栏位

    public Double Z_core_kg;
    public String Z_print;
    public String print_back;
//    public String printor_jk;
//    public String printor_back;     //打印机应该不会变的


    public boolean isSelected;
}
