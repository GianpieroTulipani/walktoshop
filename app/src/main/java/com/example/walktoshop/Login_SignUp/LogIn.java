package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LogIn extends AppCompatActivity {

    private EditText emailLogIn;
    private EditText passwordLogIn;
    private Button buttonLogIn;
    private TextView notRegistered;
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
        notRegistered = (TextView) findViewById(R.id.not_registered);

        mAuth = FirebaseAuth.getInstance();

        notRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUserRegistrationActivity();
            }
        });

    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(LogIn.this)){
            networkController.connectionDialog(LogIn.this);
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
           Uid = mAuth.getUid();
           Log.d("loggato",Uid);
           db.collection("venditore").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
               @Override
               public void onSuccess(DocumentSnapshot documentSnapshot) {
                   if(documentSnapshot.exists()) {
                       goSellerViewActivity();
                   }else{
                       goUserViewActivity();
                   }
               }
           });
        }
    }

    //questo metodo effettua l'autenticazione dell'utente
    public void login(View v){

        this.email = this.emailLogIn.getText().toString().trim();
        this.password = this.passwordLogIn.getText().toString().trim();

        //qui viene effettuata l'autenticazione che se va a buon fine, controlla la tipologia di utente
        //e lo manda alla home rispettiva
        //altrimenti segnala degli errori rispetto ai campi
        if(checkInfo()) {

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Uid = mAuth.getCurrentUser().getUid();
                                checkTypeUser();
                            }
                            else
                            {
                                emailLogIn.setError(getResources().getString(R.string.InvalidEmail));
                                passwordLogIn.setError(getResources().getString(R.string.InvalidPassword));
                                Toast toast = Toast.makeText(LogIn.this,"EMAIL O PASSWORD ERRATI",Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
                    });
        }
    }

    // questo metodo controlla che tipo di utente si sta loggando e quindi in che home indirizzarlo
    public void  checkTypeUser()
    {
        db.collection("venditore").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            //dopo aver confrontato l'uid dell'utente dell'utente loggato  con quello dei venditori
            //controlliamo che il documento effettivamente esista e se esiste
            //questo logga come venditore, andando alla home venditore
            //altrimenti come utente, andando alla home utente
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    goSellerViewActivity();
                    finish();
                }
                else{
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
    private void goUserRegistrationActivity(){
        final Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    private boolean checkInfo(){

        if(email.isEmpty() ){
            emailLogIn.setError(getResources().getString(R.string.emailEmpty));
            this.emailLogIn.requestFocus();
            return false;

        }else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailLogIn.setError(getResources().getString(R.string.InvalidEmail));
            this.emailLogIn.requestFocus();
            return false;

        }else if(password.isEmpty()){
            passwordLogIn.setError(getResources().getString(R.string.passwordEmpty));
            this.passwordLogIn.requestFocus();
            return false;
        }

        return true;
    }

}
