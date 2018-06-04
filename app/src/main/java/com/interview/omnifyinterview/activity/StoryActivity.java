package com.interview.omnifyinterview.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.interview.omnifyinterview.MyApplication;
import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.adapter.TopStoriesRealmAdapter;
import com.interview.omnifyinterview.model.TopStory;
import com.interview.omnifyinterview.realm.TopStoryRealmObject;
import com.interview.omnifyinterview.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;

public class StoryActivity extends AppCompatActivity {

    private static final String TAG = "StoryActivity";
    private static final String STATE_LAST_UPDATED = "state:lastUpdated";

    final Handler mHandler = new Handler();
    @Inject
    RequestQueue requestQueue;
    @Inject
    Realm realm;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.realmRecyclerViewStories)
    RealmRecyclerView realmRecyclerViewStories;
    private FirebaseAuth firebaseAuth;
    private Gson gson;
    private Long lastUpdated;

    /* runnable for check last updated time */
    private final Runnable lastUpdateTask = new Runnable() {
        @Override
        public void run() {
            if (lastUpdated == null) {
                return;
            }
            //noinspection ConstantConditions
            if (toolbar == null) {
                return;
            }
            if (hasConnection(StoryActivity.this)) {
                toolbar.setSubtitle("Updated " +
                        DateUtils.getRelativeTimeSpanString(lastUpdated,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL));
                mHandler.postAtTime(this, SystemClock.uptimeMillis() + DateUtils.MINUTE_IN_MILLIS);
            } else {
                toolbar.setSubtitle("Offline");
            }
        }
    };

    /* check if connection is available */
    public static boolean hasConnection(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        if (savedInstanceState != null) {
            lastUpdated = savedInstanceState.getLong(STATE_LAST_UPDATED);
        }
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ((MyApplication) getApplication()).getComponent().inject(this);

        firebaseAuth = FirebaseAuth.getInstance();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        RealmResults<TopStoryRealmObject> topStoryRealmObjects = realm
                .where(TopStoryRealmObject.class)
                .findAll();

        TopStoriesRealmAdapter topStoriesRealmAdapter = new TopStoriesRealmAdapter(this
                , topStoryRealmObjects
                , true
                , true);

        topStoriesRealmAdapter.setTopStoryRealmObjectOnItemClickListener(new TopStoriesRealmAdapter.OnItemClickListener<TopStoryRealmObject>() {
            @Override
            public void onItemClickListener(TopStoryRealmObject topStoryRealmObject) {
                startActivity(new Intent(getApplicationContext(), StoryDetailsActivity.class).putExtra("storyId", topStoryRealmObject.getId()));
            }
        });

        realmRecyclerViewStories.setAdapter(topStoriesRealmAdapter);
        realmRecyclerViewStories.setOnRefreshListener(new RealmRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestForTopStories(Constants.TOP_STORIES);
            }
        });

        requestForTopStories(Constants.TOP_STORIES);
    }

    /* get request for fetching top stories */
    private void requestForTopStories(String url) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET
                , url
                , null
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                lastUpdated = System.currentTimeMillis();
                mHandler.removeCallbacks(lastUpdateTask);
                mHandler.post(lastUpdateTask);

                realmRecyclerViewStories.setRefreshing(false);
                Log.d(TAG, "onResponse: " + response.toString());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        long storyId = response.getLong(i);
                        String url = Constants.GET_STORY + storyId + Constants.PRINT_PRETTY;
                        TopStoryRealmObject topStoryRealmObject = realm.where(TopStoryRealmObject.class)
                                .equalTo("id", storyId)
                                .findFirst();

                        if (topStoryRealmObject == null) {
                            requestForStroyDetails(url);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                realmRecyclerViewStories.setRefreshing(false);
            }
        });
        requestQueue.add(request);
    }

    /* get request for fetching a story details */
    private void requestForStroyDetails(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , url
                , null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: " + response.toString());
                final TopStory topStory = gson.fromJson(response.toString(), TopStory.class);
                String kidsString = "";
                try {
                    kidsString = response.getString("kids");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String finalKidsString = kidsString;
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        TopStoryRealmObject hackerStory = realm.createObject(TopStoryRealmObject.class, topStory.getId());
                        hackerStory.setTitle(topStory.getTitle());
                        hackerStory.setScore(topStory.getScore().toString());
                        hackerStory.setUrl(topStory.getUrl());
                        hackerStory.setTime(topStory.getTime().toString());
                        hackerStory.setBy(topStory.getBy());
                        hackerStory.setKids(finalKidsString);
                        hackerStory.setKidsSize(String.valueOf(topStory.getKids().size()));
                        realm.copyToRealmOrUpdate(hackerStory);
                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            Intent loginIntent = new Intent(StoryActivity.this, MainActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.removeCallbacks(lastUpdateTask);
        mHandler.post(lastUpdateTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(lastUpdateTask);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastUpdated != null) {
            outState.putLong(STATE_LAST_UPDATED, lastUpdated);
        }
    }
}
