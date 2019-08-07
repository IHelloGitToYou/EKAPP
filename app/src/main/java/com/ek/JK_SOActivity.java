package com.ek;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ek.adapter.HistoryJLAdapter;
import com.ek.adapter.JK_solineAdapter;
import com.ek.model.HistoryJLModel;
import com.ek.model.NormalResult;
import com.ek.model.OnlyNoItem;
import com.ek.model.PrintorModel;
import com.ek.model.SelectSoLineModel;
import com.ek.model.WHInfoModel;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;

import java.io.IOException;
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

public class JK_SOActivity extends AppCompatActivity implements View.OnClickListener{
    JK_solineAdapter adapter;
    HistoryJLAdapter historyJLAdapter;
    ListView listView,listHistoryJL;

    EditText  edit_Z_top_no, edit_Z_hou3,edit_only_no,edit_prd_no_header;

    EditText edit_Z_work_no,edit_prd_no,edit_prd_no_bottom,edit_Z_hou3_bottom,edit_FD_core,edit_Z_core_kg,edit_qty, edit_qty1,edit_Z_kg,edit_Z_iface,edit_only_rem;
    EditText FD_width, FD_length;
    TextView edit_BZInfo;

    CheckBox edit_is_multi;
    Spinner edit_machine, edit_wh_no, edit_print_jk, edit_print_back ,edit_printor_jk, edit_printor_back;
    OkHttpClient client;

