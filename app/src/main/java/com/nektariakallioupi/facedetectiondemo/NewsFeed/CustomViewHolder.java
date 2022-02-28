package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.nektariakallioupi.facedetectiondemo.R;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    ConstraintLayout headlineLayout;

    TextView titleTextView,sourceTextView ;

    ImageView headlineImageView;

    CardView cardView;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.cardView);
        headlineLayout = itemView.findViewById(R.id.headlineLayout);
        titleTextView = itemView.findViewById(R.id.titleTextView);
        sourceTextView = itemView.findViewById(R.id.sourceTextView);
        headlineImageView = itemView.findViewById(R.id.headlineImageView);

    }
}
