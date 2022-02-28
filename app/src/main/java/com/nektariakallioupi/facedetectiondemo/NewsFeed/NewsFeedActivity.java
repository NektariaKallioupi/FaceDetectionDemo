package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nektariakallioupi.facedetectiondemo.LoadingDialog;
import com.nektariakallioupi.facedetectiondemo.Models.NewsApiResponse;
import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;
import com.nektariakallioupi.facedetectiondemo.R;
import com.nektariakallioupi.facedetectiondemo.Utils;

import java.util.List;

public class NewsFeedActivity extends AppCompatActivity implements SelectNewsListener, View.OnClickListener {

    private LoadingDialog loadingDialog;

    private RecyclerView recyclerView;
    private CustomAdapter adapter;

    private RequestsManager manager;

    private Button businessBtn, entertainmentBtn, generalBtn, healthBtn, scienceBtn, sportsBtn, technologyBtn, exitBtn, accountBtn;

    private SearchView searchBarSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Utils.hideSystemUI(getWindow().getDecorView());

        businessBtn = (Button) findViewById(R.id.businessBtn);
        entertainmentBtn = (Button) findViewById(R.id.entertainmentBtn);
        generalBtn = (Button) findViewById(R.id.generalBtn);
        healthBtn = (Button) findViewById(R.id.healthBtn);
        scienceBtn = (Button) findViewById(R.id.scienceBtn);
        sportsBtn = (Button) findViewById(R.id.sportsBtn);
        technologyBtn = (Button) findViewById(R.id.technologyBtn);
        exitBtn = (Button) findViewById(R.id.exitNewsBtn);
        accountBtn = (Button) findViewById(R.id.accountBtn);
        searchBarSearchView = (SearchView) findViewById(R.id.searchBarSearchView);

        businessBtn.setOnClickListener(this);
        entertainmentBtn.setOnClickListener(this);
        generalBtn.setOnClickListener(this);
        healthBtn.setOnClickListener(this);
        scienceBtn.setOnClickListener(this);
        sportsBtn.setOnClickListener(this);
        technologyBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        accountBtn.setOnClickListener(this);

        //on search
        searchBarSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBarSearchView.clearFocus();
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(NewsFeedActivity.this);
                manager.getNewsHeadlines(listener, "general", query);
                manageBtnColours("general");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //show loading bar while fetching data
        loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDialog();

        manager = new RequestsManager(this);
        //default category -> general
        manager.getNewsHeadlines(listener, "general", null);
        manageBtnColours("general");
    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.businessBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "business", null);
                manageBtnColours("business");
                break;
            case R.id.entertainmentBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "entertainment", null);
                manageBtnColours("entertainment");
                break;
            case R.id.generalBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "general", null);
                manageBtnColours("general");
                break;
            case R.id.healthBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "health", null);
                manageBtnColours("health");
                break;
            case R.id.scienceBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "science", null);
                manageBtnColours("science");
                break;
            case R.id.sportsBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "sports", null);
                manageBtnColours("sports");
                break;
            case R.id.technologyBtn:
                loadingDialog.startLoadingDialog();
                manager = new RequestsManager(this);
                manager.getNewsHeadlines(listener, "technology", null);
                manageBtnColours("technology");
                break;
            case R.id.exitNewsBtn:
                finish();
                break;
            case R.id.accountBtn:

                break;
        }

    }

    //show which category is chosen by changing the color of the counterpart btn
    public void manageBtnColours(String category) {
        businessBtn.setBackgroundColor(0xFF03A9F4);
        entertainmentBtn.setBackgroundColor(0xFF03A9F4);
        generalBtn.setBackgroundColor(0xFF03A9F4);
        healthBtn.setBackgroundColor(0xFF03A9F4);
        scienceBtn.setBackgroundColor(0xFF03A9F4);
        sportsBtn.setBackgroundColor(0xFF03A9F4);
        technologyBtn.setBackgroundColor(0xFF03A9F4);

        if (category.equals("business")) {
            businessBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("entertainment")) {
            entertainmentBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("general")) {
            generalBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("health")) {
            healthBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("science")) {
            scienceBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("sports")) {
            sportsBtn.setBackgroundColor(Color.MAGENTA);
        } else if (category.equals("technology")) {
            technologyBtn.setBackgroundColor(Color.MAGENTA);
        }
    }

    //creation of listener
    private final OnFetchDataListener<NewsApiResponse> listener = new OnFetchDataListener<NewsApiResponse>() {
        @Override
        public void onFetchData(List<NewsHeadlines> list, String message) {
            if (list.isEmpty()) {
                //dismiss loading bar when data fetched
                loadingDialog.dismissDialog();
                searchBarSearchView.setQuery("", false);
                searchBarSearchView.clearFocus();
                Toast.makeText(NewsFeedActivity.this, "No data found!", Toast.LENGTH_LONG).show();
            } else {
                showNews(list);
                //dismiss loading bar when data fetched
                loadingDialog.dismissDialog();
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(NewsFeedActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
        }
    };

    private void showNews(List<NewsHeadlines> list) {
        recyclerView = (RecyclerView) findViewById(R.id.newsFeedRecyclerView);
        //Avoid unnecessary layout passes by setting setHasFixedSize to true when changing the contents of the adapter does not change it's height or the width.
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1)); // 1 stands for 1 cell per row

        adapter = new CustomAdapter(this, list, this);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void OnNewsClicked(NewsHeadlines headlines) {
        startActivity(new Intent(this, ClickedNewsDetails.class).putExtra("data", headlines));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideSystemUI(getWindow().getDecorView());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideSystemUI(getWindow().getDecorView());
    }
}