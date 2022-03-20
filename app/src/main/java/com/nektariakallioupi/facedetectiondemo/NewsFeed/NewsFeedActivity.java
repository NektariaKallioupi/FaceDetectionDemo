package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.nektariakallioupi.facedetectiondemo.Authentication.AccountActivity;
import com.nektariakallioupi.facedetectiondemo.FaceDetection.FaceContourGraphic;
import com.nektariakallioupi.facedetectiondemo.FaceDetection.FaceUtils;
import com.nektariakallioupi.facedetectiondemo.LoadingDialog;
import com.nektariakallioupi.facedetectiondemo.LoadingTab;
import com.nektariakallioupi.facedetectiondemo.Models.NewsApiResponse;
import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;
import com.nektariakallioupi.facedetectiondemo.R;
import com.nektariakallioupi.facedetectiondemo.Utils;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class NewsFeedActivity extends AppCompatActivity implements SelectNewsListener, View.OnClickListener {

    private LoadingDialog loadingDialog;

    private RecyclerView recyclerView;
    private CustomAdapter adapter;

    private RequestsManager manager;

    private Button businessBtn, entertainmentBtn, generalBtn, healthBtn, scienceBtn, sportsBtn, technologyBtn, exitBtn, accountBtn;

    private SearchView searchBarSearchView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String category;

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
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        businessBtn.setOnClickListener(this);
        entertainmentBtn.setOnClickListener(this);
        generalBtn.setOnClickListener(this);
        healthBtn.setOnClickListener(this);
        scienceBtn.setOnClickListener(this);
        sportsBtn.setOnClickListener(this);
        technologyBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        accountBtn.setOnClickListener(this);

        //initializing the firebase instance
        mAuth = FirebaseAuth.getInstance();
        //Obtaining the Database Reference
        database = FirebaseDatabase.getInstance().getReference("UserStats");

        //default category -> general
        category = "general";

        //on search
        searchBarSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBarSearchView.clearFocus();
                fetchNewsPerCategory("general", query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsPerCategory(category, null);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //initialize loading bar
        loadingDialog = new LoadingDialog(this);

        fetchNewsPerCategory(category, null);

        //check camera permissions and request them if they are not given
        if (checkPermissions()) {
            cameraInitialization();
        } else {
            requestPermission();
        }
    }

    public void fetchNewsPerCategory(String category, String query) {

        //show loading bar while fetching data
        loadingDialog.startLoadingDialog();
        //request data
        manager = new RequestsManager(this);
        manager.getNewsHeadlines(listener, category, query);

        //search Bar gets initialized if another category is clicked
        if ((query == null)) {
            //empty searchBar
            searchBarSearchView.setQuery("", false);
            searchBarSearchView.clearFocus();
        }

        manageBtnColours(category);
    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.businessBtn:
                category = "business";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.entertainmentBtn:
                category = "entertainment";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.generalBtn:
                category = "general";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.healthBtn:
                category = "health";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.scienceBtn:
                category = "science";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.sportsBtn:
                category = "sports";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.technologyBtn:
                category = "technology";
                fetchNewsPerCategory(category, null);
                break;
            case R.id.exitNewsBtn:
                finish();
                break;
            case R.id.accountBtn:
                startActivity(new Intent(this, AccountActivity.class));
                finish();
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
                //empty searchBar
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

                String axeXFacing = faceUtils.checkAxeXFacing();
                String axeYFacing = faceUtils.checkAxeYFacing();

                float rotX = face.getHeadEulerAngleX();
                float rotY = face.getHeadEulerAngleY();
                float rotZ = face.getHeadEulerAngleZ();

//                //get current user
//                currentUser = mAuth.getCurrentUser();
//
//                String frame = database.push().getKey();
//
//                database.child(currentUser.getUid()).child("frames").child(frame).child("rotX").setValue(rotX);
//                database.child(currentUser.getUid()).child("frames").child(frame).child("rotY").setValue(rotY);
//                database.child(currentUser.getUid()).child("frames").child(frame).child("rotZ").setValue(rotZ);
//
//                database.child(currentUser.getUid()).child("frames").child(frame).child("axeXFacing").setValue(axeXFacing);
//                database.child(currentUser.getUid()).child("frames").child(frame).child("axeYFacing").setValue(axeYFacing);

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