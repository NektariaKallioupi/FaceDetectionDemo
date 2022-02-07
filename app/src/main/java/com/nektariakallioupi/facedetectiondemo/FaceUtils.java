package com.nektariakallioupi.facedetectiondemo;

import com.google.mlkit.vision.face.Face;

public class FaceUtils  {
    private volatile Face face;
    String axeYFacing="straight";
    String axeXFacing="straight";

    public FaceUtils(Face face){
        this.face = face;
    }

    public String checkAxeYFacing(){
        float rotY = face.getHeadEulerAngleY();  // A face with a negative Euler Y angle is looking to the right of the camera, or looking to the left if positive.
        if (rotY < -36.0) {
            axeYFacing="right";
        }else if (rotY > 36.0){
            axeYFacing="left";
        }else{
            if (!(axeYFacing.equals("straight"))){
                axeYFacing="straight";
            }
        }
        return axeYFacing;
    }

    public String checkAxeXFacing(){
        float rotX = face.getHeadEulerAngleX();  // A face with a positive Euler X angle is facing upward.
        if (rotX > 13.0) {
            axeXFacing="upwards";
        }else if (rotX < -13.0){
            axeXFacing="downwards";
        }else{
            if (!(axeXFacing.equals("straight"))){
                axeXFacing="straight";
            }
        }
        return axeXFacing;
    }







}
