/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.annotation.SuppressLint;
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
    public final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        cropImageView = findViewById(R.id.cropImageView);
        progressBar = findViewById(R.id.progress_bar);  // Assuming you have a ProgressBar in your layout
        cropImageView.setAspectRatio(1, 1);

        Uri imageUri = getIntent().getParcelableExtra(getString(R.string.imageuri));
        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri);
        }

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
                if (finalCroppedImageUri != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(getString(R.string.croppedimageuri), finalCroppedImageUri.toString());
                    setResult(Activity.RESULT_OK, resultIntent);
                }
                finish();
            });
        });
    }

    public Uri saveCroppedImage(Bitmap croppedImage) {
        try {
            File imageFile = new File(getExternalCacheDir(), "cropped_image.png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            croppedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int radius = Math.min(width, height) / 2;

        Bitmap circularBitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(radius, radius, radius, paint);

        return circularBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();  // Shut down the ExecutorService when done
    }
}
