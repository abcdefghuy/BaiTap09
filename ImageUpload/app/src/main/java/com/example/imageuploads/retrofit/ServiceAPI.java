package com.example.imageuploads.retrofit;

import android.os.Message;

import com.example.imageuploads.constant.Const;
import com.example.imageuploads.model.ImageUpload;
import com.example.imageuploads.model.MessageResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceAPI {
    public static final String BASE_URL = "http://app.iotstar.vn:8081/appfoods/";
    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss")
                    .setLenient().create();

    ServiceAPI serviceAPI = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ServiceAPI.class);
    // Define your API endpoints here
    // For example:
    @Multipart
    @POST("upload.php")
    Call<List<ImageUpload>> upload(@Part(Const.MY_USERNAME)RequestBody username, @Part MultipartBody.Part avatar);

    @Multipart
    @POST("updateimages.php")
    Call<MessageResponse> upload1(@Part(Const.MY_ID)RequestBody username, @Part MultipartBody.Part avatar);
}
