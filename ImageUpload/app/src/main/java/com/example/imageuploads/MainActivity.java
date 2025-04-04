 package com.example.imageuploads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.imageuploads.constant.Const;
import com.example.imageuploads.model.ImageUpload;
import com.example.imageuploads.retrofit.ServiceAPI;
import com.example.imageuploads.util.RealPathUtil;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

 public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private Button btnUpload, btnChoose;
    private Uri mUri;
    ImageView imageViewChoose, imageViewUpload;
    EditText editTextUserName;
    TextView textViewUserName;
    private ProgressDialog mProgressDialog;
    private static final int MY_REQUEST_CODE = 100;
    public static String[] storage_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_Permission_33 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AnhXa();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait upload...");
        btnChoose.setOnClickListener(v -> {
            checkPermission();
        });
        btnUpload.setOnClickListener(v -> {
            if (mUri != null) {

                // Upload the image to your server or perform any action
                // For example, using Retrofit or any other library
                // After upload, dismiss the progress dialog
                UploadImage1();
            } else {
                Log.e(TAG, "onClick: No image selected");
            }
        });
    }

    private void UploadImage1() {
        mProgressDialog.show();
        String username = editTextUserName.getText().toString();
        RequestBody requestUserName = RequestBody.create(MediaType.parse("multipart/form-data"), username);
        // Create a RequestBody for the image file
        String ImagePath = RealPathUtil.getRealPath(this,mUri);
        File file = new File(ImagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);
        // Call your API to upload the image
        ServiceAPI.serviceAPI.upload1(requestUserName, body).enqueue(new retrofit2.Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                mProgressDialog.dismiss();
                List<ImageUpload> imageUploads = response.body();
                if (imageUploads.size() > 0) {
                   for(int i=0; i<imageUploads.size(); i++){
                       textViewUserName.setText(imageUploads.get(i).getUsername());
                       Glide.with(MainActivity.this)
                               .load(imageUploads.get(i).getAvatar())
                               .into(imageViewUpload);
                       Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }
                } else {
                    Log.e(TAG, "onResponse: Upload failed");
                }
            }

            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                mProgressDialog.dismiss();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    public static String[] getStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return storage_Permission_33;
        } else {
            return storage_permissions;
        }
    }
    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(getStoragePermission(), MY_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                // Permission denied
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }
    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    Log.e(TAG, "onActivityResult: ");
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            Log.e(TAG, "onActivityResult: " + uri);
                            // Handle the selected image URI
                            mUri =uri;
                            // Do something with the URI, like displaying it in an ImageView
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                imageViewChoose.setImageBitmap(bitmap);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.e(TAG, "onActivityResult: Cancelled");
                    }
                }
            }
    );
    private void AnhXa(){
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewChoose = findViewById(R.id.imgChoose);
        imageViewUpload = findViewById(R.id.imgMultipart);
        editTextUserName = findViewById(R.id.editUserName);
        textViewUserName = findViewById(R.id.tvUsername);
    }
}