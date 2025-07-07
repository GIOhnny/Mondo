package ro.giohnnysoftware.mondo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Objects;

import ro.giohnnysoftware.mondo.interfaces.Constants;
import ro.giohnnysoftware.mondo.interfaces.OnGetDataListener;
import ro.giohnnysoftware.mondo.library.HiScore_details;
import ro.giohnnysoftware.mondo.library.HiScoresCustomAdapter;
import ro.giohnnysoftware.mondo.library.dbHiScore;

/**
 * Created by GIOhnny on 31/01/2017
 * Refactored by GIOhnny on 12/07/2020
 */

public class PlayToScore extends AppCompatActivity //extends RootActivity
{
    //nrTranslatii = nr Butoane + 1
    // translatia n foloseste pentru curatatea ecranului
    private final int nrTrans = 2;

    private TextView tvTitle;
    private TextView tvScore100No, tvScore100UserName, tvScore100Time;
    private TextView tvScore250No, tvScore250UserName, tvScore250Score;
    private TextView tvScore500No, tvScore500UserName, tvScore500Score;
    private Button btStartToScore;

    //HiScores
    private HiScoresCustomAdapter lvAdapter;
    private final ArrayList<HiScore_details> lista_scores = new ArrayList<>();
    private int gamePlayActivityReturnCode;
    private String gameType;
    private final TranslateAnimation[] animation = new TranslateAnimation[nrTrans];
    private int i = 0;

    //EXTRAS
    private String EXTRAS_time, EXTRAS_points;

    // Syncronizare last score
    private String lastInsertedId;

    //Google Ads
    private AdView mAdView;
    //AdView adView = new AdView(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.play_to_score);
        getExtras();
        relateLayoutXML();
        mFbGetHiSores(gameType);
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
        //GoogleAds
        mAdView = findViewById(R.id.VPlayToScoreFooter);

        //assign entities
        tvTitle = findViewById(R.id.tvTitle);
        btStartToScore = findViewById(R.id.btStartToScore);

        //HiScores
        tvScore100No = findViewById(R.id.tvScore100No);
        tvScore100UserName = findViewById(R.id.tvScore100UserName);
        tvScore100Time = findViewById(R.id.tvScore100Time);
        tvScore250No = findViewById(R.id.tvScore250No);
        tvScore250UserName = findViewById(R.id.tvScore250Username);
        tvScore250Score = findViewById(R.id.tvScore250Time);
        tvScore500No = findViewById(R.id.tvScore500No);
        tvScore500UserName = findViewById(R.id.tvScore500Username);
        tvScore500Score = findViewById(R.id.tvScore500Time);
        ListView lvTime100 = findViewById(R.id.lvScore100Time);
        ListView lvTime250 = findViewById(R.id.lvScore250Time);
        ListView lvTime500 = findViewById(R.id.lvScore500Time);

