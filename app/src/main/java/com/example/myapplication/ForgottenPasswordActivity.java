package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.example.myapplication.databinding.ActivityForgottenPasswordBinding;

public class ForgottenPasswordActivity extends AppCompatActivity {
    ActivityForgottenPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgottenPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.restorePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.emailRestore.getText().toString().trim().isEmpty()) {
                    String email = binding.emailRestore.getText().toString().trim();
                    restorePassword(email);
                }
            }
        });
    }
    void restorePassword(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "Email sent.");
                            Toast.makeText(ForgottenPasswordActivity.this, "We've sent a recovery email to your email", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(ForgottenPasswordActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                    }
                });
    }
}