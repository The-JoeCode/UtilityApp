package com.example.mobileappwrapped.gallery;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileappwrapped.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private GalleryImageAdapter adapter;
    private List<Uri> imageUris = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.galleryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        imageUris = loadSavedImages();
        adapter = new GalleryImageAdapter(getContext(), imageUris);
        recyclerView.setAdapter(adapter);

        Button btnTakePhoto = root.findViewById(R.id.btnTakePhoto);
        Button backBtn = root.findViewById(R.id.backButton);

        btnTakePhoto.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_camera)
        );
        backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_home)
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.nav_home);
            }
        });

        // Trigger a scan of the directory to find newly pasted files
        refreshMediaScanner();

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshMediaScanner() {
        // Corrected directory name to match CameraFragment's "Utility App"
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Utility App");
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                String[] paths = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    paths[i] = files[i].getAbsolutePath();
                }
                MediaScannerConnection.scanFile(getContext(), paths, null, (path, uri) -> {
                    // Callback is called for each file.
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            List<Uri> updatedUris = loadSavedImages();
                            if (updatedUris.size() != imageUris.size()) {
                                imageUris.clear();
                                imageUris.addAll(updatedUris);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        }
    }

    private List<Uri> loadSavedImages() {
        List<Uri> uris = new ArrayList<>();

        String[] projection = {MediaStore.Images.Media._ID};
        // RELATIVE_PATH check: DCIM/Utility App/
        String selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"DCIM/Utility App/%"};

        try (android.database.Cursor cursor = requireActivity().getContentResolver().query(
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
                    Uri contentUri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    uris.add(contentUri);
                }
            }
        } catch (Exception e) {
            Log.e("loadImageError", "Images not loaded", e);
        }

        return uris;
    }
}
