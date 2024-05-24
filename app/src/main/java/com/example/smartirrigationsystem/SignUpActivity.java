package com.example.smartirrigationsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import  com.example.smartirrigationsystem.entity.User;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private TextView RedirectionToLogin;

    private FirebaseAuth auth;
    private EditText nameUser, signupEmail, signupPassword;
    private Button buttonLoad;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();  // Ensure FirebaseAuth instance is initialized
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");  // Initialize DatabaseReference

        RedirectionToLogin = findViewById(R.id.loginRedirectText);
        nameUser = findViewById(R.id.nameUser);
        signupEmail = findViewById(R.id.emailUSer);
        signupPassword = findViewById(R.id.passwordUser);
        buttonLoad = findViewById(R.id.bottonLoad);

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String name = nameUser.getText().toString().trim();

                if (user.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                }
                if (pass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                }
                if (name.isEmpty()) {
                    nameUser.setError("Name cannot be empty");
                } else {
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();
                                // Store email separately
                                String email = user;
                                User newUser = new User(name, email, pass); // Modified here
                                // Use userId as the key
                                databaseReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Failed to add user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(SignUpActivity.this, "SignUp Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        RedirectionToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
}