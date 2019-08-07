package com.ek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.ek.adapter.ShowHistoryOnlyAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.PageResultModel;
import com.ek.model.PrintorModel;
import com.ek.model.SelectSoLineModel;
import com.ek.model.WHInfoModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowHistoryOnlyActivity extends AppCompatActivity implements View.OnClickListener  {

    public static final int SELECTED_COMPLETE = 2;
    ShowHistoryOnlyAdapter adapter;
    OkHttpClient client;
    Spinner edit_machine;
    ArrayAdapter<WHInfoModel> spinnerMachineAdapter;
    EditText edit_prd_no, edit_Z_work_no, edit_so_no ,edit_sal_no;
    CheckBox edit_show_jk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history_only);

        client = new OkHttpClient();
        adapter = new ShowHistoryOnlyAdapter(new ArrayList<OnlyNoItem>());

        Button btnSearch = findViewById(R.id.btn_search);
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btn_update = findViewById(R.id.btn_update);
        //Button btn_reprint = findViewById(R.id.btn_reprint);

        btnSearch.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        //btn_reprint.setOnClickListener(this);


        edit_so_no = findViewById(R.id.edit_so_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        edit_sal_no = findViewById(R.id.edit_sal_no);
        edit_machine = findViewById(R.id.edit_machine);
        edit_show_jk = findViewById(R.id.edit_show_jk);

        ListView lv = findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OnlyNoItem line = (OnlyNoItem) adapter.getItem(position);
                adapter.setSelctItem(position);
            }
        });

        setMachines();

        String defaultMachine = getIntent().getStringExtra("defaultMachine");
        setSpinnerWHValue(edit_machine, defaultMachine);

        //this.edit_show_jk.setOnCheckedChangeListener(new OnCheckedChangeListenerImpl());
    }



    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.btn_delete:
                android.content.DialogInterface.OnClickListener c1 = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete();
                    }
                };
                beforeDelete(c1);
                break;
            case R.id.btn_update:
                //startActivityForResult(new Intent(this, edit_jl_activity.class),3 );
                //Toast.makeText(getApplicationContext(),String.format("功能开发中，亲"), Toast.LENGTH_LONG);
                OnlyNoItem only = adapter.getSelectFirst();
                if(only == null){
                    Toast.makeText(getApplicationContext(),String.format("请先选择卷料 "), Toast.LENGTH_SHORT).show();
                    return;
                }
                ShowWin(only);
                break;
            case R.id.btn_search:
                LoadData();
                break;
        }
    }

    boolean Clicking = false;
    OnlyNoItem currentOnly;
    void ShowWin(OnlyNoItem only){
        currentOnly = only;
        AlertDialog.Builder cDialog = new AlertDialog.Builder(ShowHistoryOnlyActivity.this);
        final View cView = LayoutInflater.from(ShowHistoryOnlyActivity.this).inflate(R.layout.activity_edit_jl, null);
        cDialog.setTitle("修改卷料信息");
        cDialog.setView(cView);
        cDialog.setCancelable(true);

        final EditText FD_width_win, FD_length_win,edit_Z_core_kg_win, edit_Z_kg_win, edit_qty1_win,edit_qty_win;
        final Spinner edit_print_jk_win,edit_printor_jk_win;
        final CheckBox edit_is_multi_win;

        FD_width_win = cView.findViewById(R.id.edit_FD_width);
        FD_length_win = cView.findViewById(R.id.edit_FD_length);
        edit_Z_core_kg_win = cView.findViewById(R.id.edit_Z_core_kg);
        edit_Z_kg_win = cView.findViewById(R.id.edit_Z_kg);
        edit_qty1_win = cView.findViewById(R.id.edit_qty1);
        edit_print_jk_win = cView.findViewById(R.id.edit_print_jk);
        edit_printor_jk_win = cView.findViewById(R.id.edit_printor_jk);
        edit_is_multi_win = cView.findViewById(R.id.edit_is_multi);
        edit_qty_win = cView.findViewById(R.id.edit_qty);

        FD_width_win.setText(JK_SOActivity.getInt(currentOnly.FD_width.toString())+ "");
        FD_length_win.setText(currentOnly.FD_length + "");

        edit_Z_core_kg_win.setText(currentOnly.Z_core_kg + "");
        edit_Z_kg_win.setText(currentOnly.Z_kg + "");
        if(currentOnly.is_multi.equals("T") || currentOnly.is_multi.equals("on"))
            edit_is_multi_win.setChecked(true);

        edit_qty_win.setText(JK_SOActivity.getInt(currentOnly.qty.toString())+ "");
        edit_qty1_win.setText(JK_SOActivity.getDouble(currentOnly.qty1.toString())+ "");

        if(edit_show_jk.isChecked())
            loadPrintorModels("JLIN", edit_print_jk_win);
        else
            loadPrintorModels("JLBACK", edit_print_jk_win);

        loadPrintotr(edit_printor_jk_win);

        editChangeListen(edit_Z_core_kg_win, edit_Z_kg_win, edit_qty1_win);

        cDialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Integer width = JK_SOActivity.getInt( FD_width_win.getText().toString());
                Integer length = JK_SOActivity.getInt( FD_length_win.getText().toString());
                Double  z_core_kg = JK_SOActivity.getDouble( edit_Z_core_kg_win.getText().toString());
                Double  z_kg = JK_SOActivity.getDouble( edit_Z_kg_win.getText().toString());
                Double  qty1 = JK_SOActivity.getDouble( edit_qty1_win.getText().toString());
                Integer  qty = JK_SOActivity.getInt( edit_qty_win.getText().toString());
                boolean isMulti = edit_is_multi_win.isChecked();

                if(isMulti == true && edit_qty_win.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"集合卷必须输入卷数!", Toast.LENGTH_SHORT).show();
                    holdDialog(dialog, false);
                    return ;
                }

                if( JK_SOActivity.getDouble(edit_qty_win.getText().toString()) > 1 && isMulti == false){
                    Toast.makeText(getApplicationContext(),String.format("卷数不能大于1"), Toast.LENGTH_SHORT).show();
                    holdDialog(dialog, false);
                    return ;
                }

                if(Clicking) return;
                Clicking = true;
                doEditApi(width, length, z_core_kg, z_kg, qty1, qty, isMulti, dialog);
            }
        });

        cDialog.setPositiveButton("补打", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrintorModel printor = (PrintorModel)edit_printor_jk_win.getSelectedItem();
                PrintorModel printModel  = (PrintorModel)edit_print_jk_win.getSelectedItem();
                if(printor == null || printor.name.isEmpty() || printModel ==null || printModel.name.isEmpty()){
                    //Log.d("Isempty","模版与打印机要选择");
                    holdDialog(dialog, false);
                    Toast.makeText(getApplicationContext(),String.format("模版与打印机要选择"), Toast.LENGTH_LONG).show();
                    holdDialog(dialog, true);
                    return;
                }

                if(printor != null && printor.name.isEmpty() == false && printModel != null && printModel.name.isEmpty() == false){
                    HashMap<String,String> paramsMap = getPrintJLParams();
                    doPrintJL(printor.name, printModel.name, paramsMap, dialog);
                }

            }
        });

        cDialog.show();
    }



