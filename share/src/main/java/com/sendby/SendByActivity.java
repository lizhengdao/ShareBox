package com.sendby;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.common.componentes.activity.ImmersiveFragmentActivity;
import com.common.utils.activity.ActivityUtil;
import com.sendby.ads.AdCoreManager;
import com.sendby.entity.ConfigEntity;
import com.sendby.entity.HttpResponse;
import com.sendby.entity.LoginEntity;
import com.sendby.fragment.LoginFragment;
import com.sendby.fragment.PaymentFragment;
import com.sendby.http.HttpManager;
import com.flybd.sharebox.AppExecutorManager;
import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.Nullable;

public class SendByActivity extends ImmersiveFragmentActivity {

    private static final String TAG = "SendByActivity";

    private static final int REQUEST_LOGIN_CODE = 111;

    private static final int REQUEST_BUY_VIP_CODE = 112;

    private FileChooseFragment fileChooseFragment;

    private Button btnSend;

    private Button btnReceive;

    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendby);
        AdCoreManager.initAdmob(this, "ca-app-pub-1847326177341268~7271377670");
        //interstital ca-app-pub-1847326177341268/3659435721
        //video ca-app-pub-1847326177341268/4206230631
        //banner ca-app-pub-1847326177341268/6423246157
        View content = findViewById(R.id.content);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop() + ActivityUtil.getStatusBarHeight(this), content.getPaddingRight(), content.getPaddingBottom());

        navigationView = findViewById(R.id.nv_menu_left);
        navigationView.setPadding(navigationView.getPaddingLeft(), navigationView.getPaddingTop() + ActivityUtil.getStatusBarHeight(this), navigationView.getPaddingRight(), navigationView.getPaddingBottom());

        btnSend = findViewById(R.id.btn_send);
        btnSend.setActivated(true);
        btnSend.setOnClickListener(v -> fileChooseFragment.sendFiles());

        btnReceive = findViewById(R.id.btn_receive);
        btnReceive.setOnClickListener(v -> {
            Intent intent = ImmersiveFragmentActivity.newInstance(this, ReceiveFileFragment.class);
            startActivity(intent);
        });

        btnSend.getTranslationY();
        fileChooseFragment = new FileChooseFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.design_bottom_sheet, fileChooseFragment, "SendByActivity").commitAllowingStateLoss();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_login) {
                Intent intent = ImmersiveFragmentActivity.newInstance(this, LoginFragment.class);
                startActivityForResult(intent, REQUEST_LOGIN_CODE);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_vip) {
                Intent intent = ImmersiveFragmentActivity.newInstance(this, PaymentFragment.class);
                startActivityForResult(intent, REQUEST_BUY_VIP_CODE);
                return true;
            }
            return false;
        });


        FrameLayout flAd = findViewById(R.id.fl_ad);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        if (BuildConfig.DEBUG) {
            adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        } else {
            adView.setAdUnitId("ca-app-pub-1847326177341268/6423246157");
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
            }
        });
        adView.loadAd(adRequest);
        flAd.addView(adView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int offsetY = (int) (((ViewGroup) btnSend.getParent()).getY() + btnSend.getHeight());
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int height = size.y - offsetY - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        fileChooseFragment.setPeekHeight(height);
    }

    @Override
    public void onBackPressed() {
        if (fileChooseFragment.isExpandable()) {
            fileChooseFragment.collapse();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
            try {
                HttpResponse<ConfigEntity> response = HttpManager.getInstance().getConfig(Constants.get().getRestUrl());
                if (response != null && response.getData() != null) {
                    Constants.get().setChunkSize(response.getData().getChunkSize());
                    Constants.get().setChunkedStreamingMode(response.getData().getChunkedStreamingMode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doLogin() {
        AppExecutorManager.INSTANCE.getInstance().networkIO().execute(() -> {
            try {
                HttpResponse<LoginEntity> response = HttpManager.getInstance().googleVerify(Constants.get().getRestUrl(), Constants.get().getToken());
                if (response != null && response.getData() != null) {
                    getHandler().post(() -> Toast.makeText(this, "登录成功", Toast.LENGTH_LONG).show());
                } else {
                    Constants.get().setToken("");
                    getHandler().post(() -> Toast.makeText(this, "登录失败", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (!TextUtils.isEmpty(Constants.get().getToken())) {
                    doLogin();
                }
            }
        } else if (requestCode == REQUEST_BUY_VIP_CODE) {

        }
    }
}