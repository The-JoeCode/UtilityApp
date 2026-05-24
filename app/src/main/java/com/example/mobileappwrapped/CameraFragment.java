package com.example.mobileappwrapped;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

public class CameraFragment extends Fragment {

    private ImageView imageView;
    private Uri photoUri;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher; // ✅ NEW

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        imageView = root.findViewById(R.id.imageView);
        Button takePhotoBtn = root.findViewById(R.id.takePhotoBtn);
        Button exitBtn = root.findViewById(R.id.backButton);
        Button viewImagesBtn = root.findViewById(R.id.viewImagesBtn);

        viewImagesBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_gallery)
        );

        // 📸 Take picture launcher (unchanged logic)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photoUri != null) {
                        imageView.setImageURI(photoUri);
                        Toast.makeText(getContext(), "Saved to gallery", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 🔐 CAMERA PERMISSION launcher (NEW)
        cameraPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                openCamera();
                            } else {
                                Toast.makeText(getContext(),
                                        "Camera permission is required to take photos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

        takePhotoBtn.setOnClickListener(v -> {
            // 🔍 CHECK permission first (NEW)
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {
                // 🔔 Ask permission (first-time users)
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        exitBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        return root;
    }

    // 📸 Moved camera-opening logic here (NEW, clean)
    private void openCamera() {
        photoUri = createImageUri();
        if (photoUri != null) {
            takePictureLauncher.launch(photoUri);
        } else {
            Toast.makeText(getContext(), "Failed to create file", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "photo_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + "/Utility App");

        return requireActivity().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );
    }

    /** @noinspection unused */
    private List<Uri> loadSavedImages() {
        List<Uri> imageUris = new ArrayList<>();

        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"DCIM/Utility App%"};

        try (Cursor cursor = requireActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    imageUris.add(uri);
                }
            }
        }
        return imageUris;
    }
}
