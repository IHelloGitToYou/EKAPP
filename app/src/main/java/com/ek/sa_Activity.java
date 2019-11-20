package com.ek;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.ek.Controls.ExampleListView;
import com.ek.adapter.Sa_SelectAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.Sa_SelectModel;
import com.ek.model.Sa_TableBody;
import com.ek.model.Sa_TableHead;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class sa_Activity extends AppCompatActivity implements View.OnClickListener{
    ExampleListView listView;
    Sa_SelectAdapter adapter;
    Switch edit_switch;
    EditText edit_sa_no;
    EditText edit_Z_plateOrOnly;
    EditText edit_show_qty;
    OkHttpClient client;
    Button Btn_Submit;
    String LastSA_NO = "";
    Boolean LastSA_HadJL = false;
    Sa_TableBody[] currentSABody;
    OnlyNoItem[] currentSABodyJLs;
    private  static  String lastNo;
    boolean IS_LOADING = false;
    boolean IS_LOADING_JL = false;
    boolean IS_SubmitZPlateIng = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sa);
        edit_switch = findViewById(R.id.edit_switch);
        edit_show_qty = findViewById(R.id.edit_show_qty);
        edit_sa_no = findViewById(R.id.edit_sa_no);
        edit_Z_plateOrOnly = findViewById(R.id.edit_Z_plateOrOnly);
        Btn_Submit = findViewById(R.id.Btn_Submit);
        listView = findViewById(R.id.listView);
        client = new OkHttpClient();




        edit_show_qty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });
        edit_show_qty.setInputType(InputType.TYPE_NULL);//禁止输入法


        edit_Z_plateOrOnly.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if( IS_LOADING == true )
                        return false;
                    if (edit_Z_plateOrOnly.getText().toString().isEmpty())
                        return false;

                    String so_no = edit_Z_plateOrOnly.getText().toString();
                    if(so_no.isEmpty()){
                        clear();
                        remember();
                        return false;
                    }


                    if(LastSA_NO.isEmpty()){
                        Toast.makeText(getApplicationContext(),String.format("[销售发货单]请先输入"), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    boolean isScanPlate = edit_switch.isChecked();

                    //无扫过卷的，查后台
                    if(LastSA_HadJL == false) {
                        if (isScanPlate == true)
                            loadZPlate(edit_Z_plateOrOnly.getText().toString());
                        else
                            loadJLFormAPI(edit_Z_plateOrOnly.getText().toString());
                    }
                    else{
                        if (isScanPlate == true) {
                            loadZPlate(edit_Z_plateOrOnly.getText().toString());
                        }
                        else {
                            loadJLFormCache();
                            return  true;
                        }
                    }
                }
                return false;
            }
        });


        edit_sa_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (edit_sa_no.getText().toString().isEmpty()) {
                        return false;
                    }
                    if( IS_LOADING == true || IS_LOADING_JL == true)
                        return false;

                    String sa_no = edit_sa_no.getText().toString();
                    if(LastSA_NO.equals( sa_no)) {
                        Toast.makeText(getApplicationContext(),String.format("[相同的发货单，不重新加载]"), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    loadSAHead();
                }

                return false;
            }
        });

        listView.setAdapter(adapter = new Sa_SelectAdapter(new ArrayList<Sa_SelectModel>()));


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showMenu((Sa_SelectModel) adapter.getItem(position));
                return false;
            }
        });

        Btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSubmiting == true)
                    return;

                submit();
            }
        });

        getRemember();
        focusToSAEditText();


        edit_Z_plateOrOnly.setFocusable(true);
        edit_Z_plateOrOnly.setSelectAllOnFocus(true);


       // ExampleListView listView = (ExampleListView) View.findViewById(R.id.practice_exercises_list);
        listView.setListener(new ExampleListView.ListViewListener() {
            @Override
            public void onChangeFinished() {
                //txtCurrentFocus.requestFocus();
                focusToEditText();
            }
        });
    }

    Boolean isSubmiting = false;

    public  void clear(){
        //edit_sa_no.setText("");
        edit_Z_plateOrOnly.setText("");
        LastSA_NO = "";
        LastSA_HadJL = false;
        adapter.removeAll();

        focusToSAEditText();
        remember();
    }

    public void submit(){
        if(LastSA_NO.isEmpty()){
            Toast.makeText(getApplicationContext(),String.format("[销售发货单]请先输入"), Toast.LENGTH_LONG).show();
            isSubmiting = false;
            return;
        }

        if(lastScanQty > 0) {
            Toast.makeText(getApplicationContext(),String.format("[扫描未完成]"), Toast.LENGTH_LONG).show();
            isSubmiting = false;
            return;
        }
        if(lastScanQty < 0) {
            Toast.makeText(getApplicationContext(),String.format("[扫描超过销货发货量]"), Toast.LENGTH_LONG).show();
            isSubmiting = false;
            return;
        }

        //提交什么？
        //sa_no + jl.list?
        String only_nos = "";
        List<String> allOnlys = adapter.getAllOnlyNo();
        for (int i = 0; i <allOnlys.size() ; i++) {
            only_nos = only_nos + allOnlys.get(i) +",";
        }

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        if(LastSA_HadJL == true)
            paramsMap.put("action",  "SCAN_JL_CHECK_BY_PDA");
        else
            paramsMap.put("action",  "SCAN_JL_BY_PDA");

        paramsMap.put("sa_id","SO3");
        paramsMap.put("sa_no", LastSA_NO );

        paramsMap.put("only_nos",only_nos);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKSA))
                .post(builder.build())
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                IS_LOADING = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("提交出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                IS_LOADING = false;
                final String respTxt =  response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson2 = new Gson();
                        final NormalResult result = gson2.fromJson(respTxt, NormalResult.class);
                        if(result.result == true){
                            focusToSAEditText();
                            Toast.makeText(getApplicationContext(), "提交“销售托盘”成功:", Toast.LENGTH_LONG).show();

                            clear();
                            remember();
                            edit_sa_no.setText("");
                        }
                        else{
                            focusToSAEditText();
                            Toast.makeText(getApplicationContext(), "提交“销售托盘”失败:" + result.msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });

    }


    public void loadSAHead(){
        clear();

        IS_LOADING = true;

        final String sa_no = edit_sa_no.getText().toString();
        if(sa_no.isEmpty()){
            Toast.makeText(getApplicationContext(),String.format("[销售发货单号]请先输入"), Toast.LENGTH_LONG).show();
            focusToSAEditText();
            return;
        }

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetTableHead");
        paramsMap.put("sa_id","SO3");
        paramsMap.put("sa_no", sa_no );
        paramsMap.put("CheckHasJL","1112要检查有无JL233");


        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKSA))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                IS_LOADING = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载[销售发货单信息]出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                IS_LOADING = false;
                final String respTxt =  response.body().string();
                Log.d("abc", respTxt);
                Gson gson = new Gson();
                final Sa_TableHead[] list = gson.fromJson(respTxt, Sa_TableHead[].class);
                if (list != null )
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( list.length > 0){
                                if( list[0].table_state.equals("1") == false) {
                                    Toast.makeText(getApplicationContext(), "[销售发货单]单据已作废" , Toast.LENGTH_SHORT).show();
                                    focusToSAEditText();
                                    return;
                                }

                                if( list[0].check_state.equals("1") == false) {
                                    Toast.makeText(getApplicationContext(), "[销售发货单]必须是未提交状态" , Toast.LENGTH_SHORT).show();
                                    focusToSAEditText();
                                    return;
                                }

                                LastSA_NO = list[0].sa_no;
                                //Toast.makeText(getApplicationContext(), String.format("加载[销售发货单信息][%s]成功!", sa_no), Toast.LENGTH_LONG).show();
                                loadSABody();

                                if(list[0].is_had_jl == 1) {
                                    LastSA_HadJL = true;
                                    loadSAJLs();
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "加载[销售发货单信息]出错" , Toast.LENGTH_SHORT).show();
                                focusToSAEditText();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "加载[销售发货单信息]出错" , Toast.LENGTH_SHORT).show();
                    focusToSAEditText();
                    LastSA_NO = "";
                }
            }
        });
        //LastSA_NO = sa_no;
    }

    public void loadSABody(){
        IS_LOADING = true;
        final String sa_no = edit_sa_no.getText().toString();
        if(sa_no.isEmpty()){
            Toast.makeText(getApplicationContext(),String.format("[销售发货单号]请先输入"), Toast.LENGTH_LONG).show();
            focusToSAEditText();
            IS_LOADING = false;
            return;
        }

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetTableBody");
        paramsMap.put("sa_id","SO3");
        paramsMap.put("sa_no", sa_no );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKSA))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                IS_LOADING = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LastSA_NO = "";
                        Toast.makeText(getApplicationContext(),String.format("加载[销售发货单信息]出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                IS_LOADING = false;
                final String respTxt =  response.body().string();
                //Log.d("abc", respTxt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        final Sa_TableBody[] list = gson.fromJson(respTxt, Sa_TableBody[].class);
                        if (list != null )
                        {
                            if( list.length > 0){
                                currentSABody = list;
                                Toast.makeText(getApplicationContext(), String.format("加载[销售发货单][%s]成功!", sa_no), Toast.LENGTH_LONG).show();
                                focusToEditText();

                                RefreshScanOKQty();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "加载[销售发货单]出错" , Toast.LENGTH_SHORT).show();
                                focusToSAEditText();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "加载[销售发货单]出错" , Toast.LENGTH_SHORT).show();
                            focusToSAEditText();
                            LastSA_NO = "";
                            currentSABody = null;
                        }
                    }
                });
            }
        });
    }


    public void loadSAJLs(){
        IS_LOADING_JL = true;
        final String sa_no = edit_sa_no.getText().toString();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetTableBodyJL");
        paramsMap.put("sa_id","SO3");
        paramsMap.put("sa_no", sa_no );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }

        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKSA))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                IS_LOADING_JL = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LastSA_NO = "";
                        Toast.makeText(getApplicationContext(),String.format("出错加载[销售发料已扫卷料]"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                IS_LOADING_JL = false;
                final String respTxt =  response.body().string();
                //Log.d("abc", respTxt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        final OnlyNoItem[] list = gson.fromJson(respTxt, OnlyNoItem[].class);
                        if (list != null )
                        {
                            if( list.length > 0){
                                currentSABodyJLs = list;
                                //Toast.makeText(getApplicationContext(), String.format("加载[销售发料已扫卷料][%s]成功!", sa_no), Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(), "加载[销售发料已扫卷料]成功" + list.length, Toast.LENGTH_SHORT).show();
                                focusToEditText();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "出错加载[销售发料已扫卷料]" , Toast.LENGTH_SHORT).show();
                                focusToSAEditText();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "加载[销售发料已扫卷料]出错" , Toast.LENGTH_SHORT).show();
                            focusToSAEditText();
                            LastSA_NO = "";
                            currentSABodyJLs = null;
                        }
                    }
                });
            }
        });
    }


    Integer lastScanQty = 0;
