package com.example.android.todo.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.todo.R;
import com.example.android.todo.data.database.TodoEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    public interface OnTodoClick {
        void onTodoClick(int todoId);
    }

    private Context mContext;
    private List<TodoEntry> mTodoList;
    private OnTodoClick mListener;
    private long mLastClickTime = 0;

    public TodoAdapter(Context context, OnTodoClick listener) {
        mContext = context;
        mListener = listener;
        mTodoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public TodoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date dateObject = mTodoList.get(position).getDate();
        String date = DateFormat.getDateTimeInstance().format(dateObject);
        String description = mTodoList.get(position).getDescription();
        int importance = mTodoList.get(position).getImportance();

        holder.mDateTv.setText(date);
        holder.mDescriptionTv.setText(description);
        holder.mImportanceTv.setText(String.valueOf(importance));

        GradientDrawable importanceCircle = (GradientDrawable) holder.mImportanceTv.getBackground();
        int importanceColor = getImportanceColor(importance);
        importanceCircle.setColor(importanceColor);
    }

    private int getImportanceColor(int importance) {
        int importanceColor;
        switch (importance) {
            case 1:
                importanceColor = ContextCompat.getColor(mContext, R.color.highImportance);
                break;
            case 2:
                importanceColor = ContextCompat.getColor(mContext, R.color.lowImportance);
                break;
            case 3:
                importanceColor = ContextCompat.getColor(mContext, R.color.lowImportance);
                break;
            default:
                importanceColor = ContextCompat.getColor(mContext, R.color.highImportance);
                break;
        }
        return importanceColor;
    }

    public void setData(final List<TodoEntry> newList) {
        if (mTodoList.isEmpty()) {
            mTodoList = newList;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mTodoList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldPosition, int newPosition) {
                    return mTodoList.get(oldPosition).getId() == newList.get(newPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldPosition, int newPosition) {
                    String newDescription = newList.get(newPosition).getDescription();
                    String oldDescription = mTodoList.get(oldPosition).getDescription();
                    int newImportance = newList.get(newPosition).getImportance();
                    int oldImportance = mTodoList.get(oldPosition).getImportance();
                    Date newDate = newList.get(newPosition).getDate();
                    Date oldDate = mTodoList.get(oldPosition).getDate();
                    return newDescription.equals(oldDescription) &&
                            newImportance == oldImportance &&
                            newDate.equals(oldDate);
                }
            });
            mTodoList = newList;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public List<TodoEntry> getTodoList() {
        return mTodoList;
    }

    @Override
    public int getItemCount() {
        return mTodoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mDateTv;
        TextView mDescriptionTv;
        TextView mImportanceTv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            mDateTv = itemView.findViewById(R.id.date_tv);
            mDescriptionTv = itemView.findViewById(R.id.description_tv);
            mImportanceTv = itemView.findViewById(R.id.importance_tv);
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            mListener.onTodoClick(mTodoList.get(getAdapterPosition()).getId());
        }
    }
}
