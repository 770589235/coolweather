package com.ivpoints.application;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ivpoints.exception.GlobalException;
import com.ivpoints.util.LogUtil;


/**
 * Created by Administrator on 2016/8/19.
 */
public class MyApplication extends Application {


    private static int mCurrentWidthPixels;
    private static int mCurrentHeightPixels;
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        GlobalException exception = GlobalException.getInstance();
        exception.init(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displayMetrics);
        mCurrentWidthPixels = displayMetrics.widthPixels;
        mCurrentHeightPixels = displayMetrics.heightPixels;

        LogUtil.d("屏幕的宽:"+mCurrentWidthPixels+", 屏幕的高:"+mCurrentHeightPixels);

    }

    public static Context getContext() {
        return context;
    }



}
