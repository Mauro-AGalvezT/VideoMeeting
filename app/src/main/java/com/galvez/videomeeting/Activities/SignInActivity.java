package com.galvez.videomeeting.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.galvez.videomeeting.R;
import com.galvez.videomeeting.Utilities.Constants;
import com.galvez.videomeeting.Utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText edtEmail, edtpass;
    private MaterialButton btnSignIn;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        preferenceManager=new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.textSignUp).setOnClickListener(v -> {
          startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        });

        edtEmail=findViewById(R.id.inputEmail);
        edtpass=findViewById(R.id.inputPassword);

        progressBar=findViewById(R.id.signInProgressBar);
        btnSignIn=findViewById(R.id.buttonSignIn);
        btnSignIn.setOnClickListener(v -> {
            if(edtEmail.getText().toString().trim().isEmpty()) {
                edtEmail.setError("Ingrese su Email");
                edtEmail.requestFocus();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()){
                edtEmail.setError("Ingrese un Email valido");
                edtEmail.requestFocus();
            }else if(edtpass.getText().toString().trim().isEmpty()){
                edtpass.setError("Ingrese su contraseña");
                edtpass.requestFocus();
            }else{
                signin();
            }
        });

    }
    private void signin(){

        btnSignIn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,edtEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,edtpass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() || task.getResult()!=null || task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        progressBar.setVisibility(View.INVISIBLE);
                        btnSignIn.setVisibility(View.VISIBLE);
                        Toast.makeText(SignInActivity.this, "No se pudo acceder", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}