package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;

import java.util.List;

//listener class so we can handle the responses from our main activity
public interface OnFetchDataListener<NewsApiResponse > {

    void onFetchData(List<NewsHeadlines> list,String message);

    void onError(String message);
}
