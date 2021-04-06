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
import com.galvez.videomeeting.databinding.ActivitySignUpBinding;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ActivitySignUpBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        preferenceManager= new PreferenceManager(getApplicationContext());
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        fieldValidation();

    }
    private void fieldValidation(){
        binding.buttonSignUp.setOnClickListener(v -> {
            if(binding.inputName.getText().toString().trim().isEmpty()){
                binding.inputName.setError("Campo requerido");
                binding.inputName.requestFocus();
            }else if(binding.inputApellido.getText().toString().trim().isEmpty()){
                binding.inputApellido.setError("Campo requerido");
                binding.inputApellido.requestFocus();
            }else if(binding.inputEmail.getText().toString().trim().isEmpty()){
                binding.inputEmail.setError("Campo requerido");
                binding.inputEmail.requestFocus();
            }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
                binding.inputEmail.setError("Ingrese un correo valido");
                binding.inputEmail.requestFocus();
            }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
                binding.inputPassword.setError("Campo requerido");
                binding.inputPassword.requestFocus();
            }else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
                binding.inputConfirmPassword.setError("Campo requerido");
                binding.inputConfirmPassword.requestFocus();
            }else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
                binding.inputConfirmPassword.setError("Las contraseñas no coinciden");
                binding.inputConfirmPassword.requestFocus();
            }else {
                signup();
            }
        });
    }

    private void signup(){
        binding.buttonSignUp.setVisibility(View.INVISIBLE);
        binding.signUpProgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,binding.inputName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,binding.inputApellido.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_FIRST_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME,binding.inputApellido.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    binding.signUpProgressBar.setVisibility(View.INVISIBLE);
                    binding.buttonSignUp.setVisibility(View.VISIBLE);
                    Toast.makeText(SignUpActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

}