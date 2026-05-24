package com.example.mobileappwrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.databinding.FragmentDiscountBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Objects;

public class DiscountFragment extends Fragment {

    private FragmentDiscountBinding binding; // View binding

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDiscountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextInputEditText discountInput = binding.discountInput;
        TextInputEditText priceInput = binding.priceInput;
        Button calcDiscountBtn = binding.calcDiscount;
        Button resetBtn = binding.resetInputs;
        Button backBtn = binding.backButton;
        TextView discountValue = binding.discountValue;
        TextView priceValue = binding.priceValue;

        backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
        });

        resetBtn.setOnClickListener(r -> {
            discountInput.setText("");
            priceInput.setText("");
            discountValue.setText("");
            priceValue.setText("");
        });

        calcDiscountBtn.setOnClickListener(v -> {
            if (Objects.requireNonNull(discountInput.getText()).toString().isEmpty() || Objects.requireNonNull(priceInput.getText()).toString().isEmpty()) {
                Toast.makeText(getContext(), "Please enter both numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            double givenPrice = Double.parseDouble(priceInput.getText().toString());
            double givenRate = Double.parseDouble(discountInput.getText().toString());

            double foundDiscount = (givenPrice * givenRate) / 100.00;
            double foundNewPrice = ((100.00 - givenRate) / 100.00) * givenPrice;

            discountValue.setText(String.format(Locale.US, "DISCOUNT  :  %.2f", foundDiscount));
            priceValue.setText(String.format(Locale.US, "NEW PRICE :  %.2f", foundNewPrice));
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}