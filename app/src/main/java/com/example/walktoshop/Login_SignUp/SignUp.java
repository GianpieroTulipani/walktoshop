package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Seller;
import com.example.walktoshop.Seller.SellerView;
import com.example.walktoshop.User.User;
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
    private EditText username;
    private EditText password;
    private EditText email;
    private Switch switchButton;
    private Button goNext;
    private TextView already_registered;
    private EditText height;
    private EditText weight;
    String stringEmail=null;
    String stringPassword=null;
    String stringUsername=null;
    String stringHeight=null;
    String stringWeight=null;
    Seller seller=new Seller();
    boolean isSeller;
    private User user=new User();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        username=(EditText)findViewById(R.id.username);
        password=(EditText) findViewById(R.id.password);
        email=(EditText) findViewById(R.id.email);
        switchButton=(Switch)findViewById(R.id.switch1);
        goNext=(Button)findViewById(R.id.nextButton);
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
                    isSeller = true;
                    weight.setVisibility(View.GONE);
                    height.setVisibility(View.GONE);
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

    private void goSellerViewActivity(){
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
        user.setUsername(stringUsername);
        user.setHeight(stringHeight);
        user.setWeight(stringWeight);
        user.setUID(mAuth.getUid());
        user.setWalk(new ArrayList<String>());
        user.setDisocuntUID(new ArrayList<>());

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
        seller.setUsername(stringUsername);
        String sellerUID=mAuth.getUid();
        seller.setUID(sellerUID);
        db.collection("venditore").document(sellerUID).set(seller).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("SUCCESSO","successoo");
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
        stringUsername=this.username.getText().toString().trim();
        stringHeight = this.height.getText().toString().trim();
        stringWeight = this.weight.getText().toString().trim();

        Pattern PASSWORD_PATTERN
                = Pattern.compile(
                "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}");

        if(stringUsername.isEmpty()){
            this.username.setError(getResources().getString(R.string.usernameEmpty));
            this.username.requestFocus();
            return false;
        }else if(stringEmail.isEmpty()){
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
        }else if(stringPassword.length()<6 || stringPassword.length()>20 || !PASSWORD_PATTERN.matcher(stringPassword).matches()){
            Toast toast = Toast.makeText(this,"La password deve essere lunga almeno 8 caratteri e nserire almeno: una lettera minuscola[a-z], un carattere speciale[!,@,#,$] e due numeri[0,9]",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            this.password.setError(getResources().getString(R.string.InvalidPassword));
            this.password.requestFocus();
            return false;
        }else if(!stringHeight.isEmpty() && !isSeller ){
            try {
                int num = Integer.parseInt(stringHeight);

            } catch (NumberFormatException e) {
                Toast toast = Toast.makeText(this,"inserire un'altezza compresa tra 62cm e 278cm",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                this.height.setError( getResources().getString(R.string.InvalidHeight));
                this.height.requestFocus();
                return false;
            }finally {
                if(Integer.parseInt(stringHeight) < 62 || Integer.parseInt(stringHeight) > 278){
                    Toast toast = Toast.makeText(this,"inserire un'altezza compresa tra 62cm e 278cm",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    this.height.setError( getResources().getString(R.string.InvalidHeight));
                    this.height.requestFocus();
                    return false;
                }

            }
        }else if(!stringWeight.isEmpty() && !isSeller){
            //DA FINIRE!!
            try {
                 int num = Integer.parseInt(stringWeight);
                 Log.d("weight",stringWeight);
            } catch (NumberFormatException e) {
                Toast toast = Toast.makeText(this,"inserire un peso che sia compreso tra 40kg e 250kg",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                this.weight.setError( getResources().getString(R.string.InvalidWeight));
                this.weight.requestFocus();
                return false;
            }finally {
                if(Integer.parseInt(stringWeight) < 40 || Integer.parseInt(stringWeight) > 250){
                    Toast toast = Toast.makeText(this,"inserire un peso che sia compreso tra 40kg e 250kg",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    this.weight.setError( getResources().getString(R.string.InvalidWeight));
                    this.weight.requestFocus();
                    return false;
                }
            }
        }
        return true;
    }
}

