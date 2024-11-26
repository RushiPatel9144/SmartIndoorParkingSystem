package ca.tech.sense.it.smart.indoor.parking.system.ui.bottomNav.AccountItems.manageAccount;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class ManageAccountHelper {


    public static boolean isPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(ActivityResultLauncher<String> requestPermissionLauncher) {
        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void openGallery(ActivityResultLauncher<Intent> imagePickerLauncher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    public static void loadCroppedImage(Uri uri, Context context, ImageView profilePicture) {
        Glide.with(context).load(uri).circleCrop().into(profilePicture);
    }
}
