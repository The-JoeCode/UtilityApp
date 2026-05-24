package com.example.mobileappwrapped.registration;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.R;
import com.example.mobileappwrapped.databinding.FragmentRegistrationBinding;

import java.util.Objects;

public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private RegistrationDBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new RegistrationDBHelper(requireContext());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
        });

        Button registerBtn = binding.registerBtn;
        Button viewAllBtn = binding.viewAllBtn;

        registerBtn.setOnClickListener(v -> registerStudent());
        viewAllBtn.setOnClickListener(v -> viewAllStudents());
        return root;
    }

    private void registerStudent() {
        String name = Objects.requireNonNull(binding.fullName.getText()).toString();
        String sid = Objects.requireNonNull(binding.studentId.getText()).toString();
        String dept = Objects.requireNonNull(binding.department.getText()).toString();
        String mail = Objects.requireNonNull(binding.email.getText()).toString();

        int selectedId = binding.genderGroup.getCheckedRadioButtonId();
        assert getView() != null;
        RadioButton selectedGender = getView().findViewById(selectedId);
        String gender = (selectedGender != null) ? selectedGender.getText().toString() : "";

        if (name.isEmpty() || sid.isEmpty() || dept.isEmpty() || mail.isEmpty() || gender.isEmpty()) {
            showToast("All fields are required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            showToast("Invalid email format");
            return;
        }

        boolean inserted = dbHelper.insertStudent(name, sid, dept, mail, gender);
        if (inserted) {
            showToast("Student Registered Successfully");

            clearFields();
        } else {
            showToast("Registration Failed");
        }
    }

    private void viewAllStudents() {
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor.getCount() == 0) {
            showMessage("No Data", "No student records found.");
            return;
        }

        StringBuilder buffer = new StringBuilder();
        while (cursor.moveToNext()) {
            buffer.append("ID: ").append(cursor.getInt(0)).append("\n");
            buffer.append("Name: ").append(cursor.getString(1)).append("\n");
            buffer.append("Student ID: ").append(cursor.getString(2)).append("\n");
            buffer.append("Department: ").append(cursor.getString(3)).append("\n");
            buffer.append("Email: ").append(cursor.getString(4)).append("\n");
            buffer.append("Gender: ").append(cursor.getString(5)).append("\n\n");
        }

        showMessage("Student Records", buffer.toString());
    }

    private void showMessage(String title, String message) {
        new AlertDialog.Builder((requireContext())).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearFields() {
        binding.fullName.setText("");
        binding.studentId.setText("");
        binding.department.setText("");
        binding.email.setText("");
        binding.genderGroup.clearCheck();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}