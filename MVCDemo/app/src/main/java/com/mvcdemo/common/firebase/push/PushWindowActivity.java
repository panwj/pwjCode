package com.mvcdemo.common.firebase.push;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.mvcdemo.R;


/**
 * Created by panwenjuan on 16-11-16.
 */
public class PushWindowActivity extends Activity {

    private static final String TAG = "PushWindowActivity";
    private static final boolean DBG = false;

    private ImageView mPushImage;
    private TextView mPushTitleTv;
    private TextView mPushContentTv;
    private TextView mPushVersionTv;
    private Button mPushNowBg;
    private PushData mPushData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_push_layout);

        Intent intent = getIntent();
        if (intent != null) {
            mPushData = intent.getParcelableExtra(PushData.PUSH_DATA);
        }

        if (mPushData == null) {
            mPushData = new PushData();
        }

        init();
    }

    private void init() {
        mPushImage = (ImageView) findViewById(R.id.push_image);
        mPushTitleTv = (TextView) findViewById(R.id.push_title);
        mPushContentTv = (TextView) findViewById(R.id.push_content);
        mPushVersionTv = (TextView) findViewById(R.id.push_version);
        mPushNowBg = (Button) findViewById(R.id.push_down_bg);

        try {
            if (TextUtils.isEmpty(mPushData.mImageLink) || mPushData.mImageLink == "") {
                mPushImage.setImageResource(R.drawable.bg_upgrade);
            } else {
                RequestOptions options = new RequestOptions();
                options.placeholder(R.drawable.bg_upgrade);
                Glide.with(this)
                        .load(mPushData.mImageLink)
                        .apply(options)
                        .into(mPushImage);
            }
        } catch (Exception ex) {
            Log.w(TAG, " exception happens " + ex.getMessage());
        }

        mPushTitleTv.setText(mPushData.mTitle);
        mPushVersionTv.setText("V" + mPushData.mVersionName);
        mPushVersionTv.setVisibility(View.VISIBLE);

        String versionInfo = "";
        if (!TextUtils.isEmpty(mPushData.mContents)) {
            String[] str = mPushData.mContents.split(";");
            if (str != null) {
                for (int i = 0; i < str.length; i++) {
                    versionInfo = versionInfo + str[i];
                    if (i != str.length - 1) {
                        versionInfo = versionInfo + "\n";
                    }

                }
            }

        }
        mPushContentTv.setText(versionInfo);

        mPushNowBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri fansUri = Uri.parse(mPushData.mUpdateLink);
                Intent intentFans = new Intent(Intent.ACTION_VIEW);
                intentFans.setData(fansUri);
                try {
                    startActivity(intentFans);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.iv_close_pushpage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
