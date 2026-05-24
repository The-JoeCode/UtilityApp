package com.example.mobileappwrapped.todo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileappwrapped.databinding.FragmentTodoBinding;

import java.util.Collections;
import java.util.List;

public class ToDoFragment extends Fragment implements DialogCloseListener {
    private ToDoAdapter tasksAdapter;
    private DatabaseHandler db;
    private FragmentTodoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTodoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize db handler first
        db = new DatabaseHandler(getContext());
        db.openDatabase();

        RecyclerView taskRecyclerView = binding.taskView;
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.floatBtn.setOnClickListener(v -> {
            AddNewTask addNewTaskDialog = AddNewTask.newInstance();
            addNewTaskDialog.show(getChildFragmentManager(), AddNewTask.TAG);
        });

        tasksAdapter = new ToDoAdapter(db, this, binding.taskCounter);
        taskRecyclerView.setAdapter(tasksAdapter);

        // Setup ItemTouchHelper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

        // Load initial tasks
        loadTasksAndUpdateAdapter();
        return root;
    }

    //Loads tasks from the database, reverses them, and updates the adapter.
    private void loadTasksAndUpdateAdapter() {
        List<ToDoModel> taskList = db.getAllTasks();
        Collections.reverse(taskList);   // newest first
        tasksAdapter.setTasks(taskList); // notifyDataSetChanged() happens here
    }

    //This method is intended to be called by AddNewTask dialog when it closes.
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        loadTasksAndUpdateAdapter(); // Reload tasks and refresh the RecyclerView
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}