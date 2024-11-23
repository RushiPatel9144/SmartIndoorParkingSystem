package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.canhub.cropper.CropImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ca.tech.sense.it.smart.indoor.parking.system.R;

public class ImageCropActivity extends AppCompatActivity {

    private CropImageView cropImageView;
    private ProgressBar progressBar;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        initViews();
        handleIntentData();
        setUpListeners();
    }

    private void initViews() {
        cropImageView = findViewById(R.id.cropImageView);
        progressBar = findViewById(R.id.progress_bar);
        cropImageView.setAspectRatio(1, 1);
    }

    private void handleIntentData() {
        Uri imageUri = getIntent().getParcelableExtra(getString(R.string.imageuri));
        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri);
        }
    }

    private void setUpListeners() {
        findViewById(R.id.btnCrop).setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            cropAndSaveImage();
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void cropAndSaveImage() {
        executorService.execute(() -> {
            Bitmap croppedImage = cropImageView.getCroppedImage();
            Uri croppedImageUri = null;

            if (croppedImage != null) {
                Bitmap circularImage = getCircularBitmap(croppedImage);
                croppedImageUri = saveCroppedImage(circularImage);
            }

            Uri finalCroppedImageUri = croppedImageUri;
            mainThreadHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                handleResult(finalCroppedImageUri);
            });
        });
    }

    private Uri saveCroppedImage(Bitmap croppedImage) {
        File imageFile = new File(getExternalCacheDir(), "cropped_image.png");
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            croppedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int radius = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;
        Bitmap circularBitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(radius, radius, radius, paint);
        return circularBitmap;
    }

    private void handleResult(Uri croppedImageUri) {
        if (croppedImageUri != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(getString(R.string.croppedimageuri), croppedImageUri.toString());
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
