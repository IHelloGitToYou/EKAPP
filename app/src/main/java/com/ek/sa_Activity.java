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

import com.ek.adapter.Sa_SelectAdapter;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.Sa_SelectModel;
import com.ek.model.Sa_TableBody;
import com.ek.model.Sa_TableHead;
import com.ek.model.SelectSoLineModel;
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
    ListView listView;
    Sa_SelectAdapter adapter;
    Switch edit_switch;
    EditText edit_sa_no;
    EditText edit_Z_plateOrOnly;
    EditText edit_show_qty;
    OkHttpClient client;
    Button Btn_Submit;
    String LastSA_NO = "";
    Sa_TableBody[] currentSABody;
    private  static  String lastNo;
    boolean IS_LOADING = false;
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

                    if(LastSA_NO.isEmpty()){
                        Toast.makeText(getApplicationContext(),String.format("[销售发货单]请先输入"), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    boolean isScanPlate = edit_switch.isChecked();
                    if(isScanPlate == true)
                        loadZPlate(edit_Z_plateOrOnly.getText().toString() );
                    else
                        loadJL(edit_Z_plateOrOnly.getText().toString());
                }
                return false;
            }
        });


        edit_sa_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (edit_sa_no.getText().toString().isEmpty()) {
                        //adapter.removeAll();
                        return false;
                    }

                    String sa_no = edit_sa_no.getText().toString();
                    if(LastSA_NO == sa_no)
                        return false;
                    if( IS_LOADING == true )
                        return false;

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
    }

    Boolean isSubmiting = false;

    public  void clear(){
        //edit_sa_no.setText("");
        edit_Z_plateOrOnly.setText("");
        LastSA_NO = "";
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
        paramsMap.put("action","SCAN_JL_BY_PDA");
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
                //Log.d("abc", respTxt);
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
                return false;
            }

            if(only.state.equals("1") == false ) {
                Toast.makeText(getApplicationContext(),String.format("卷料[%s][库存状态]非在库", only.only_no), Toast.LENGTH_LONG).show();
                focusToEditText();
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

                        if (onlys != null && onlys.length > 0) {
                            Sa_SelectModel newRec = new Sa_SelectModel();
                            newRec.type = "PLATE";
                            newRec.no = Z_plate;
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
                            Toast.makeText(getApplicationContext(),String.format("扫描托盘[%s]卷料成功!", Z_plate), Toast.LENGTH_LONG).show();
                            remember();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("扫描托盘[%s]失败!", Z_plate), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    public  void loadJL(final String only_no) {
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
        if(currentSABody == null || currentSABody.length <=0) {
            spEditor.clear();
        }
        else {
            spEditor.putString("SA_NO", edit_sa_no.getText().toString());
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
        String adapterJson = sp.getString("adapter", "");
        String currentSABodyJson = sp.getString("currentSABody", "");
//        Log.d("abccc", "getRemember: sa_no"+sa_no);
//        Log.d("abccc", "getRemember: adapterJson"+adapterJson);
//        Log.d("abccc", "getRemember: currentSABodyJson"+currentSABodyJson);
        if(sa_no.isEmpty() || adapterJson.isEmpty())
            return;
        edit_sa_no.setText(sa_no);
        LastSA_NO = sa_no;

        Sa_SelectModel[] list1 = new Gson().fromJson(adapterJson,Sa_SelectModel[].class);
        List<Sa_SelectModel> list1_2 = Arrays.asList(list1);
        for (int i = 0; i < list1_2.size(); i++) {
            adapter.add(list1_2.get(i));
        }

        currentSABody = new Gson().fromJson(currentSABodyJson,Sa_TableBody[].class);
        RefreshScanOKQty();
    }

    public void focusToEditText(){
        edit_Z_plateOrOnly.setSelectAllOnFocus(true);
        edit_Z_plateOrOnly.requestFocus();
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
