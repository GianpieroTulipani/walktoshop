package com.example.walktoshop.Login_SignUp;

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
    private ImageView goNext;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        password=(EditText) findViewById(R.id.password);
        confpassword=(EditText) findViewById(R.id.confermapassword);
        email=(EditText) findViewById(R.id.email);
        switchButton=(SwitchCompat)findViewById(R.id.switch1);
        switchButton.setHintTextColor(Color.WHITE);
        goNext=(ImageView)findViewById(R.id.nextButton);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        already_registered = (TextView) findViewById(R.id.already_registered);
        goNext.setVisibility(View.VISIBLE);
        seller.setUID(mAuth.getUid());

        already_registered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUserLogInActivity();
            }
        });

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

    @Override
    protected void onStart() {
        super.onStart();
        NetworkController networkController =new NetworkController();
        if(!networkController.isConnected(SignUp.this)){
            networkController.connectionDialog(SignUp.this);
        }
    }

    private void goSellerViewActivity(){
        Toast toast = Toast.makeText(this,"Registrazione effettuata con succeso!",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        final Intent intent = new Intent(this, SellerView.class);
        intent.putExtra("UID", seller.getUID());
        startActivity(intent);
    }

    private void goUserLogInActivity(){
        final Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }

    private void createUser() {
        user.setEmail(stringEmail);
        user.setPassword(stringPassword);
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

    private void uploadUser() {
        user.setDiscountSteps(new ArrayList<String>());
        user.setHeight(stringHeight);
        user.setWeight(stringWeight);
        user.setUID(mAuth.getUid());
        user.setWalk(new ArrayList<String>());
        user.setDiscountUID(new ArrayList<>());

        db.collection("utente").document(user.getUID()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                goHomeActivity();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void goHomeActivity() {
        Toast toast = Toast.makeText(getApplicationContext(),"Registrazione effettuata con succeso!",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        final Intent intent = new Intent(SignUp.this, UserView.class);
        intent.putExtra("UID",user.getUID());
        startActivity(intent);
    }

    private void createSeller() {
        seller.setEmail(stringEmail);
        seller.setPassword(stringPassword);
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

    private void uploadSeller() {
        String sellerUID=mAuth.getUid();
        seller.setUID(sellerUID);
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
            Toast toast = Toast.makeText(this,"La password deve essere lunga almeno 8 caratteri e inserire almeno: una lettera minuscola,una maiuscola, un carattere speciale[@,#,$,%,&,?,!,_] e un numero",Toast.LENGTH_LONG);
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
            Toast toast = Toast.makeText(this,"inserire un'altezza compresa tra 100cm e 278cm",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.height.setError( getResources().getString(R.string.InvalidHeight));
            this.height.requestFocus();
            return false;
        }else if(!isSeller && (Long.parseLong(stringWeight) < 40 || Long.parseLong(stringWeight) > 250)){
            Toast toast = Toast.makeText(this,"inserire un peso che sia compreso tra 40kg e 250kg",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.weight.setError(getResources().getString(R.string.InvalidWeight));
            this.weight.requestFocus();
            return false;
        }
        return true;
    }
}

