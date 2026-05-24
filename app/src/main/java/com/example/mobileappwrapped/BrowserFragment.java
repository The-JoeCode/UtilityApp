package com.example.mobileappwrapped;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.databinding.FragmentBrowserBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class BrowserFragment extends Fragment {

    private FragmentBrowserBinding binding; // View binding

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBrowserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextInputEditText urlInput = binding.urlInput;
        Button reset = binding.resetUrl;
        Button open = binding.btnOpenWebpage;
        Button backBtn = binding.backButton;

        backBtn.setOnClickListener(view ->
                Navigation.findNavController(root).navigate(R.id.nav_home));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
        });
        reset.setOnClickListener(v -> urlInput.setText(""));
        open.setOnClickListener(v -> {
            String url = Objects.requireNonNull(urlInput.getText()).toString();
            if (url.isEmpty()) {
                urlInput.setError("Please enter a URL");
                return;
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}