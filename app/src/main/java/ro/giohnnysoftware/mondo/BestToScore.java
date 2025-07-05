package ro.giohnnysoftware.mondo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import ro.giohnnysoftware.mondo.interfaces.Constants;

public class BestToScore extends AppCompatActivity
        implements View.OnClickListener {

    private Button btFirstTo100, btFirstTo250, btFirstTo500, btCustomPlay;
    TextView tvTitle;
    //nrTranslatii = nr Butoane + 1
    // translatia n foloseste pentru curatatea ecranului
    private final int nrTrans = 5;
    private final TranslateAnimation[] animation = new TranslateAnimation[nrTrans];
    private int i = 0;
    //Google Ads
    private AdView mAdView;
    //AdView adView = new AdView(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.best_to_score);
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

    private void addOnClickListeners() {
        //animate screen to next intent => best score in TIME
        btFirstTo100.setOnClickListener(this);
        btFirstTo250.setOnClickListener(this);
        btFirstTo500.setOnClickListener(this);
        btCustomPlay.setOnClickListener(this);
    }

    private void relateLayoutXML() {
        //assign entities
        tvTitle = findViewById(R.id.tvTitle);
        btFirstTo100 = findViewById(R.id.btFirstTo100);
        btFirstTo250 = findViewById(R.id.btFirstTo250);
        btFirstTo500 = findViewById(R.id.btFirstTo500);
        btCustomPlay = findViewById(R.id.btCustomPlay);
        //Ad Colony
        mAdView = findViewById(R.id.VBestToScoreFooter);
    }

    private void modifyFonts() {
        //modify text font
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);

        //font for buttons
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        btFirstTo100.setTypeface(face2);
        btFirstTo250.setTypeface(face2);
        btFirstTo500.setTypeface(face2);
        btCustomPlay.setTypeface(face2);
    }

    private void animate_out() {
        //get display size
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        animation[nrTrans - 1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans - 1].setDuration(500);
        animation[nrTrans - 1].setRepeatCount(0);
        animation[nrTrans - 1].setRepeatMode(0);
        animation[nrTrans - 1].setFillAfter(true);
        btFirstTo100.startAnimation(animation[nrTrans-1]);
        btFirstTo250.startAnimation(animation[nrTrans-1]);
        btFirstTo500.startAnimation(animation[nrTrans-1]);
        btCustomPlay.startAnimation(animation[nrTrans-1]);
    }

    private void animate_in()
    {
        //get display size
        final DisplayMetrics displayMetrics=getResources().getDisplayMetrics();

        if (animation[0] == null)
            for (int j=0;j< nrTrans-1;j++) {
                animation[j] = new TranslateAnimation(displayMetrics.widthPixels,10f, 0.0f, 0.0f);
                animation[j].setDuration(300);
                animation[j].setRepeatCount(0);
                animation[j].setRepeatMode(0);
                animation[j].setFillAfter(true);
            }

        switch(i){
            case 0:
                btFirstTo100.startAnimation(animation[i]);
                i++;
                break;
            case 1:
                btFirstTo250.startAnimation(animation[i]);
                i++;
                break;
            case 2:
                btFirstTo500.startAnimation(animation[i]);
                i++;
                break;
            case 3:
                btCustomPlay.startAnimation(animation[i]);
                i++;
                break;
            default: i=999;
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
        if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
        animate_out();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
        Intent intent = new Intent(BestToScore.this, PlayToScore.class);
        switch (v.getId()) {
            case R.id.btFirstTo100:
                intent.putExtra("TIME", "0");
                intent.putExtra("POINTS", "100");
                break;
            case R.id.btFirstTo250:
                intent.putExtra("TIME", "0");
                intent.putExtra("POINTS", "250");
                break;
            case R.id.btFirstTo500:
                intent.putExtra("TIME", "0");
                intent.putExtra("POINTS", "500");
                break;
            default:
                intent.putExtra("TIME", "0");
                intent.putExtra("POINTS", "1000");
                break;
        }
        startActivity(intent);
    }
}
