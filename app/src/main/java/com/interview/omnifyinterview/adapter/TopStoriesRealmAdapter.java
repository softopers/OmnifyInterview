package com.interview.omnifyinterview.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;

import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.realm.TopStoryRealmObject;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

public class TopStoriesRealmAdapter extends RealmBasedRecyclerViewAdapter<TopStoryRealmObject, TopStoriesRealmAdapter.TopStoryViewHolder> {
    private OnItemClickListener<TopStoryRealmObject> topStoryRealmObjectOnItemClickListener;

    public TopStoriesRealmAdapter(Context context, RealmResults<TopStoryRealmObject> realmResults, boolean automaticUpdate, boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) {
            sb.append(days);
            sb.append(days > 1 ? " Days " : " Day ");
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append(hours > 1 ? " Hours " : " Hour ");
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append(minutes > 1 ? " Minutes " : " Minute ");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(seconds > 1 ? " Seconds" : " Second");
        }
        return (sb.toString());
    }

    public void setTopStoryRealmObjectOnItemClickListener(OnItemClickListener<TopStoryRealmObject> topStoryRealmObjectOnItemClickListener) {
        this.topStoryRealmObjectOnItemClickListener = topStoryRealmObjectOnItemClickListener;
    }

    @Override
    public TopStoryViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.list_item_top_story, viewGroup, false);
        return new TopStoryViewHolder(view);
    }

    @Override
    public void onBindRealmViewHolder(TopStoryViewHolder viewHolder, int position) {
        final TopStoryRealmObject topStoryRealmObject = realmResults.get(position);
        viewHolder.textViewScore.setText(topStoryRealmObject.getScore());
        viewHolder.textViewTitle.setText(topStoryRealmObject.getTitle());
        viewHolder.textViewUrl.setText(topStoryRealmObject.getUrl());
        viewHolder.textViewComments.setText(topStoryRealmObject.getKidsSize());

        long timeMillis = System.currentTimeMillis() / 1000 - Long.parseLong(topStoryRealmObject.getTime());

        viewHolder.textViewTimeUser.setText(String.format("%s ago%s%s", getDurationBreakdown(timeMillis), getContext().getString(R.string.dot), topStoryRealmObject.getBy()));
    }

    public interface OnItemClickListener<T> {
        void onItemClickListener(T t);
    }

    public class TopStoryViewHolder extends RealmViewHolder {

        @BindView(R.id.textViewScore)
        AppCompatTextView textViewScore;
        @BindView(R.id.textViewTitle)
        AppCompatTextView textViewTitle;
        @BindView(R.id.textViewUrl)
        AppCompatTextView textViewUrl;
        @BindView(R.id.textViewComments)
        AppCompatTextView textViewComments;
        @BindView(R.id.textViewTimeUser)
        AppCompatTextView textViewTimeUser;

        public TopStoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (topStoryRealmObjectOnItemClickListener != null) {
                        topStoryRealmObjectOnItemClickListener.onItemClickListener(realmResults.get(getAdapterPosition()));
                    }
                }
            });

        }
    }
}
