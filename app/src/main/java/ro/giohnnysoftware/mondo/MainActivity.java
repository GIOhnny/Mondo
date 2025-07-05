package ro.giohnnysoftware.mondo;

import static ro.giohnnysoftware.mondo.interfaces.Constants.PRV_ANONYMOUS;
import static ro.giohnnysoftware.mondo.interfaces.Constants.PRV_FACEBOOK;
import static ro.giohnnysoftware.mondo.interfaces.Constants.PRV_GOOGLE;
import static ro.giohnnysoftware.mondo.interfaces.Constants.PRV_MAIL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ro.giohnnysoftware.mondo.interfaces.Constants;
import ro.giohnnysoftware.mondo.interfaces.OnGetDataListener;
import ro.giohnnysoftware.mondo.library.dbUserExtension;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 101;
    //Animation
    //nrTranslatii = nr Butoane + 1
    //translatia n foloseste pentru curatatea ecranului
    private final int nrTrans = 5;
    private Button btBestScore, btBestTime, btAbout, btSettings;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView lbUserProvider, lbUserNickname;
    private final TranslateAnimation[] animation = new TranslateAnimation[nrTrans];
    private String current_provider, nNickName;
    private Handler UIhandler;
    private int i = 0;
    public static MediaPlayer sfxPlayer;
    public static MediaPlayer musicPlayer;
    private int internet_check=0;

    //Google Ads
    private AdView mAdView;
    //AdView adView = new AdView(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        relateLayoutXML();
        //Async firebase read
        mFbAsyncGetUserExt(getIntent().getStringExtra("userId"));
        modifyFonts();
        internet_check = 0;//unchecked
        addOnClickListeners();
        setUIhandler();
        setSFX_MediaPlayer();

        //Ads
        InitMobileAds();
        requestBannerAd();
    }


    private void InitMobileAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }

    private void requestBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
/*
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
*/
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied\nThe application can't continue!", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }
    }

    private void setSFX_MediaPlayer() {
        sfxPlayer = MediaPlayer.create(this, R.raw.button_sfx);
    }

    public void startBackgroundMusic() {
        musicPlayer = MediaPlayer.create(this, R.raw.mondo_music);
        musicPlayer.setLooping(true);
        if (LoginActivity.getDbUserExt().getMusic()) musicPlayer.start();
    }

    private void setUIhandler() {
        UIhandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Bundle b = msg.getData();
                int value = b.getInt("KEY");
                switch (value) {
                    case Constants.NICKNAME_EXIST: {
                        Context context = getApplicationContext();
                        Toast t = Toast.makeText(context, R.string.Nickname_exist, Toast.LENGTH_LONG);
                        t.show();
                        break;
                    }
                    case Constants.NICKNAME_NOT_EXIST: {
                        lbUserNickname.setText(nNickName);
                        break;
                    }
                    case Constants.GUEST_TO_USER: {
                        lbUserNickname.setText(LoginActivity.getDbUserExt().getNickName());
                        break;
                    }

                    case Constants.NO_INTERNET_CONNECTION: {
                        internet_check=2;
                        Context context = getApplicationContext();
                        Toast t = Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG);
                        t.show();
                        break;
                    }

                    case Constants.BANNER_NOT_LOADED: {
                        //Log.d("AdColonyMainActivity", "setUIhandler");
                        break;
                    }

                    default:
                        break;
                }
            }
        };
    }

    private void relateLayoutXML() {
        //assign entities
        tvTitle = findViewById(R.id.tvTitle);
        //provider image google/Fb/Twitter/Email
        lbUserProvider = findViewById(R.id.lbUserProvider);
        //Nickname
        lbUserNickname = findViewById(R.id.lbUserNickname);

        //Buttons
        btBestScore = findViewById(R.id.btBestScore);
        btBestTime = findViewById(R.id.btBestTime);
        btSettings = findViewById(R.id.btSettings);
        btAbout = findViewById(R.id.btAbout);

        //progress bar
        progressBar = findViewById(R.id.progressBar);
        //GoogleAds
        mAdView = findViewById(R.id.VMainFooter);
    }

    private void modifyFonts() {
        //modify text font
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);

        //font for buttons
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        btBestScore.setTypeface(face2);
        btBestTime.setTypeface(face2);
        btSettings.setTypeface(face2);
        btAbout.setTypeface(face2);
    }

    private boolean checkInternetConnection() {
        //send message to handler in order to display the Toast in the application context
        if (internet_check!=1) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("KEY", Constants.NO_INTERNET_CONNECTION);
            msg.setData(b);
            UIhandler.sendMessage(msg);
            return false;
        }
        return  true;
    }

    @SuppressLint("SetTextI18n")
    //https://stackoverflow.com/questions/62245794/why-do-we-need-to-add-suppresslintsettexti18n-annotation-before-concatenati
    //Internationalization = Multilanguage
    private void addOnClickListeners() {
        //animate screen to next intent => best score in time
        btBestScore.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            Intent intent = new Intent(MainActivity.this, BestInTime.class);
            startActivity(intent);
        });

        //animate screen to next intent => best time to score
        btBestTime.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            Intent intent = new Intent(MainActivity.this, BestToScore.class);
            startActivity(intent);
        });

        //animate screen to next intent => Settings
        btSettings.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        });
        //animate screen to next intent => About
        btAbout.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
        });

        lbUserProvider.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            //Login allowed only for passing from guest to provider
            //Dialog box if already user
            if (!current_provider.equals(PRV_ANONYMOUS)) {
                LayoutInflater dialog = LayoutInflater.from(this);
                final View logoutDialogView = dialog.inflate(R.layout.custom_dialog_yes_no, null);
                final AlertDialog logoutDialog = new AlertDialog.Builder(this).create();
                logoutDialog.setCancelable(false);
                logoutDialog.setView(logoutDialogView);
                TextView dialogMessage = logoutDialogView.findViewById(R.id.txt_dia);
                dialogMessage.setText(getResources().getString(R.string.logout_question));

                logoutDialogView.findViewById(R.id.btn_no).setOnClickListener(v1 -> {
                    //your business logic
                    logoutDialog.dismiss();
                });
                logoutDialogView.findViewById(R.id.btn_yes).setOnClickListener(v1 -> {
                    //your business logic
/*
                    FirebaseAuth.getInstance().signOut();
                    logoutDialog.dismiss();
                    loginProcess();
*/
                    logoutDialog.dismiss();
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(task -> {
                                // user is now signed out
                                //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                loginProcess();
                                //finish();
                            });
                });
                logoutDialog.show();
            }
            //Firebase login process
            else {
                loginProcess();
            }
        });

        lbUserNickname.setOnClickListener(v -> {
            if (!checkInternetConnection()) return;
            if (LoginActivity.getDbUserExt().getSFX()) sfxPlayer.start();
            changeNickNameDialog(LoginActivity.getDbUserExt().getNickName());
        });
    }

    private void loginProcess() {
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.AnonymousBuilder().build());
        // Use ActivityResultLauncher instead of deprecated startActivityForResult
        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    onActivityResult(Constants.RC_SIGN_IN, resultCode, data);
                });

        signInLauncher.launch(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher)
                        .build()
        );

