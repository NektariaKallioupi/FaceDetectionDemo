package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT;
import static android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;
import com.nektariakallioupi.facedetectiondemo.R;
import com.nektariakallioupi.facedetectiondemo.Utils;
import com.squareup.picasso.Picasso;

public class ClickedNewsDetails extends AppCompatActivity implements View.OnClickListener {
    NewsHeadlines headlines;

    TextView newsTitle, author, timeOfPublishing, newsDetails, newsContent;
    ImageView imageNewsImage;

    Button readMoreBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicked_news_details);
        Utils.hideSystemUI(getWindow().getDecorView());

        newsTitle = (TextView) findViewById(R.id.newsTitleTextView);
        author = (TextView) findViewById(R.id.authorTextView);
        timeOfPublishing = (TextView) findViewById(R.id.timeOfPublishingTextView);
        newsDetails = (TextView) findViewById(R.id.newsDetailsTextView);
        newsContent = (TextView) findViewById(R.id.newsContentTextView);
        imageNewsImage = (ImageView) findViewById(R.id.imageNewsImageView);

        readMoreBtn = (Button) findViewById(R.id.readMoreBtn);

        readMoreBtn.setOnClickListener(this);

        headlines = (NewsHeadlines) getIntent().getSerializableExtra("data");

        initializeElements();

    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.readMoreBtn:
//                Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(headlines.getUrl()));
//                Intent chooser = Intent.createChooser(sendIntent, "Choose Your Browser");
//                if (sendIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(chooser);
//                }
                Intent defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                defaultBrowser.setData(Uri.parse(headlines.getUrl()));
                startActivity(defaultBrowser);
                break;
        }
    }

    public void initializeElements() {
        newsTitle.setText(headlines.getTitle());
        author.setText(headlines.getAuthor());
        timeOfPublishing.setText(headlines.getPublishedAt());
        newsDetails.setText(headlines.getDescription());
        newsContent.setText(headlines.getContent());
        Picasso.get().load(headlines.getUrlToImage()).into(imageNewsImage);

    }

    @Override
    public void onBackPressed() {
        finish();
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