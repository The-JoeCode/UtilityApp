package com.example.mobileappwrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.exitBtn.setOnClickListener(v -> requireActivity().finishAffinity());

        setupNav(binding.bmi, R.id.nav_bmi);
        setupNav(binding.camera, R.id.nav_camera);
        setupNav(binding.discount, R.id.nav_discount);
        setupNav(binding.gallery, R.id.nav_gallery);
        setupNav(binding.openBrowser, R.id.nav_browser);
        setupNav(binding.shortCodes, R.id.nav_toolkit);
        setupNav(binding.simpleCalc, R.id.nav_calculator);
        setupNav(binding.toDo, R.id.nav_todo);
        setupNav(binding.webApp, R.id.nav_webapp);
//        setupNav(binding.powerConsumption, R.id.nav_power);
//        setupNav(binding.studentReg, R.id.nav_registration);
        return root;
    }

    private void setupNav(Button button, int destinationId) {
        if (button != null) {
            button.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(destinationId));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
