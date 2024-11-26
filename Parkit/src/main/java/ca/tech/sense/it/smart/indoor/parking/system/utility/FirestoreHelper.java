package ca.tech.sense.it.smart.indoor.parking.system.utility;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addLegalDocuments() {
        // Create terms_of_use document
        Map<String, String> termsData = new HashMap<>();
        termsData.put("acceptance_of_terms", "By downloading, accessing, or using the \"SmartIndoorParkingSystem\" application , you agree to abide by these Terms of Use. If you do not agree to these terms, please discontinue the use of the App immediately.");
        termsData.put("purpose_of_the_app", "The App facilitates connections between parking lot owners and users who are searching for parking spaces. It allows Users to search, book, and manage parking spaces and provides Owners a platform to list and manage parking lots. The App also provides additional features such as a favorites list, user feedback, and promotional offers.");
        termsData.put("user_responsibilities", "You are solely responsible for the accuracy of the information you provide during registration and use of the App. Users must use the booking system responsibly and adhere to the cancellation or refund policies defined by the parking lot Owners. Owners must ensure the availability and accuracy of the parking lot details listed on the App. You agree not to misuse the App or use it for any illegal or unauthorized purpose.");
        termsData.put("booking_and_payment", "Booking of parking spaces is subject to availability and confirmation by the Owner. All payment transactions are handled through third-party services. The App is not responsible for transaction failures, delays, or disputes.");
        termsData.put("parking_rules", "Users must follow all parking lot rules and regulations established by the Owners. The App is not liable for damages, theft, or losses occurring at any parking lot listed in the App.");
        termsData.put("account_management", "You are responsible for maintaining the confidentiality of your account credentials. Misuse of the App, including false reviews, fraudulent bookings, or inappropriate behavior, will result in the termination of your account.");
        termsData.put("restrictions", "The App prohibits the use of automated tools, bots, or scripts to access or interact with the platform. Users may not attempt to reverse-engineer, decompile, or hack the App.");
        termsData.put("modifications_to_the_app", "We reserve the right to update, modify, or discontinue features of the App without prior notice.");
        termsData.put("liability", "The App acts as a platform connecting Users and Owners. We are not liable for disputes, damages, or losses resulting from interactions or transactions between Users and Owners.");

        db.collection("legal").document("terms_of_use").set(termsData)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Terms of Use added successfully"))
                .addOnFailureListener(e -> Log.w("FirestoreHelper", "Error adding Terms of Use", e));

        // Create privacy_policy document
        Map<String, String> privacyData = new HashMap<>();
        privacyData.put("data_we_collect", "We collect personal information such as your name, email, and phone number during registration. Location data is collected to recommend nearby parking lots, but only with your permission. Booking information, feedback, promo code usage, and notification preferences are also gathered to enhance the App's functionality.");
        privacyData.put("how_we_use_your_data", "Your data is used to provide and improve features like parking searches, booking management, and personalized promotions. It also helps us communicate updates and analyze feedback for better user experiences.");
        privacyData.put("data_sharing", "We share data with parking lot Owners to process bookings and resolve disputes. Third-party services assist with payments, analytics, and geolocation, but we do not sell or rent your data. Data may be shared with legal authorities if required by law.");
        privacyData.put("security", "We use encryption and secure storage to protect your information. While no system is 100% secure, we are committed to safeguarding your data to the best of our ability.");
        privacyData.put("cookies_and_tracking", "Cookies and tracking tools help improve your experience by enabling features like favorites and notifications. You can disable these in your device settings, but some features may not work properly.");
        privacyData.put("user_rights", "You can view and update your account details or request data deletion in the \"Manage Account\" section. The \"Settings\" section lets you manage privacy preferences like notifications and location access.");
        privacyData.put("location_data", "Location services are used only to recommend parking lots near you. We do not store or share this data unless required for a booking.");
        privacyData.put("childrens_privacy", "The App is not intended for children under 13. We do not knowingly collect data from children under this age and will delete any such data if identified.");
        privacyData.put("changes_to_privacy_policy", "We may update this Privacy Policy periodically to reflect changes in our practices. Significant updates will be communicated through the App.");
        privacyData.put("contact_us", "If you have any questions or concerns, contact us via the Help Screen in the App or email us at support@smartindoorparking.com.");

        db.collection("legal").document("privacy_policy").set(privacyData)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Privacy Policy added successfully"))
                .addOnFailureListener(e -> Log.w("FirestoreHelper", "Error adding Privacy Policy", e));
    }
}
