package com.nektariakallioupi.facedetectiondemo.Authentication;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nektariakallioupi.facedetectiondemo.R;
import com.nektariakallioupi.facedetectiondemo.Utils;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username, password, email;
    private Button signUp, exit;
    private TextView signIn;

    //firebase instance
    private FirebaseAuth mAuth;

    //current user
    FirebaseUser currentUser;

    //database reference
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Utils.hideSystemUI(getWindow().getDecorView());

        username = (EditText) findViewById(R.id.signUpUsernameEditText);
        password = (EditText) findViewById(R.id.signUpPasswordEditText);
        email = (EditText) findViewById(R.id.signUpEmailEditText);

        signUp = (Button) findViewById(R.id.signUpBtn);
        exit = (Button) findViewById(R.id.exitSignUpBtn);

        signIn = (TextView) findViewById(R.id.signInTextView);

        signUp.setOnClickListener(this);
        exit.setOnClickListener(this);
        signIn.setOnClickListener(this);

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Obtaining the Database Reference
        database = FirebaseDatabase.getInstance().getReference("users");

    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.signUpBtn:
                signUpUser();
                break;
            case R.id.exitSignUpBtn:
                finish();
                System.exit(0);
                break;
            case R.id.signInTextView:
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                break;
        }
    }

    private void signUpUser() {

        String givenEmail = email.getText().toString();
        String givenPassword = password.getText().toString();
        String givenUsername = username.getText().toString();

        if (givenEmail.isEmpty()) {
            email.setError("Should not be empty");
            email.requestFocus();
            return;
        }

        if (givenPassword.isEmpty()) {
            password.setError("Should not be empty");
            password.requestFocus();
            return;
        }

        if (givenUsername.isEmpty()) {
            username.setError("Should not be empty");
            username.requestFocus();
            return;
        }

        if (!(Patterns.EMAIL_ADDRESS.matcher(givenEmail).matches())) {
            email.setError("Please provide a valid email");
            email.getText().clear();
            email.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(givenEmail, givenPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Congratulations your account has been successfully created!",
                                    Toast.LENGTH_LONG).show();

                            //get current user
                            currentUser = mAuth.getCurrentUser();

                            // creating user object
                            UserInfo user = new UserInfo(givenUsername);

                            // pushing user to 'users' node using the userId
                            database.child(currentUser.getUid()).setValue(user);

                            // user signed out in order to sign in again
                            mAuth.signOut();
                            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                            finish();

                        } else {

                            // If sign up fails, display a message to the user.
                            Log.e("SignUp", task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "Something went wrong!Please try again.",
                                    Toast.LENGTH_LONG).show();
                            username.getText().clear();
                            email.getText().clear();
                            password.getText().clear();

                        }

                    }
                });

    }

    public void onBackPressed() {

        startActivity(new Intent(this, SignInActivity.class));
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