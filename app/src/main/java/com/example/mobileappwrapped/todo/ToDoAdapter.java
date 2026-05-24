package com.example.mobileappwrapped.todo;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileappwrapped.R;

import java.util.List;
import java.util.Locale;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todolist;
    private final DatabaseHandler db;
    private final ToDoFragment fragment;
    private final TextView taskCounter;

    public ToDoAdapter(DatabaseHandler db, ToDoFragment fragment, TextView taskCounter) {
        this.db = db;
        this.fragment = fragment;
        this.taskCounter = taskCounter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        db.openDatabase();
        final ToDoModel item = todolist.get(position);

        holder.task.setOnCheckedChangeListener(null);
        holder.task.setChecked(toBoolean(item.getStatus()));
        holder.task.setText(item.getTask());

        holder.task.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int status = isChecked ? 1 : 0;
            db.updateStatus(item.getId(), status);
            item.setStatus(status);
            updateTaskCounter();
            holder.itemView.clearFocus();
            holder.itemView.setActivated(false);
            holder.itemView.setPressed(false);
        });

    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todolist != null ? todolist.size() : 0;
    }

    public Context getContext() {
        return fragment.getContext();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<ToDoModel> todolist) {
        this.todolist = todolist;
        notifyDataSetChanged();
        updateTaskCounter();
    }

    public void deleteItem(int position) {
        ToDoModel item = todolist.get(position);
        db.deleteTask(item.getId());
        todolist.remove(position);
        notifyItemRemoved(position);
        updateTaskCounter();
    }

    public void editItem(int position) {
        ToDoModel item = todolist.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(bundle);
        fragment.show(this.fragment.getChildFragmentManager(), AddNewTask.TAG);
        notifyItemChanged(position);
        updateTaskCounter();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
        }
    }

    private int lastCompletedCount = -1;

    public void updateTaskCounter() {
        int completed = 0;
        for (ToDoModel task : todolist) {
            if (task.getStatus() == 1) completed++;
        }

        int total = todolist.size();
        int start = (lastCompletedCount >= 0) ? lastCompletedCount : completed;
        int end = completed;

        // Animate number count
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            taskCounter.setText(String.format(Locale.US, "Completed %,d out of %,d", value, total));
        });
        animator.start();

        // Flash color for increase or decrease
        if (lastCompletedCount >= 0 && end != start) {
            int flashColor = (end > start)
                    ? Color.parseColor("#4CAF50") // green
                    : Color.parseColor("#F44336"); // red

            taskCounter.setTextColor(flashColor);

            int finalColor = Color.parseColor("#888888"); // theme default

            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    taskCounter.setTextColor(finalColor), 1000);
        }
        lastCompletedCount = completed;
    }
}