package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.walktoshop.R;
import com.example.walktoshop.User.User;
import com.example.walktoshop.User.UserView;
import com.example.walktoshop.User.Walk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Fragment_SignUp extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private EditText weight;
    private EditText height;
    private Button confirm;
    private ProgressBar fragmentSignUpprogressBar;
    String userWeight;
    String userHeight;
    private User user =new User();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_signup,container,false);
        fragmentSignUpprogressBar=view.findViewById(R.id.fragmentSignUpprogressBar);
        fragmentSignUpprogressBar.setVisibility(View.INVISIBLE);
        weight=(EditText)view.findViewById(R.id.weight);
        height=(EditText)view.findViewById(R.id.height);
        confirm=(Button)view.findViewById(R.id.confirm);
        String email;
        String password;
        String username;

        if(getArguments()!=null){
            email=getArguments().getString("email");
            password=getArguments().getString("password");
            username=getArguments().getString("username");
            user.setEmail(email);
            user.setPassword(password);
            user.setUsername(username);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInfo() && getArguments()!=null){
                    fragmentSignUpprogressBar.setVisibility(View.VISIBLE);
                    uploadUser();
                    fragmentSignUpprogressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        return view;
    }

    public void goHomeActivity() {
        final Intent intent = new Intent(getActivity(), UserView.class);
        intent.putExtra("UID",user.getUID());
        startActivity(intent);
    }

    private void uploadUser()
    {
        user.setUID(mAuth.getUid());
        user.setWalk(new ArrayList<String>());
        user.setDisocuntUID(new ArrayList<>());
        db.collection("utente").document(user.getUID()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                goHomeActivity();
                getActivity().finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private boolean checkInfo(){
        userWeight=this.weight.getText().toString().trim();
        userHeight=this.height.getText().toString().trim();

        if(userWeight.isEmpty()){
            try {
                int num = Integer.parseInt(userWeight);
            } catch (NumberFormatException e) {
                this.weight.setError( getResources().getString(R.string.InvalidWeight));
                this.weight.requestFocus();
                return false;
            }
        }else if(userHeight.isEmpty()){
            try {
                int num = Integer.parseInt(userHeight);
            } catch (NumberFormatException e) {
                this.height.setError( getResources().getString(R.string.InvalidHeight));
                this.height.requestFocus();
                return false;
            }
        }

        user.setHeight(userHeight);
        user.setWeight(userWeight);
        return true;
    }
}

