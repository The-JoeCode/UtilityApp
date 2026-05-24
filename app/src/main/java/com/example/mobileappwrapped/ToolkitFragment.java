package com.example.mobileappwrapped;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.databinding.FragmentToolkitBinding;

public class ToolkitFragment extends Fragment {

    private FragmentToolkitBinding binding;

    private String pendingUssdCode; // ✅ NEW
    private ActivityResultLauncher<String> phonePermissionLauncher; // ✅ NEW

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentToolkitBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 🔐 Permission launcher (NEW)
        phonePermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted && pendingUssdCode != null) {
                                sendUssdCode(pendingUssdCode);
                            } else {
                                Toast.makeText(getContext(),
                                        "Phone permission is required to use this feature",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

        binding.balanceBtn.setOnClickListener(v -> requestOrSend("*124#"));
        binding.momoBtn.setOnClickListener(v -> requestOrSend("*170#"));
        binding.dataBtn.setOnClickListener(v -> requestOrSend("*138#"));
        binding.just4you.setOnClickListener(v -> requestOrSend("*141#"));
        binding.mashup.setOnClickListener(v -> requestOrSend("*567#"));

        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(root).navigate(R.id.nav_home);
                    }
                });

        return root;
    }

    // 🔍 Permission check wrapper (NEW)
    private void requestOrSend(String ussdCode) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED) {
            sendUssdCode(ussdCode);
        } else {
            pendingUssdCode = ussdCode;
            phonePermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    // 📞 Original logic (UNCHANGED)
    private void sendUssdCode(String ussdCode) {
        String encodeHash = Uri.encode("#");
        String ussd = ussdCode.replace("#", encodeHash);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussd));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}