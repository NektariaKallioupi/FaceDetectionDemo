package com.nektariakallioupi.facedetectiondemo;

import static androidx.camera.view.PreviewView.ScaleType.FILL_CENTER;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.nfc.Tag;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainTab extends AppCompatActivity implements View.OnClickListener {

    int numberOfFaces = 0;

    private static final int PERMISSION_REQUEST_CODE = 200;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;

    private Button exitBtn, reverseBtn;
    PreviewView cameraPreviewView;

    private GraphicOverlay mGraphicOverlay;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.hideSystemUI(getWindow().getDecorView());

        exitBtn = (Button) findViewById(R.id.exitBtn);
        reverseBtn = (Button) findViewById(R.id.reverseBtn);
        cameraPreviewView = (PreviewView) findViewById(R.id.cameraView);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.graphic_overlay);

        cameraPreviewView.setScaleType(FILL_CENTER);

        if (checkPermissions()) {
            cameraInitialization();
        } else {
            requestPermission();
        }

        exitBtn.setOnClickListener(this);
        reverseBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Utils.preventTwoClick(v);
        switch (v.getId()) {
            case R.id.reverseBtn:
                try {
                    flipCamera();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.exitBtn:
                finish();
                System.exit(0);
                break;
        }

    }

    private void flipCamera() throws InterruptedException {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA;
        } else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) {
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA;
        }
        bindPreview(cameraProvider);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////

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

        //Preview Use Case
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

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
                                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                                    .enableTracking()
                                    .build();

                    FaceDetector detector = FaceDetection.getClient(options);

                    detector.process(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @SuppressLint("RestrictedApi")
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            mGraphicOverlay.setCameraInfo(imageProxy.getWidth(),imageProxy.getHeight(),lensFacing.getLensFacing());
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

        cameraProvider.bindToLifecycle((LifecycleOwner) this, lensFacing, imageAnalysis, preview);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("RestrictedApi")
    private void processFaceContourDetectionResult(List<Face> faces) {

        // Task completed successfully
        if (faces.size() == 0) {
            //Toast.makeText(getApplicationContext(), "No face found", Toast.LENGTH_SHORT).show();
            mGraphicOverlay.clear();
            return;
        } else {
            mGraphicOverlay.clear();
            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.get(i);
                FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
                mGraphicOverlay.add(faceGraphic);
                faceGraphic.updateFace(face);
            }
        }
    }
///////////////////////////////////////CameraPermissions/////////////////////////////////////////////////

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