        lvAdapter = new HiScoresCustomAdapter(this, lista_scores, lastInsertedId);
        lvTime100.setAdapter(lvAdapter);
        lvTime250.setAdapter(lvAdapter);
        lvTime500.setAdapter(lvAdapter);
        //HiScores TABS
        //HiScores
        TabHost tabHiScores = findViewById(R.id.tabHiScores);
        tabHiScores.setup();
        tabHiScores.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        //tabul 1
        TabHost.TabSpec specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestToScore_tab1_name));
        specs.setContent(R.id.tab100);
        specs.setIndicator(createTabView(tabHiScores.getContext(), getResources().getString(R.string.bestToScore_tab1_name)));
        tabHiScores.addTab(specs);

        // tabul 2
        specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestToScore_tab2_name));
        specs.setContent(R.id.tab250);
        specs.setIndicator(createTabView(tabHiScores.getContext(), getResources().getString(R.string.bestToScore_tab2_name)));
        tabHiScores.addTab(specs);

        //tabul 3
        specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestToScore_tab3_name));
        specs.setContent(R.id.tab500);
        specs.setIndicator(createTabView(tabHiScores.getContext(), getResources().getString(R.string.bestToScore_tab3_name)));
        tabHiScores.addTab(specs);

        //OnTabChange Listener ==> read Firebase
        tabHiScores.setOnTabChangedListener(tabId -> {
            Boolean sfx = LoginActivity.getDbUserExt().getSFX();
            if (sfx != null && sfx) {
                MainActivity.sfxPlayer.start();
            }
            if (tabId.equals(getResources().getString(R.string.bestToScore_tab1_name)))
                gameType = "P100";
            else if (tabId.equals(getResources().getString(R.string.bestToScore_tab2_name)))
                gameType = "P250";
            else if (tabId.equals(getResources().getString(R.string.bestToScore_tab3_name)))
                gameType = "P500";
            mFbGetHiSores(gameType);
        });

        switch (gameType) {
            case "P100":
                tabHiScores.setCurrentTab(0);
                break;
            case "P250":
                tabHiScores.setCurrentTab(1);
                break;
            case "P500":
                tabHiScores.setCurrentTab(2);
                break;
        }
    }

    private View createTabView(final Context context, final String text) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        TextView tv = view.findViewById(R.id.tabsText);
        tv.setText(text);
        //modify text font
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        tv.setTypeface(face2);
        return view;
    }

    private void modifyFonts() {
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);
        Typeface face3 = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        tvScore100No.setTypeface(face3);
        tvScore100UserName.setTypeface(face3);
        tvScore100Time.setTypeface(face3);
        tvScore250No.setTypeface(face3);
        tvScore250UserName.setTypeface(face3);
        tvScore250Score.setTypeface(face3);
        tvScore500No.setTypeface(face3);
        tvScore500UserName.setTypeface(face3);
        tvScore500Score.setTypeface(face3);

        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        btStartToScore.setTypeface(face2);
    }

    private void addOnClickListeners() {
        btStartToScore.setOnClickListener(v -> {
            Boolean sfx = LoginActivity.getDbUserExt().getSFX();
            if (sfx != null && sfx) {
                MainActivity.sfxPlayer.start();
            }
            Intent intent = new Intent(PlayToScore.this, GamePlay.class);
            intent.putExtra("TIME", EXTRAS_time);
            intent.putExtra("POINTS", EXTRAS_points);
            lastInsertedId = null;
            startActivityForResult(intent, gamePlayActivityReturnCode);
        });
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            EXTRAS_time = extras.getString("TIME");
            EXTRAS_points = extras.getString("POINTS");
            gameType = "P" + EXTRAS_points;
        }
    }

    private void animate_out() {
        //get display size
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        animation[nrTrans - 1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans - 1].setDuration(500);
        animation[nrTrans - 1].setRepeatCount(0);
        animation[nrTrans - 1].setRepeatMode(0);
        animation[nrTrans - 1].setFillAfter(true);
        btStartToScore.startAnimation(animation[nrTrans - 1]);
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

        if (i == 0) {
            btStartToScore.startAnimation(animation[i]);
            i++;
        } else {
            i = 999;
        }
    }

    @SuppressLint("HandlerLeak")
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

    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (gamePlayActivityReturnCode == requestCode) {
            if (resultCode == RESULT_OK) {
                // get String data from Intent
                lastInsertedId = data.getStringExtra("HiScoreKey");
                mFbGetHiSores(gameType);
            }
        }
    }

    //Async read from Firebase UserExt class
    private void mFbGetHiSores(String gameType) {
        lista_scores.clear();
        LoginActivity.listHiScores(gameType, 100, new OnGetDataListener() {
            @Override
            public void onStart() {
                //DO SOME THING WHEN START GET DATA HERE
            }

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    i++;
                    dbHiScore d = ds.getValue(dbHiScore.class);
                    HiScore_details detScore = new HiScore_details(i, Objects.requireNonNull(d).getNickName(), d.getScore(), d.getKey());
                    lista_scores.add(detScore);
                }
                //Collections.reverse(lista_scores);
                lvAdapter.notifyDataSetChanged();
                //After game finished it will return the key of the last inserted hiScore
                if (lastInsertedId != null) lvAdapter.setIdToMatch(lastInsertedId);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
            }
        });
    }
}
