package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import android.content.Context;
import android.widget.Toast;

import com.nektariakallioupi.facedetectiondemo.Models.NewsApiResponse;
import com.nektariakallioupi.facedetectiondemo.NewsFeed.OnFetchDataListener;
import com.nektariakallioupi.facedetectiondemo.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RequestsManager {

    //the base url of our api
    public static final String BASE_URL = "https://newsapi.org/v2/";

    Context context;
    //Retrofit instance
    Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

    //constructor
    public RequestsManager(Context context) {
        this.context = context;
    }

    //Here we manage our api calls
    public interface CallNewsApi {

        // request to the API to get data
        @GET("top-headlines")
        Call<NewsApiResponse> callHeadlines(@Query("country") String country,
                                            @Query("category") String category,
                                            @Query("q") String query,
                                            @Query("apiKey") String api_key);

    }


    public void getNewsHeadlines(OnFetchDataListener listener, String category, String query) {

        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);

        Call<NewsApiResponse> call = callNewsApi.callHeadlines("us", category, query, context.getString(R.string.api_key));

        try {
            call.enqueue(new Callback<NewsApiResponse>() {
                @Override
                public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(context, "Fetch Data Error!", Toast.LENGTH_SHORT).show();
                    }

                    listener.onFetchData(response.body().getArticles(),response.message());

                }

                @Override
                public void onFailure(Call<NewsApiResponse> call, Throwable t) {

                    listener.onError("Request Failed");

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
