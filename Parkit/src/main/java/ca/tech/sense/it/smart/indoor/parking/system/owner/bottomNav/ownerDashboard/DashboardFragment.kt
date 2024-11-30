package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import ca.tech.sense.it.smart.indoor.parking.system.R
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment
import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment

class DashboardFragment : Fragment() {

    private var containerViewId: Int = 0
    private val sectionMap = mapOf(
        R.id.cardLocations to DashboardSection.LOCATIONS,
        R.id.cardTransactions to DashboardSection.TRANSACTIONS,
        R.id.cardActiveParkingLot to DashboardSection.ACTIVE_PARKING
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            containerViewId = it.getInt(ARG_CONTAINER_VIEW_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        setupClickListeners(view)
        return view
    }

    private fun setupClickListeners(view: View) {
        sectionMap.forEach { (layoutId, section) ->
            setSectionClickListener(view, layoutId, section)
        }
    }

    private fun setSectionClickListener(view: View, layoutId: Int, section: DashboardSection) {
        val card = view.findViewById<CardView>(layoutId)
        card.setOnClickListener { handleSectionSelection(section) }
    }

    private fun handleSectionSelection(section: DashboardSection) {
        val fragment: Fragment = when (section) {
            DashboardSection.LOCATIONS -> LocationsFragment()
            DashboardSection.TRANSACTIONS -> TransactionsFragment()
            DashboardSection.ACTIVE_PARKING -> {
                // Replace with your ActiveParkingFragment once implemented
                Fragment() // Placeholder
            }
        }
        openFragment(fragment)
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(containerViewId, fragment, tag)
            addToBackStack(tag)
            commit()
        }
    }

    companion object {
        private const val ARG_CONTAINER_VIEW_ID = "containerViewId"

        @JvmStatic
        fun newInstance(containerViewId: Int) = DashboardFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CONTAINER_VIEW_ID, containerViewId)
            }
        }
    }

    private enum class DashboardSection {
        LOCATIONS,
        TRANSACTIONS,
        ACTIVE_PARKING
    }
}
