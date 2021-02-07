package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SplashActivity extends AppCompatActivity {

    private static final long MIN_WAIT_INTERVAL = 1500L;
    private static final long MAX_WAIT_INTERVAL = 3000L;
    private static final int GO_AHEAD_WHAT = 1;
    private long mStartTime;
    private boolean mIsDone;
    private FirebaseAuth mAuth;
    private String Uid = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private android.os.Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_AHEAD_WHAT:
                    long elapsedTime = SystemClock.uptimeMillis() - mStartTime;
                    if (elapsedTime >= MIN_WAIT_INTERVAL && !mIsDone) {
                        mIsDone = true;
                        goAhead();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decoderView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decoderView.setSystemUiVisibility(uiOptions);
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * All'interno del metodo onStart() viene effettuato un controllo per quanto riguarda il currentUser,
     * ovvero se l'utente quando è entrato nell'applicazione e non ha effettuato il LogOut, l'utente nel momento in cui riapre l'app
     * viene rispedito nella rispettiva home
     * altrimenti viene mandato nel LogIn
     */
    protected void onStart() {
        super.onStart();
        mStartTime = SystemClock.uptimeMillis();

        /*qui viene effettuato il controllo sullo stato dell'utente se è ancora loggato o meno
         * il controllo viene fatto prendendo il currentUser attraverso l'mAuth e se questo non è vuoto, quindi è rimasto loggato
         * viene fatta una chiamata al db controllando se l'uid(identificativo dell'utente) corrisponde a quello di un venditore
         * se così non è allora sarà sicuramente un utente*/
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Uid = mAuth.getUid();
            db.collection("venditore").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        goSellerViewActivity();
                        finish();
                    }else{
                        goUserViewActivity();
                        finish();
                    }
                }
            });
        }else{
            final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
            mHandler.sendMessageAtTime(goAheadMessage, mStartTime + MAX_WAIT_INTERVAL);
        }

    }

    private void goAhead() {
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
        finish();
    }

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nella home venditore
     */
    private void goSellerViewActivity(){
        final Intent intent = new Intent(this, SellerView.class);
        intent.putExtra("UID", Uid);
        startActivity(intent);
    }

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nella home utente
     */
    private void goUserViewActivity() {
        final Intent intent = new Intent(this, UserView.class);
        intent.putExtra("UID", Uid);
        startActivity(intent);
    }
}
