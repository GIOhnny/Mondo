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
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class BestInTime extends AppCompatActivity
        implements View.OnClickListener {

    //nrTranslatii = nr Butoane + 1
    // translatia n foloseste pentriu curatatea ecranului
    private final int nrTrans = 5;
    private Button btBest60, btBest120, btBest180, btCustomPlay;
    private TextView tvTitle;
    private final TranslateAnimation[] animation = new TranslateAnimation[nrTrans];
    private int i = 0;

    //Google Ads
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.best_in_time);
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
        tvTitle = findViewById(R.id.tvTitle);
        btBest60 = findViewById(R.id.btBest60);
        btBest120 = findViewById(R.id.btBest120);
        btBest180 = findViewById(R.id.btBest180);
        btCustomPlay = findViewById(R.id.btCustomPlay);
        //Ad Colony
        mAdView = findViewById(R.id.VBestInSecondsFooter);
    }

    private void modifyFonts() {
        //modify text font
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);

        //font for buttons
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        btBest60.setTypeface(face2);
        btBest120.setTypeface(face2);
        btBest180.setTypeface(face2);
        btCustomPlay.setTypeface(face2);
    }

    private void addOnClickListeners() {
        //animate screen to next intent => best score in TIME
        btBest60.setOnClickListener(this);
        btBest120.setOnClickListener(this);
        btBest180.setOnClickListener(this);
        btCustomPlay.setOnClickListener(this);
    }

    private void animate_out()
    {
        //get display size
        final DisplayMetrics displayMetrics=getResources().getDisplayMetrics();
        animation[nrTrans-1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans-1].setDuration(500);
        animation[nrTrans-1].setRepeatCount(0);
        animation[nrTrans-1].setRepeatMode(0);
        animation[nrTrans-1].setFillAfter(true);
        btBest60.startAnimation    (animation[nrTrans-1]);
        btBest120.startAnimation   (animation[nrTrans-1]);
        btBest180.startAnimation   (animation[nrTrans-1]);
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
                btBest60.startAnimation(animation[i]);
                i++;
                break;
            case 1:
                btBest120.startAnimation(animation[i]);
                i++;
                break;
            case 2:
                btBest180.startAnimation(animation[i]);
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
        Boolean sfx = LoginActivity.getDbUserExt().getSFX();
        if (sfx != null && sfx) {
            MainActivity.sfxPlayer.start();
        }
        animate_out();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        Boolean sfx = LoginActivity.getDbUserExt().getSFX();
        if (sfx != null && sfx) {
            MainActivity.sfxPlayer.start();
        }
        Intent intent = new Intent(BestInTime.this, PlayVsTime.class);
        switch (v.getId()) {
            case R.id.btBest60:
                intent.putExtra("TIME", "60");
                intent.putExtra("POINTS", "0");
                break;
            case R.id.btBest120:
                intent.putExtra("TIME", "120");
                intent.putExtra("POINTS", "0");
                break;
            case R.id.btBest180:
                intent.putExtra("TIME", "180");
                intent.putExtra("POINTS", "0");
                break;
            default:
                intent.putExtra("TIME", "999");
                intent.putExtra("POINTS", "0");
                break;
        }
        startActivity(intent);
    }
}
