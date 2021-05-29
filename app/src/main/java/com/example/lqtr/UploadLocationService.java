package com.example.lqtr;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UploadLocationService {
    @POST("add_location_ts")
    Call<Object> addLocationTs(
            @Header("Authorization") String authorization,
            @Query("lat") Double lat,
            @Query("lon") Double lon,
            @Query("ts") Long ts
    );
}
