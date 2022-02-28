package com.nektariakallioupi.facedetectiondemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nektariakallioupi.facedetectiondemo.Authentication.SignInActivity;
import com.nektariakallioupi.facedetectiondemo.NewsFeed.NewsFeedActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingTab extends AppCompatActivity {

    //firebase instance
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_tab);
        Utils.hideSystemUI(getWindow().getDecorView());

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // this code will be executed after 2 seconds
                currentUser=mAuth.getCurrentUser();


                //checks is a user has logged into the app or not
                if (currentUser != null) {
                    // User is signed in
                    //redirection to the newsfeed activity
                    startActivity(new Intent(LoadingTab.this , NewsFeedActivity.class));
                    finish();


                } else {
                    // No user is signed in
                    //redirection to login page
                    startActivity(new Intent(LoadingTab.this , SignInActivity.class));
                    finish();
                }

                // finish();
            }
        }, 2000);

    }

}