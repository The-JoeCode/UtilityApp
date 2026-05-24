package com.example.mobileappwrapped.power;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.R;
import com.example.mobileappwrapped.databinding.FragmentPowerBinding;

import java.util.Locale;
import java.util.Objects;

public class PowerFragment extends Fragment {

    private PowerDBHelper dbHelper;
    private FragmentPowerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPowerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new PowerDBHelper(requireContext());

        // Initially hide the resultView
        binding.resultView.setVisibility(View.INVISIBLE);

        setupNavigation(root);
        setupResetInputs();
        setupFindStuff();
        setupViewRecords();
        setupClearRecordsButton(); // Added call to the new method

        return root;
    }

    private void setupNavigation(View root) {
        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(root).navigate(R.id.nav_home);
                    }
                });
    }

    private void setupResetInputs() {
        binding.resetInputs.setOnClickListener(v -> {
            binding.nameInput.setText("");
            binding.prevInput.setText("");
            binding.curInput.setText("");
            binding.customerName.setText("");
            binding.electricityUsed.setText("");
            binding.category.setText("");
            binding.bill.setText("");
            binding.tax.setText("");
            binding.totalAmount.setText("");
            showToast("All inputs have been reset");

            // Hide resultView on reset
            binding.resultView.setVisibility(View.INVISIBLE);
        });
    }

    private void setupFindStuff() {
        binding.findStuff.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.nameInput.getText()).toString().trim();
            String prevStr = Objects.requireNonNull(binding.prevInput.getText()).toString().trim();
            String curStr = Objects.requireNonNull(binding.curInput.getText()).toString().trim();

            if (name.isEmpty() || prevStr.isEmpty() || curStr.isEmpty()) {
                showToast("Please provide all inputs");
                // Hide resultView if inputs are missing
                binding.resultView.setVisibility(View.INVISIBLE);
                return;
            }

            double prev, current;
            try {
                prev = Double.parseDouble(prevStr);
                current = Double.parseDouble(curStr);
            } catch (NumberFormatException e) {
                showToast("Invalid input");
                // Hide resultView if input is invalid
                binding.resultView.setVisibility(View.INVISIBLE);
                return;
            }

            if (current < prev) {
                showToast("Current reading cannot be less than previous reading");
                // Hide resultView if current reading is less than previous
                binding.resultView.setVisibility(View.INVISIBLE);
                return;
            }

            double consumption = current - prev;
            BillDetails billDetails = calculateBill(consumption);

            // Update UI
            binding.customerName.setText(name);
            binding.electricityUsed.setText(String.format(Locale.US, "Consumption : %.2f kWH", consumption));
            binding.category.setText(billDetails.category);
            binding.totalAmount.setText(String.format(Locale.US, "Total Amount : GH₵ %.2f", billDetails.totalBill));
            binding.bill.setText(String.format(Locale.US, "Bill : GH₵ %.2f", billDetails.bill));
            binding.tax.setText(String.format(Locale.US, "2 percent Tax : GH₵ %.2f", billDetails.tax));

            // Show resultView as data is valid and processed
            binding.resultView.setVisibility(View.VISIBLE);

            // Save to DB
            boolean inserted = dbHelper.insertRecords(name, consumption, billDetails.category, billDetails.totalBill, billDetails.bill, billDetails.tax);
            showToast(inserted ? "Records Saved" : "Failed to save records");
        });
    }

    private BillDetails calculateBill(double consumption) {
        double bill;
        String category;

        if (consumption <= 100) {
            bill = consumption <= 60 ? consumption * 0.3 : (60 * 0.3) + ((consumption - 60) * 0.5);
            category = "Category : Domestic";
        } else if (consumption <= 200) {
            bill = consumption <= 120 ? consumption * 0.5 : (120 * 0.5) + ((consumption - 120) * 0.75);
            category = "Category : Industrial";
        } else {
            bill = (120 * 0.5) + ((consumption - 120) * 1.5);
            category = "Category : Commercial";
        }

        double tax = bill * 0.02;
        double totalBill = bill + tax;

        return new BillDetails(bill, tax, totalBill, category);
    }

    private void setupViewRecords() {
        binding.viewRecordsBtn.setOnClickListener(v -> {
            Cursor cursor = dbHelper.getAllRecords();
            if (cursor.getCount() == 0) {
                showMessage("No Records", "No previous records found.");
                return;
            }

            StringBuilder buffer = new StringBuilder();
            while (cursor.moveToNext()) {
                buffer.append(String.format(Locale.US,
                        "ID: %d%nCustomer Name: %s%nConsumption: %.2f kWh%nCategory: %s%nTotal Bill: %.2f GH₵%nBill: %.2f GH₵%nTax: %.2f GH₵%nDate/Time: %s%n%n",
                        cursor.getInt(0), cursor.getString(1), cursor.getDouble(2),
                        cursor.getString(3), cursor.getDouble(4), cursor.getDouble(5),
                        cursor.getDouble(6), cursor.getString(7)));
            }
            cursor.close();
            showMessage("Power Records", buffer.toString());
        });
    }

    // New method to set up the clear records button
    private void setupClearRecordsButton() {
        binding.clearRecordsBtn.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Clear")
                .setMessage("Are you sure you want to delete all power records? This action cannot be undone.")
                .setPositiveButton("Yes, Clear All", (dialog, which) -> {
                    boolean cleared = dbHelper.clearAllRecords(); // You'll need to implement clearAllRecords() in PowerDBHelper
                    if (cleared) {
                        showToast("All power records have been cleared.");
                        // Optionally, refresh any displayed records view or hide resultView
                        binding.resultView.setVisibility(View.INVISIBLE); // Example: hide details if shown
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

        com.example.mobileappwrapped.databinding.CustomDialogBinding dialogBinding =
                com.example.mobileappwrapped.databinding.CustomDialogBinding.bind(dialogView);

        dialogBinding.dialogTitle.setText(title);
        dialogBinding.dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogBinding.customOkBtn.setOnClickListener(v -> dialog.dismiss());
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

    //Helper class for bill calculation results
    private static class BillDetails {
        final double bill;
        final double tax;
        final double totalBill;
        final String category;

        BillDetails(double bill, double tax, double totalBill, String category) {
            this.bill = bill;
            this.tax = tax;
            this.totalBill = totalBill;
            this.category = category;
        }
    }
}
