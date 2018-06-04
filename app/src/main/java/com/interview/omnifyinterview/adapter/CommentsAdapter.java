package com.interview.omnifyinterview.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.interview.omnifyinterview.R;
import com.interview.omnifyinterview.model.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final Context context;
    private ArrayList<Comment> comments;

    public CommentsAdapter(Context context, ArrayList<Comment> comments) {
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.textViewComment.setText(comment.getText() != null && comment.getText().length() > 0 ? Html.fromHtml(comment.getText()) : comment.getText());

        long currentDateTime = System.currentTimeMillis();

        Date date = new Date(currentDateTime);
        SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy - HH:mm", Locale.getDefault());

        holder.textViewDateTimeUser.setText(String.format("%s%s%s", df.format(date), context.getString(R.string.dot), comment.getBy()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textViewDateTimeUser)
        AppCompatTextView textViewDateTimeUser;
        @BindView(R.id.textViewComment)
        AppCompatTextView textViewComment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
