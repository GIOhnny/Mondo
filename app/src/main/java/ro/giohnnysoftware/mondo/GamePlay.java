package ro.giohnnysoftware.mondo;

import static ro.giohnnysoftware.mondo.MainActivity.musicPlayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import ro.giohnnysoftware.mondo.library.DBAdapter;

public class GamePlay extends AppCompatActivity
        implements View.OnClickListener {

    private TextView tvTime, tvTitle, tvCountry, tvPoints, tvToWin;
    private ImageView imgDrapel;
    private final Button[] btCapital = new Button[10];
    private TableLayout tbAnswers;
    private String hiScoreKey;
    private MediaPlayer goodAnswer, wrongAnswer;

    //EXTRAS
    private String EXTRAS_time, EXTRAS_points;
    private int maxPoints, gameTime;

    //Game play => TO POINTS
    private final Integer[] a = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final ArrayList<Integer> aa = new ArrayList<>(Arrays.asList(a));
    private int NextPoints = 10;
    private Timer timer;
    private CountDownTimer timerDown;
    private int count = 0;
    private final ArrayList<String> capitale = new ArrayList<>();
    private DBAdapter info;
    private int dbCount;
    private boolean gameToScore = false;

    //Google Ads
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private static float pxToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.game_play);
        //Preluare parametri in intrare
        getExtras();
        RelateLayoutXML();
        //modify text font
        modifyFonts();
        setFieldsValues();
        addOnClickListeners();
        setAnswersMediaPlayers();
        openDB();

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
        initInterstitial(adRequest);
        mAdView.loadAd(adRequest);
    }

    private void initInterstitial(AdRequest adRequest) {
        InterstitialAd.load(this, getResources().getString(R.string.adUnitIdInterstitial), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("Interstitial", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("Interstitial", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void insertHiScore() {
        //After closing the Ad return to previous activity the hiScore inserted key
        Intent intent = new Intent();
        intent.putExtra("HiScoreKey", hiScoreKey);
        setResult(RESULT_OK, intent);
        //Log.d("AdColonyInterstitial","onClosed");
        finish();
    }

    private void setInterstitialCallback(){
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d("GoogleAds Interstitial", "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d("GoogleAds Interstitial", "Ad dismissed fullscreen content.");
                insertHiScore();
                mInterstitialAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e("GoogleAds Interstitial", "Ad failed to show fullscreen content.");
                mInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d("GoogleAds Interstitial", "Ad recorded an impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d("GoogleAds Interstitial", "Ad showed fullscreen content.");
            }
        });
    }
    private void displayInterstitialAd() {
        if (mInterstitialAd != null) {
            musicPlayer.pause();
            setInterstitialCallback();
            mInterstitialAd.show(GamePlay.this);
            musicPlayer.start();
        } else {
            Toast.makeText(getApplicationContext(),"Interstitial Ad Is Not Loaded Yet or Request Not Filled",Toast.LENGTH_SHORT).show();
            insertHiScore();
        }
    }
    private void setAnswersMediaPlayers() {
        goodAnswer = MediaPlayer.create(this, R.raw.good_answer);
        wrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            EXTRAS_time = extras.getString("TIME");
            EXTRAS_points = extras.getString("POINTS");

            gameTime = Integer.parseInt(Objects.requireNonNull(EXTRAS_time));
            maxPoints = Integer.parseInt(Objects.requireNonNull(EXTRAS_points));

            gameToScore = (gameTime == 0);
        }
    }

    private void setFieldsValues() {
        tvTime.setText(EXTRAS_time);
        tvPoints.setText(EXTRAS_points);
        tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
    }

    private void RelateLayoutXML() {
        //assign entities
        tvTitle = findViewById(R.id.tvTitle);
        tvCountry = findViewById(R.id.tvCountry);
        tvTime = findViewById(R.id.tvTime);
        tvPoints = findViewById(R.id.tvPoints);
        tvToWin = findViewById(R.id.tvToWin);
        imgDrapel = findViewById(R.id.imgDrapel);
        imgDrapel.setScaleType(ImageView.ScaleType.FIT_XY);

        mAdView = findViewById(R.id.VGamePlayFooter);

        //Answers table
        tbAnswers = findViewById(R.id.tbAnswers);
        btCapital[0] = findViewById(R.id.btCapital0);
        btCapital[1] = findViewById(R.id.btCapital1);
        btCapital[2] = findViewById(R.id.btCapital2);
        btCapital[3] = findViewById(R.id.btCapital3);
        btCapital[4] = findViewById(R.id.btCapital4);
        btCapital[5] = findViewById(R.id.btCapital5);
        btCapital[6] = findViewById(R.id.btCapital6);
        btCapital[7] = findViewById(R.id.btCapital7);
        btCapital[8] = findViewById(R.id.btCapital8);
        btCapital[9] = findViewById(R.id.btCapital9);
    }

    private void modifyFonts() {
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");
        tvTitle.setTypeface(face1);
        Typeface face3 = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        tvTime.setTypeface(face3);
        tvPoints.setTypeface(face3);
        tvToWin.setTypeface(face3);
    }

    private void addOnClickListeners() {
        for (Button b : btCapital) {
            b.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (gameToScore) gameplayToScore(v.getId());
        else gameplayToTime(v.getId());
    }

    private void endOfGameDialog(String text) {

        if (LoginActivity.getDbUserExt().getSFX()) {
            MediaPlayer timeUp = MediaPlayer.create(this, R.raw.time_up);
            timeUp.start();
        }

        LayoutInflater dialog = LayoutInflater.from(this);
        final View endDialogView = dialog.inflate(R.layout.custom_dialog_ok, null);
        final AlertDialog endDialog = new AlertDialog.Builder(this).create();
        endDialog.setCancelable(false);
        endDialog.setView(endDialogView);

        TextView dialogMessage = endDialogView.findViewById(R.id.txt_dia);
        dialogMessage.setText(text);

        endDialogView.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            //your business logic
            insertHiScoreToDB();
            restore_buttons();
            endDialog.dismiss();
            displayInterstitialAd();
        });
        endDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void gameplayToScore(int btnId) {
        Button b = findViewById(btnId);
        String buttonText = b.getText().toString();

        //capitala ghicita
        if (buttonText.equals(capitale.get(0))) {
            if (LoginActivity.getDbUserExt().getSFX()) goodAnswer.start();
            int num = Integer.parseInt(tvPoints.getText().toString()) - NextPoints;
            //am depasit nr de puncte
            if (num <= 0) {
                //dezactivare ecran de joc
                timer.cancel();
                deactivateGameScreen();
                endOfGameDialog(getString(R.string.EndGameToScore, maxPoints, tvTime.getText()));
                closeDB();
            } else {
                //after starting the game init interstitial Ad
                if ((num < maxPoints / 1.5) /*&& (!isInterstitialLoaded)*/){
                    //Log.d("AdColonyInterstitial","gameplayToScore");
                    //==>initInterstitial();
                }

                restore_buttons();
                tvPoints.setText(Integer.toString(num));
                NextPoints = 10;
                tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
                //tvToWin.setText("for " + NextPoints + " points");
                queryNextCountry();
            }
        } else {
            //doar 4 sanse 10, 7, 4, 1 puncte
            if (LoginActivity.getDbUserExt().getSFX()) wrongAnswer.start();
            NextPoints -= 3;
            tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
            //tvToWin.setText("for " + NextPoints + " points");
            b.setVisibility(View.INVISIBLE);
            //daca din 4 incercari nu a reusit
            if (NextPoints < 0) {
                NextPoints = 10;
                //tvToWin.setText("for " + NextPoints + " points");
                tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
                queryNextCountry();
                restore_buttons();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void gameplayToTime(int btnId) {
        Button b = findViewById(btnId);
        String buttonText = b.getText().toString();

        // Raspuns corect
        if (buttonText.equals(capitale.get(0))) {
            if (LoginActivity.getDbUserExt().getSFX()) goodAnswer.start();
            restore_buttons();
            int num = Integer.parseInt(tvPoints.getText().toString()) + NextPoints;
            tvPoints.setText(Integer.toString(num));
            NextPoints = 10;
            //tvToWin.setText("for " + NextPoints + " points");
            tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
            queryNextCountry();
        } else {
            //doar 4 sanse 10, 7, 4, 1 puncte
            if (LoginActivity.getDbUserExt().getSFX()) wrongAnswer.start();
            NextPoints -= 3;
            //tvToWin.setText("for " + NextPoints + " points");
            tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
            b.setVisibility(View.INVISIBLE);
            //daca din 4 incercari nu a reusit
            if (NextPoints < 0) {
                NextPoints = 10;
                //tvToWin.setText("for " + NextPoints + " points");
                tvToWin.setText(getString(R.string.ForNPoints, NextPoints));
                queryNextCountry();
                restore_buttons();
            }
        }
    }

    private void restore_buttons() {
        for (int i = 0; i < 10; i++) {
            btCapital[i].setVisibility(View.VISIBLE);
            btCapital[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                    pxToDp(getResources().getDimension(R.dimen.btPlayTextSize), this.getBaseContext()));
            btCapital[i].invalidate();
        }
    }

    private void deactivateGameScreen() {
        tbAnswers.setVisibility(View.INVISIBLE);
        tvCountry.setVisibility(View.INVISIBLE);
        tvTime.setVisibility(View.INVISIBLE);
        tvPoints.setVisibility(View.INVISIBLE);
        tvToWin.setVisibility(View.INVISIBLE);
        imgDrapel.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        //Log.d("AdColony","Action Pause");
/*
        if (tvTime.getVisibility() == View.VISIBLE) {
            if (gameToScore) timer.cancel();
            else timerDown.cancel();
        }
*/
        super.onPause();
    }

    //@SuppressLint("HandlerLeak")
    @Override
    protected void onResume() {
        super.onResume();
        if (tbAnswers.getVisibility() == View.INVISIBLE) {
            initFirstCountry();
            startGame();
        }
    }

    private void initFirstCountry() {
        setFieldsValues();
        queryNextCountry();
        tvPoints.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
        tvToWin.setVisibility(View.VISIBLE);
        tvCountry.setVisibility(View.VISIBLE);
        tbAnswers.setVisibility(View.VISIBLE);
        imgDrapel.setVisibility(View.VISIBLE);
    }

    private void startGame() {
        //best time to achieve score
        if (gameToScore) setTimer();
            //best score in n seconds
        else {
            setCountdownTimer();
            timerDown.start();
        }
    }

    private void setTimer() {
        timer = new Timer();
        count = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    count++;
                    tvTime.setText(String.valueOf(count));
                });
            }
        }, 1000, 1000);
    }

    private void setCountdownTimer() {
        final int num = gameTime;
        timerDown = new CountDownTimer(num * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if ((millisUntilFinished / 1000) < (num / 1.5) /*&&(!isInterstitialLoaded)*/) {
                    //Log.d("AdColonyInterstitial","setCountdownTimer");
                    //==>initInterstitial();
                }
                tvTime.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                //dezactivare ecran de joc
                timerDown.cancel();
                deactivateGameScreen();
                //Dialog box final cu timpul realizat / nr de puncte
                // dupa terminarea timerului
                closeDB();
                endOfGameDialog(getString(R.string.EndGameToTime, tvPoints.getText(), gameTime));
            }
        };
    }

    //DB Connections
    @SuppressLint("SetTextI18n")
    private void queryNextCountry() {
        Random r = new Random();
        int countryNo = r.nextInt(dbCount) + 1;
        //Split after - => Bosnia-
        //                 Herzegovina
        String tara = info.getTara(countryNo);
        String[] sp = tara.split("-");
        if (sp.length == 2) {
            tvCountry.setText(sp[0] + "-\n" + sp[1]);
        } else
            tvCountry.setText(info.getTara(countryNo));

        capitale.clear();
        //alimentare capitale in ordine
        capitale.add(info.getCapital(countryNo));
        capitale.add(info.getOras(countryNo, 1));
        capitale.add(info.getOras(countryNo, 2));
        capitale.add(info.getOras(countryNo, 3));
        capitale.add(info.getOras(countryNo, 4));

        //insert BLOB in DB din imagine fizica de pe card
       /* for (CountryNo=1;CountryNo<=DBCount+1;CountryNo++ ){
        Bitmap b= BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()+"/flags/"+'f'+String.valueOf(CountryNo)+".png");
        info.updateImg(CountryNo,b);}*/

        imgDrapel.setImageBitmap(info.getDrapel(countryNo));

        //+5 capitale random diferite de ce exista
        for (int i = 5; i < 10; i++) {
            do {
                capitale.add(i, info.getCapital(r.nextInt(dbCount) + 1));
            } while (noOthers(i));
        }

        //shuffle vector
        Collections.shuffle(aa);

        //Mapare 1 la 1 vector de cifre - capitala - buton
        for (int i = 0; i < 10; i++) {
            btCapital[aa.get(i)].setText(capitale.get(i));
            if (btCapital[aa.get(i)].length() > 12) {
                float sizedip = btCapital[aa.get(i)].getTextSize();
                btCapital[aa.get(i)].setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(sizedip, this.getBaseContext()) - 1);
            } else if (btCapital[aa.get(i)].length() > 16) {
                float sizedip = btCapital[aa.get(i)].getTextSize();
                btCapital[aa.get(i)].setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(sizedip, this.getBaseContext()) - 2);
            } else if (btCapital[aa.get(i)].length() >= 20) {
                float sizedip = btCapital[aa.get(i)].getTextSize();
                btCapital[aa.get(i)].setTextSize(TypedValue.COMPLEX_UNIT_DIP, pxToDp(sizedip, this.getBaseContext()) - 3);
            }
        }

        NextPoints = 10;
    }

    private boolean noOthers(int max) {
        if (capitale.get(max).equals("")) return true;
        return IntStream.range(0, max).anyMatch(i -> capitale.get(i).equals(capitale.get(max)));
        /*
                for (int i = 0; i < max; i++) {
            if (capitale.get(i).equals(capitale.get(max)))
                return true;
        }
         */
    }

    private void openDB() {
        info = new DBAdapter(this, Locale.getDefault().getLanguage());
        info.open();
        dbCount = info.getCount().intValue() + 1;
        //in the case the DB don't exist and another error occurs
        if (dbCount <= 0) {
            finishAndRemoveTask();
        }
    }

    private void closeDB() {
        info.close();
    }

    private void insertHiScoreToDB() {
        hiScoreKey =
                LoginActivity.addHiScore(LoginActivity.getDbUserExt().getId()
                        , LoginActivity.getDbUserExt().getNickName()
                        , gameTime == 0 ? "P" + maxPoints : "T" + gameTime
                        , Integer.parseInt((String) (gameTime == 0 ? tvTime.getText() : tvPoints.getText())));
    }

}
