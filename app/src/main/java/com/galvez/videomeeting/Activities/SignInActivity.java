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
import com.galvez.videomeeting.databinding.ActivitySignInBinding;
import com.galvez.videomeeting.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    ActivitySignInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        preferenceManager=new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding.textSignUp.setOnClickListener(v -> {
          startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        });

        binding.buttonSignIn.setOnClickListener(v -> {
            fieldValidation();
        });

    }
    private void fieldValidation(){

        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Ingrese su Email");
            binding.inputEmail.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            binding.inputEmail.setError("Ingrese un Email valido");
            binding.inputEmail.requestFocus();
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            binding.inputPassword.setError("Ingrese su contraseña");
            binding.inputPassword.requestFocus();
        }else{
            signin();
        }
    }

    private void signin(){

        binding.buttonSignIn.setVisibility(View.INVISIBLE);
        binding.signInProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId() );
                        preferenceManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else  {
                        binding.signInProgressBar.setVisibility(View.INVISIBLE);
                        binding.buttonSignIn.setVisibility(View.VISIBLE);
                        Toast.makeText(SignInActivity.this, "No se pudo acceder", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}