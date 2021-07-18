package com.mvcdemo.common.google.billing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.util.ArrayList;
import java.util.List;

import com.mvcdemo.modules.main.activity.MainActivity;
import com.mvcdemo.R;
import com.mvcdemo.common.event.EventLogger;
import com.mvcdemo.common.util.PrimeManager;
import com.mvcdemo.customview.markbtn.CommonMaskView;


public class BillingActivity extends AppCompatActivity implements View.OnClickListener,
        BillingManager.BillingUpdatesListener {

    public static final String EXTRA_FROM = "extra_from";
    public static final String EXTRA_IS_SUB = "extra_is_sub";
    public static final String FROM_TOOL_BAR = "tool_bar";
    public static final String FROM_APP_GUIDE = "app_guide";
    public static final String FROM_REMOVE_ADS = "remove_ads";
    public static final String FROM_RESOLUTION = "resolution_1080p";

    private static final String TAG = "Billing";

    private View mSubRl;
    private ImageView mCloseIv;
    private View mSubMonth;
    private ImageView mMonthMarks;
    private TextView mMonthTv;
    private View mSubYear;
    private ImageView mYearMarks;
    private TextView mYearTv;
    private TextView mYearDes;
    private TextView mSaveTv;
    private CommonMaskView mSubscription;
    private TextView mSubscriptionDes;
    private View mFrameLayout;

    private RecyclerView mRecyclerView;
    private RecyAdapter mRecyAdapter;
    private Handler mHandler = new Handler();
    private LinearLayoutManager layoutManager;
    private List<SubItem> mData;
    private int oldItem = 0;
    private Integer[] mImgIds = {R.drawable.ic_pro_remove_ads, R.drawable.ic_pro_1080p, R.drawable.ic_pro_more};
    private Integer[] mStrIds = {R.string.remove_ads, R.string.sub_future_1080p, R.string.sub_future_more};


    private ViewStub mViewStub;
    private View mSubSuc;
    private ImageView mBackIv;
    private TextView mSubTv;
    private TextView mSkipTv;

    private BillingManager mBillingManager;
    private Purchase mPurchase;
    private boolean mNewSub;
    private String mSku = "";
    private String mTempSku = "";
    private String mOldSku = "";
    private boolean isDestroy;
    private boolean mClickSub;
    private String mFrom = FROM_APP_GUIDE;
    private boolean isSub;
    private AnimatorSet mStep1Anim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.billing_activity);

        mBillingManager = new BillingManager(this, this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mHandler != null) mHandler.postDelayed(scrollRunnable, 10);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mHandler != null) mHandler.removeCallbacks(scrollRunnable);
    }

    @Override
    protected void onDestroy() {
        isDestroy = true;
        super.onDestroy();
        destroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!PrimeManager.isSubscriptionUser()) {
            eventClickClose(mFrom);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                eventClickClose(mFrom);
                finish();
                break;
            case R.id.rl_month:
                mTempSku = BillingConstants.SKU_PRO_MONTHLY_NEW;
                if (!BillingConstants.SKU_PRO_MONTHLY_NEW.equals(mSku)) {
                    mOldSku = mSku;
                }
                refreshUI();
                break;
            case R.id.rl_year_sub:
                mTempSku = BillingConstants.SKU_PRO_YEARLY_NEW;
                if (!BillingConstants.SKU_PRO_YEARLY_NEW.equals(mSku)) {
                    mOldSku = mSku;
                }
                refreshUI();
                break;
            case R.id.mask_view:
                eventClickContinue(mFrom, mTempSku);
                startSubscribe();
                break;
            case R.id.tv_continue_limited_version:
                eventClickSkip(mFrom);
                finish();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    @Override
    public void onBillingClientSetupFinished() {
        Log.d(TAG, "billing  onBillingClientSetupFinished()...");
    }

    @Override
    public void onConsumeFinished(String token, int result) {
        Log.d(TAG, "billing  onConsumeFinished()...");
    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchases) {
        Log.d(TAG, "billing  onPurchasesUpdated()...");
        if (purchases != null && !purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                String sku = purchase.getSku();

                if (sku.equals(BillingConstants.SKU_PRO_MONTHLY_NEW)
                        || sku.equals(BillingConstants.SKU_PRO_YEARLY_NEW)) {
                    mNewSub = true;
                    mSku = sku;
                    mPurchase = purchase;
                }
            }
        } else {
            mNewSub = false;
        }

        if (mNewSub) {
            PrimeManager.saveSubscriptionsUser(true);
            if (mClickSub) {
                eventSubSuccess(mFrom, mTempSku);
                showSubsSuccessDialog();
            } else {
                //进入主界面检查失败后进入订阅界面检查成功，则提示重启应用
                if (!isSub) showSubsSuccessDialog();
            }
            subscriptionSuccess();
        } else {
            PrimeManager.saveSubscriptionsUser(false);
        }
    }

    @Override
    public void onBillingError(int result) {
        Log.d(TAG, "onBillingError()...  result = " + result);

        if (mClickSub) {
            if (result != BillingClient.BillingResponseCode.USER_CANCELED) showSubFailedDialog();
            eventSubFailed(mFrom, mTempSku, result);
        }
    }

    private void init() {
        Intent intent = getIntent();
        if (intent != null) {
            mFrom = intent.getStringExtra(EXTRA_FROM);
            isSub = intent.getBooleanExtra(EXTRA_IS_SUB, false);
        }
        mClickSub = false;
        mSubRl = findViewById(R.id.rl_sub_view);
        mCloseIv = findViewById(R.id.iv_close);
        mCloseIv.setOnClickListener(this);
        mSubMonth = findViewById(R.id.rl_month);
        mSubMonth.setOnClickListener(this);
        mMonthMarks = findViewById(R.id.iv_sub_month_marks);
        mMonthTv = findViewById(R.id.tv_sub_month);
        mSubYear = findViewById(R.id.rl_year_sub);
        mSubYear.setOnClickListener(this);
        mYearMarks = findViewById(R.id.iv_sub_year_marks);
        mYearTv = findViewById(R.id.tv_sub_year);
        mYearDes = findViewById(R.id.tv_sub_year_des);
        mSaveTv = findViewById(R.id.tv_save);
        mFrameLayout = findViewById(R.id.fl_mask);
        mSubscription = mFrameLayout.findViewById(R.id.mask_view);
        mSubscription.setOnClickListener(this);
        mSubscription.setBackground(getResources().getDrawable(R.drawable.fillet_eight_degrees_primary_bg));
        mSubscriptionDes = mFrameLayout.findViewById(R.id.tv_btn);
//        mSubscription.setRippleColor(Color.argb(10, 255, 255, 213));
//        if (mSubscription != null) mSubscription.showRipple();
        mTempSku = BillingConstants.SKU_PRO_YEARLY_NEW;

        setTextType();
        eventShowSubPage(mFrom);
        mStep1Anim = notifyToUserWithAnimation(mFrameLayout);

        mRecyclerView = findViewById(R.id.rl_banner);
        initData();
        initRecy();
        mSkipTv = findViewById(R.id.tv_continue_limited_version);
        mSkipTv.setOnClickListener(this);
    }

    private void refreshUI() {
        if (TextUtils.equals(BillingConstants.SKU_PRO_MONTHLY_NEW, mTempSku)) {
            mSubMonth.setBackgroundResource(R.drawable.gray_light_bg);
            mMonthMarks.setImageResource(R.drawable.selected_dot);
            mMonthTv.setTextColor(getResources().getColor(R.color.orange_3));

            mSubYear.setBackgroundResource(R.drawable.white_light_bg);
            mYearMarks.setImageResource(R.drawable.unselected_dot);
            mYearTv.setTextColor(getResources().getColor(R.color.color_transparent_alpha_30));
            mYearDes.setVisibility(View.GONE);
            mSaveTv.setVisibility(View.INVISIBLE);

            mSubscriptionDes.setText(getString(R.string.sub_continue_mon));
        } else if (TextUtils.equals(BillingConstants.SKU_PRO_YEARLY_NEW, mTempSku)) {
            mSubMonth.setBackgroundResource(R.drawable.white_light_bg);
            mMonthMarks.setImageResource(R.drawable.unselected_dot);
            mMonthTv.setTextColor(getResources().getColor(R.color.color_transparent_alpha_30));

            mSubYear.setBackgroundResource(R.drawable.gray_light_bg);
            mYearMarks.setImageResource(R.drawable.selected_dot);
            mYearTv.setTextColor(getResources().getColor(R.color.orange_3));
            mYearDes.setVisibility(View.VISIBLE);
            mSaveTv.setVisibility(View.VISIBLE);

            mSubscriptionDes.setText(getString(R.string.sub_continue_year));
        }
    }

    private void setTextType() {
        try {
            AssetManager mgr = getAssets();
            Typeface tf = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
            mMonthTv.setTypeface(tf);
            mYearTv.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscriptionSuccess() {
        mViewStub = findViewById(R.id.vs_sub_suc);
        if (mViewStub != null) {
            View view = mViewStub.inflate();
            mSubSuc = view.findViewById(R.id.rl_sub_success);
            mBackIv = view.findViewById(R.id.iv_back);
            mBackIv.setOnClickListener(this);
            mSubTv = view.findViewById(R.id.tv_sub_success);
        }
        if (mSubRl != null) mSubRl.setVisibility(View.GONE);
        if (mSubSuc != null) mSubSuc.setVisibility(View.VISIBLE);

        if (mSubTv != null) {
            mSubTv.setText(getString(R.string.pro_old_user_tip));
        } else if (mSubTv != null && TextUtils.equals(BillingConstants.SKU_NO_ADS_MONTHLY, mSku)) {
            mSubTv.setText(getString(R.string.sub_suc_month_old));
        } else if (mSubTv != null && (TextUtils.equals(BillingConstants.SKU_PRO_MONTHLY, mSku)
                || TextUtils.equals(BillingConstants.SKU_PRO_MONTHLY_NEW, mSku))) {
            mSubTv.setText(getString(R.string.sub_suc_month));
        } else if (mSubTv != null && TextUtils.equals(BillingConstants.SKU_NO_ADS_YEARLY, mSku)) {
            mSubTv.setText(getString(R.string.sub_suc_year_old));
        } else if (mSubTv != null && (TextUtils.equals(BillingConstants.SKU_PRO_YEARLY, mSku)
                || TextUtils.equals(BillingConstants.SKU_PRO_YEARLY_NEW, mSku))) {
            mSubTv.setText(getString(R.string.sub_suc_year));
        }
    }

    private void destroy() {
        mClickSub = false;
        if (mCloseIv != null) mCloseIv.setOnClickListener(null);
        if (mSubMonth != null) mSubMonth.setOnClickListener(null);
        if (mSubYear != null) mSubYear.setOnClickListener(null);
        if (mSubscription != null) mSubscription.setOnClickListener(null);
        if (mBackIv != null) mBackIv.setOnClickListener(null);
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        if (mHandler != null) mHandler.removeCallbacks(scrollRunnable);
        removeStep1Anim();
    }

    private void startSubscribe() {
        if (TextUtils.isEmpty(mTempSku)) return;

        ArrayList<String> oldSkuList = new ArrayList<>();
        if (!TextUtils.isEmpty(mOldSku)) {
            oldSkuList.add(mOldSku);
        }
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add(BillingConstants.SKU_NO_ADS_MONTHLY);
        skuList.add(BillingConstants.SKU_NO_ADS_YEARLY);
        skuList.add(BillingConstants.SKU_PRO_MONTHLY);
        skuList.add(BillingConstants.SKU_PRO_YEARLY);
        skuList.add(BillingConstants.SKU_PRO_MONTHLY_NEW);
        skuList.add(BillingConstants.SKU_PRO_YEARLY_NEW);

        try {
            if (mBillingManager != null && !TextUtils.isEmpty(mTempSku)) {
                if (!oldSkuList.isEmpty() && mPurchase != null) {
                    mBillingManager.initiatePurchaseFlow(mTempSku, skuList, mOldSku, mPurchase.getPurchaseToken(), BillingClient.SkuType.SUBS);
                } else {
                    mBillingManager.initiatePurchaseFlow(mTempSku, skuList, BillingClient.SkuType.SUBS);
                }
                mClickSub = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mClickSub = false;
        }
    }

    private void showSubFailedDialog() {
        View view = View.inflate(getApplicationContext(), R.layout.sub_failed_dialog, null);
        if (view == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog_appcompat);
        final AlertDialog dialog = builder.create();
        dialog.setView(view);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = (float) 0.3;
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogWindow.setAttributes(lp);

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (!isFinishing() && !isDestroy) dialog.show();
    }

    private void showSubsSuccessDialog() {
        View view = View.inflate(getApplicationContext(), R.layout.subs_confirm_dialog, null);
        if (view == null) return;
        TextView textView = (TextView) view.findViewById(R.id.desc);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog_appcompat);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                restartApp();
            }
        });
        if (!isFinishing() && !isDestroy) {
            dialog.show();
            EventLogger.logEvent(getApplicationContext(), "sub_suc_restart");
        }
    }

    private void restartApp() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eventShowSubPage(String from) {
        if (!PrimeManager.isSubscriptionUser()) {
            if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                EventLogger.logEvent(getApplicationContext(), "show_sub_page_launch");
            } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                EventLogger.logEvent(getApplicationContext(), "show_sub_page_topbar");
            } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                EventLogger.logEvent(getApplicationContext(), "show_sub_page_remove_ad");
            } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                EventLogger.logEvent(getApplicationContext(), "show_sub_page_hp");
            }

            EventLogger.logEvent(getApplicationContext(), "show_sub_page");
        }
    }

    private void eventClickClose(String from) {
        if (TextUtils.equals(from, FROM_APP_GUIDE)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_close_launch");
        } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_close_topbar");
        } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_close_remove_ad");
        } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_close_hp");
        }

        EventLogger.logEvent(getApplicationContext(), "sub_view_close");
    }

    private void eventClickSkip(String from) {
        if (TextUtils.equals(from, FROM_APP_GUIDE)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_skip_launch");
        } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_skip_topbar");
        } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_skip_remove_ad");
        } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
            EventLogger.logEvent(getApplicationContext(), "sub_view_skip_hp");
        }

        EventLogger.logEvent(getApplicationContext(), "sub_view_skip");
    }

    private void eventClickContinue(String from, String subId) {
        switch (subId) {
            case BillingConstants.SKU_PRO_MONTHLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_monthly_launch");
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_monthly_topbar");
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_monthly_remove_ad");
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_monthly_hp");
                }

                EventLogger.logEvent(getApplicationContext(), "click_sub_monthly");
                break;
            case BillingConstants.SKU_PRO_YEARLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_yearkly_launch");
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_yearkly_topbar");
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_yearkly_remove_ad");
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "click_sub_yearkly_hp");
                }

                EventLogger.logEvent(getApplicationContext(), "click_sub_yearkly");
                break;
        }
    }

    private void eventSubSuccess(String from, String subId) {
        switch (subId) {
            case BillingConstants.SKU_PRO_MONTHLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_success_1_launch");
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_success_1_topbar");
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_success_1_remove_ad");
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_success_1_hp");
                }

                EventLogger.logEvent(getApplicationContext(), "purchased_monthly_success_1");
                break;
            case BillingConstants.SKU_PRO_YEARLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_try_launch");
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_try_topbar");
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_try_remove_ad");
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_try_hp");
                }

                EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_try");
                break;
        }
    }

    private void eventSubFailed(String from, String subId, int result) {
        Bundle bundle = new Bundle();
        bundle.putInt("fail_reason", result);
        switch (subId) {
            case BillingConstants.SKU_PRO_MONTHLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_fail_1_launch", bundle);
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_fail_1_topbar", bundle);
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_fail_1_remove_ad", bundle);
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "purchased_monthly_fail_1_hp", bundle);
                }

                EventLogger.logEvent(getApplicationContext(), "purchased_monthly_fail_1", bundle);
                break;
            case BillingConstants.SKU_PRO_YEARLY_NEW:
                if (TextUtils.equals(from, FROM_APP_GUIDE)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_fail_launch", bundle);
                } else if (TextUtils.equals(from, FROM_TOOL_BAR)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_fail_topbar", bundle);
                } else if (TextUtils.equals(from, FROM_REMOVE_ADS)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_fail_remove_ad", bundle);
                } else if (TextUtils.equals(from, FROM_RESOLUTION)) {
                    EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_fail_hp", bundle);
                }

                EventLogger.logEvent(getApplicationContext(), "first_purchased_yearly_fail", bundle);
                break;
        }
    }

    private AnimatorSet notifyToUserWithAnimation(final View view) {
        if (PrimeManager.isSubscriptionUser()) return null;

        final AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 15, -15, 6, -6, 0)
        );
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                set.start();
            }
        });
        set.setDuration(1000);
        set.setStartDelay(500);
        set.start();
        return set;
    }

    private void removeStep1Anim() {
        if (mStep1Anim != null) {
            mFrameLayout.clearAnimation();
            mStep1Anim.removeAllListeners();
            mStep1Anim.cancel();
        }
    }

    private void initRecy() {
        mRecyAdapter = new RecyAdapter(this, mData);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyAdapter);
    }

    private void initData() {
        mData = new ArrayList<>();
        for (int i = 0; i < mImgIds.length; i++) {
            SubItem item = new SubItem();
            item.drawableId = mImgIds[i];
            item.strId = mStrIds[i];
            mData.add(item);
        }
    }

    Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            mRecyclerView.scrollBy(3, 0);

            int firstItem = layoutManager.findFirstVisibleItemPosition();
            if (firstItem != oldItem && firstItem > 0) {
                oldItem = firstItem;
            }

            mHandler.postDelayed(scrollRunnable, 10);
        }
    };
}
