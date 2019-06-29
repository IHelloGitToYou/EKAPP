package com.ek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import com.ek.adapter.JK_solineAdapter;
import com.ek.model.PrintorModel;
import com.ek.model.SelectSoLineModel;
import com.ek.model.WHInfoModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JK_SOActivity extends AppCompatActivity implements View.OnClickListener{
    JK_solineAdapter adapter;
    ListView listView;

    EditText edit_Z_work_no,edit_prd_no,edit_prd_no_bottom,edit_Z_hou3_bottom,edit_FD_core,edit_Z_core_kg,edit_qty1,edit_Z_kg,edit_Z_iface;
    EditText FD_width, FD_length;
    TextView edit_BZInfo;

    CheckBox edit_is_multi;
    Spinner edit_wh_no, edit_print_jk, edit_print_back ,edit_printor_jk, edit_printor_back;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jk_so);

        client = new OkHttpClient();

        listView = findViewById(R.id.listViewSoItem);
        edit_wh_no = findViewById(R.id.edit_wh_no);
        edit_print_jk = findViewById(R.id.edit_print_jk);
        edit_print_back = findViewById(R.id.edit_print_back);
        edit_printor_jk = findViewById(R.id.edit_printor_jk);
        edit_printor_back = findViewById(R.id.edit_printor_back);

        findViewById(R.id.JK_SO_BtnAdd).setOnClickListener(this);
        findViewById(R.id.Do_JK).setOnClickListener(this);
        findViewById(R.id.Do_BACK_WH4).setOnClickListener(this);
        findViewById(R.id.Do_BACK_WH5).setOnClickListener(this);

        findViewById(R.id.JK_SO_BtnAdd).setOnClickListener(this);
        adapter = new JK_solineAdapter(new ArrayList<SelectSoLineModel>());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("test", "onItemClick");
                SelectSoLineModel line = (SelectSoLineModel)adapter.getItem(position);
                adapter.ClearSelected();
                line.isSelected = !line.isSelected;

                //Load To Form
                edit_Z_work_no.setText(line.Z_work_no);
                edit_prd_no_bottom.setText(line.prd_no);
                edit_Z_hou3_bottom.setText(line.Z_sale_hou3+"");
                edit_FD_core.setText(line.FD_core+"");
                edit_Z_core_kg.setText("");
                edit_qty1.setText("");      //净重
                edit_Z_kg.setText("");      //毛重

                edit_is_multi.setChecked(false); //是否集合
                edit_Z_iface.setText("");        //接口

                FD_width.setText(line.FD_width+"");
                FD_length.setText(line.FD_length+"");
                edit_BZInfo.setText(line.Z_bzinfo +"");

                setSpinnerWHValue(edit_wh_no, line.wh_no);
                adapter.notifyDataSetChanged();
            }
        });


        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_prd_no_bottom = findViewById(R.id.edit_prd_no_bottom);
        edit_Z_hou3_bottom = findViewById(R.id.edit_Z_hou3_bottom);
        edit_FD_core = findViewById(R.id.edit_FD_core);
        edit_Z_core_kg = findViewById(R.id.edit_Z_core_kg);
        edit_qty1 = findViewById(R.id.edit_qty1);
        edit_Z_kg = findViewById(R.id.edit_Z_kg);
        edit_FD_core = findViewById(R.id.edit_FD_core);
        edit_Z_iface = findViewById(R.id.edit_Z_iface);
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
        ArrayAdapter<WHInfoModel> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, whInos);
        //这个在不同的Theme下，显示的效果是不同的
        //spinnerAdapter.setDropDownViewTheme(Resources.Theme.LIGHT);
        edit_wh_no.setAdapter(spinnerAdapter);

        loadPrintorModels("JLIN");
        loadPrintorModels("JLBACK");
        loadPrintotr();
    }

    private Boolean checkSubmit(){
        if(edit_FD_core.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"纸芯重未输入!", Toast.LENGTH_LONG).show();
            return false;
        };
        if(edit_Z_kg.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),String.format("毛重未输入!"), Toast.LENGTH_LONG).show();
            return false;
        };
        if(edit_qty1.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),String.format("净重不能为空!"), Toast.LENGTH_LONG).show();
            return false;
        };

        WHInfoModel wh = (WHInfoModel)edit_wh_no.getSelectedItem();
        if(wh == null){
            Toast.makeText(getApplicationContext(),String.format("仓库非选择!"), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void doJK(){
        if(checkSubmit() == false)
            return;
    }

    private  void doBack(String wh_no){
        if(checkSubmit() == false)
            return;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.JK_SO_BtnAdd:
                //login();
                //如何打开一个 弹窗?
                // 如果返回,还是""源来的UI" 所有临时的编辑 都无变过?
                startActivityForResult(new Intent(this, SelectSOItemActivity.class),SelectSOItemActivity.SELECTED_COMPLETE );
                break;
            case R.id.Do_JK:
                    doJK();
                break;
            case R.id.Do_BACK_WH4:
                    doBack("WH4");
                break;
            case R.id.Do_BACK_WH5:
                    doBack("WH5");
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //处理返回的数据 增加订单规格 返回的
        if(requestCode == SelectSOItemActivity.SELECTED_COMPLETE){
            List<SelectSoLineModel> list = (List<SelectSoLineModel>)data.getSerializableExtra("SoItems");
            for (int i = 0; i < list.size(); i++) {
                list.get(i).isSelected = false;
                adapter.add(list.get(i));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private  Double getDouble(String s){
        if(s.isEmpty())
            return 0.0;

        return Double.parseDouble(s);
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
                        Toast.makeText(getApplicationContext(),String.format("加载打印机出错"), Toast.LENGTH_LONG).show();
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
                            edit_printor_back.setAdapter(spinnerAdapter);

                            ArrayAdapter<PrintorModel> spinnerAdapter2 = new ArrayAdapter<PrintorModel>(getApplicationContext(),
                                    R.layout.support_simple_spinner_dropdown_item, models);
                            edit_printor_jk.setAdapter(spinnerAdapter2);
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
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }
}
