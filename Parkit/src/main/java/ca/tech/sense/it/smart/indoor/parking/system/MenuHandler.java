package ca.tech.sense.it.smart.indoor.parking.system;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

public class MenuHandler extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.notification) {

        }
        if (item.getItemId() == R.id.favorites) {

        }
        if (item.getItemId() == R.id.support) {

        }
        if (item.getItemId() == R.id.Offers){

        }
        return false;

    }
}
