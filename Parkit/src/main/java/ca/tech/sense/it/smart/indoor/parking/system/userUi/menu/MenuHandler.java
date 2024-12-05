/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.userUi.menu;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import ca.tech.sense.it.smart.indoor.parking.system.R;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.HelpFragment;
import ca.tech.sense.it.smart.indoor.parking.system.userUi.bottomNav.AccountItems.NotificationsFragment;

public class MenuHandler extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.notification) {
            loadFragment(new NotificationsFragment());
            return true;
        }
        if (item.getItemId() == R.id.favorites) {
            loadFragment(new FavoritesFragment());
            return true;
        }
        if (item.getItemId() == R.id.support) {
            loadFragment(new HelpFragment());
            return true;
        }
        if (item.getItemId() == R.id.Offers){
            loadFragment(new PromotionFragment());
            return true;
        }
        return false;

    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment);  // Assuming you have a FrameLayout with id fragment_container in your layout
        transaction.addToBackStack(null);  // Optional: to add the transaction to the back stack
        transaction.commit();
    }
}