/*
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                Constants.RC_SIGN_IN);
*/
    }

    private void changeNickNameDialog(String text) {
        LayoutInflater dialog = LayoutInflater.from(this);
        final View renameDialogView = dialog.inflate(R.layout.custom_dialog_edit, null);
        final AlertDialog renameDialog = new AlertDialog.Builder(this).create();
        renameDialog.setCancelable(false);
        renameDialog.setView(renameDialogView);

        EditText newNickname = renameDialogView.findViewById(R.id.etChangeNickname);
        newNickname.setText(text);
        newNickname.requestFocus();

        renameDialogView.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            nNickName = newNickname.getText().toString();
            //user not changed
            if (newNickname.getText().toString().equals(lbUserNickname.getText().toString())) {
                Toast.makeText(this, R.string.User_not_changed, Toast.LENGTH_LONG).show();
                renameDialog.dismiss();
                return;
            }
            // can't contain Guest or User
            if (newNickname.getText().toString().contains("User") || newNickname.getText().toString().contains("Guest")) {
                Toast.makeText(this, R.string.Usr_guest_text_not_allowed, Toast.LENGTH_LONG).show();
                renameDialog.dismiss();
                return;
            }
            //check in firebase if the nickname is taken
            LoginActivity.checkNewNickName(newNickname.getText().toString());
            //new thread that will wait the check on the new nickname
            waitForCheckNewNickNameExists(newNickname.getText().toString());
            renameDialog.dismiss();
        });
        renameDialog.show();
    }

    //Async read from Firebase UserExt class
    private void mFbAsyncGetUserExt(String child) {
        //Log.w("GIOhnny", "mFbAsyncGetUserExt: "+child );
        LoginActivity.mReadDataOnce(child, new OnGetDataListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(DataSnapshot data) {
                internet_check = 1;
                LoginActivity.setDbUserExt(data.getValue(dbUserExtension.class));
                lbUserNickname.setText(LoginActivity.getDbUserExt().getNickName());
                //provider image google/Fb/Twitter/Email
                displayProvider(LoginActivity.getDbUserExt().getProvider());
                startBackgroundMusic();
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putInt("KEY", Constants.NO_INTERNET_CONNECTION);
                msg.setData(b);
                UIhandler.sendMessage(msg);
            }
        });
    }

    private void displayProvider(String provider) {

        Drawable drawable;
        if (provider.contains(PRV_GOOGLE)) {
            drawable = ContextCompat.getDrawable(this, R.drawable.googleg_standard_color_18);
            current_provider = PRV_GOOGLE;
        } else if (provider.contains(PRV_FACEBOOK)) {
            drawable = ContextCompat.getDrawable(this, R.drawable.fui_ic_facebook_white_22dp);
            current_provider = PRV_FACEBOOK;
        } else if (provider.contains(PRV_MAIL)) {
            drawable = ContextCompat.getDrawable(this, R.drawable.mail);
            current_provider = PRV_MAIL;
        } else {
            drawable = ContextCompat.getDrawable(this, R.drawable.fui_ic_anonymous_white_24dp);
            current_provider = PRV_ANONYMOUS;
        }
        lbUserProvider.setBackground(drawable);
    }

    private void animate_out() {
        //get display size
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        animation[nrTrans - 1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans - 1].setDuration(500);
        animation[nrTrans - 1].setRepeatCount(0);
        animation[nrTrans - 1].setRepeatMode(0);
        animation[nrTrans - 1].setFillAfter(true);
        btBestScore.startAnimation(animation[nrTrans - 1]);
        btBestTime.startAnimation(animation[nrTrans - 1]);
        btSettings.startAnimation(animation[nrTrans - 1]);
        btAbout.startAnimation(animation[nrTrans - 1]);
    }

    private void animate_in() {
        //get display size
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (animation[0] == null)
            for (int j = 0; j < nrTrans - 1; j++) {
                animation[j] = new TranslateAnimation(displayMetrics.widthPixels, 10f, 0.0f, 0.0f);
                animation[j].setDuration(300);
                animation[j].setRepeatCount(0);
                animation[j].setRepeatMode(0);
                animation[j].setFillAfter(true);
            }

        switch (i) {
            case 0:
                btBestScore.startAnimation(animation[i]);
                i++;
                break;
            case 1:
                btBestTime.startAnimation(animation[i]);
                i++;
                break;
            case 2:
                btSettings.startAnimation(animation[i]);
                i++;
                break;
            case 3:
                btAbout.startAnimation(animation[i]);
                i++;
                break;
            default:
                i = 999;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        animateThread();
    }

    private void animateThread() {
        // Start long running operation in a background thread
        i = 0;
        new Thread(() -> {
            while (i != 999) {
                animate_in();
                // Update the progress bar and display the progress
                try {
                    // Sleep for 200 milliseconds.
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //after while
            Thread.currentThread().interrupt();
        }).start();
    }

    @Override
    protected void onPause() {
        animate_out();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.finishAffinity();
        System.exit(0);
    }

    //New thread in Firebase to check if the user exists
    private void waitForCheckNewNickNameExists(String newNickName) {
        // Start long running operation in a background thread
        progressBar.setVisibility(View.VISIBLE);
        //handler = new Handler();
        new Thread(() -> {
            /*0 default
              1 exists
              2 not exist */
            while (true) {
                if (LoginActivity.getNickNameExists() > 0) {
                    if (LoginActivity.getNickNameExists() == 1) {
                        //send message to handler in order to display the Toast in the application context
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putInt("KEY", Constants.NICKNAME_EXIST);
                        msg.setData(b);
                        UIhandler.sendMessage(msg);
                    } else {
                        LoginActivity.updateNickname(LoginActivity.getDbUserExt().getId(), newNickName);
                        LoginActivity.updateHiScores(LoginActivity.getDbUserExt().getId(),
                                LoginActivity.getDbUserExt().getId(),
                                newNickName);
                        //send message to handler in order to refresh the username
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putInt("KEY", Constants.NICKNAME_NOT_EXIST);
                        msg.setData(b);
                        UIhandler.sendMessage(msg);
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                    //exit while
                    break;
                }
                try {
                    // Sleep for 200 milliseconds.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    //https://www.journaldev.com/9629/android-progressbar-example
    private void delayedUserResync() {
        // Start long running operation in a background thread
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            while (true) {
                if (LoginActivity.getDbUserExt().getId().equals(FirebaseAuth.getInstance().getUid())) {
                    progressBar.setVisibility(View.INVISIBLE);
                    mFbAsyncGetUserExt(FirebaseAuth.getInstance().getUid());
                    //send message to handler in order to refresh the username
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putInt("KEY", Constants.GUEST_TO_USER);
                    msg.setData(b);
                    UIhandler.sendMessage(msg);
                    break;
                }
                try {
                    // Sleep for 200 milliseconds.
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    //lbUserProvider.setOnClickListener => firebase login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("MainActivity onActivityResult","requestCode="+requestCode+", resultCode="+resultCode);
        // RC_SIGN_IN is the request code you passed when starting the sign in flow.
        if (requestCode == Constants.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.e("MainActivity onActivityResult", "response="+response);
            if (resultCode == RESULT_OK) {
                //after authentication OK ==> process transferred to async task
                LoginActivity.updateUser(LoginActivity.getDbUserExt().getId()
                        , FirebaseAuth.getInstance().getUid());
                //Wait for users to sync
                delayedUserResync();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("MainActivity Login", "Login canceled by User");
                    this.finishAffinity();
                    System.exit(0);
                }

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e("MainActivity Login", "No Internet Connection");
                    Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("MainActivity Login", "Unknown Error");
                    Toast.makeText(this, getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Log.e("MainActivity Login", "Unknown sign in response");
        }
    }
}