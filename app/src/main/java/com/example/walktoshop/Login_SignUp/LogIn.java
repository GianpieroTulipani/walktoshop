package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView buttonLogIn;
    private TextView notRegistered;
    private String email;
    private String password;
    private String Uid = null;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * all'interno di questo metodo vengono salvati i dati riguardo email e password
     * in prevenzione di un cambio di configurazione
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
            outState.putString("emailLogIn", emailLogIn.getText().toString());
            outState.putString("passwordLogIn", passwordLogIn.getText().toString());
    }

    /**
     * all'interno del metodo onCreate() oltre alla definizione del layout e dei riferimenti alle componenti del layout
     * viene fatto un controllo nel caso in cui ci fosse un cambio di configurazione, per mantenere i dati all'interno degli editText
     * viene creata un'istanza di FirebaseAuth per effettuare l'Authentication e definito un link che porta all'activity della registrazione
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogIn = (EditText) findViewById(R.id.emailLogIn);
        passwordLogIn = (EditText) findViewById(R.id.passwordLogIn);
        buttonLogIn = (ImageView) findViewById(R.id.buttonlogIn);
        notRegistered = (TextView) findViewById(R.id.not_registered);


        //controllo sul cambio di configurazione
        if(savedInstanceState != null){
            emailLogIn.setText(savedInstanceState.getString("emailLogIn"));
            passwordLogIn.setText(savedInstanceState.getString("passwordLogIn"));
        }

        mAuth = FirebaseAuth.getInstance();

        //controllo che se il link viene cliccato, l'utente viene rimandato all'activity di registrazione
        notRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUserRegistrationActivity();
            }
        });

    }

    /**
     * All'interno del metodo onStart() viene effettuato un controllo sulla connessione
     * e un controllo per quanto riguarda il currentUser, ovvero se l'utente quando è entrato nell'applicazione
     * non ha effettuato il LogOut, l'utente nel momento in cui riapre l'app viene rispedito nella rispettiva home
     * altrimenti viene lasciato nel LogIn
     */
    @Override
    public void onStart() {
        super.onStart();
        //qui viene effettuato il controllo sullo stato della connessione
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(LogIn.this)){
            networkController.connectionDialog(LogIn.this);
        }

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
                   }else{
                       goUserViewActivity();
                   }
               }
           });
        }
    }

    /**
     * all'interno di questo metodo viene effettuato il log-in dell'utente
     * controllando prima che le credenziali siano corrette
     * @param v
     */
    public void login(View v){

        this.email = this.emailLogIn.getText().toString().trim();
        this.password = this.passwordLogIn.getText().toString().trim();

        /*qui viene effettuato il controllo dei campi, che non possono essere vuoti
        * e l'email deve rispettare il pattern standard
          */
        if(checkInfo()) {
            /*qui viene effettuato il log-in tramite mAuth, che:
            * in caso di successo richiama il metodo checkTypeUser() per capire che tipo di utente è, e mandarlo alla home corrispettiva
            * salvando anche l'uid(identificativo dell'utente)
            * altrimenti mostra degli errori*/
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
                                Toast toast = Toast.makeText(LogIn.this,getResources().getString(R.string.emailOrPassWrong),Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
                    });
        }
    }

    /**
     * questo metodo si occupa di verificare che tipo di utente sta effettuando il log-in
     * e quindi in quale home indirizzarlo
     */
    public void  checkTypeUser()
    {
        /*qui viene effettuato il controllo, tramite confronto di uid, ovvero:
        * viene controllato che l'uid(identificativo dell'utente) precedentemente salvato corrisponda ad un venditore
        * nel caso in cui questo sia vero e quindi esiste un documento, l'utente viene rimandato alla home venditore
        * altrimenti viene rimandato alla home utente*/
        db.collection("venditore").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

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

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nell'activity di registrazione
     */
    private void goUserRegistrationActivity(){
        final Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    /**
     * all'interno di questo metodo vengono effettuati controlli sui campi email e password
     * in modo particolare viene controllato che i due campi non siano vuoti
     * e che l'email rispetti il pattern standard di un'email, se ciò è vero viene ritornato il valore true
     * altrimenti viene ritornato false nel momento in cui anche una sola di queste condizioni non venga rispettata
     * @return
     */
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
