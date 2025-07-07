package ro.giohnnysoftware.mondo;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ro.giohnnysoftware.mondo.interfaces.Constants;
import ro.giohnnysoftware.mondo.interfaces.OnGetDataListener;
import ro.giohnnysoftware.mondo.library.dbHiScore;
import ro.giohnnysoftware.mondo.library.dbUserExtension;

public class LoginActivity extends AppCompatActivity {
    public LoginActivity() {

    }

    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseDatabase mFirebaseDatabase;
    private static int guestNo;
    private static int nickNameExists;
    private static dbUserExtension dbUserExt;
    private static DatabaseReference mDbUserExtTableRef;
    private static DatabaseReference mDbHiScoresTable;
    private static DatabaseReference mDbGuestIteration;

    public static dbUserExtension getDbUserExt() {
        return dbUserExt;
    }

    public static void setDbUserExt(dbUserExtension dbUserExt) {
        LoginActivity.dbUserExt = dbUserExt;
    }

    public static int getNickNameExists() {
        return nickNameExists;
    }

//https://bugsdb.com/_en/debug/0f5b44e187b5ef960a30310c3e702571
// This function will be called as asynchronous in the main class
public static void mReadDataOnce(String child, final OnGetDataListener listener) {
    listener.onStart();
    mFirebaseDatabase.getReference().child("user_ext").child(child).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            listener.onSuccess(dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            listener.onFailed(databaseError);
        }
    });
}

    public static String addHiScore(String userId, String nickName, String gameType, int score) {
        String key = mDbHiScoresTable.push().getKey();
        dbHiScore hiScore = new dbHiScore(userId, nickName, score, key);
        mDbHiScoresTable.child(gameType).child(Objects.requireNonNull(key)).setValue(hiScore);
        return key;
    }

    // This function will be called as asynchronous in the PlayToScore and PlayVsTime classes
    public static void listHiScores(String gameType, int limit, final OnGetDataListener listener) {
        listener.onStart();
        Query query = mDbHiScoresTable.child(gameType).orderByChild("score").limitToLast(limit);
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listener.onSuccess(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailed(error);
                    }
                }
        );
    }

    public static void updateGuestNo() {
        guestNo++;
        mDbGuestIteration.setValue(guestNo);
    }

    private static String getProvider() {
        String provider = null;
        for (UserInfo user : Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderData()) {
            //array [0] Firebase [1] Google.Fb/...
            provider = user.getProviderId();
        }
        return provider;
    }

    //used only when passing from guest to user
    public static void updateUser(String guestUserId, String newUserId) {
        Query queryUserExist = mDbUserExtTableRef.child(newUserId);
        queryUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //user already connected
                if (dataSnapshot.exists()) {
                    dbUserExt = dataSnapshot.getValue(dbUserExtension.class);
                } else {
                    dbUserExt = new dbUserExtension(newUserId, Integer.toString(guestNo), getProvider());
                    //store the record
                    mDbUserExtTableRef.child(newUserId).setValue(dbUserExt);
                }
                //update high scores
                updateHiScores(guestUserId, newUserId, Objects.requireNonNull(dbUserExt).getNickName());

                //delete old Guest
                deleteGuest(guestUserId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //used only when passing from guest to user
    public static void checkNewNickName(String newNickName) {
        Query queryUser = mDbUserExtTableRef.orderByKey();
        nickNameExists = 0;
        //Log.w("GIOhnny", "Check nickname "+newNickName + " // "+nickNameExists);
        queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //user already connected
                //Log.w("GIOhnny", "LoginActivity onDatachange "+snapshot.getChildrenCount());
                for (DataSnapshot ds : snapshot.getChildren()) {
                    dbUserExtension d = ds.getValue(dbUserExtension.class);
                    assert d != null;
                    if (d.getNickName().equals(newNickName)) {
                        nickNameExists = 1;
                        //Log.w("GIOhnny", "Nickname exists on user : " + d.getId() + " // " + newNickName);
                        break;
                    }
                }
                if (nickNameExists == 0) {
                    nickNameExists = 2;
                    //Log.w("GIOhnny", "Nickname not exists : " + nickNameExists);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void updateNickname(String userID, String newNickname) {
        Query queryUserExist = mDbUserExtTableRef.orderByKey().equalTo(userID);
        queryUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().child(userID).child("nickName").setValue(newNickname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void updateMusicSFX(String userID, Boolean music, Boolean sfx) {
        Query queryUserExist = mDbUserExtTableRef.orderByKey().equalTo(userID);
        queryUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().child(userID).child("music").setValue(music);
                snapshot.getRef().child(userID).child("sfx").setValue(sfx);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void startMainActivity(String action) {
        String userId = FirebaseAuth.getInstance().getUid();
        //Log.w("GIOhnny", "startMainActivity: "+action );
        if (action.contains("SignIn")) {
            //create new record of dbUserExt DB
            dbUserExt = new dbUserExtension(userId, Integer.toString(guestNo), getProvider());
            //store the record
            mDbUserExtTableRef.child(Objects.requireNonNull(userId)).setValue(dbUserExt);
            updateGuestNo();
        }

        //start MainClass
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", userId);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(intent, options.toBundle());
    }


    private void getUserExt(String userId, String provider) {
        dbUserExt = new dbUserExtension();
        DatabaseReference mDbUserReference = mFirebaseDatabase.getReference().child("user_ext").child(userId);
        mDbUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //create new user
                    dbUserExt = new dbUserExtension(userId, userId, provider);
                    mDbUserExtTableRef.child(userId).setValue(dbUserExt);
                } else {
                    //read current user values
                    dbUserExt = dataSnapshot.getValue(dbUserExtension.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //This function can be called also for updating the nick name
    public static void updateHiScores(String oldUser, String newUser, String newNickName) {
        Query query = mDbHiScoresTable.orderByKey();
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get HiScores types P100 P250 ... T60 T120 ...
                        //Log.w("HiScore", "count = : "+(int) snapshot.getChildrenCount());
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //Log.w("HiScore children ", ds.getKey()+ " = " +String.valueOf(ds.getChildrenCount()));

                            //For each score in each category search for the old UserId and update it
                            //Also update the nickname
                            for (DataSnapshot dd : ds.getChildren()) {
                                dbHiScore d = dd.getValue(dbHiScore.class);
                                //Log.w("HiScore ", d.getKey() + " // " + d.getUserId() + " // " + d.getScore());
                                //Update by direct reference
                                assert d != null;
                                if (d.getUserId().equals(oldUser)) {
                                    dd.getRef().child("userId").setValue(newUser);
                                    dd.getRef().child("nickName").setValue(newNickName);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static void deleteGuest(String oldUser) {
        Query query = mDbUserExtTableRef.orderByChild("id").equalTo(oldUser);
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                //delete record
                                ds.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {
            // Already signed in
            //Log.w("GIOhnny ", "onCreate: login");
            startMainActivity("Login");
            finish();
        } else {
            //Log.w("GIOhnny ", "onCreate: Signin");
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.AnonymousBuilder().build());
            // Not signed in. Start the login flow.

            // Create and launch sign-in intent
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.mipmap.ic_launcher)
                    .setTosAndPrivacyPolicyUrls("https://mondo.giohnnysoftware.ro/terms-of-service.html",
                                         "https://mondo.giohnnysoftware.ro/privacy.html")
                    .build();
            signInLauncher.launch(signInIntent);

/*
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
*/
        }

        //connect database
        mDbUserExtTableRef = mFirebaseDatabase.getReference().child("user_ext");
        mDbHiScoresTable = mFirebaseDatabase.getReference().child("hi_scores");
        mDbGuestIteration = mFirebaseDatabase.getReference().child("guest_no");

        //Initialize record guest_no
        //mDbGuestIteration.push();
        //mDbGuestIteration.setValue(0);
    }

    public void checkUserExistence(String userId) {
        Query queryUserExist = mDbUserExtTableRef.child(userId);
        queryUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //user already connected
                if (dataSnapshot.exists()) {
                    dbUserExt = dataSnapshot.getValue(dbUserExtension.class);
                    //Log.w("GIOhnny",  "checkUserExistence" );
                    startMainActivity("Login");
                }
                //new user signIn
                else {
                    startMainActivity("SignIn");
                }
                finish();//end on the FirebaseUI Authentication window
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getLastGuestNo() {
        Query queryGuest = mDbGuestIteration.orderByKey();
        queryGuest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guestNo = Objects.requireNonNull(dataSnapshot).getValue(Integer.class);
                //Log.w("GIOhnny",  "onDataChange: getLastGuestNo "+ guestNo );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // After authentication OK, process transferred to async task
            // to read the existing dbUserExtension DB and check if it is a new login or just an existing user
            getLastGuestNo();
            checkUserExistence(FirebaseAuth.getInstance().getUid());
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                Log.e("LoginActivity Login", "Login canceled by User");
                this.finishAffinity();
                System.exit(0);
                return;
            }

            if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                Log.e("LoginActivity Login", "No Internet Connection");
                Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                Log.e("LoginActivity Login", "Unknown Error");
                Toast.makeText(this, getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                return;
            }

            Log.e("LoginActivity Login", "Unknown sign in response");
        }
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LoginActivity + onActivityResult","requestCode="+requestCode+", resultCode="+resultCode);
        // RC_SIGN_IN is the request code you passed when starting the sign in flow.
        if (requestCode == Constants.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //after authentication OK ==> process transferred to async task
                // in order to read the existing dbUserExtension DB and check if is a new login or just
                //an existing user
                getLastGuestNo();
                checkUserExistence(FirebaseAuth.getInstance().getUid());
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("LoginActivity Login", "Login canceled by User");
                    this.finishAffinity();
                    System.exit(0);
                }

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e("LoginActivity Login", "No Internet Connection");
                    Toast.makeText(this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("LoginActivity Login", "Unknown Error");
                    Toast.makeText(this, getResources().getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Log.e("LoginActivity Login", "Unknown sign in response");
        }
    }
*/

}
