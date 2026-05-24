package com.example.mobileappwrapped.bmi;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.R;
import com.example.mobileappwrapped.databinding.FragmentBmiBinding;

import java.util.Locale;
import java.util.Objects;

public class BMIFragment extends Fragment {

    private FragmentBmiBinding binding;
    private BMIDBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBmiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new BMIDBHelper(requireContext());

        // Reset button clears all inputs
        binding.resetBMI.setOnClickListener(r -> {
            binding.heightInput.setText("");
            binding.weightInput.setText("");
            binding.bmiDisplay.setText("");
            binding.weightShow.setText("");
        });
        setupClearRecordsButton(); // Added call to the new method

        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
        });

        // BMI Calculation and saving record
        binding.calcBMI.setOnClickListener(v -> {
            String heightStr = Objects.requireNonNull(binding.heightInput.getText()).toString();
            String weightStr = Objects.requireNonNull(binding.weightInput.getText()).toString();

            if (heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(getContext(), "Enter both height and weight", Toast.LENGTH_SHORT).show();
                return;
            }

            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);

            if (height <= 0 || weight <= 0) {
                Toast.makeText(getContext(), "Height and weight must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            double bmi = weight / (height / 100 * height / 100);
            binding.bmiDisplay.setText(String.format(Locale.US, "BMI value: %.2f", bmi));

            String category;
            if (bmi < 18.5) {
                category = getString(R.string.underweight);
            } else if (bmi < 24.9) {
                category = getString(R.string.normal_weight);
            } else if (bmi < 29.9) {
                category = getString(R.string.overweight);
            } else {
                category = getString(R.string.obesity);
            }

            binding.weightShow.setText(category);

            boolean inserted = dbHelper.insertRecords(height, weight, bmi, category);
            if (inserted) {
                Toast.makeText(getContext(), "BMI Record Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to Save Record", Toast.LENGTH_SHORT).show();
            }
        });

        // View all saved BMI records using custom dialog
        binding.viewRecordsBtn.setOnClickListener(v -> {
            Cursor cursor = dbHelper.getAllRecords();
            if (cursor.getCount() == 0) {
                showMessage("No Records", "No BMI records found.");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            while (cursor.moveToNext()) {
                buffer.append("ID: ").append(cursor.getInt(0)).append("\n");
                buffer.append("Height: ").append(cursor.getDouble(1)).append(" cm\n");
                buffer.append("Weight: ").append(cursor.getDouble(2)).append(" kg\n");
                buffer.append("BMI: ").append(cursor.getDouble(3)).append("\n");
                buffer.append("Category: ").append(cursor.getString(4)).append("\n");
                buffer.append("Date/Time: ").append(cursor.getString(5)).append("\n\n"); // ✅ Added line
            }

            showMessage("BMI Records", buffer.toString());
        });
        return root;
    }

    private void setupClearRecordsButton() {
        binding.clearRecordsBtn.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Clear")
                .setMessage("Are you sure you want to delete all power records? This action cannot be undone.")
                .setPositiveButton("Yes, Clear All", (dialog, which) -> {
                    boolean cleared = dbHelper.clearAllRecords(); // You'll need to implement clearAllRecords() in PowerDBHelper
                    if (cleared) {
                        showToast("All power records have been cleared.");
                        // Optionally, refresh any displayed records view or hide resultView
                        binding.heightInput.setText("");
                        binding.weightInput.setText("");
                        binding.bmiDisplay.setText("");
                        binding.weightShow.setText("");
                    } else {
                        showToast("Failed to clear records.");
                    }
                })
                .setNegativeButton("No, Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show());

    }

    private void showMessage(String title, String message) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null);

        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageView = dialogView.findViewById(R.id.dialogMessage);
        Button okBtn = dialogView.findViewById(R.id.customOkBtn);

        titleView.setText(title);
        messageView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        okBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}