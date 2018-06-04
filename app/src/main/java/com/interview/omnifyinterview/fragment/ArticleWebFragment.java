package com.interview.omnifyinterview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.activity.StoryDetailsActivity;
import com.interview.omnifyinterview.realm.TopStoryRealmObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleWebFragment extends Fragment {

    @BindView(R.id.webView)
    WebView webView;
    private long storyId;
    private StoryDetailsActivity storyDetailsActivity;

    public ArticleWebFragment() {
        // Required empty public constructor
    }

    public static ArticleWebFragment newInstance(long storyId) {

        Bundle args = new Bundle();

        ArticleWebFragment fragment = new ArticleWebFragment();
        args.putLong("storyId", storyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            storyId = getArguments().getLong("storyId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_article_web, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storyDetailsActivity = (StoryDetailsActivity) context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TopStoryRealmObject topStoryRealmObject = storyDetailsActivity.realm.where(TopStoryRealmObject.class)
                .equalTo("id", storyId)
                .findFirst();

        if (topStoryRealmObject != null) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            WebSettings webSetting = webView.getSettings();
            webSetting.setJavaScriptEnabled(true);
            webSetting.setDisplayZoomControls(true);
            webView.loadUrl(topStoryRealmObject.getUrl());
        }
    }
}
