package com.ek;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ek.adapter.SOLineListAdapter;
import com.ek.model.PageResultModel;
import com.ek.model.SelectSoLineModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SelectSOItemActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int SELECTED_COMPLETE = 1;
    EditText edit_prd_no, edit_Z_work_no, edit_so_no;

    SOLineListAdapter adapter;
    OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_soitem);
        ListView lv = findViewById(R.id.listView);
        Button btn = findViewById(R.id.btn_search);
        Button btnOk = findViewById(R.id.btn_ok);
        btn.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        edit_so_no = findViewById(R.id.edit_so_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        //edit_Z_work_no.setFocusable(true);

        client = new OkHttpClient();
        adapter = new SOLineListAdapter(new ArrayList<SelectSoLineModel>());

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectSoLineModel line = (SelectSoLineModel) adapter.getItem(position);
                line.isSelected = !line.isSelected;
                if (line.isSelected ) {
                    view.findViewById(R.id.label_Z_work_no).setBackgroundColor(Color.rgb(0, 255, 20));
                    view.findViewById(R.id.label_prd_no).setBackgroundColor(Color.rgb(0, 255, 20));
                    view.findViewById(R.id.label_FD_width).setBackgroundColor(Color.rgb(0, 255, 20));
                }
                else
                {
                    view.findViewById(R.id.label_Z_work_no).setBackgroundColor(Color.rgb(255, 255, 255));
                    view.findViewById(R.id.label_prd_no).setBackgroundColor(Color.rgb(255, 255, 255));
                    view.findViewById(R.id.label_FD_width).setBackgroundColor(Color.rgb(255, 255, 255));
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //showMenu((OnlyNoItem) adapter.getItem(position));
                Toast.makeText(SelectSOItemActivity.this, "abc", Toast.LENGTH_LONG).show();

                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.btn_ok:

                List<SelectSoLineModel> list2 = adapter.getList();
                ArrayList<SelectSoLineModel> list = new ArrayList<SelectSoLineModel>();
                for (int i=0;i<list2.size();++i) {
                    if(list2.get(i).isSelected)
                        list.add(list2.get(i));
                }

                Intent intent = new Intent();
                intent.putExtra("SoItems", list);
                setResult(SELECTED_COMPLETE, intent);
                finish();
                break;

            case R.id.btn_search:
                LoadSOLine();
                break;
        }
    }

    String GetLast6MonthDateString()
    {
        //Time t= new Time("GMT+8");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -6);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar calendar2 = Calendar.getInstance();
        int year2 = calendar2.get(Calendar.YEAR);
        int month2 = calendar2.get(Calendar.MONTH) + 1;
        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);

        String str = year + "-" + month + "-" + day + "~" + year2 + "-" + month2 + "-" + day2;
        return  str;
    }

    void LoadSOLine()
    {
        String prd_no = edit_prd_no.getText().toString();
        String z_work_no = edit_Z_work_no.getText().toString();
        String so_no = edit_so_no.getText().toString();

        HashMap<String,String> paramsMap=new HashMap<>();
        paramsMap.put("NowLoginId", MainActivity.current_login_id);
        paramsMap.put("NowUnderPassKey", MainActivity.current_NowUnderPassKey);
        paramsMap.put("action","GetTableBodyListAfterCheck");
        paramsMap.put("so_no", so_no);


        //Toast.makeText(getApplicationContext(),, Toast.LENGTH_LONG);
        Log.d("GetLast6MonthDateString", GetLast6MonthDateString());

        paramsMap.put("so_dd", GetLast6MonthDateString()); //
        paramsMap.put("finish_sa", "F");
        paramsMap.put("Z_work_no", z_work_no);
        paramsMap.put("prd_no", prd_no);

        paramsMap.put("FD_width", "");

        paramsMap.put("check_state", "3");
        paramsMap.put("so_id", "SO");
        paramsMap.put("zr_form_type", "SC");
        paramsMap.put("page", "1");
        paramsMap.put("start", "0");
        paramsMap.put("limit", "50");

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            Log.d("test", "key="+key+", val="+paramsMap.get(key));
            builder.add(key, paramsMap.get(key));
        }
        Request request = new Request.Builder()
                .url(WebApi.getRealUrl(WebApi.URL_SO))
                .post(builder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),String.format("加载订单出错"), Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respTxt =  response.body().string();
                Gson gson = new Gson();
                //Log.d("gson", respTxt);

                final PageResultModel<SelectSoLineModel> result = gson.fromJson(respTxt, new TypeToken<PageResultModel<SelectSoLineModel>>(){}.getType());

                final SelectSoLineModel[] list = result.items; //gson.fromJson(result.items, SelectSoLineModel[].class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.removeAll();

                        if (list != null && list.length>0)
                        {
                            for(int i = 0; i<list.length; ++i) {
                                adapter.add(list[i]);
                            }
                        }
                    }
                });
            }
        });
    }


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
