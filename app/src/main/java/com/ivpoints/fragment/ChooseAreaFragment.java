package com.ivpoints.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivpoints.application.MyApplication;
import com.ivpoints.bean.City;
import com.ivpoints.bean.County;
import com.ivpoints.bean.Province;
import com.ivpoints.coolweather.R;
import com.ivpoints.coolweather.WeatherActivity;
import com.ivpoints.db.MyDBHelper;
import com.ivpoints.util.HttpUtil;
import com.ivpoints.util.LogUtil;
import com.ivpoints.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/6.
 */
public class ChooseAreaFragment extends Fragment {

    private ListView listView;
    private Button back;
    private TextView title;
    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>();
    private static int LEVEL_PROVINCE=0;
    private static int LEVEL_CITY=1;
    private static int LEVEL_COUNTY=2;
    private int currentLevel;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;


    private static final String BASE_URL="http://guolin.tech/api/china";

    private ProgressDialog progressDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area, container, false);
        listView= (ListView) view.findViewById(R.id.listview);
        title= (TextView) view.findViewById(R.id.title);
        back= (Button) view.findViewById(R.id.back);
        adapter=new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                     selectedProvince=provinceList.get(position);
                     queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    startActivity(new Intent(getActivity(), WeatherActivity.class).putExtra("weather_id", weatherId));
                    getActivity().finish();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    private void queryProvinces() {
        title.setText("中国");
        back.setVisibility(View.GONE);
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        Cursor cursor=database.rawQuery("select * from Province",null);
        Province province;
        if(provinceList!=null && provinceList.size()>0){
            provinceList.clear();
        }else{
            provinceList=new ArrayList<>();
        }
        if(cursor.moveToFirst()){
            if(datalist!=null && datalist.size()>0){
                datalist.clear();
            }
            do{
                province=new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceCode(cursor.getInt(cursor.getColumnIndex("provinceCode")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("provinceName")));
                LogUtil.d(province.getId()+"..."+province.getProvinceCode()+"..."+province.getProvinceName());
                provinceList.add(province);
                datalist.add(province.getProvinceName());
            }while(cursor.moveToNext());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            queryFromServer(BASE_URL,"province");
        }

        if(cursor!=null){
            cursor.close();
        }

        if(database!=null){
            database.close();
        }
    }

    private void queryFromServer(String baseUrl, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(baseUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void queryCities() {
        title.setText(selectedProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        Cursor cursor=database.rawQuery("select * from City where provinceId = ? ", new String[]{String.valueOf(selectedProvince.getId())});
        City city;
        if(cityList!=null && cityList.size()>0){
            cityList.clear();
        }else{
            cityList=new ArrayList<>();
        }
        if(cursor.moveToFirst()){
            datalist.clear();
            do{
                city=new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityCode(cursor.getInt(cursor.getColumnIndex("cityCode")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
                city.setProvinceId(cursor.getInt(cursor.getColumnIndex("provinceId")));
                LogUtil.d(city.getId()+"..."+city.getCityCode()+"..."+city.getProvinceId()+"..."+city.getCityName());
                cityList.add(city);
                datalist.add(city.getCityName());
            }while(cursor.moveToNext());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            queryFromServer(BASE_URL+"/"+selectedProvince.getProvinceCode(),"city");
        }

        if(cursor!=null){
            cursor.close();
        }

        if(database!=null){
            database.close();
        }
    }

    private void queryCounties() {
        title.setText(selectedCity.getCityName());
        back.setVisibility(View.VISIBLE);
        SQLiteDatabase database=new MyDBHelper(MyApplication.getContext()).getWritableDatabase();
        Cursor cursor=database.rawQuery("select * from County where cityId = ? ", new String[]{String.valueOf(selectedCity.getId())});
        County county;
        if(countyList==null){
            countyList=new ArrayList<>();
        }
        if(cursor.moveToFirst()){
            datalist.clear();
            do{
                county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCityId(cursor.getInt(cursor.getColumnIndex("cityId")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("countyName")));
                county.setWeatherId(cursor.getString(cursor.getColumnIndex("weatherId")));
                LogUtil.d(county.getId()+"..."+county.getCityId()+"..."+county.getCountyName()+"..."+county.getWeatherId());
                countyList.add(county);
                datalist.add(county.getCountyName());
            }while(cursor.moveToNext());
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            queryFromServer(BASE_URL+"/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode(),"county");
        }

        if(cursor!=null){
            cursor.close();
        }

        if(database!=null){
            database.close();
        }

    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

}
