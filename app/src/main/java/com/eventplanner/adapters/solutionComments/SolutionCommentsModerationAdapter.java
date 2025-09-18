package com.eventplanner.adapters.solutionComments;

import static androidx.core.content.ContextCompat.getString;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.model.enums.RequestStatus;
import com.google.android.material.button.MaterialButton;
import com.eventplanner.R;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentResponse;

import java.util.List;

public class SolutionCommentsModerationAdapter extends RecyclerView.Adapter<SolutionCommentsModerationAdapter.CommentViewHolder> {

    public interface CommentActionListener {
        void onStatusChange(GetSolutionCommentResponse comment, RequestStatus newStatus);
        void onDelete(GetSolutionCommentResponse comment);
        void onGoToSolution(long solutionId);
        void onGoToUser(long userId);
    }

    private List<GetSolutionCommentResponse> comments;
    private CommentActionListener listener;

    public SolutionCommentsModerationAdapter(List<GetSolutionCommentResponse> comments, CommentActionListener listener) {
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solution_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        GetSolutionCommentResponse comment = comments.get(position);

        holder.idText.setText(String.format(getString(holder.contentText.getContext(), R.string.comment_id), comment.getId()));
        holder.isDeletedText.setText(String.format(getString(holder.contentText.getContext(), R.string.is_deleted), comment.getDeleted().toString()));
        holder.contentText.setText(comment.getContent());
        holder.solutionLink.setText(String.format(getString(holder.contentText.getContext(), R.string.solution_id), comment.getSolutionId()));
        holder.commenterLink.setText(String.format(getString(holder.contentText.getContext(), R.string.user_id), comment.getCommenterId()));
        holder.statusSpinner.setSelection(getStatusIndex(comment.getStatus().toString()));

        holder.solutionLink.setOnClickListener(v -> listener.onGoToSolution(comment.getSolutionId()));
        holder.commenterLink.setOnClickListener(v -> listener.onGoToUser(comment.getCommenterId()));

        holder.updateButton.setOnClickListener(v -> {
            String newStatus = holder.statusSpinner.getSelectedItem().toString();
            listener.onStatusChange(comment, RequestStatus.valueOf(newStatus));
        });

        holder.deleteButton.setOnClickListener(v -> listener.onDelete(comment));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private int getStatusIndex(String status) {
        switch (status) {
            case "PENDING": return 1;
            case "ACCEPTED": return 2;
            case "REJECTED": return 3;
            default: return 0;
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView idText, contentText, solutionLink, commenterLink, isDeletedText;
        Spinner statusSpinner;
        MaterialButton updateButton, deleteButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            idText = itemView.findViewById(R.id.commentIdText);
            isDeletedText = itemView.findViewById(R.id.commentIsDeletedText);
            contentText = itemView.findViewById(R.id.commentContentText);
            solutionLink = itemView.findViewById(R.id.commentSolutionLink);
            commenterLink = itemView.findViewById(R.id.commentCommenterLink);
            statusSpinner = itemView.findViewById(R.id.commentStatusSpinner);
            updateButton = itemView.findViewById(R.id.updateStatusButton);
            deleteButton = itemView.findViewById(R.id.deleteCommentButton);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(itemView.getContext(),
                    R.array.comment_statuses, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);
        }
    }
}
