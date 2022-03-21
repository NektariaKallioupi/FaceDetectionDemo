package com.nektariakallioupi.newsFeedUserStats.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nektariakallioupi.newsFeedUserStats.NewsFeed.NewsFeedActivity;
import com.nektariakallioupi.newsFeedUserStats.R;
import com.nektariakallioupi.newsFeedUserStats.Utils;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private Button signIn, exit;
    private TextView signUp;

    //firebase instance
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Utils.hideSystemUI(getWindow().getDecorView());

        email = (EditText) findViewById(R.id.signInEmailEditText);
        password = (EditText) findViewById(R.id.signInPasswordEditText);
        signIn = (Button) findViewById(R.id.signInBtn);
        exit = (Button) findViewById(R.id.exitSignInBtn);
        signUp = (TextView) findViewById(R.id.signUpTextView);

        signIn.setOnClickListener(this);
        exit.setOnClickListener(this);
        signUp.setOnClickListener(this);

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.signInBtn:
                signInUser();
                break;
            case R.id.exitSignInBtn:
                finish();
                System.exit(0);
                break;
            case R.id.signUpTextView:
                startActivity(new Intent(this, SignUpActivity.class));
                finish();
                break;
        }

    }

    // sign in with email and password using firebase
    public void signInUser() {

        String givenEmail = email.getText().toString();
        String givenPassword = password.getText().toString();

        if (givenEmail.isEmpty()) {
            email.setError("Should not be empty");
            email.requestFocus();
            email.getText().clear();
            password.getText().clear();
            return;
        }

        if (givenPassword.isEmpty()) {
            password.setError("Should not be empty");
            password.requestFocus();
            email.getText().clear();
            password.getText().clear();
            return;
        }

        if (!(Patterns.EMAIL_ADDRESS.matcher(givenEmail).matches())) {
            email.setError("Please provide a valid email");
            email.getText().clear();
            email.requestFocus();
            email.getText().clear();
            password.getText().clear();
            return;
        }

        mAuth.signInWithEmailAndPassword(givenEmail,givenPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            currentUser = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "Welcome!Sign-In confirmed.",
                                    Toast.LENGTH_LONG).show();
                            updateUI(currentUser);

                        } else {

                            // If sign in fails, display a message to the user.
                            Log.i("Exception", task.getException().getMessage());
                            Toast.makeText(getApplicationContext(),"Email or Password incorrect!Please try again"
                                    , Toast.LENGTH_LONG)
                                    .show();
                            email.getText().clear();
                            password.getText().clear();
                        }

                    }
                });

    }

    public void updateUI(FirebaseUser user) {

        if(user!=null) {

            startActivity(new Intent(this, NewsFeedActivity.class));
            finish();

        }else{

            Toast.makeText(getApplicationContext(), "Something went wrong!Please try again.",
                    Toast.LENGTH_LONG).show();
            email.getText().clear();
            password.getText().clear();
        }

    }

    public void onBackPressed() {

        finish();
        System.exit(0);
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