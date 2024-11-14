/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */

package ca.tech.sense.it.smart.indoor.parking.system.launcherActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ca.tech.sense.it.smart.indoor.parking.system.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIME_OUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);




        TextView splashText = findViewById(R.id.splash_text);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        splashText.startAnimation(textAnimation);

        // Handler to transition based on onboarding status
        new Handler().postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
            boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

            Intent intent;
            if (isFirstTime) {
                // Navigate to OnboardingActivity for first-time users
                intent = new Intent(SplashScreen.this, OnboardingActivity.class);
            } else {
                // Navigate to FirstActivity if onboarding has been completed
                intent = new Intent(SplashScreen.this, FirstActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_TIME_OUT);
    }
}
