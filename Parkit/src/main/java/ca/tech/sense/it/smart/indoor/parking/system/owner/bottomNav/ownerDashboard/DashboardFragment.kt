//package ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.ownerDashboard
//
//import android.annotation.SuppressLint
//import android.os.Bundle
//import android.text.SpannableString
//import android.text.style.ForegroundColorSpan
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.fragment.app.Fragment
//import ca.tech.sense.it.smart.indoor.parking.system.R
//import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.location.LocationsFragment
//import ca.tech.sense.it.smart.indoor.parking.system.owner.bottomNav.transactions.TransactionsFragment
//import java.util.Calendar
//
//class DashboardFragment : Fragment() {
//
//    private var containerViewId: Int = 0
//    private val sectionMap = mapOf(
//        R.id.cardLocations to DashboardSection.LOCATIONS,
//        R.id.cardTransactions to DashboardSection.TRANSACTIONS,
//        R.id.cardActiveParkingLot to DashboardSection.ACTIVE_PARKING
//    )
//
//    private val userName = "John Doe" // You can replace this with actual user data
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            containerViewId = it.getInt(ARG_CONTAINER_VIEW_ID)
//        }
//    }
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
//        setupClickListeners(view)
//
//        // Add the greeting with the user's name
//        val greetingTextView = view.findViewById<TextView>(R.id.dashboardGreetingTextView)
//        greetingTextView.text = getGreetingMessageWithUserName()
//
//        return view
//    }
//
//    private fun setupClickListeners(view: View) {
//        sectionMap.forEach { (layoutId, section) ->
//            setSectionClickListener(view, layoutId, section)
//        }
//    }
//
//    private fun setSectionClickListener(view: View, layoutId: Int, section: DashboardSection) {
//        val card = view.findViewById<CardView>(layoutId)
//        card.setOnClickListener { handleSectionSelection(section) }
//    }
//
//    private fun handleSectionSelection(section: DashboardSection) {
//        val fragment: Fragment = when (section) {
//            DashboardSection.LOCATIONS -> LocationsFragment()
//            DashboardSection.TRANSACTIONS -> TransactionsFragment()
//            DashboardSection.ACTIVE_PARKING -> {
//                // Replace with your ActiveParkingFragment once implemented
//                Fragment() // Placeholder
//            }
//        }
//        openFragment(fragment)
//    }
//
//    private fun openFragment(fragment: Fragment) {
//        parentFragmentManager.beginTransaction().apply {
//            replace(containerViewId, fragment, tag)
//            addToBackStack(tag)
//            commit()
//        }
//    }
//
//    // Function to generate greeting based on the time of the day
//    private fun getGreetingMessage(): String {
//        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//        return when {
//            currentHour in 5..11 -> "Good Morning"
//            currentHour in 12..17 -> "Good Afternoon"
//            currentHour in 18..20 -> "Good Evening"
//            else -> "Good Night"
//        }
//    }
//
//    // Function to generate greeting with user's name and different colors
//    private fun getGreetingMessageWithUserName(): SpannableString {
//        val greetingMessage = getGreetingMessage() + ", " + userName
//        val spannableString = SpannableString(greetingMessage)
//
//        // Change color of the user's name (after the comma)
//        val startIndex = greetingMessage.indexOf(",") + 2
//        val endIndex = greetingMessage.length
//        spannableString.setSpan(
//            ForegroundColorSpan(resources.getColor(R.color.colorAccent, null)),
//            startIndex,
//            endIndex,
//            0
//        )
//
//        return spannableString
//    }
//
//    companion object {
//        private const val ARG_CONTAINER_VIEW_ID = "containerViewId"
//
//        @JvmStatic
//        fun newInstance(containerViewId: Int) = DashboardFragment().apply {
//            arguments = Bundle().apply {
//                putInt(ARG_CONTAINER_VIEW_ID, containerViewId)
//            }
//        }
//    }
//
//    private enum class DashboardSection {
//        LOCATIONS,
//        TRANSACTIONS,
//        ACTIVE_PARKING
//    }
//}
