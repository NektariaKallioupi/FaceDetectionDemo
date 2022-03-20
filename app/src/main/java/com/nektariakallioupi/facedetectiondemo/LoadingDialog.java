package com.nektariakallioupi.facedetectiondemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {

    private Activity activity;

    private AlertDialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    //show loading progress circle
    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        //user cant interact with the app when this dialog appears
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    //dismiss loading progress circle from screen
    public void dismissDialog(){
        dialog.dismiss();
    }

}
