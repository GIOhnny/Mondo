package ro.giohnnysoftware.mondo.interfaces;

public interface Constants {
    String EMPTY_STRING = "";
    String SQL_ERR_NO_SUCH_TABLE = "no such table";
    int RC_SIGN_IN = 2110;
    String PRV_GOOGLE = "google";
    String PRV_MAIL = "password";
    String PRV_FACEBOOK = "facebook";
    String PRV_ANONYMOUS = "firebase";
    int NICKNAME_EXIST = 0;
    int NICKNAME_NOT_EXIST = 1;
    int GUEST_TO_USER = 2;
    int NO_INTERNET_CONNECTION = 3;
    int BANNER_NOT_LOADED = 4;

    //GoogleAds => directly in layout using @String
    //String APP_ID = "";
    //PROD Google Ads
    //String BANNER_ZONE_ID = "ca-app-pub-6512817804641390/4994236999";
    //String INTERSTITIAL_ZONE_ID = "";
    //DEMO
    //String INTERSTITIAL_ZONE_ID = "";
    //String BANNER_ZONE_ID = "ca-app-pub-3940256099942544/6300978111";
}
