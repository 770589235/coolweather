package com.ivpoints.util;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.ivpoints.application.MyApplication;
import com.ivpoints.bean.Weather;
import com.ivpoints.db.MyDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/6.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        try {
            JSONArray allProvince=new JSONArray(response);
            for(int i=0;i<allProvince.length();i++){
                JSONObject province=allProvince.getJSONObject(i);
                database.execSQL("insert into Province(provinceName, provinceCode) values(?,?)",
                        new Object[]{province.getString("name"), province.getInt("id")});
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            if(database!=null){
                database.close();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId){
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        try {
            JSONArray allCity=new JSONArray(response);
            for(int i=0;i<allCity.length();i++){
                JSONObject city=allCity.getJSONObject(i);
                database.execSQL("insert into City(cityName, cityCode, provinceId) values(?,?,?)",
                        new Object[]{city.getString("name"), city.getInt("id"), provinceId});
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            if(database!=null){
                database.close();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId){
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        try {
            JSONArray allCounty=new JSONArray(response);
            for(int i=0;i<allCounty.length();i++){
                JSONObject county=allCounty.getJSONObject(i);
                database.execSQL("insert into County(countyName, cityId, weatherId) values(?,?,?)",
                        new Object[]{county.getString("name"),cityId, county.getString("weather_id") });
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            if(database!=null){
                database.close();
            }
        }
        return false;
    }


    /**
     * 将返回的JSON数据解析成weather实体类
     */

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
