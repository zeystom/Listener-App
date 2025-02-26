package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    String email;
    String username;
    FirebaseAuth mAuth;
    String password;
    String retypePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();

        setContentView(binding.getRoot());


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString().trim();
                password = binding.password.getText().toString().trim();
                retypePassword = binding.confirmPassword.getText().toString().trim();
                username = binding.username.getText().toString().trim();
                if (checkFields())
                    signUp(email, password,username);
                else
                    Toast.makeText (SignUpActivity.this, "Some fields are empty", Toast.LENGTH_LONG).show();
            }

        });
    }



    void signUp(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Send email verification
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> verificationTask) {
                                                if (verificationTask.isSuccessful()) {
                                                    Log.d("TAG", "Email verification sent.");
                                                    Toast.makeText(SignUpActivity.this, "Check your email to confirm your account",
                                                            Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                } else {
                                                    Log.w("TAG", "Email verification failed", verificationTask.getException());
                                                    Toast.makeText(SignUpActivity.this, "Failed to send verification email",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    boolean checkFields() {
        if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty() || username.isEmpty()) {
            return false;
        } else {
            return password.equals(retypePassword);
        }
    }
    void updateUI(){
    }


}