package com.example.lqtr;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebAPI {
    private static WebAPI instance;
    private Retrofit retrofit;

    public static WebAPI getInstance() {
        if (instance == null) {
            instance = new WebAPI();
        }

        return instance;
    }

    private WebAPI() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://davidbarke.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public <T> T getService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
