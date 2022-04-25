package com.ex.simi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ex.simi.dao.PictureDaoManager;
import com.ex.simi.util.Logv;

public class SettingActivity extends AppCompatActivity {

    private EditText mEditText;
    private CheckBox mDCb, mACb, mOpencv, mAD, mPro1, mPro2,mDescCb;
    private TextView mOk;
    private boolean dHash = true, aHash = true, opencv, desc, pro1, pro2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mEditText = findViewById(R.id.editText);
        mDCb = findViewById(R.id.cb_dHash);
        mACb = findViewById(R.id.cb_aHash);
        mDescCb = findViewById(R.id.cb_desc);
        mOpencv = findViewById(R.id.cb_cv);
        mAD = findViewById(R.id.cb_ad);
        mPro1 = findViewById(R.id.cb_pro1);
        mPro2 = findViewById(R.id.cb_pro2);
        mOk = findViewById(R.id.ok);

        SharedPreferences sharedPreferences = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int sampleSize = sharedPreferences.getInt("sampleSize", 8);
        dHash = sharedPreferences.getBoolean("dHash", true);
        aHash = sharedPreferences.getBoolean("aHash", true);
        desc = sharedPreferences.getBoolean("desc", true);
        opencv = sharedPreferences.getBoolean("opencv", false);
        pro1 = sharedPreferences.getBoolean("pro1", false);
        pro2 = sharedPreferences.getBoolean("pro2", false);
//        Logv.e("setting sampleSize : " + sampleSize + "   dHash : " + dHash + "  aHash : " + aHash + "  desc : " + desc);

        updateUI();
        mEditText.setText("" + sampleSize);

        mDCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dHash = isChecked;
                aHash = false;
                opencv = false;
                pro1 = false;
                pro2 = false;
            }
        });

        mACb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aHash = isChecked;
                dHash = false;
                opencv = false;
                pro1 = false;
                pro2 = false;
            }
        });

        mAD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pro1 = false;
                pro2 = false;
                aHash = true;
                dHash = true;
                opencv = false;
            }
        });

        mOpencv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                opencv = isChecked;
                aHash = false;
                dHash = false;
                pro1 = false;
                pro2 = false;
            }
        });

        mPro1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pro1 = true;
                pro2 = false;
                aHash = true;
                dHash = true;
                opencv = true;
            }
        });

        mPro2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pro1 = false;
                pro2 = true;
                aHash = true;
                dHash = true;
                opencv = true;
            }
        });

        mDescCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                desc = isChecked;
            }
        });

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sm = mEditText.getText().toString();

                if (TextUtils.isEmpty(sm)) {
                    Toast.makeText(SettingActivity.this, "采样率必须设置", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!(dHash || aHash || opencv || pro1 || pro2)) {
                    Toast.makeText(SettingActivity.this, "必须选择一个比较策略", Toast.LENGTH_SHORT).show();
                    return;
                }

//                Logv.e(sm + "   " + dHash + "   " + aHash);
                editor.putInt("sampleSize", Integer.valueOf(sm));
                editor.putBoolean("dHash", dHash);
                editor.putBoolean("aHash", aHash);
                editor.putBoolean("opencv", opencv);
                editor.putBoolean("pro1", pro1);
                editor.putBoolean("pro2", pro2);
                editor.putBoolean("desc", desc);
                editor.commit();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PictureDaoManager.getDatabase(SettingActivity.this).getPictureDao().deleteAll();
                    }
                }).start();
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pro1 = false;
                pro2 = false;
                aHash = false;
                dHash = false;
                opencv = false;
                updateUI();
            }
        });
    }

    private void updateUI() {
        mDCb.setChecked(dHash && !aHash);
        mACb.setChecked(aHash && !dHash);
        mAD.setChecked(aHash && dHash);
        mDescCb.setChecked(desc);
        mOpencv.setChecked(opencv);
        mPro1.setChecked(pro1);
        mPro2.setChecked(pro2);
    }
}
