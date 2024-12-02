package ca.tech.sense.it.smart.indoor.parking.system.utility;

public class AppConstants {
    public static final String USER_TYPE_USER = "user";
    public static final String USER_TYPE_OWNER = "owner";

    public static final String COLLECTION_OWNER = "owners";
    public static final String COLLECTION_USER = "users";
    public static final String COLLECTION_FEEDBACK = "feedback";
    public static final String COLLECTION_HELP = "help";

    public static final String COLLECTION_LEGAL = "legal";

    public static final String COLLECTION_LOCATION_OWNER = "parkingLocationIds";
    public static final String APP_CLIENT_ID = "361561575523-vh9n3c4q3brm58ejsqgj8s9henk085i5.apps.googleusercontent.com"; // used for googleSignIn in CoroutineHelper
    public static final int SPLASH_SCREEN_TIME_OUT = 3000; // used in Splash Screen
    public static final String API_PLACE_SEARCH =  "AIzaSyCBb9Vk3FUhAz6Tf7ixMIk5xqu3IGlZRd0";

    public static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";
    public static final String FIELD = "profilePhotoUrl";



    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}