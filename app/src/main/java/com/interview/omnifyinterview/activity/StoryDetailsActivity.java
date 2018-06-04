package com.interview.omnifyinterview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.volley.RequestQueue;
import com.interview.omnifyinterview.MyApplication;
import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.fragment.ArticleWebFragment;
import com.interview.omnifyinterview.fragment.CommentsFragment;
import com.interview.omnifyinterview.realm.TopStoryRealmObject;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.interview.omnifyinterview.adapter.TopStoriesRealmAdapter.getDurationBreakdown;

public class StoryDetailsActivity extends AppCompatActivity {

    private static final int COMMENTS = 0;
    private static final int ARTICLE_WEB_VIEW = 1;
    @Inject
    public RequestQueue requestQueue;
    @Inject
    public Realm realm;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.textViewTitle)
    AppCompatTextView textViewTitle;
    @BindView(R.id.textViewUrl)
    AppCompatTextView textViewUrl;
    @BindView(R.id.textViewTimeUser)
    AppCompatTextView textViewTimeUser;

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tab;

    private String kidsSize;
    private boolean urlAvailable = false;
    private long storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_details);

        ButterKnife.bind(this);

        ((MyApplication) getApplication()).getComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("");
            }
        });

        storyId = getIntent().getLongExtra("storyId", 0);

        TopStoryRealmObject topStoryRealmObject = realm.where(TopStoryRealmObject.class)
                .equalTo("id", storyId)
                .findFirst();

        if (topStoryRealmObject != null) {
            textViewTitle.setText(topStoryRealmObject.getTitle());
            textViewUrl.setText(topStoryRealmObject.getUrl());
            kidsSize = topStoryRealmObject.getKidsSize();
            long timeMillis = System.currentTimeMillis() / 1000 - Long.parseLong(topStoryRealmObject.getTime());
            textViewTimeUser.setText(String.format("%s ago%s%s", getDurationBreakdown(timeMillis), getString(R.string.dot), topStoryRealmObject.getBy()));

            String url = topStoryRealmObject.getUrl();
            if (url == null || topStoryRealmObject.getUrl().equals("")) {
                urlAvailable = true;
            }
        }

        if (urlAvailable) {
            viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), urlAvailable));
            tab.setupWithViewPager(viewPager);
        } else {
            viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), urlAvailable));
            tab.setupWithViewPager(viewPager);
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private boolean urlAvailable;

        ViewPagerAdapter(FragmentManager fm, boolean urlAvailable) {
            super(fm);
            this.urlAvailable = urlAvailable;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (urlAvailable) {
                if (position == COMMENTS) {
                    fragment = CommentsFragment.newInstance(storyId);
                }
            } else {
                if (position == COMMENTS) {
                    fragment = CommentsFragment.newInstance(storyId);
                } else {
                    fragment = ArticleWebFragment.newInstance(storyId);
                }
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            if (urlAvailable) {
                if (position == COMMENTS) {
                    title = kidsSize + " COMMENTS";
                }
            } else {
                if (position == COMMENTS) {
                    title = kidsSize + " COMMENTS";
                } else {
                    title = "ARTICLE";
                }
            }
            return title;
        }

        @Override
        public int getCount() {
            if (urlAvailable) {
                return 1;
            } else {
                return 2;
            }
        }
    }

}
