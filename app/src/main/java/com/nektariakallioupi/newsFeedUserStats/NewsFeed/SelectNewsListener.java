package com.nektariakallioupi.newsFeedUserStats.NewsFeed;

import com.nektariakallioupi.newsFeedUserStats.Models.NewsHeadlines;

public interface SelectNewsListener {
    void OnNewsClicked(NewsHeadlines headlines);
}
