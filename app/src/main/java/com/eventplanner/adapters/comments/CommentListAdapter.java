package com.eventplanner.adapters.comments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eventplanner.R;

import com.eventplanner.model.responses.comments.GetCommentPreviewResponse;

import java.util.List;

public class CommentListAdapter extends ArrayAdapter<GetCommentPreviewResponse> {
    public CommentListAdapter(Context context, List<GetCommentPreviewResponse> comments) {
        super(context, 0, comments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_card, parent, false);
        }

        GetCommentPreviewResponse comment = getItem(position);

        TextView commentText = convertView.findViewById(R.id.comment);
        TextView commenter = convertView.findViewById(R.id.commenter);
        TextView commented = convertView.findViewById(R.id.commented);

        commentText.setText("\"" + comment.getContent() + "\"");
        commenter.setText("- " + comment.getCommenterName());
        commented.setText(comment.getSolutionName());

        return convertView;
    }
}
