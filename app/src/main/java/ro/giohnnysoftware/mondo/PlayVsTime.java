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
import java.util.Collections;
import java.util.Objects;

import ro.giohnnysoftware.mondo.interfaces.Constants;
import ro.giohnnysoftware.mondo.interfaces.OnGetDataListener;
import ro.giohnnysoftware.mondo.library.HiScore_details;
import ro.giohnnysoftware.mondo.library.HiScoresCustomAdapter;
import ro.giohnnysoftware.mondo.library.dbHiScore;

/**
 * Created by GIOhnny on 04/04/2016.
 * Refactored by GIOhnny on 13/07/2020
 */

public class PlayVsTime extends AppCompatActivity {
    //nrTranslatii = nr Butoane + 1
    // translatia n foloseste pentriu curatatea ecranului
    private final int nrTrans = 2;

    private TextView tvTitle;
    private TextView tv60No, tv60UserName, tv60Score;
    private TextView tv120No, tv120UserName, tv120Score;
    private TextView tv180No, tv180UserName, tv180Score;
    private Button btStartVsTime;

    //HiScores
    private TabHost tabHiScores;
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
        setContentView(R.layout.play_vs_time);
        //Preluare parametri in intrare
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
        //assign entities
        tvTitle = findViewById(R.id.tvTitle);
        btStartVsTime = findViewById(R.id.btStartVsTime);

        //GoogleAds
        mAdView = findViewById(R.id.VPlayVsTimeFooter);

        //HiScores
        tv60No = findViewById(R.id.tv60No);
        tv60UserName = findViewById(R.id.tv60Username);
        tv60Score = findViewById(R.id.tv60Score);
        tv120No = findViewById(R.id.tv120No);
        tv120UserName = findViewById(R.id.tv120Username);
        tv120Score = findViewById(R.id.tv120Score);
        tv180No = findViewById(R.id.tv180No);
        tv180UserName = findViewById(R.id.tv180Username);
        tv180Score = findViewById(R.id.tv180Score);
        ListView lvHiScoresT60 = findViewById(R.id.lvHiScore60);
        ListView lvHiScoresT120 = findViewById(R.id.lvHiScore120);
        ListView lvHiScoresT180 = findViewById(R.id.lvHiScore180);
        lvAdapter = new HiScoresCustomAdapter(this, lista_scores, lastInsertedId);
        lvHiScoresT60.setAdapter(lvAdapter);
        lvHiScoresT120.setAdapter(lvAdapter);
        lvHiScoresT180.setAdapter(lvAdapter);
        //HiScores TABS
        tabHiScores = findViewById(R.id.tabHiScores);
        tabHiScores.setup();
        tabHiScores.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        //tabul 1
        TabHost.TabSpec specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestInTime_tab1_name));
        specs.setContent(R.id.tab60);
        specs.setIndicator(createTabView(tabHiScores.getContext(),getResources().getString(R.string.bestInTime_tab1_name)));
        tabHiScores.addTab(specs);

        // tabul 2
        specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestInTime_tab2_name));
        specs.setContent(R.id.tab120);
        specs.setIndicator(createTabView(tabHiScores.getContext(),getResources().getString(R.string.bestInTime_tab2_name)));
        tabHiScores.addTab(specs);

        //tabul 3
        specs = tabHiScores.newTabSpec(getResources().getString(R.string.bestInTime_tab3_name));
        specs.setContent(R.id.tab180);
        specs.setIndicator(createTabView(tabHiScores.getContext(),getResources().getString(R.string.bestInTime_tab3_name)));
        tabHiScores.addTab(specs);

        tabHiScores.setOnTabChangedListener(tabId -> {
            if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
            if (tabId.equals(getResources().getString(R.string.bestInTime_tab1_name)))
                gameType = "T60";
            else if (tabId.equals(getResources().getString(R.string.bestInTime_tab2_name)))
                gameType = "T120";
            else if (tabId.equals(getResources().getString(R.string.bestInTime_tab3_name)))
                gameType = "T180";
            mFbGetHiSores(gameType);
        });

        switch (gameType) {
            case "T60":
                tabHiScores.setCurrentTab(0);
                break;
            case "T120":
                tabHiScores.setCurrentTab(1);
                break;
            case "T180":
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
        tv60No.setTypeface(face3);
        tv60UserName.setTypeface(face3);
        tv60Score.setTypeface(face3);
        tv120No.setTypeface(face3);
        tv120UserName.setTypeface(face3);
        tv120Score.setTypeface(face3);
        tv180No.setTypeface(face3);
        tv180UserName.setTypeface(face3);
        tv180Score.setTypeface(face3);

        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/font2.ttf");
        btStartVsTime.setTypeface(face2);
    }

    private void addOnClickListeners() {
        btStartVsTime.setOnClickListener(v -> {
            if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
            Intent intent = new Intent(PlayVsTime.this, GamePlay.class);
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
            gameType = "T" + EXTRAS_time;
        }
    }

    private void animate_out() {
        //get display size
        final DisplayMetrics displayMetrics=getResources().getDisplayMetrics();
        animation[nrTrans-1] = new TranslateAnimation(10f, -displayMetrics.widthPixels, 0.0f, 0.0f);
        animation[nrTrans-1].setDuration(500);
        animation[nrTrans-1].setRepeatCount(0);
        animation[nrTrans-1].setRepeatMode(0);
        animation[nrTrans-1].setFillAfter(true);
        btStartVsTime.startAnimation(animation[nrTrans - 1]);
    }

    private void animate_in() {
        //get display size
        final DisplayMetrics displayMetrics=getResources().getDisplayMetrics();

        if (animation[0] == null)
            for (int j = 0; j < nrTrans - 1; j++) {
                animation[j] = new TranslateAnimation(displayMetrics.widthPixels, 10f, 0.0f, 0.0f);
                animation[j].setDuration(300);
                animation[j].setRepeatCount(0);
                animation[j].setRepeatMode(0);
                animation[j].setFillAfter(true);
            }

        if (i == 0) {
            btStartVsTime.startAnimation(animation[i]);
            i++;
        } else {
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
        if (LoginActivity.getDbUserExt().getSFX()) MainActivity.sfxPlayer.start();
        animate_out();
        super.onPause();
    }

    // This method is called when the second activity finishes
    // from addOnClickListeners()
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
                int i;
                i = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    dbHiScore d = ds.getValue(dbHiScore.class);
                    HiScore_details detScore = new HiScore_details(i, Objects.requireNonNull(d).getNickName(), d.getScore(), d.getKey());
                    lista_scores.add(detScore);
                    i--;
                }
                Collections.reverse(lista_scores);
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