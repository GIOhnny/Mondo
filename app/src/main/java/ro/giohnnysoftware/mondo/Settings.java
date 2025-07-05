package ro.giohnnysoftware.mondo;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class Settings extends AppCompatActivity implements View.OnClickListener {
    //nrTranslatii = nr Butoane + 1
    // translatia n foloseste pentriu curatatea ecranului
    private final int nrTrans = 3;
    private final TranslateAnimation[] animation = new TranslateAnimation[nrTrans];
    private int i = 0;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch swMusic, swSFX;
    private TextView tvTitle;

    //Google Ads
    private AdView mAdView;
    //AdView adView = new AdView(this);

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.settings_layout);
        relateLayoutXML();
        modifyFonts();
        addOnClickListeners();

        //Ads
        //InitMobileAds();
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
    private void relateLayoutXML() {
        //assign entities
        swMusic = findViewById(R.id.swMusic);
        swSFX = findViewById(R.id.swSFX);
        tvTitle = findViewById(R.id.tvTitle);
        //GoogleAds
        mAdView = findViewById(R.id.VSettingsFooter);
    }

    private void modifyFonts() {
        //modify text font
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);

        //font for buttons
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        swMusic.setTypeface(face2);
        swSFX.setTypeface(face2);
    }

    private void addOnClickListeners() {
        //animate screen to next intent => best score in TIME
        swSFX.setOnClickListener(this);
        swMusic.setOnClickListener(this);
    }

    private void animate_out() {
        //get display size
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        animation[nrTrans - 1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans - 1].setDuration(500);
        animation[nrTrans - 1].setRepeatCount(0);
        animation[nrTrans - 1].setRepeatMode(0);
        animation[nrTrans - 1].setFillAfter(true);
        swMusic.startAnimation(animation[nrTrans - 1]);
        swSFX.startAnimation(animation[nrTrans - 1]);
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
                swMusic.startAnimation(animation[i]);
                i++;
                break;
            case 1:
                swSFX.startAnimation(animation[i]);
                i++;
                break;
            default:
                i = 999;
        }

    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onResume() {
        super.onResume();
        syncSWvalues();
        animateThread();
    }

    @Override
    protected void onPause() {
        if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
        animate_out();
        super.onPause();
    }

    private void syncSWvalues() {
        swMusic.setChecked(LoginActivity.getDbUserExt().getMusic());
        swSFX.setChecked(LoginActivity.getDbUserExt().getSFX());
    }

    @Override
    public void onClick(View v) {
        if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
        switch (v.getId()) {
            case R.id.swMusic:
                if (swMusic.isChecked()) {
                    MainActivity.musicPlayer.seekTo(0);
                    MainActivity.musicPlayer.start();
                    LoginActivity.getDbUserExt().setMusic(true);
                } else {
                    MainActivity.musicPlayer.pause();
                    LoginActivity.getDbUserExt().setMusic(false);
                }
                break;
            case R.id.swSFX:
                if (swSFX.isChecked()) {
                    MainActivity.sfxPlayer.seekTo(0);
                    MainActivity.sfxPlayer.start();
                    LoginActivity.getDbUserExt().setSFX(true);
                } else
                    LoginActivity.getDbUserExt().setSFX(false);
                break;
        }
        //Async Firebase update
        LoginActivity.updateMusicSFX(LoginActivity.getDbUserExt().getId(), swMusic.isChecked(), swSFX.isChecked());
    }


}