//    private class OnCheckedChangeListenerImpl implements
//            CompoundButton.OnCheckedChangeListener {
//
//        public void onCheckedChanged(CompoundButton button, boolean checked) {
//            if (edit_show_jk.isChecked()) {
//                /* tableType : JLBACK 或 JLIN */
//                loadPrintorModels("JLIN", edit_print_jk_win);
//            } else {
//                loadPrintorModels("JLBACK", edit_print_jk_win);
//            }
//        }
//    }

    private HashMap<String,String>  getPrintJLParams(){

        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","InsertJLRow");
        paramsMap.put("Z_top_no",currentOnly.Z_top_no);
        paramsMap.put("up_only_no","");


        //PrintorModel pModel = (PrintorModel)edit_print_jk.getSelectedItem();
        //paramsMap.put("signPrinter", pModel.name);
        paramsMap.put("machine", currentOnly.machine);
        paramsMap.put("json","[]");
        paramsMap.put("workId","WORK");
        paramsMap.put("workingNo","PDA_2019");

        paramsMap.put("prd_no", currentOnly.prd_no);
        paramsMap.put("Z_hou3","");
        paramsMap.put("FD_width",   JK_SOActivity.getInt( currentOnly.FD_width.toString()) + "");
        paramsMap.put("FD_length", JK_SOActivity.getInt( currentOnly.FD_length.toString()) + "");
        paramsMap.put("FD_core", JK_SOActivity.getDouble(currentOnly.FD_core.toString()) + "");
        if(paramsMap.get("FD_core").toString().equals("3.0"))
            paramsMap.put("FD_core", "3");
        if(paramsMap.get("FD_core").toString().equals("1.0"))
            paramsMap.put("FD_core", "1");
        if(paramsMap.get("FD_core").toString().equals("6.0"))
            paramsMap.put("FD_core", "6");

        paramsMap.put("Z_core_kg", JK_SOActivity.getDouble(currentOnly.Z_core_kg.toString()) + "");

        if(currentOnly.is_multi.equals( "on") || currentOnly.is_multi.equals( "T")  )
            paramsMap.put("is_multi", "on");

        paramsMap.put("qty", JK_SOActivity.getInt(currentOnly.qty.toString()) + "");
        paramsMap.put("qty1",JK_SOActivity.getDouble( currentOnly.qty1.toString()) + "");

        paramsMap.put("Z_kg",JK_SOActivity.getDouble( currentOnly.Z_kg.toString()) + "");

        paramsMap.put("wh_no",currentOnly.wh_no);

        paramsMap.put("only_rem",currentOnly.only_rem);
        paramsMap.put("Z_iface",currentOnly.Z_iface);

        paramsMap.put("Z_work_no",currentOnly.Z_work_no);


        paramsMap.put("so_id","SO");
        paramsMap.put("so_no",currentOnly.lock_table_no);
        paramsMap.put("itm",currentOnly.lock_table_itm.toString() + "");

        paramsMap.put("lock_table_id", "SO");
        paramsMap.put("lock_table_no", currentOnly.lock_table_no);
        paramsMap.put("lock_table_itm",currentOnly.lock_table_itm+ "");

        paramsMap.put("only_no",currentOnly.only_no);
        return paramsMap;
    }

    protected void doPrintJL(String printor, String printModel, HashMap<String,String> paramsJL, final DialogInterface dialog)  {

        paramsJL.put("CONN", "StarEK");
        paramsJL.put("action", "PrintJLByAndroid");         //new String(b, "GB2312")
        paramsJL.put("printor", printor + WebApi.Utf8_Split); //
        paramsJL.put("print", printModel + WebApi.Utf8_Split);
        paramsJL.put("only_rem",  currentOnly.only_rem + WebApi.Utf8_Split);
        paramsJL.put("Z_iface",currentOnly.Z_iface+ WebApi.Utf8_Split);

        //


        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme("http")
                .host(WebPrintorApi.HOST_IP)
                .port(8223)
                .addPathSegment("/StarService");

        for (String key : paramsJL.keySet()) {
            //追加表单信息
            builder.addQueryParameter(key, paramsJL.get(key));
        }

        HttpUrl url = builder.build();
        Request request = new Request.Builder()
                //.addHeader("content-type", "application/x-www-form-urlencoded;charset=gb2312")
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holdDialog(dialog, false);
                        Toast.makeText(getApplicationContext(),String.format("打印出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final NormalResult result = gson.fromJson(respTxt, NormalResult.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holdDialog(dialog, true);

                        Toast.makeText(getApplicationContext(),String.format("打印成功"), Toast.LENGTH_LONG).show();
                        holdDialog(dialog, true);
                        dialog.dismiss();
                        dialog.cancel();
                    }
                });
            }
        });

    }

    void doEditApi(Integer width , Integer length, Double  z_core_kg , Double  z_kg,Double qty1, Integer  qty, boolean isMulti, final DialogInterface dialog){
        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","UpdateJLRow");
        paramsMap.put("only_no",currentOnly.only_no);
        paramsMap.put("Z_top_no",currentOnly.Z_top_no.toString());
        paramsMap.put("up_only_no","");
        paramsMap.put("machine", currentOnly.machine);
        paramsMap.put("json","[]");
        paramsMap.put("workId","WORK");
        paramsMap.put("workingNo","PDA_2019");
        paramsMap.put("prd_no", currentOnly.prd_no.toString());
        paramsMap.put("Z_hou3","");
        paramsMap.put("FD_width", width + "");
        paramsMap.put("FD_length",  length + "");
        paramsMap.put("FD_core", currentOnly.FD_core + "");
        if(paramsMap.get("FD_core").toString().equals("3.0"))
            paramsMap.put("FD_core", "3");
        if(paramsMap.get("FD_core").toString().equals("1.0"))
            paramsMap.put("FD_core", "1");
        if(paramsMap.get("FD_core").toString().equals("6.0"))
            paramsMap.put("FD_core", "6");
        paramsMap.put("Z_core_kg",  z_core_kg+ "");
        if(isMulti)
            paramsMap.put("is_multi", "on");

        paramsMap.put("qty", qty + "");
        paramsMap.put("qty1",qty1 + "");

        paramsMap.put("Z_kg",z_kg + "");
        paramsMap.put("wh_no",currentOnly.wh_no);
        paramsMap.put("only_rem",currentOnly.only_rem.toString());
        paramsMap.put("Z_iface",currentOnly.Z_iface.toString());

        paramsMap.put("Z_work_no",edit_Z_work_no.getText().toString());

        paramsMap.put("so_id",currentOnly.lock_table_id);
        paramsMap.put("so_no",currentOnly.lock_table_no);
        paramsMap.put("itm", currentOnly.lock_table_itm + "");

        paramsMap.put("lock_table_id",currentOnly.lock_table_id);
        paramsMap.put("lock_table_no",currentOnly.lock_table_no);
        paramsMap.put("lock_table_itm",currentOnly.lock_table_itm);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJOB))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Clicking = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holdDialog(dialog, false);
                        Toast.makeText(getApplicationContext(),String.format("修改卷料失败!"), Toast.LENGTH_SHORT).show(); }});
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final NormalResult model = gson.fromJson(respTxt, NormalResult.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Clicking = false;
                        if (model != null )
                        {
                            if(model.result ==  true) {
                                holdDialog(dialog, true);
                                Toast.makeText(getApplicationContext(), String.format("修改卷料成功!"), Toast.LENGTH_LONG).show();
                                holdDialog(dialog, true);
                                dialog.dismiss();
                            }
                            else{
                                holdDialog(dialog, false);
                                Toast.makeText(getApplicationContext(),String.format("修改卷料失败:"+ model.msg), Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            holdDialog(dialog, false);
                            Toast.makeText(getApplicationContext(),String.format("修改卷料失败:"), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    void editChangeListen(final EditText edit_Z_core_kg ,final EditText edit_Z_kg, final EditText edit_qty1){
       edit_Z_kg.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {            }
           @Override
           public void afterTextChanged(Editable s) {
               Double Z_kg = JK_SOActivity.getDouble(edit_Z_kg.getText().toString());
               Double Z_core_kg = JK_SOActivity.getDouble( edit_Z_core_kg.getText().toString());

               Double qty1 = Math.round((Z_kg - Z_core_kg) *100.00)/100.00;
               edit_qty1.setText(qty1 + "");
           }
       });

       edit_Z_core_kg.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {            }
           @Override
           public void afterTextChanged(Editable s) {
               Double Z_kg = JK_SOActivity.getDouble(edit_Z_kg.getText().toString());
               Double Z_core_kg = JK_SOActivity.getDouble( edit_Z_core_kg.getText().toString());

               Double qty1 = Math.round((Z_kg - Z_core_kg) *100.00)/100.00;
               edit_qty1.setText(qty1 + "");
           }
       });
   }


    void holdDialog(DialogInterface dialog, boolean res){
        try
        {
            Field field = dialog.getClass().getSuperclass().getDeclaredField( "mShowing" );
            field.setAccessible( true );
            field.set( dialog,
                    res ); // false - 使之不能关闭(此为机关所在，其它语句相同)
        }
        catch ( Exception e )
        {
            // Log.e( e.getMessage() );
            e.printStackTrace();
        }
    }



    void LoadData()
    {
//        string machine = Request["machine"];
//        string Z_work_no = Request["Z_work_no"];
//        string FD_width = Request["FD_width"];
//        string prd_no = Request["prd_no"];
//        string wh_no = Request["wh_no"];
//        string sal_no = Request["sal_no"];
//        string only_no = Request["only_no"];
//        string table_dd = Request["table_dd"];

        //机台, 成品 或 退料？ machine GetNotFinishBack GetNotFinishJK
        boolean is_JK = edit_show_jk.isChecked();
        String prd_no = edit_prd_no.getText().toString();
        String z_work_no = edit_Z_work_no.getText().toString();
        String so_no = edit_so_no.getText().toString();
        String sal_no = edit_sal_no.getText().toString();
        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        if(is_JK == true)
            paramsMap.put("action","GetNotFinishJK");
        else
            paramsMap.put("action","GetNotFinishBack");

        paramsMap.put("Z_work_no", z_work_no);
        paramsMap.put("machine", machineNumber.name);
        paramsMap.put("so_no", so_no);
        paramsMap.put("prd_no", prd_no);
        paramsMap.put("wh_no", "");
        paramsMap.put("sal_no", sal_no);
        paramsMap.put("only_no", "");
        paramsMap.put("table_dd", "");

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            Log.d("test", "key="+key+", val="+paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJOB))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载卷料出错"), Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                //Log.d("gson", respTxt);
                try {
                    final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.removeAll();
                            if (onlys != null && onlys.length>0)
                            {
                                for(int i = 0; i<onlys.length; ++i) {
                                    adapter.add(onlys[i]);
                                }
                            }
                        }
                    });
                }
                catch ( Exception e){
                    Toast.makeText(getApplicationContext(),String.format("加载卷料出错"), Toast.LENGTH_LONG);
                }



            }
        });
    }


    void doDelete(){

        OnlyNoItem only = adapter.getSelectFirst();
        final String only_no = only.only_no;

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","DeleteJL");
        paramsMap.put("only_no", only.only_no);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            Log.d("test", "key="+key+", val="+paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJOB))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("删除卷料出错"), Toast.LENGTH_LONG);
                        LoadData();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                Log.d("gson", respTxt);
                final NormalResult model = gson.fromJson(respTxt, NormalResult.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(model.result == true) {
                            Toast.makeText(getApplicationContext(),String.format("成功删除卷料["+ only_no + "]"), Toast.LENGTH_LONG);
                            LoadData();
                        }
                        else
                            Toast.makeText(getApplicationContext(),model.msg, Toast.LENGTH_LONG);
                    }
                });
            }
        });

    }

    //提示 超缴库 确认
    //, android.content.DialogInterface.OnClickListener callback2
    protected void beforeDelete(android.content.DialogInterface.OnClickListener callback) {

        OnlyNoItem only = adapter.getSelectFirst();
        if(only == null){
            Toast.makeText(getApplicationContext(),String.format("请先选择卷料 "), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ShowHistoryOnlyActivity.this);
        builder.setMessage("确定删除吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认",
                callback);

        builder.setNegativeButton("取消",new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),String.format("选  取消 "), Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }






    /* tableType : JLBACK 或 JLIN */
    public  void loadPrintorModels(final String tableType, final  Spinner edit_print){

        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetPrintFields");
        paramsMap.put("table_type",tableType);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_HostPrint))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载模版出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final PrintorModel[] models = gson.fromJson(respTxt, PrintorModel[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (models != null )
                        {
                            ArrayAdapter<PrintorModel> spinnerAdapter = new ArrayAdapter<PrintorModel>(getApplicationContext(),
                                    R.layout.support_simple_spinner_dropdown_item, models);

                            edit_print.setAdapter(spinnerAdapter);
                        }
                    }
                });
            }
        });
    }


    /*
        取指定PC的打印机 //http://192.168.43.117:8223/StarService?action=GetPrinter
    *  */
    public  void loadPrintotr(final Spinner edit_printor){
        FormBody.Builder builder = new FormBody.Builder();
        Request request = new Request.Builder()
                .url( WebPrintorApi.getRealUrl("?action=GetPrinter"))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), String.format("加载打印机出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final PrintorModel[] models = gson.fromJson(respTxt, PrintorModel[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), respTxt, Toast.LENGTH_LONG).show();
                        if (models != null )
                        {
                            ArrayAdapter<PrintorModel> spinnerAdapter2 = new ArrayAdapter<PrintorModel>(getApplicationContext(),
                                    R.layout.support_simple_spinner_dropdown_item, models);
                            edit_printor.setAdapter(spinnerAdapter2);
                        }
                    }
                });
            }
        });
    }



    private  void setMachines(){
        WHInfoModel[] machineItems = new WHInfoModel[8];//{"A1","A2","A3","A4","A5","A6", "A7", "A8"};
        machineItems[0] = new WHInfoModel();machineItems[0].wh_no = "A1"; machineItems[0].name = "A1";
        machineItems[1] = new WHInfoModel();machineItems[1].wh_no = "A2"; machineItems[1].name = "A2";
        machineItems[2] = new WHInfoModel();machineItems[2].wh_no = "A3"; machineItems[2].name = "A3";
        machineItems[3] = new WHInfoModel();machineItems[3].wh_no = "A4"; machineItems[3].name = "A4";
        machineItems[4] = new WHInfoModel();machineItems[4].wh_no = "A5"; machineItems[4].name = "A5";
        machineItems[5] = new WHInfoModel();machineItems[5].wh_no = "A6"; machineItems[5].name = "A6";
        machineItems[6] = new WHInfoModel();machineItems[6].wh_no = "A7"; machineItems[6].name = "A7";
        machineItems[7] = new WHInfoModel();machineItems[7].wh_no = "A8"; machineItems[6].name = "A8";


        spinnerMachineAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, machineItems);
        edit_machine.setAdapter(spinnerMachineAdapter);
    }

    private void setSpinnerWHValue(Spinner spinner, String value) {
        SpinnerAdapter apsAdapter = spinner.getAdapter();
        int size = apsAdapter.getCount();
        for (int i = 0; i < size; i++) {
            //if(apsAdapter.getItem(i).equals(value)){
            WHInfoModel item = (WHInfoModel)apsAdapter.getItem(i);
            if (TextUtils.equals(value, item.wh_no)) {
                spinner.setSelection(i,true);
                break;
            }
        }
    }
}
