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
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.HashMap;
    import java.util.Map;
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



        private void signUp(String email, String password, String username) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersRef = db.collection("users");

            String normalizedUsername = username.toLowerCase();
            usersRef.whereEqualTo("username", normalizedUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("TAG", "Error checking username", task.getException());
                            Toast.makeText(SignUpActivity.this, "Error checking username", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!task.getResult().isEmpty()) {
                            // Username exists: abort sign-up.
                            Toast.makeText(SignUpActivity.this, "Username already exists. Please choose another one.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Username is available; proceed to create the Auth user.
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (!authTask.isSuccessful()) {
                                        Log.w("TAG", "createUserWithEmail:failure", authTask.getException());
                                        Toast.makeText(SignUpActivity.this, Objects.requireNonNull(authTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user == null) {
                                        Log.e("TAG", "User is null after creation.");
                                        return;
                                    }

                                    // Send verification email.
                                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                        if (!verificationTask.isSuccessful()) {
                                            Log.w("TAG", "Email verification failed", verificationTask.getException());
                                            Toast.makeText(SignUpActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("username", normalizedUsername);

                                        db.collection("users").document(user.getUid())
                                                .set(userData)
                                                .addOnCompleteListener(setTask -> {
                                                    if (!setTask.isSuccessful()) {
                                                        Log.e("TAG", "Error adding user data to Firestore", setTask.getException());
                                                        // Delete the created Auth user to avoid an orphaned account.
                                                        user.delete();
                                                        Toast.makeText(SignUpActivity.this, "Failed to create account, please try again.", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    Log.d("TAG", "User data added to Firestore successfully.");
                                                    Toast.makeText(SignUpActivity.this, "Check your email to confirm your account", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                });
                                    });
                                });
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