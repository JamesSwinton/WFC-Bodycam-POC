package com.zebra.jamesswinton.wfcbodycampoc.networking;

import com.zebra.jamesswinton.wfcbodycampoc.pojos.AnnouncerResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface AnnouncerApi {

  // Send Message
  @Multipart
  @POST("gw/announce/msg")
  Call<AnnouncerResponse> sendMessage(
          @Query("api_password") String api_key,
          @Part("message") String message,
          @Part("eid") int eid
  );

  // Send Message
  @Multipart
  @POST("gw/announce/msg")
  Call<AnnouncerResponse> sendImage(
          @Query("api_password") String api_key,
          @Part MultipartBody.Part file,
          @Part("message") RequestBody message,
          @Part("attachment") RequestBody attachment,
          @Part("eid") int eid
  );

  // Send Message
  @Multipart
  @POST("gw/announce/msg")
  Call<AnnouncerResponse> sendVideo(
          @Query("api_password") String api_key,
          @Part MultipartBody.Part file,
          @Part("message") String message,
          @Part("attachment") String attachment,
          @Part("eid") int eid
  );

}
