/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIME_OUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TextView splashText = findViewById(R.id.splash_text);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        splashText.startAnimation(textAnimation);

        // Handler to transition to the main activity after the splash screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_TIME_OUT);
    }
}
