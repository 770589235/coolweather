package com.ivpoints.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivpoints.bean.Weather;
import com.ivpoints.util.HttpUtil;
import com.ivpoints.util.LogUtil;
import com.ivpoints.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int minutes=30*1000;
        long triggerTime= SystemClock.elapsedRealtime()+minutes;
        Intent i=new Intent(this, AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sp.getString("weather", null);

        if(weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String  weatherId=weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=d059afdc0cee450ea683ef03e200d28e";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtil.e(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText=response.body().string();
                    LogUtil.d(responseText);
                     Weather weather=Utility.handleWeatherResponse(responseText);
                    if(weather!=null && "ok".equals(weather.status)) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }
}
