package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.walktoshop.R;
import com.example.walktoshop.Seller.Seller;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private EditText username;
    private EditText password;
    private EditText email;
    private Switch switchButton;
    private Button goNext;
    private Button goBack;
    String stringEmail=null;
    String stringPassword=null;
    String stringUsername=null;
    Seller seller=new Seller();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        username=(EditText)findViewById(R.id.username);
        password=(EditText) findViewById(R.id.password);
        email=(EditText) findViewById(R.id.email);
        switchButton=(Switch)findViewById(R.id.switch1);
        goNext=(Button)findViewById(R.id.nextButton);
        goBack=(Button)findViewById(R.id.backButton);
        goBack.setVisibility(View.VISIBLE);
        goNext.setVisibility(View.VISIBLE);
        goNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkUserInfo()){
                    if(switchButton.isChecked()){
                        //crea e carica il negoziante
                        createSeller();

                    }else{
                        //lato utente
                        username.setVisibility(View.INVISIBLE);
                        password.setVisibility(View.INVISIBLE);
                        email.setVisibility(View.INVISIBLE);
                        startFragment();
                    }
                }
            }
        });

    }
    public void goHomeActivity() {
        final Intent intent = new Intent(this, UserView.class);
        intent.putExtra("UID",seller.getUID());
        startActivity(intent);
    }

    private void startFragment()
    {
        Log.d("prova","ok");
        Bundle bundle = new Bundle();
        bundle.putString("email", stringEmail.toString());
        bundle.putString("password", stringPassword.toString());
        bundle.putString("username", stringUsername.toString());
        FragmentManager fm=getSupportFragmentManager();
        Fragment_SignUp fragment_signup =new Fragment_SignUp();
        //addaniamtion
        fragment_signup.setArguments(bundle);
        fm.beginTransaction().addToBackStack(null).replace(R.id.signUpLayout,fragment_signup).commit();
        goBack.setVisibility(View.GONE);
        goNext.setVisibility(View.GONE);
    }

    private void createSeller() {
        Log.d("venditore","venditore");
        seller.setEmail(stringEmail);
        seller.setPassword(stringPassword);
        seller.setUsername(stringUsername);
        mAuth.createUserWithEmailAndPassword(seller.getEmail(),seller.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    uploadSeller();
                }
            }
        });
    }

    private void uploadSeller() {
        seller.setUID(mAuth.getUid());
        db.collection("venditore").document(seller.getUID()).set(seller).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //vai alla home seller
                //.finish()
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private boolean checkUserInfo(){
        stringEmail=this.email.getText().toString().trim();
        stringPassword= this.password.getText().toString().trim();
        stringUsername=this.username.getText().toString().trim();

        if(stringEmail.isEmpty()){
            this.email.setError( getResources().getString(R.string.emailEmpty));
            this.email.requestFocus();
            return false;
        }else if(stringPassword.isEmpty()){
            this.password.setError(getResources().getString(R.string.passwordEmpty));
            this.password.requestFocus();
            return false;
        }else if(stringUsername.isEmpty()){
            this.username.setError( getResources().getString(R.string.usernameEmpty));
            this.username.requestFocus();
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(stringEmail).matches()){
            this.email.setError(getResources().getString(R.string.InvalidEmail));
            this.email.requestFocus();
            return false;
        }else if(stringPassword.length()<6 || stringPassword.length()>20){
            this.password.setError(getResources().getString(R.string.InvalidPassword));
            this.password.requestFocus();
            return false;
        }
        return true;
    }
}

