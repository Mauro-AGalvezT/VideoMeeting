package com.galvez.videomeeting.Activities;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputFirstName,inputLastName,inputEmail,inputPassword,inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private ProgressBar signupProgressBar;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        preferenceManager= new PreferenceManager(getApplicationContext());


        findViewById(R.id.imageBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.textSignIn).setOnClickListener(v -> onBackPressed());

        inputFirstName=findViewById(R.id.inputName);
        inputLastName=findViewById(R.id.inputApellido);
        inputEmail=findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        inputConfirmPassword=findViewById(R.id.inputConfirmPassword);
        buttonSignUp=findViewById(R.id.buttonSignUp);
        signupProgressBar=findViewById(R.id.signUpProgressBar);

        buttonSignUp.setOnClickListener(v -> {
            if(inputFirstName.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUpActivity.this, "Ingrese su nombre.", Toast.LENGTH_SHORT).show();
            }else if(inputLastName.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUpActivity.this, "Ingrese su apellido.", Toast.LENGTH_SHORT).show();
            }else if(inputEmail.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUpActivity.this, "Ingrese su Email.", Toast.LENGTH_SHORT).show();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){
                Toast.makeText(SignUpActivity.this, "Ingrese un Email valido.", Toast.LENGTH_SHORT).show();
            }else if(inputPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUpActivity.this, "Ingrese una contraseña.", Toast.LENGTH_SHORT).show();
            }else if(inputConfirmPassword.getText().toString().trim().isEmpty()){
                Toast.makeText(SignUpActivity.this, "Confirma tu contraseña.", Toast.LENGTH_SHORT).show();
            }else if(!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())){
                Toast.makeText(SignUpActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            }else {
                signup();
            }
        });

    }

    private void signup(){
        buttonSignUp.setVisibility(View.INVISIBLE);
        signupProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL,inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME,inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL,inputEmail.getText().toString());
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    signupProgressBar.setVisibility(View.INVISIBLE);
                    buttonSignUp.setVisibility(View.VISIBLE);
                    Toast.makeText(SignUpActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

}