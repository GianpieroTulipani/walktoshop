package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LogIn extends AppCompatActivity {

    private EditText emailLogIn;
    private EditText passwordLogIn;
    private Button buttonLogIn;
    private String email;
    private String password;
    private String Uid = null;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogIn = (EditText) findViewById(R.id.emailLogIn);
        passwordLogIn = (EditText) findViewById(R.id.passwordLogIn);
        buttonLogIn = (Button) findViewById(R.id.buttonlogIn);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            //rimane nel login
        }
        else{
            //checkUser();
        }
    }

    //questo metodo effettua l'autenticazione dell'utente
    public void login(View v){

        this.email = this.emailLogIn.getText().toString().trim();
        this.password = this.passwordLogIn.getText().toString().trim();

        //qui viene effettuata l'autenticazione che se va a buon fine, controlla la tipologia di utente
        //e lo manda alla home rispettiva
        //altrimenti segnala degli errori rispetto ai campi
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("ok", "è andato");
                            Uid = mAuth.getCurrentUser().getUid();
                            checkUser();
                        }
                        else {
                            emailLogIn.setError(getResources().getString(R.string.InvalidEmail));
                            passwordLogIn.setError(getResources().getString(R.string.InvalidPassword));
                            Log.d("ok", "non è andato");
                        }
                    }
                });

    }

    // questo metodo controlla che tipo di utente si sta loggando e quindi in che home indirizzarlo
    public void  checkUser()
    {
        db.collection("venditore").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            //dopo aver confrontato l'uid dell'utente dell'utente loggato  con quello dei venditori
            //controlliamo che il documento effettivamente esista e se esiste
            //questo logga come venditore, andando alla home venditore
            //altrimenti come utente, andando alla home utente
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    Log.d("ok", "venditore");
                    goSellerViewActivity();
                    finish();
                }
                else{
                    Log.d("ok", "utente");
                    goUserViewActivity();
                    finish();
                }
            }
        });
    }
    private void goSellerViewActivity(){
        final Intent intent = new Intent(this, SellerView.class);
        intent.putExtra("UID", Uid);
        startActivity(intent);
    }
    private void goUserViewActivity() {
        final Intent intent = new Intent(this, UserView.class);
        intent.putExtra("UID", Uid);
        startActivity(intent);
    }
}