//    boolean IsScanFinish = false;
    public void RefreshScanOKQty(){
        Integer allQty = 0;
        Integer okSacnQty = 0;

        for (int i = 0; i < currentSABody.length; i++) {
            allQty = allQty + currentSABody[i].qty;
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            okSacnQty = okSacnQty + ((Sa_SelectModel)adapter.getItem(i)).valid_qty;
        }

        edit_show_qty.setText(String.format("  %1s / %2s", okSacnQty.toString(), allQty.toString()));

        lastScanQty = allQty - okSacnQty;
//        if(okSacnQty.equals(allQty))
//            IsScanFinish = true;
//        else
//            IsScanFinish = false;

    }

    public boolean CheckSaSelectModel(Sa_SelectModel model){

        for (int i = 0; i < model.jls.size(); i++) {
            OnlyNoItem only = (OnlyNoItem)model.jls.get(i);
            if( adapter.hasOnly(only.only_no) == true){
                Toast.makeText(getApplicationContext(),String.format("卷数[%s]重复扫描", only.only_no), Toast.LENGTH_LONG).show();
                return false;
            }

            if(only.lock_table_no.isEmpty() || only.lock_table_itm.isEmpty()){
                Toast.makeText(getApplicationContext(),String.format("卷数[%s}]没有所属订单信息",only.only_no), Toast.LENGTH_LONG).show();
                //focusToEditText();
                return false;
            }

            if(LastSA_HadJL == false && only.state.equals("1") == false ) {
                Toast.makeText(getApplicationContext(),String.format("卷料[%s][库存状态]非在库", only.only_no), Toast.LENGTH_LONG).show();
                //focusToEditText();
                return false;
            }

            Boolean hited = false;
            for (int j = 0; j < currentSABody.length; j++) {
                Sa_TableBody body = currentSABody[j];

                //Log.d("TAG", "CheckSaSelectModel: "+only.lock_table_itm+"|"+body.so_itm);
                if(body.so_no.equals(only.lock_table_no) && body.so_itm.equals(only.lock_table_itm)){
                    hited = true;
                    break;
                }
            }

            if(hited == false){
                Toast.makeText(getApplicationContext(),String.format("卷数[%s}]超出订单范围",only.only_no), Toast.LENGTH_LONG).show();
                return false;
            }

            model.valid_qty += only.qty;
            //检测卷料有无占用信息
            //缴库数量有无超？
        }

        return true;
    }

    public void loadZPlate(final String Z_plate){

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action", "GetOnlyByZ_Plate");
        paramsMap.put("Z_plate", Z_plate);
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_EKJK))
                .post(builder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载托盘卷料出错"), Toast.LENGTH_LONG).show();
                        focusToEditText();
                        IS_LOADING = false;
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                IS_LOADING = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String temp = "[复核]";
                        if(LastSA_HadJL == false)
                            temp = "";

                        if (onlys != null && onlys.length > 0) {
                            Sa_SelectModel newRec = new Sa_SelectModel();
                            newRec.type = "PLATE";
                            newRec.no = Z_plate;
                            newRec.jls = new ArrayList<OnlyNoItem>();

                            for (int i = 0; i < onlys.length; i++) {
                                if (LastSA_HadJL == true) {
                                    OnlyNoItem cacheJL = findJLOnScaned(onlys[i].only_no);
                                    if(cacheJL == null){
                                        Toast.makeText(getApplicationContext(),String.format("[复核]卷料[%s]不属于销售发货单!", onlys[i].only_no), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    //如果卷料是集合卷，发货Qty = 0的，所以要从 销售发货卷料中复制出来
                                    if (onlys[i].is_multi.equals("T") || onlys[i].is_multi.equals("t")) {
                                        onlys[i].qty = cacheJL.qty;
                                        onlys[i].qty1 = cacheJL.qty1;
                                    }
                                }
                                newRec.jls.add(onlys[i]);
                            }

                            if(CheckSaSelectModel(newRec) == false){
                                focusToEditText();
                                return;
                            }

                            adapter.insert(newRec);
                            RefreshScanOKQty();
                            edit_Z_plateOrOnly.setText("");
                            focusToEditText();
                            Toast.makeText(getApplicationContext(),String.format("%s2扫描托盘%1s卷料成功!", Z_plate, temp), Toast.LENGTH_LONG).show();
                            remember();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("%s2扫描托盘%1s失败!", Z_plate, temp), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    public  void loadJLFormAPI(final String only_no) {
        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetJL");
        paramsMap.put("only_no",only_no);
        paramsMap.put("wh_no", "" );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_PRDTONLY))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载卷料出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final OnlyNoItem[] onlys = gson.fromJson(respTxt, OnlyNoItem[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onlys != null && onlys.length>0)
                        {
                            if(onlys[0].state.equals("1") == false ) {
                                Toast.makeText(getApplicationContext(),String.format("卷料[库存状态]非在库", only_no), Toast.LENGTH_LONG).show();
                                focusToEditText();
                            }
                            else{
                                //Toast.makeText(getApplicationContext(),String.format("卷料[%s]扫描成功!", only_no ), Toast.LENGTH_LONG).show();
                                Sa_SelectModel newRec = new Sa_SelectModel();
                                newRec.type = "JL";
                                newRec.no = only_no;
                                newRec.jls = new ArrayList<OnlyNoItem>();
                                for (int i = 0; i < onlys.length; i++) {
                                    newRec.jls.add(onlys[i]);
                                }

                                if(CheckSaSelectModel(newRec) == false){
                                    focusToEditText();
                                    return;
                                }

                                adapter.add(newRec);
                                RefreshScanOKQty();
                                edit_Z_plateOrOnly.setText("");
                                focusToEditText();
                                Toast.makeText(getApplicationContext(),String.format("扫描卷料[%s]成功!", only_no), Toast.LENGTH_LONG).show();
                                remember();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("卷料[%s]不存在", only_no), Toast.LENGTH_LONG).show();
                        }
                        focusToEditText();
                    }
                });
            }
        });
    }

    public void loadJLFormCache(){

        String only_no = edit_Z_plateOrOnly.getText().toString();
        OnlyNoItem cacheJL = findJLOnScaned(only_no);
        if(cacheJL == null){
            Toast.makeText(getApplicationContext(),String.format("[复核]卷料[%s]不属于销售发货单!", only_no), Toast.LENGTH_LONG).show();
            return ;
        }
        else{
            Sa_SelectModel newRec = new Sa_SelectModel();
            newRec.type = "JL";
            newRec.no = only_no;
            newRec.jls = new ArrayList<OnlyNoItem>();
            newRec.jls.add(cacheJL);

            if(CheckSaSelectModel(newRec) == false){
                focusToEditText();
                return;
            }

            adapter.insert(newRec);
            RefreshScanOKQty();
            edit_Z_plateOrOnly.setText("");
            Toast.makeText(getApplicationContext(),String.format("[复核]扫描卷料[%s]成功!", only_no), Toast.LENGTH_LONG).show();
            remember();

            focusToEditText();
        }
    }

    //在SA单已扫描的卷料中 查找 卷料
    public OnlyNoItem findJLOnScaned(final String only_no) {
        for (int i = 0; i < currentSABodyJLs.length; i++) {
            //Log.d("findJLOnScaned", currentSABodyJLs[i].only_no);
            if(currentSABodyJLs[i].only_no.equals(only_no))
                return  currentSABodyJLs[i];
        }

        return  null;
    }

    public void showMenu(final Sa_SelectModel item){
        String[] menuText = {"移出"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(menuText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    AlertDialog.Builder bulDelete = new AlertDialog.Builder(sa_Activity.this);
                    bulDelete.setTitle("询问");
                    bulDelete.setMessage(String.format(String.format("确定移出[%s]吗？", item.no)));

                    bulDelete.setPositiveButton("移出吧", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(item);
                        dialog.dismiss();
                        RefreshScanOKQty();

                        remember();
                        }
                    });

                    bulDelete.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        }
                    });
                    bulDelete.show();
                }
                arg0.dismiss();
            }
        });
        dialog.show();
    }


    public void remember(){
        Gson gson = new Gson();
        SharedPreferences sp = getSharedPreferences("SA_ACTIVITY", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        if(adapter.getCount() == 0 || currentSABody == null || currentSABody.length <= 0) {
            spEditor.clear();
        }
        else {
            spEditor.putString("SA_NO", edit_sa_no.getText().toString());
            if(LastSA_HadJL == true) {
                spEditor.putString("LastSA_HadJL", "1");
                spEditor.putString("currentSABodyJL", gson.toJson(currentSABodyJLs));
            }
            else
                spEditor.putString("LastSA_HadJL", "0");


            spEditor.putString("currentSABody", gson.toJson(currentSABody));
            spEditor.putString("adapter", gson.toJson(adapter.getList()));
            //Sa_TableBody[] currentSABody;
        }

        spEditor.commit();
    }


    public  void getRemember(){
        Gson gson = new Gson();
        SharedPreferences sp = getSharedPreferences("SA_ACTIVITY", MODE_PRIVATE);

        String sa_no = sp.getString("SA_NO", "");
        String lastSA_HadJL = sp.getString("LastSA_HadJL", "0");
        String adapterJson = sp.getString("adapter", "");
        String currentSABodyJson = sp.getString("currentSABody", "");
//        Log.d("abccc", "getRemember: sa_no"+sa_no);
//        Log.d("abccc", "getRemember: adapterJson"+adapterJson);
//        Log.d("abccc", "getRemember: currentSABodyJson"+currentSABodyJson);
        if(sa_no.isEmpty() || adapterJson.isEmpty())
            return;
        edit_sa_no.setText(sa_no);
        LastSA_NO = sa_no;
        LastSA_HadJL = lastSA_HadJL.equals("1");

        if(LastSA_HadJL == true) {
            String currentSABodyJLJson = sp.getString("currentSABodyJL", "");
            OnlyNoItem[] list1 = new Gson().fromJson(currentSABodyJLJson, OnlyNoItem[].class);
            currentSABodyJLs = list1;
        }

        Sa_SelectModel[] list1 = new Gson().fromJson(adapterJson,Sa_SelectModel[].class);
        List<Sa_SelectModel> list1_2 = Arrays.asList(list1);
        for (int i = 0; i < list1_2.size(); i++) {
            adapter.add(list1_2.get(i));
        }

        currentSABody = new Gson().fromJson(currentSABodyJson,Sa_TableBody[].class);
        RefreshScanOKQty();
    }

    public void focusToEditText(){
        Log.d("focusToEditText", "开始");

        edit_Z_plateOrOnly.requestFocus();
        Log.d("focusToEditText", "结束");
    }

    public void focusToSAEditText(){
        edit_sa_no.setSelectAllOnFocus(true);
        edit_sa_no.requestFocus();
    }

    Boolean Clicking = false;
    @Override
    public void onClick(View v) {
        if (Clicking.equals(true))
            return;
        int viewId = v.getId();
        switch (viewId) {
            case R.id.Btn_OutSubmit:

                break;
        }
    }


}
