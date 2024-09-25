package ca.tech.sense.it.smart.indoor.parking.system;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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


        // Handling back button press using OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back button logic here, such as showing a dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.are_you_sure_you_want_to_exit)
                        .setCancelable(false)
                        .setTitle(R.string.leaving)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();  // Close the activity
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();  // Stay in the app
                            }
                        })
                        .show();
            }

        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

}
