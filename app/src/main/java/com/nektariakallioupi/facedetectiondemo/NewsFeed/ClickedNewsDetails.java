package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT;
import static android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.nektariakallioupi.facedetectiondemo.FaceDetection.FaceUtils;
import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;
import com.nektariakallioupi.facedetectiondemo.R;
import com.nektariakallioupi.facedetectiondemo.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClickedNewsDetails extends AppCompatActivity implements View.OnClickListener {
    NewsHeadlines headlines;

    TextView newsTitle, author, timeOfPublishing, newsDetails, newsContent;
    ImageView imageNewsImage;

    Button readMoreBtn,clickedNewsBackBtn;

    private static final int PERMISSION_REQUEST_CODE = 200;

    //firebase instance
    private FirebaseAuth mAuth;

    //current user
    FirebaseUser currentUser;

    //database reference
    private DatabaseReference database;

    //Face Detection Variables
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

    @RequiresApi(api = Build.VERSION_CODES.P)
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
        clickedNewsBackBtn = (Button) findViewById(R.id.clickedNewsBackBtn);

        readMoreBtn.setOnClickListener(this);
        clickedNewsBackBtn.setOnClickListener(this);

        headlines = (NewsHeadlines) getIntent().getSerializableExtra("data");

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();

        //get current user
        currentUser = mAuth.getCurrentUser();

        //Obtaining the Database Reference
        database = FirebaseDatabase.getInstance().getReference("UserStatsClickedArticles").child(currentUser.getUid()).child(headlines.getTitle());

        //check camera permissions and request them if they are not given
        if (checkPermissions()) {
            cameraInitialization();
       } else {
            requestPermission();
        }

        initializeElements();

    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.readMoreBtn:

                Intent defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                defaultBrowser.setData(Uri.parse(headlines.getUrl()));
                startActivity(defaultBrowser);
                break;
            case  R.id.clickedNewsBackBtn:
                startActivity(new Intent(this, NewsFeedActivity.class));
                finish();
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

//////////////////////////////////////////Face Detection Methods/////////////////////////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void cameraInitialization() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        //Unbinds all use cases from the lifecycle and removes them from CameraX.
        cameraProvider.unbindAll();

        //Image Analysis Use Case
        //CameraX receives a new image before the application finishes processing, the new image is saved to the same buffer, overwriting the previous image
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

                @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, rotationDegrees);

                    FaceDetectorOptions options =
                            new FaceDetectorOptions.Builder()
                                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                    .setContourMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                    .enableTracking()
                                    .build();

                    FaceDetector detector = FaceDetection.getClient(options);

                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @SuppressLint("RestrictedApi")
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            processFaceContourDetectionResult(faces);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception

                                            e.printStackTrace();
                                        }
                                    })
                            .addOnCompleteListener(
                                    new OnCompleteListener<List<Face>>() {
                                        @Override
                                        public void onComplete(@NonNull Task<List<Face>> task) {
                                            imageProxy.close();
                                        }
                                    });
                }
            }
        });

        cameraProvider.bindToLifecycle((LifecycleOwner) this, lensFacing, imageAnalysis);

    }

    @SuppressLint("RestrictedApi")
    private void processFaceContourDetectionResult(List<Face> faces) {

        // Task completed successfully
        if (faces.size() == 0) {
            //no face was detected
            return;
        } else {
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.get(i);

                FaceUtils faceUtils = new FaceUtils(face);

                //get current user
                currentUser = mAuth.getCurrentUser();

                String frame = database.push().getKey();

                String axeXFacing = faceUtils.checkAxeXFacing();
                String axeYFacing = faceUtils.checkAxeYFacing();

                float rotX = face.getHeadEulerAngleX();
                float rotY = face.getHeadEulerAngleY();
                float rotZ = face.getHeadEulerAngleZ();

                database.child("url").setValue(headlines.getUrl());

                database.child("frames").child(frame).child("rotX").setValue(rotX);
                database.child("frames").child(frame).child("rotY").setValue(rotY);
                database.child("frames").child(frame).child("rotZ").setValue(rotZ);

                database.child("frames").child(frame).child("axeXFacing").setValue(axeXFacing);
                database.child("frames").child(frame).child("axeYFacing").setValue(axeYFacing);

                // smiling probability
                if (face.getSmilingProbability() != null) {
                    float smileProb = face.getSmilingProbability();
                    database.child("frames").child(frame).child("smilingProbability").setValue(smileProb);
                }

            }
        }
    }

//////////////////////////////////////////CameraPermissions/////////////////////////////////////////////////

    //check if permissions are granted or not
    private boolean checkPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    //requesting camera permission
    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    //start camera
                    cameraInitialization();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}