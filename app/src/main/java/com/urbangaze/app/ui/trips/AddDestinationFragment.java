package com.urbangaze.app.ui.trips;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.urbangaze.app.R;
import com.urbangaze.app.utils.Constants;

import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddDestinationFragment extends Fragment {
    TextView location;
    ImageView imgCover;
    LinearLayout imgPlaceholder;

    Button btnAdd, btnCancel;

    double lat, lng;
    String title;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private File selectedFile;
    Uri imageUri;


    public AddDestinationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_destination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSearch();
        setupSearchListener();
        setupImagePicker();
        setupAdd();
    }

    private void initViews(View view) {
        location = view.findViewById(R.id.tvLocation);
        imgCover = view.findViewById(R.id.imgCover);
        imgPlaceholder = view.findViewById(R.id.imgPlaceholder);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void setupSearch() {
        location.setOnClickListener(v -> {
            openSearchFragment();
        });

        setupSearchListener();
    }
    private void openSearchFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new SearchTripsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupSearchListener() {
        requireActivity()
            .getSupportFragmentManager()
            .setFragmentResultListener(
                "place_selected",
                getViewLifecycleOwner(),
                (key, bundle) -> {
                    title = bundle.getString("title");
                    lat = bundle.getDouble("lat");
                    lng = bundle.getDouble("lng");

                    location.setText(title);
                });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            InputStream input = getContext().getContentResolver().openInputStream(uri);
                            selectedFile = new File(getContext().getFilesDir(), "trip_image_" + System.currentTimeMillis());
                            OutputStream output = new FileOutputStream(selectedFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = input.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            output.close();
                            input.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        imageUri = Uri.fromFile(selectedFile);
                        imgCover.setImageURI(imageUri);
                        imgPlaceholder.setVisibility(View.GONE);
                    }
                }
        );

        imgCover.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadImageToSupabase(File file, UploadCallback callback) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                String mimeType = requireContext()
                        .getContentResolver()
                        .getType(imageUri);

                if (mimeType == null) {
                    mimeType = "image/jpeg";
                }

                RequestBody requestBody = RequestBody.create(
                        file,
                        okhttp3.MediaType.parse(mimeType)
                );

                String fileName = file.getName();

                Request request = new Request.Builder()
                        .url(Constants.SUPABASE_URL + "/storage/v1/object/" + Constants.BUCKET_NAME + "/" + fileName)
                        .addHeader("Authorization", "Bearer " + Constants.SUPABASE_API_KEY)
                        .header("apikey", Constants.SUPABASE_API_KEY)
                        .addHeader("Content-Type", mimeType)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {

                    String publicUrl = Constants.SUPABASE_URL +
                            "/storage/v1/object/public/" +
                            Constants.BUCKET_NAME + "/" + fileName;

                    file.delete();

                    requireActivity().runOnUiThread(() ->
                            callback.onSuccess(publicUrl)
                    );

                } else {
                    Log.d("UPLOAD", response.code() + " " + response.body().string());
                    requireActivity().runOnUiThread(() ->
                            callback.onError("Upload failed")
                    );
                }

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        callback.onError(e.getMessage())
                );
            }
        }).start();
    }
    private void setupAdd() {
        btnAdd.setOnClickListener(v -> {
            if (title == null || title.isEmpty() || imageUri == null) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImageToSupabase(selectedFile, new UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    bundle.putDouble("lat", lat);
                    bundle.putDouble("lng", lng);
                    bundle.putString("imageUrl", imageUrl);

                    requireActivity().getSupportFragmentManager().setFragmentResult("add_destination", bundle);
                    requireActivity().getSupportFragmentManager().popBackStack();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}


