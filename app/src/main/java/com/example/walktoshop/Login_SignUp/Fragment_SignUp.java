package com.example.walktoshop.Login_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.walktoshop.R;
import com.example.walktoshop.User.User;
import com.example.walktoshop.User.UserView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Fragment_SignUp extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private EditText weight;
    private EditText height;
    private Button confirm;
    String userWeight;
    String userHeight;
    private User user =new User();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_signup,container,false);
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
            Log.d("user",user.getEmail()+user.getPassword());
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInfo() && getArguments()!=null){
                    Log.d("user",user.toString());
                    mAuth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                uploadUser();
                                Log.d("ok","ok");
                            }
                        }
                    });
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
        Log.d("ok","ok");
    }

    private boolean checkInfo(){
        userWeight=this.weight.getText().toString().trim();
        userHeight=this.height.getText().toString().trim();
        //finire controlli
        /*
        if(userWeight.isEmpty()){
            this.weight.setError( getResources().getString(R.string.weightEmpty));
            this.weight.requestFocus();
            return false;
        }else if(userHeight.isEmpty()){
            this.height.setError( getResources().getString(R.string.heightEmpty));
            this.height.requestFocus();
            return false;
        }*/
        user.setHeight(userHeight);
        user.setWeight(userWeight);
        return true;
    }
}

