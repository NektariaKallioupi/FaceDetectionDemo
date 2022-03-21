package com.nektariakallioupi.newsFeedUserStats.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nektariakallioupi.newsFeedUserStats.FaceDetection.LiveFaceDetection;
import com.nektariakallioupi.newsFeedUserStats.NewsFeed.NewsFeedActivity;
import com.nektariakallioupi.newsFeedUserStats.R;
import com.nektariakallioupi.newsFeedUserStats.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    TextView emailTextView, usernameTextView;

    //firebase instance
    private FirebaseAuth mAuth;

    //current user
    FirebaseUser currentUser;

    //database reference
    private DatabaseReference database;

    Button accountBackBtn, signOutBtn, liveFaceDetectionBtn, deleteAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Utils.hideSystemUI(getWindow().getDecorView());

        accountBackBtn = (Button) findViewById(R.id.accountBackBtn);
        signOutBtn = (Button) findViewById(R.id.signOutBtn);
        liveFaceDetectionBtn = (Button) findViewById(R.id.liveFaceDetectionBtn);
        deleteAccountBtn = (Button) findViewById(R.id.deleteAccountBtn);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);

        accountBackBtn.setOnClickListener(this);
        signOutBtn.setOnClickListener(this);
        liveFaceDetectionBtn.setOnClickListener(this);
        deleteAccountBtn.setOnClickListener(this);

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        displayUserData();
    }

    private void displayUserData() {

        database = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("username");

        emailTextView.setText("Email : " + currentUser.getEmail());

        // calling add value event listener method
        // for getting the values from database.
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // this method is call to get the realtime
                // updates in the data.

                usernameTextView.setText("Username : " + snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.accountBackBtn:
                startActivity(new Intent(this, NewsFeedActivity.class));
                finish();
                break;
            case R.id.signOutBtn:
                mAuth.signOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                break;
            case R.id.liveFaceDetectionBtn:
                startActivity(new Intent(this, LiveFaceDetection.class));
                finish();
                break;
            case R.id.deleteAccountBtn:
                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Congratulations your account has been successfully deleted!",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(AccountActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong!Please try again.",
                                    Toast.LENGTH_LONG).show();
                            Log.w("Delete Account", task.getException().getMessage());
                        }
                    }
                });
                break;
        }
    }
}