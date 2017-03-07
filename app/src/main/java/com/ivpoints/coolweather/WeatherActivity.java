package com.ivpoints.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivpoints.bean.Forecast;
import com.ivpoints.bean.Weather;
import com.ivpoints.util.HttpUtil;
import com.ivpoints.util.LogUtil;
import com.ivpoints.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/7.
 */
public class WeatherActivity extends AppCompatActivity {

    private TextView cityTitle;
    private TextView updateTime;
    private ScrollView scrollView;
    private TextView degree;
    private TextView info;
    private LinearLayout linearLayout;
    private TextView aqi;
    private TextView pm;
    private TextView comfort;
    private TextView carwash;
    private TextView sport;
    private ImageView iv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        cityTitle= (TextView) findViewById(R.id.title_city);
        updateTime= (TextView) findViewById(R.id.title_update_time);
        scrollView= (ScrollView) findViewById(R.id.weather_layout);
        degree= (TextView) findViewById(R.id.now_degree);
        info= (TextView) findViewById(R.id.now_text);
        linearLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqi= (TextView) findViewById(R.id.aqi_text);
        pm= (TextView) findViewById(R.id.pm_text);
        comfort= (TextView) findViewById(R.id.comfort_text);
        carwash= (TextView) findViewById(R.id.carwash_text);
        sport= (TextView) findViewById(R.id.sport_text);
        iv= (ImageView) findViewById(R.id.iv);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sp.getString("weather", null);
        String bindString=sp.getString("imgUrl", null);
        if(bindString!=null){
            Glide.with(this).load(bindString).into(iv);
        }else{
            loadImg();
        }
        if(weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            String weatherId=getIntent().getStringExtra("weather_id");
            scrollView.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    private void loadImg() {
        String imgUrl="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(imgUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String url=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("imgUrl", url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(url).into(iv);
                    }
                });
            }
        });
    }

    private void requestWeather(String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                LogUtil.d(responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status)){
                            SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor editor=sp.edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        cityTitle.setText(weather.basic.cityName);
        updateTime.setText(weather.basic.update.updateTime.split(" ")[1]);
        degree.setText(weather.now.temperature+"℃");
        info.setText(weather.now.more.info);
        linearLayout.removeAllViews();
        for(Forecast forecast: weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item, null);
            ((TextView)view.findViewById(R.id.date_text)).setText(forecast.date);
            ((TextView)view.findViewById(R.id.info_text)).setText(forecast.more.info);
            ((TextView)view.findViewById(R.id.max_text)).setText(forecast.temperature.max);
            ((TextView)view.findViewById(R.id.min_text)).setText(forecast.temperature.min);
            linearLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqi.setText(weather.aqi.city.aqi);
            pm.setText(weather.aqi.city.pm25);
        }
        comfort.setText(weather.suggestion.comfort.info);
        carwash.setText(weather.suggestion.carWash.info);
        sport.setText(weather.suggestion.sport.info);
        scrollView.setVisibility(View.VISIBLE);
    }
}
