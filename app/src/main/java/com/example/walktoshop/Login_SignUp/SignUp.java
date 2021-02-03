package com.example.walktoshop.Login_SignUp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.walktoshop.Utils.NetworkController;
import com.example.walktoshop.R;
import com.example.walktoshop.Model.Seller;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.Model.User;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class SignUp extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private EditText password;
    private EditText confpassword;
    private EditText email;
    private SwitchCompat switchButton;
    private Button goNext;
    private TextView already_registered;
    private EditText height;
    private EditText weight;
    private boolean isSeller;
    String stringEmail=null;
    String stringPassword=null;
    String stringConfPassword=null;
    String stringHeight=null;
    String stringWeight=null;
    Seller seller=new Seller();
    private User user=new User();

    /**
     * all'interno di questo metodo vengono salvati i dati riguardo email, password, altezza e peso
     * in prevenzione di un cambio di configurazione
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", email.getText().toString());
        outState.putString("password", password.getText().toString());
        outState.putString("height", height.getText().toString());
        outState.putString("weight", weight.getText().toString());
    }

    /**
     * all'interno del metodo onCreate() oltre alla definizione del layout e dei riferimenti alle componenti del layout
     * viene fatto un controllo nel caso in cui ci fosse un cambio di configurazione, per mantenere i dati all'interno degli editText
     * viene definito un link che porta al log-in, nel caso in cui l'utente sia già registrato e sia andato per sbaglio nella registrazione
     * oltre che a uno switch button che permette di capire quale tipologia di utente si sta registrando
     * e il bottone per concludere la registrazione
     * @param savedInstanceState
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        password=(EditText) findViewById(R.id.password);
        confpassword=(EditText) findViewById(R.id.confermapassword);
        email=(EditText) findViewById(R.id.email);
        switchButton=(SwitchCompat)findViewById(R.id.switch1);
        switchButton.setHintTextColor(Color.WHITE);
        goNext=(Button)findViewById(R.id.nextButton);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        already_registered = (TextView) findViewById(R.id.already_registered);

        //controllo sul cambio di configurazione
        if(savedInstanceState != null){
            email.setText(savedInstanceState.getString("email"));
            password.setText(savedInstanceState.getString("password"));
            height.setText(savedInstanceState.getString("height"));
            weight.setText(savedInstanceState.getString("weight"));
        }

        goNext.setVisibility(View.VISIBLE);
        seller.setUID(mAuth.getUid());

        //controllo che se il link viene cliccato, l'utente viene rimandato all'activity di log-in
        already_registered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUserLogInActivity();
            }
        });

        /*controllo che se lo switch è checked l'utente è un venditore, altrimenti è un'utente.
        * Vengono gestite le visibilità di altezza e peso che sono relative solo alla registrazione lato utente
        * e quindi in caso di switch checked queste non devono essere mostrate, altrimenti si'*/
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(switchButton.isChecked()){
                    weight.setVisibility(View.GONE);
                    height.setVisibility(View.GONE);
                    isSeller = true;
                } else {
                    isSeller = false;
                    weight.setVisibility(View.VISIBLE);
                    height.setVisibility(View.VISIBLE);
                }
            }
        });

        /*qui viene controllato che se il buttone di registrazione viene cliccato allora vengono controllati i dati inseriti nei vari editText
        * e in caso di sucesso parte la creazione dell'utente o del venditore in base allo stato dello switchButton,
        * quindi se è checked viene creato un venditore
        * altrimenti un'utente*/
        goNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkUserInfo()){
                    if(switchButton.isChecked()){
                        //crea e carica il negoziante
                        createSeller();
                    }else{
                        //lato utente
                        createUser();
                    }
                }
            }
        });
    }

    /**
     * All'interno del metodo onStart() viene effettuato un controllo sulla connessione
     */
    @Override
    protected void onStart() {
        super.onStart();
        //qui avviene il controllo sullo stato della connessione
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(SignUp.this)){
            networkController.connectionDialog(SignUp.this);
        }
    }

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nella home venditore
     * con relativo messaggio di successo della registrazione
     */
    private void goSellerViewActivity(){
        Toast toast = Toast.makeText(this,getResources().getString(R.string.RegistrationSuccess),Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        final Intent intent = new Intent(this, SellerView.class);
        intent.putExtra("UID", seller.getUID());
        startActivity(intent);
    }

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nell'activity di log-in
     */
    private void goUserLogInActivity(){
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }

    /**
     * all'interno di questo metodo viene effettuate la creazione dell'utente sul db prima tramite Auth
     * e successivamente viene richiamato il metodo uploadUser che si occupa di caricare tutti i dati
     * relativi all'utente nella raccolta utente
     */
    private void createUser() {
        user.setEmail(stringEmail);
        user.setPassword(stringPassword);

        /*qui viene richiamato l'mAuth per creare un utente provvisto di email e password tramite mAuth
        * verificando che l'email inserita nella registrazione non corrisponda ad un'email già esistente,
        * in caso di successo viene chiamato il metodo uploadUser per il caricamento nella raccolta utente dei dati*/
        mAuth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    uploadUser();
                }else{
                    try
                    {
                        throw task.getException();
                    }catch (FirebaseAuthUserCollisionException existEmail) {
                        email.setError(getResources().getString(R.string.emailExists));
                        email.requestFocus();
                    }catch(Exception e ){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * questo metodo si occupa del caricamento dei dati dell'utente nella raccolta utente del db
     * e di mandare poi l'utente nella home utente
     */
    private void uploadUser() {
        user.setDiscountSteps(new ArrayList<String>());
        user.setHeight(stringHeight);
        user.setWeight(stringWeight);
        user.setUID(mAuth.getUid());
        user.setWalk(new ArrayList<String>());
        user.setDiscountUID(new ArrayList<>());

        /*qui viene effettuata la query al db che si occupa di inserire i dati dell'utente nella raccolta utente del db
        * e in caso di successo dell'operazione l'utente viene mandato nella home utente*/
        db.collection("utente").document(user.getUID()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                goHomeActivity();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FALLIMENTO","fallito");
            }
        });
    }

    /**
     * all'interno di questo metodo viene definito l'intent per mandare l'utente nella home utente
     * con rispettivo messaggio di successo nella registrazione
     */
    public void goHomeActivity() {
        Toast toast = Toast.makeText(getApplicationContext(),getResources().getString(R.string.RegistrationSuccess),Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        final Intent intent = new Intent(SignUp.this, UserView.class);
        intent.putExtra("UID",user.getUID());
        startActivity(intent);
    }

    /**
     * all'interno di questo metodo viene effettuate la creazione del venditore sul db prima tramite Auth
     * e successivamente viene richiamato il metodo uploadSeller che si occupa di caricare tutti i dati
     * relativi all'utente nella raccolta venditore
     */
    private void createSeller() {
        seller.setEmail(stringEmail);
        seller.setPassword(stringPassword);

        /*qui viene richiamato l'mAuth per creare un venditore provvisto di email e password tramite mAuth
         * verificando che l'email inserita nella registrazione non corrisponda ad un'email già esistente,
         * in caso di successo viene chiamato il metodo uploaSeller per il caricamento nella raccolta venditore dei dati*/
        mAuth.createUserWithEmailAndPassword(seller.getEmail(),seller.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    uploadSeller();
                }else{
                    try
                    {
                        throw task.getException();
                    }catch (FirebaseAuthUserCollisionException existEmail) {
                        email.setError(getResources().getString(R.string.emailExists));
                        email.requestFocus();
                    }catch(Exception e ){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * questo metodo si occupa del caricamento dei dati del venditore nella raccolta venditore del db
     * e di mandare poi il venditore nella home venditore
     */
    private void uploadSeller() {
        String sellerUID=mAuth.getUid();
        seller.setUID(sellerUID);

        /*qui viene effettuata la query al db che si occupa di inserire i dati del venditore nella raccolta venditore del db
         * e in caso di successo dell'operazione il venditore viene mandato nella home venditore*/
        db.collection("venditore").document(sellerUID).set(seller).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                goSellerViewActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FALLIMENTO","fallito");
            }
        });
    }

    /**
     * all'interno di questo metodo vengono effettuati i controlli sui campi email, password, altezza e peso
     * (altezza e peso vengono controllati solo nel momento in cui chi si sta iscrivendo sia un utente e non un venditore tramite variabile isSeller)
     * in modo  particolare viene prima controllato che nessuno di questi campi sia vuoto
     * in caso contrario viene mostrato un'errore; viene controllato che la mail corrisponda al pattern standard delle email
     * altrimenti viene mostrato errore; viene controllato che la password rispetti uno specifico pattern
     * (almeno una minuscola,una maiuscola, un numero ed un carattere speciale(@#$%&?!_) e che sia lunga almeno 8),
     * altrimenti da errore; viene controllato che nel caso un utente si stia registrando, che l'altezza non sia al di sotto dei 100 cm
     * o al di sopra dei 270cm, altrimenti da errore; viene controllato che il peso non sia al di sotto dei 40kg e al di sopra dei 250kg,
     * altrimenti da errore.
     * In caso di errore viene ritornato un valore false, altrimenti se tutte le condizioni sono rispettate viene  ritornato un valore true
     * @return
     */
    private boolean checkUserInfo(){
        stringEmail=this.email.getText().toString().trim();
        stringPassword= this.password.getText().toString().trim();
        stringConfPassword= this.confpassword.getText().toString().trim();
        stringHeight = this.height.getText().toString().trim();
        stringWeight = this.weight.getText().toString().trim();

        Pattern PASSWORD_PATTERN
                = Pattern.compile("^" + "(?=.*[0-9])" + "(?=.*[a-z])" + "(?=.*[A-Z])" + "(?=.*[@#$%&?!_])" + "(?=\\S+$)" + ".{8,}" + "$");

        if(stringEmail.isEmpty()){
            this.email.setError(getResources().getString(R.string.emailEmpty));
            this.email.requestFocus();
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(stringEmail).matches()){
            this.email.setError(getResources().getString(R.string.InvalidEmail));
            this.email.requestFocus();
            return false;
        }else if(stringPassword.isEmpty()){
            this.password.setError(getResources().getString(R.string.passwordEmpty));
            this.password.requestFocus();
            return false;
        }else if(stringConfPassword.isEmpty()){
            this.confpassword.setError(getResources().getString(R.string.passwordEmpty));
            this.confpassword.requestFocus();
            return false;
        }else if(!isSeller && stringHeight.isEmpty()){
            this.height.setError( getResources().getString(R.string.heightEmpty));
            this.height.requestFocus();
            return false;
        }else if(!isSeller && stringWeight.isEmpty()) {
            this.weight.setError(getResources().getString(R.string.weightEmpty));
            this.weight.requestFocus();
            return false;
        }else if(!PASSWORD_PATTERN.matcher(stringPassword).matches()){
            Toast toast = Toast.makeText(this,getResources().getString(R.string.WrongPasswordFormat),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.password.setError(getResources().getString(R.string.InvalidPassword));
            this.password.requestFocus();
            return false;
        }else if(!stringPassword.equals(stringConfPassword)){
            this.confpassword.setError(getResources().getString(R.string.EqualPassword));
            this.confpassword.requestFocus();
            this.password.setError(getResources().getString(R.string.EqualPassword));
            this.password.requestFocus();
            return false;
        }else  if(!isSeller && (Long.parseLong(stringHeight) < 100 || Long.parseLong(stringHeight) > 270)){
            Toast toast = Toast.makeText(this,getResources().getString(R.string.wrongHeight),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.height.setError( getResources().getString(R.string.InvalidHeight));
            this.height.requestFocus();
            return false;
        }else if(!isSeller && (Long.parseLong(stringWeight) < 40 || Long.parseLong(stringWeight) > 250)){
            Toast toast = Toast.makeText(this,getResources().getString(R.string.wrongWeight),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.weight.setError(getResources().getString(R.string.InvalidWeight));
            this.weight.requestFocus();
            return false;
        }
        return true;
    }
}

