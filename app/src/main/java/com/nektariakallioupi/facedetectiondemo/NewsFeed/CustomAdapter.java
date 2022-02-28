package com.nektariakallioupi.facedetectiondemo.NewsFeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nektariakallioupi.facedetectiondemo.Models.NewsHeadlines;
import com.nektariakallioupi.facedetectiondemo.NewsFeed.CustomViewHolder;
import com.nektariakallioupi.facedetectiondemo.NewsFeed.SelectNewsListener;
import com.nektariakallioupi.facedetectiondemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {

    private Context context;
    private List<NewsHeadlines> headlines;
    private SelectNewsListener onClickListener;

    public CustomAdapter(Context context, List<NewsHeadlines> headlines,SelectNewsListener onClickListener) {
        this.context = context;
        this.headlines = headlines;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.headline_list_items,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
           holder.titleTextView.setText(headlines.get(position).getTitle());
           holder.sourceTextView.setText(headlines.get(position).getSource().getName());

           //if the news headline has an available image -> loaded it,else show the default one.
           if(headlines.get(position).getUrlToImage()!=null){
               Picasso.get().load(headlines.get(position).getUrlToImage()).into(holder.headlineImageView);
           }

           holder.cardView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   onClickListener.OnNewsClicked(headlines.get(position));
               }
           });
    }

    @Override
    public int getItemCount() {
        return headlines.size();
    }
}
