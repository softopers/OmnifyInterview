package com.interview.omnifyinterview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.activity.StoryDetailsActivity;
import com.interview.omnifyinterview.adapter.CommentsAdapter;
import com.interview.omnifyinterview.model.Comment;
import com.interview.omnifyinterview.realm.TopStoryRealmObject;
import com.interview.omnifyinterview.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CommentsFragment extends Fragment {


    @BindView(R.id.recyclerViewComments)
    RecyclerView recyclerViewComments;
    ArrayList<Comment> comments = new ArrayList<>();
    private long storyId;
    private Gson gson;
    private StoryDetailsActivity storyDetailsActivity;
    private CommentsAdapter commentsAdapter;


    public CommentsFragment() {
        // Required empty public constructor
    }

    public static CommentsFragment newInstance(long storyId) {

        Bundle args = new Bundle();

        CommentsFragment fragment = new CommentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewComments.setLayoutManager(linearLayoutManager);

        commentsAdapter = new CommentsAdapter(getActivity(), comments);
        recyclerViewComments.setAdapter(commentsAdapter);

        TopStoryRealmObject topStoryRealmObject = storyDetailsActivity.realm.where(TopStoryRealmObject.class)
                .equalTo("id", storyId)
                .findFirst();


        if (topStoryRealmObject != null) {
            comments.clear();
            String commentsArray = topStoryRealmObject.getKids();

            try {
                JSONArray array = new JSONArray(commentsArray);
                for (int i = 0; i < array.length(); i++) {
                    String url = Constants.GET_STORY + array.getString(i) + Constants.PRINT_PRETTY;
                    requestComments(url);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void requestComments(String url) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET
                , url
                , null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Comment comment = gson.fromJson(response.toString(), Comment.class);
                comments.add(comment);
                commentsAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        storyDetailsActivity.requestQueue.add(request);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        storyDetailsActivity = (StoryDetailsActivity) context;
    }
}
