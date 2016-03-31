package cn.edu.scnu.face;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.scnu.face.FaceDetect.FaceDetectFragment;
import cn.edu.scnu.face.FunnyChartlet.FunnyCharletFragment;
import cn.edu.scnu.face.SimilarFace.SimilarFaceFragment;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;
import yalantis.com.sidemenu.util.ViewAnimator.ViewAnimatorListener;


public class MainActivity extends ActionBarActivity implements ViewAnimatorListener
{
    private final String CLOSE = "Close";
    private final String FaceDetect = "人脸检测";
    private final String SimilarFace = "相似指数";
    private final String FunnyChartlet = "趣味贴图";

    private DrawerLayout drawerLayout;              // 主视图(带有侧边栏)
    private ActionBarDrawerToggle drawerToggle;     // 侧边栏的监听类
    private LinearLayout linearLayout;              // 侧边栏
    private List<SlideMenuItem> menuItemList = new ArrayList<>();       // 侧边栏的项目
    private ViewAnimator viewAnimator;              // 侧边栏的管理类

    private FaceDetectFragment faceDetectFragment;          // 人脸检测Fragment
    private SimilarFaceFragment similarFaceFragment;        // 相似指数Fragment
    private FunnyCharletFragment funnyCharletFragment;      // 趣味贴图Fragment

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setActionBar();

        setDrawerToggle();

        createMenuList();

        // 监听网络的变化
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    private void initViews()
    {
        faceDetectFragment = FaceDetectFragment.getInstance();
        similarFaceFragment = SimilarFaceFragment.getInstance();
        funnyCharletFragment = FunnyCharletFragment.getInstance();

        // 初始界面为人脸检测
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, faceDetectFragment)
                .commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        linearLayout = (LinearLayout) findViewById(R.id.left_drawer);
        linearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawerLayout.closeDrawers();
            }
        });
    }

    private void setActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.faceDetect);
    }

    private void setDrawerToggle()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close)
        {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void createMenuList()
    {
        SlideMenuItem closeMenuItem = new SlideMenuItem(CLOSE, R.drawable.icn_close);
        menuItemList.add(closeMenuItem);
        SlideMenuItem faceDetectMenuItem = new SlideMenuItem(FaceDetect, R.drawable.ic_face_detect);
        menuItemList.add(faceDetectMenuItem);
        SlideMenuItem similarFaceMenuItem = new SlideMenuItem(SimilarFace, R.drawable.ic_similar_face);
        menuItemList.add(similarFaceMenuItem);
        SlideMenuItem funnyChartletMenuItem = new SlideMenuItem(FunnyChartlet, R.drawable.ic_funny_chartlet);
        menuItemList.add(funnyChartletMenuItem);

        viewAnimator = new ViewAnimator(this, menuItemList, faceDetectFragment, drawerLayout, this);
    }

    // onPostCreate方法是指onCreate方法彻底执行完毕的回调,在屏幕旋转时候等同步下状态
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * 参数分别为：选中的按钮，当前的信息界面，触摸点的 Y 坐标
     **/
    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position)
    {
        switch (slideMenuItem.getName()) {
            case CLOSE:
                return screenShotable;
            case FaceDetect:
                return replaceFragment(screenShotable, position, FaceDetect);
            case SimilarFace:
                return replaceFragment(screenShotable, position, SimilarFace);
            case FunnyChartlet:
                return replaceFragment(screenShotable, position, FunnyChartlet);
        }
        return null;
    }

    private ScreenShotable replaceFragment(ScreenShotable screenShotable, int topPosition, String fragmentName)
    {
        View view = findViewById(R.id.content_frame);
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);

//        findViewById(R.id.content_overlay)
//                .setBackgroundDrawable(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
        animator.start();

        if (fragmentName == FaceDetect) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, faceDetectFragment).commit();
            getSupportActionBar().setTitle(R.string.faceDetect);
            return faceDetectFragment;
        } else if (fragmentName == SimilarFace) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, similarFaceFragment).commit();
            getSupportActionBar().setTitle(R.string.similarFace);
            return similarFaceFragment;
        } else if (fragmentName == FunnyChartlet) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, funnyCharletFragment).commit();
            getSupportActionBar().setTitle(R.string.funnyChartlet);
            return funnyCharletFragment;
        }

        return null;
    }

    @Override
    public void disableHomeButton()
    {
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    public void enableHomeButton()
    {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();
    }

    @Override
    public void addViewToContainer(View view)
    {
        linearLayout.addView(view);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 监听网络的变化
    class NetworkChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ConnectivityManager connectionManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                Toast.makeText(context, R.string.networkAvailable, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.networkUnavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
