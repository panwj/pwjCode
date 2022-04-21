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
    private CheckBox mDCb, mACb, mDescCb;
    private TextView mOk;
    private boolean dHash = true, aHash = true, desc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mEditText = findViewById(R.id.editText);
        mDCb = findViewById(R.id.cb_dHash);
        mACb = findViewById(R.id.cb_aHash);
        mDescCb = findViewById(R.id.cb_desc);
        mOk = findViewById(R.id.ok);

        SharedPreferences sharedPreferences = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int sampleSize = sharedPreferences.getInt("sampleSize", 8);
        dHash = sharedPreferences.getBoolean("dHash", true);
        aHash = sharedPreferences.getBoolean("aHash", true);
        desc = sharedPreferences.getBoolean("desc", true);
//        Logv.e("setting sampleSize : " + sampleSize + "   dHash : " + dHash + "  aHash : " + aHash + "  desc : " + desc);

        mDCb.setChecked(dHash);
        mACb.setChecked(aHash);
        mDescCb.setChecked(desc);
        mEditText.setText("" + sampleSize);

        mDCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dHash = isChecked;
            }
        });

        mACb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                aHash = isChecked;
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

                if (!(dHash || aHash)) {
                    Toast.makeText(SettingActivity.this, "dHash 或 aHash 必须设置", Toast.LENGTH_SHORT).show();
                    return;
                }

//                Logv.e(sm + "   " + dHash + "   " + aHash);
                editor.putInt("sampleSize", Integer.valueOf(sm));
                editor.putBoolean("dHash", dHash);
                editor.putBoolean("aHash", aHash);
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

    }
}