    ArrayAdapter<WHInfoModel> spinnerWHAdapter;
    ArrayAdapter<WHInfoModel> spinnerMachineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jk_so);

        client = new OkHttpClient();

        listView = findViewById(R.id.listViewSoItem);
        listHistoryJL = findViewById(R.id.listHistoryJL);
        edit_Z_top_no = findViewById(R.id.edit_Z_top_no);
        //edit_Z_hou3 = findViewById(R.id.edit_Z_hou3);
        edit_only_no = findViewById(R.id.edit_only_no);
        edit_prd_no_header = findViewById(R.id.edit_prd_no);

        edit_machine = findViewById(R.id.edit_machine);
        edit_wh_no = findViewById(R.id.edit_wh_no);
        edit_print_jk = findViewById(R.id.edit_print_jk);
        edit_print_back = findViewById(R.id.edit_print_back);
        edit_printor_jk = findViewById(R.id.edit_printor_jk);
        edit_printor_back = findViewById(R.id.edit_printor_back);

        findViewById(R.id.JK_SO_BtnAdd).setOnClickListener(this);
        findViewById(R.id.Do_JK).setOnClickListener(this);
        findViewById(R.id.Do_BACK_WH4).setOnClickListener(this);
        findViewById(R.id.Do_BACK_WH5).setOnClickListener(this);
        findViewById(R.id.Show_HistoryJL).setOnClickListener(this);

        findViewById(R.id.JK_SO_BtnAdd).setOnClickListener(this);
        adapter = new JK_solineAdapter(new ArrayList<SelectSoLineModel>());
        historyJLAdapter = new HistoryJLAdapter(new ArrayList<HistoryJLModel>());

        listView.setAdapter(adapter);
        listHistoryJL.setAdapter(historyJLAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("test", "onItemClick");
                SelectSoLineModel line = (SelectSoLineModel)adapter.getItem(position);

                adapter.ClearSelected();
                line.isSelected = !line.isSelected;

                //Load To Form
                edit_Z_work_no.setText(line.Z_work_no);
                edit_prd_no_bottom.setText(line.prd_no);
                edit_Z_hou3_bottom.setText(line.Z_sale_hou3+"");
                edit_FD_core.setText(line.FD_core+"");

                edit_qty.setText("1");
                edit_qty1.setText("");      //净重
                edit_Z_kg.setText("");      //毛重

                edit_is_multi.setChecked(false); //是否集合
                edit_Z_iface.setText("");        //接口

                FD_width.setText(line.FD_width+"");
                FD_length.setText(line.FD_length+"");
                edit_BZInfo.setText(line.Z_bzinfo +"");

                //缴库时缓存字段
                if(line.Z_core_kg != null &&line.Z_core_kg.isNaN() == false)
                    edit_Z_core_kg.setText(line.Z_core_kg + "");
                else
                    edit_Z_core_kg.setText("");
                setSpinnerPrintValue(edit_print_jk , line.Z_print);
                setSpinnerPrintValue(edit_print_back , line.print_back);
                //setSpinnerPrintValue(edit_printor_jk , line.printor_jk);
                //setSpinnerPrintValue(edit_printor_back , line.printor_back);

                setSpinnerWHValue(edit_wh_no, line.wh_no);
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,  int position, long id) {
                final int  position2 = position;
                showDeleteSolineMenu(position2);
                return false;
            }
        });


        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_prd_no_bottom = findViewById(R.id.edit_prd_no_bottom);
        edit_Z_hou3_bottom = findViewById(R.id.edit_Z_hou3_bottom);
        edit_FD_core = findViewById(R.id.edit_FD_core);
        edit_Z_core_kg = findViewById(R.id.edit_Z_core_kg);
        edit_qty = findViewById(R.id.edit_qty);
        edit_qty1 = findViewById(R.id.edit_qty1);
        edit_Z_kg = findViewById(R.id.edit_Z_kg);
        edit_Z_iface = findViewById(R.id.edit_Z_iface);
        edit_only_rem = findViewById(R.id.edit_only_rem);
        edit_is_multi = findViewById(R.id.edit_is_multi);
        FD_width = findViewById(R.id.edit_FD_width);
        FD_length = findViewById(R.id.edit_FD_length);
        edit_BZInfo = findViewById(R.id.edit_BZInfo);

        edit_Z_kg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {            }
            @Override
            public void afterTextChanged(Editable s) {
                Double Z_kg = getDouble(edit_Z_kg.getText().toString());
                Double Z_core_kg = getDouble( edit_Z_core_kg.getText().toString());

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
                Double Z_kg = getDouble(edit_Z_kg.getText().toString());
                Double Z_core_kg = getDouble( edit_Z_core_kg.getText().toString());

                Double qty1 = Math.round((Z_kg - Z_core_kg) *100.00)/100.00;
                edit_qty1.setText(qty1 + "");
            }
        });

        String[] spinnerItems = {"WH1","WH2","WH3","WH4","WH5","WH6"};
        WHInfoModel[] whInos = new WHInfoModel[3];
        whInos[0] = new WHInfoModel();whInos[0].wh_no = "WH4"; whInos[0].name = "半成品仓";
        whInos[1] = new WHInfoModel();whInos[1].wh_no = "WH5"; whInos[1].name = "淋涂仓";
        whInos[2] = new WHInfoModel();whInos[2].wh_no = "WH6"; whInos[2].name = "成品仓";

        //自定义选择填充后的字体样式
        //只能是textview样式，否则报错：ArrayAdapter requires the resource ID to be a TextView
        spinnerWHAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, whInos);
        //这个在不同的Theme下，显示的效果是不同的
        //spinnerAdapter.setDropDownViewTheme(Resources.Theme.LIGHT);
        edit_wh_no.setAdapter(spinnerWHAdapter);

        setMachines();

        loadPrintorModels("JLIN");
        loadPrintorModels("JLBACK");
        loadPrintotr();

        listenOnlyNoChange();

        getSoLineToShareP();
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

        edit_machine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getSoLineToShareP();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {            }
        });
    }

    private void listenOnlyNoChange(){
        edit_only_no.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    //加载母卷信息
                    loadJL(edit_only_no.getText().toString());
                }
                return false;
            }
        });
    }

    private Boolean checkSubmit(){

        if(edit_prd_no_bottom.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"货号不能为空!", Toast.LENGTH_SHORT).show();
            return false;
        };

        if(edit_Z_top_no.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"母卷区淋涂号未输入!", Toast.LENGTH_SHORT).show();
            return false;
        };

        if(edit_is_multi.isChecked() == true && edit_qty.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"集合卷必须输入卷数!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(edit_Z_core_kg.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"纸芯重未输入!", Toast.LENGTH_SHORT).show();
            return false;
        };

        if(edit_Z_kg.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),String.format("毛重未输入!"), Toast.LENGTH_SHORT).show();
            return false;
        };
        if(edit_qty1.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),String.format("净重不能为空!"), Toast.LENGTH_SHORT).show();
            return false;
        };

        if(getDouble(edit_qty1.getText().toString()) <= 0){
            Toast.makeText(getApplicationContext(),String.format("净重不能是负数!"), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(getDouble(edit_qty.getText().toString()) > 1 && edit_is_multi.isChecked() == false){
            Toast.makeText(getApplicationContext(),String.format("卷数不能大于1"), Toast.LENGTH_SHORT).show();
            return false;
        }

        WHInfoModel wh = (WHInfoModel)edit_wh_no.getSelectedItem();
        if(wh == null){
            Toast.makeText(getApplicationContext(),String.format("仓库非选择!"), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //提示 超缴库 确认
    //, android.content.DialogInterface.OnClickListener callback2
    protected void dialogOverFlowQty(android.content.DialogInterface.OnClickListener callback) {

        AlertDialog.Builder builder = new AlertDialog.Builder(JK_SOActivity.this);
        builder.setMessage("确定超缴吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认",
                callback);

        builder.setNegativeButton("取消",new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Clicking = false;
                Toast.makeText(getApplicationContext(),String.format("选  取消 "), Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    private  void doJKBefore(){
        if(checkSubmit() == false) {
            Clicking = false;
            return;
        }

        final SelectSoLineModel line = (SelectSoLineModel) adapter.GetSelecteted();
        if(line == null){
            Clicking = false;
            Toast.makeText(getApplicationContext(),"请先选择订单缴库规格!", Toast.LENGTH_SHORT).show();
            return;
        }

        PrintorModel pModel = (PrintorModel)edit_print_jk.getSelectedItem();
        if(pModel == null || pModel.name.isEmpty()){
            Clicking = false;
            Toast.makeText(getApplicationContext(),"请缴库模版, 未选择!", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.DialogInterface.OnClickListener c1 = new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getGetWallNumberByJLNumberCounter("WH4", true);
            }
        };

        if(line.qty_jk >= line.qty){
            dialogOverFlowQty(c1);
        }
        else{
            getGetWallNumberByJLNumberCounter("WH4", true);
        }
    }


    private void getGetWallNumberByJLNumberCounter(final String wh_no, final Boolean isJK){

        final String Z_top_no = edit_Z_top_no.getText().toString();
        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetWallNumberByJLNumberCounter");
        paramsMap.put("DatePair", Z_top_no);

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
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
                        Clicking = false;
                        Toast.makeText(getApplicationContext(),String.format("取卷流水号出错"), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                final NormalResult model = gson.fromJson(respTxt, NormalResult.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (model != null )
                        {
                            String jk_ZtopNo = fnGetJK_Z_TopNo(Z_top_no);
                            String newNo = "";
                            //Log.d("test", "respTxt="+ respTxt );
                            if (isJK == true) {
                                newNo = jk_ZtopNo + machineNumber.name + fnFillZero(model.data);
                                doJK(newNo);
                            }
                            else{
                                newNo = "B" + jk_ZtopNo + machineNumber.name + fnFillZero(model.data);
                                doBack(wh_no, newNo);
                            }
                        }
                    }
                });
            }
        });
        //{success:true, result: true, msg:'', data:'3'}

    }


    private  HashMap<String,String> getJLParam(){
        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();

        HashMap<String,String> paramsMap = new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","InsertJLRow");
        paramsMap.put("Z_top_no",edit_Z_top_no.getText().toString());
        paramsMap.put("up_only_no","");


        PrintorModel pModel = (PrintorModel)edit_print_jk.getSelectedItem();
        paramsMap.put("signPrinter", pModel.name);
        paramsMap.put("machine", machineNumber.name);
        paramsMap.put("json","[]");
        paramsMap.put("workId","WORK");
        paramsMap.put("workingNo","PDA_2019");

        paramsMap.put("prd_no", edit_prd_no_bottom.getText().toString());
        paramsMap.put("Z_hou3","");
        paramsMap.put("FD_width",  getInt( FD_width.getText().toString()) + "");
        paramsMap.put("FD_length", getInt( FD_length.getText().toString()) + "");
        paramsMap.put("FD_core", getDouble(edit_FD_core.getText().toString()) + "");
        if(paramsMap.get("FD_core").toString().equals("3.0"))
            paramsMap.put("FD_core", "3");
        if(paramsMap.get("FD_core").toString().equals("1.0"))
            paramsMap.put("FD_core", "1");
        if(paramsMap.get("FD_core").toString().equals("6.0"))
            paramsMap.put("FD_core", "6");

        paramsMap.put("Z_core_kg", getDouble( edit_Z_core_kg.getText().toString()) + "");

        if(edit_is_multi.isChecked() == true)
            paramsMap.put("is_multi", "on");

        paramsMap.put("qty", getInt(edit_qty.getText().toString()) + "");
        paramsMap.put("qty1",getDouble( edit_qty1.getText().toString()) + "");

        paramsMap.put("Z_kg",getDouble( edit_Z_kg.getText().toString()) + "");

        WHInfoModel wh = (WHInfoModel)edit_wh_no.getSelectedItem();
        paramsMap.put("wh_no",wh.wh_no);

        paramsMap.put("only_rem",edit_only_rem.getText().toString());
        paramsMap.put("Z_iface",edit_Z_iface.getText().toString());

        paramsMap.put("Z_work_no",edit_Z_work_no.getText().toString());



        return paramsMap;
    }

    private void doJK(final String newNo){

        if(newNo.length() <= 3){
            Toast.makeText(getApplicationContext(),String.format("卷长度少于3异常"), Toast.LENGTH_LONG).show();
            Clicking = false;
            return;
        }

        final SelectSoLineModel line = (SelectSoLineModel) adapter.GetSelecteted();
       //加载变量
        final HashMap<String,String> paramsMap = getJLParam();
        paramsMap.put("type","CKJK");
        paramsMap.put("only_no", newNo);
        paramsMap.put("so_id","SO");
        paramsMap.put("so_no",line.so_no);
        paramsMap.put("itm",line.itm + "");

        paramsMap.put("lock_table_id", "SO");
        paramsMap.put("lock_table_no", line.so_no);
        paramsMap.put("lock_table_itm",line.itm + "");

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
                    public void run() { Toast.makeText(getApplicationContext(),String.format("缴库出错!"), Toast.LENGTH_SHORT).show(); }});
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
                                Toast.makeText(getApplicationContext(), String.format("缴卷成功!" + newNo+' ' + model.qty), Toast.LENGTH_LONG).show();
                                afterDoDk(paramsMap, newNo, model);

                            }
                            else{
                                Toast.makeText(getApplicationContext(),String.format("缴卷失败:"+ model.msg), Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("缴卷失败:"), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private  void afterDoDk(HashMap<String,String> paramsMap, String newNo, NormalResult model){
        rembSoLineToShareP();

        //paramsMap.put("only_rem", LoginActivity.CommonUrlEncode( edit_only_rem.getText().toString())) ;
        //paramsMap.put("Z_iface",LoginActivity.CommonUrlEncode( edit_Z_iface.getText().toString()));
        paramsMap.put("only_rem",edit_only_rem.getText().toString() + WebApi.Utf8_Split);
        paramsMap.put("Z_iface",edit_only_rem.getText().toString() + WebApi.Utf8_Split);

        //更新订单行上的
        final SelectSoLineModel line = (SelectSoLineModel) adapter.GetSelecteted();
        paramsMap.put("Z_hou3", line.Z_sale_hou3 + "");
        line.Z_core_kg = getDouble( edit_Z_core_kg.getText().toString());

        PrintorModel pJKModel = (PrintorModel) edit_print_jk.getSelectedItem();
        PrintorModel pBackModel = (PrintorModel) edit_print_back.getSelectedItem();
        if(pJKModel != null)
            line.Z_print = pJKModel.name;
        if(pBackModel != null)
            line.print_back = pBackModel.name;

        line.qty_jk = model.qty;
        adapter.notifyDataSetChanged();


        //edit_Z_core_kg.setText("");
        edit_qty1.setText("");
        edit_Z_kg.setText("");

        //插入到历史卷区
        String showText = newNo + " " +  paramsMap.get("FD_width") + "*"
                                        + paramsMap.get("FD_length") + "  "
                                        + paramsMap.get("qty1") + "kg";

        HistoryJLModel h =  new HistoryJLModel();
        h.showMsg = showText;
        h.isNew = true;
        historyJLAdapter.add(0, h);


        //打印卷
        PrintorModel printor = (PrintorModel)edit_printor_jk.getSelectedItem();
        PrintorModel printModel  = (PrintorModel)edit_print_jk.getSelectedItem();

        if(printor != null && printor.name.isEmpty() == false && printModel != null && printModel.name.isEmpty() == false){
            doPrintJL(printor.name, printModel.name, paramsMap);
        }
    }



    private  void doBackBefore(final String wh_no, final String newNo){
        if(checkSubmit() == false) {
            Clicking = false;
            return;
        }

        getGetWallNumberByJLNumberCounter(wh_no,false);
    }

    private void doBack(final String wh_no, final String newNo){
        if(newNo.length() <= 3){
            Clicking = false;
            Toast.makeText(getApplicationContext(),String.format("卷长度少于3异常"), Toast.LENGTH_LONG).show();
            return;
        }
        //加载变量
        final HashMap<String,String> paramsMap = getJLParam();
        paramsMap.put("type","CKBACK");
        paramsMap.put("only_no", newNo);
        paramsMap.put("so_id","");
        paramsMap.put("so_no","");
        paramsMap.put("itm", "");

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
                    public void run() { Toast.makeText(getApplicationContext(),String.format("退卷出错!"), Toast.LENGTH_SHORT).show(); }});
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
                                Toast.makeText(getApplicationContext(), String.format("退卷成功!" + newNo), Toast.LENGTH_LONG).show();
                                afterDoBack(paramsMap, newNo, wh_no);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),String.format("退卷失败:"+ model.msg), Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),String.format("退卷失败:"), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    private  void afterDoBack(HashMap<String,String> paramsMap, String newNo, String wh_no){
        rembSoLineToShareP();

        //paramsMap.put("only_rem", LoginActivity.CommonUrlEncode( edit_only_rem.getText().toString()));
        //paramsMap.put("Z_iface",LoginActivity.CommonUrlEncode( edit_Z_iface.getText().toString()));
        paramsMap.put("only_rem",edit_only_rem.getText().toString() + WebApi.Utf8_Split);
        paramsMap.put("Z_iface",edit_only_rem.getText().toString() + WebApi.Utf8_Split);

        //更新订单行上的
        final SelectSoLineModel line = (SelectSoLineModel) adapter.GetSelecteted();
        paramsMap.put("Z_hou3", line.Z_sale_hou3 + "");

        PrintorModel pJKModel = (PrintorModel) edit_print_jk.getSelectedItem();
        PrintorModel pBackModel = (PrintorModel) edit_print_back.getSelectedItem();
        if(pJKModel != null)
            line.Z_print = pJKModel.name;
        if(pBackModel != null)
            line.print_back = pBackModel.name;

        //edit_Z_core_kg.setText("");
        edit_qty1.setText("");
        edit_Z_kg.setText("");

        //插入到历史卷区
        String showFix = "退";
        if(wh_no.equals("WH5"))
            showFix = "淋";

        String showText = showFix + newNo + " " +  paramsMap.get("FD_width") + "*" + paramsMap.get("FD_length") ;
        HistoryJLModel h =  new HistoryJLModel();

        h.showMsg = showText;
        h.isNew = true;
        historyJLAdapter.add(0, h);


        //打印 退料 卷
        PrintorModel printor = (PrintorModel)edit_printor_back.getSelectedItem();
        PrintorModel printModel  = (PrintorModel)edit_print_back.getSelectedItem();

        if(printor != null && printor.name.isEmpty() == false && printModel != null && printModel.name.isEmpty() == false){
            doPrintJL(printor.name, printModel.name, paramsMap);
        }
    }


    Boolean Clicking = false;
    @Override
    public void onClick(View v) {
        if(Clicking.equals(true))
            return;

        int viewId = v.getId();
        switch (viewId){
            case R.id.JK_SO_BtnAdd:
                //login();
                //如何打开一个 弹窗?
                // 如果返回,还是""源来的UI" 所有临时的编辑 都无变过?
                startActivityForResult(new Intent(this, SelectSOItemActivity.class),SelectSOItemActivity.SELECTED_COMPLETE );
                break;
            case R.id.Do_JK:
                    Clicking = true;
                    doJKBefore();
                break;
            case R.id.Do_BACK_WH4:
                    Clicking = true;
                    doBackBefore("WH4","");
                break;
            case R.id.Do_BACK_WH5:
                    Clicking = true;
                    doBackBefore("WH5","");
                 break;
            case R.id.Show_HistoryJL://历史
                //弹出新的UI 加入查询条件？  //显示JLLiveView,
                Intent nIntent = new Intent(this, ShowHistoryOnlyActivity.class);
                final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();
                nIntent.putExtra("defaultMachine", machineNumber.name);
                startActivityForResult(nIntent, ShowHistoryOnlyActivity.SELECTED_COMPLETE );
                // 进行处理
                // 修改
                // 删除
                // 打印
                //doBackBefore("WH5","");
                break;
        }


    }



    protected void loadJL(String onlyNo){
        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action", "GetJL");
        paramsMap.put("only_no", onlyNo);
        paramsMap.put("wh_no", "" );

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
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
                            edit_only_no.setText(onlys[0].only_no);
                            edit_prd_no.setText(onlys[0].prd_no);
                            edit_Z_top_no.setText(onlys[0].Z_top_no);
                        }
                        else{
                            edit_prd_no.setText("");
                            edit_Z_top_no.setText("");

                            Toast.makeText(getApplicationContext(),String.format("卷料[%s]不存在", edit_only_no.getText().toString()), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }


    //////////////////以下是工具类
    //打印卷料
    protected void doPrintJL(String printor, String printModel, HashMap<String,String> paramsJL )  {
        paramsJL.put("CONN", "StarEK");
        paramsJL.put("action", "PrintJLByAndroid");         //new String(b, "GB2312")
        paramsJL.put("printor", printor + WebApi.Utf8_Split); //
        paramsJL.put("print", printModel + WebApi.Utf8_Split);


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
                .addHeader("content-type", "application/x-www-form-urlencoded;charset=gb2312")
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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

                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //处理返回的数据 [增加订单规格] 返回的
        if(requestCode == SelectSOItemActivity.SELECTED_COMPLETE && data != null){
            List<SelectSoLineModel> list = (List<SelectSoLineModel>)data.getSerializableExtra("SoItems");
            for (int i = 0; i < list.size(); i++) {
                list.get(i).isSelected = false;
                adapter.add(list.get(i));
            }

            rembSoLineToShareP();
        }

        //历史卷料 返回 （可能是修改回来）
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void rembSoLineToShareP(){
        Gson gson = new Gson();
        List<SelectSoLineModel> lines = adapter.getList();
        String json =  gson.toJson(lines);
        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();
        String machine  = machineNumber.name;


        SharedPreferences sp = getSharedPreferences("MachineSOLine", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString(machine, json);
        spEditor.commit();
    }

    public void getSoLineToShareP(){
        //List<SelectSoLineModel> lines = new List<SelectSoLineModel>();
        final WHInfoModel machineNumber = (WHInfoModel)edit_machine.getSelectedItem();
        String machine  = machineNumber.name;
        SharedPreferences sp = getSharedPreferences("MachineSOLine", MODE_PRIVATE);
        if(sp.contains(machine)){
            Gson gson = new Gson();
            SelectSoLineModel[] lastSoLines =  gson.fromJson(sp.getString(machine, "[]"),SelectSoLineModel[].class);
            //return lines;
            //SelectSoLineModel[] lastSoLines = getSoLineToShareP();
            adapter.removeAll();
            if(lastSoLines!= null) {
                for (int i = 0; (i < lastSoLines.length); i++) {
                    adapter.add(lastSoLines[i]);
                }
                adapter.notifyDataSetChanged();
            }
        }
        else {
            adapter.removeAll();
            adapter.notifyDataSetChanged();
        }
    }

    public void showDeleteSolineMenu(final int position2) {
        String[] menuText = {"删除"};
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setItems(menuText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (arg1 == 0) {
                    android.support.v7.app.AlertDialog.Builder bulDelete = new android.support.v7.app.AlertDialog.Builder(JK_SOActivity.this);
                    bulDelete.setTitle("删除规则");
                    bulDelete.setMessage(String.format(String.format("确定要删除规则吗？")));

                    bulDelete.setPositiveButton("删吧", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SelectSoLineModel model = (SelectSoLineModel) adapter.getItem(position2);
                            if (model != null)
                                adapter.remove(model);

                            rembSoLineToShareP();
                            dialog.dismiss();
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


    public static Double getDouble(String s){
        if(s.isEmpty())
            return 0.0;
        double d = 0.0;
        try {
            d = Double.parseDouble(s);
        }
        catch (Exception e){
            d= 0.0;
        }

        return d;
    }

    public static Integer getInt(String s){
        if(s.isEmpty())
            return 0;

        return Integer.parseInt( Math.round(getDouble(s)) + "");
    }

    //转换 如果带B开头去掉B
    private String fnGetJK_Z_TopNo(String z_top_no) {
        if (z_top_no.startsWith("B") == true) {
            return z_top_no.substring(1);
        }
        return z_top_no;
    }

    private String fnFillZero(Integer num){
        String s = String.format("%03d", num);
        return s;
    }

    /**
     * spinner 接收默认值的Spinner
     * value 需要设置的默认值
     */
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

    private void setSpinnerPrintValue(Spinner spinner, String printName) {
        SpinnerAdapter apsAdapter = spinner.getAdapter();
        int size = apsAdapter.getCount();
        for (int i = 0; i < size; i++) {
            PrintorModel item = (PrintorModel)apsAdapter.getItem(i);
            if (TextUtils.equals(printName, item.name)) {
                spinner.setSelection(i,true);
                break;
            }
        }
    }

    /* tableType : JLBACK 或 JLIN */
    private void loadPrintorModels(final String tableType){

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
                            if(tableType.equals("JLBACK")) {
                                edit_print_back.setAdapter(spinnerAdapter);
                            }
                            else  if(tableType.equals("JLIN")) {
                                edit_print_jk.setAdapter(spinnerAdapter);
                            }
                        }
                    }
                });
            }
        });
    }

    /*
        取指定PC的打印机 //http://192.168.43.117:8223/StarService?action=GetPrinter
    *  */
    private void loadPrintotr(){
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
                            edit_printor_jk.setAdapter(spinnerAdapter2);

                            ArrayAdapter<PrintorModel> spinnerAdapter = new ArrayAdapter<PrintorModel>(getApplicationContext(),
                                    R.layout.support_simple_spinner_dropdown_item, models);
                            edit_printor_back.setAdapter(spinnerAdapter);
                        }
                    }
               });
            }
        });
    }

    //以下 返回页面提示
    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(JK_SOActivity.this);
        builder.setMessage("确定要退出吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        JK_SOActivity.this.finish();
                    }
                });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            dialog();
            return false;
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
// rl.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
            return false;
        } else if(keyCode == KeyEvent.KEYCODE_HOME) {
//由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if(getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }
}
