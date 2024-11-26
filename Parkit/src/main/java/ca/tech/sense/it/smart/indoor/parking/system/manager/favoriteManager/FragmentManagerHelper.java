package ca.tech.sense.it.smart.indoor.parking.system.manager.favoriteManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentManagerHelper {
    private final FragmentManager fragmentManager;
    private final int containerViewId;

    public FragmentManagerHelper(FragmentManager fragmentManager, int containerViewId) {
        this.fragmentManager = fragmentManager;
        this.containerViewId = containerViewId;
    }

    public void openFragment(Fragment fragment, String fragmentTag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment, fragmentTag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
