package com.ek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;

import com.ek.adapter.JK_solineAdapter;
import com.ek.adapter.OnlyNoListAdapter;
import com.ek.model.OnlyNoItem;
import com.ek.model.SoLineModel;

import java.util.ArrayList;
import java.util.List;

public class JK_SOActivity extends AppCompatActivity implements View.OnClickListener{
    JK_solineAdapter adapter;
    ListView listView;

    EditText edit_Z_work_no,edit_prd_no;

    Spinner edit_wh_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jk_so);

        listView = findViewById(R.id.listViewSoItem);
        edit_wh_no = findViewById(R.id.edit_wh_no);

        adapter = new JK_solineAdapter(new ArrayList<SoLineModel>());
        listView.setAdapter(adapter);
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                //showMenu((OnlyNoItem) adapter.getItem(position));
//                return false;
//            }
//        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("test", "onItemClick");
                SoLineModel line = (SoLineModel)adapter.getItem(position);
                line.isSelected = !line.isSelected;

                //Load To Form
                edit_Z_work_no.setText(line.Z_work_no);
                edit_prd_no.setText(line.prd_no);
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.JK_SO_BtnAdd).setOnClickListener(this);
        edit_Z_work_no = findViewById(R.id.edit_Z_work_no);
        edit_prd_no = findViewById(R.id.edit_prd_no);
        edit_prd_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //di.set
            }
        });

        String[] spinnerItems = {"WH1","WH2","WH3","WH4","WH5","WH6"};
        //自定义选择填充后的字体样式
        //只能是textview样式，否则报错：ArrayAdapter requires the resource ID to be a TextView
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, spinnerItems);
        //这个在不同的Theme下，显示的效果是不同的
        //spinnerAdapter.setDropDownViewTheme(Theme.LIGHT);
        edit_wh_no.setAdapter(spinnerAdapter);
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
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //处理返回的数据
        if(requestCode == SelectSOItemActivity.SELECTED_COMPLETE){
            List<SoLineModel> list = (List<SoLineModel>)data.getSerializableExtra("SoItems");
            //Toast.makeText(this, "jieshoudoa"+list.size(),Toast.LENGTH_LONG).show();
            for (int i = 0; i < list.size(); i++) {
                list.get(i).isSelected = false;
                adapter.add(list.get(i));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


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
