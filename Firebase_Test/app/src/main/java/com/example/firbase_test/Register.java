package com.example.firbase_test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, usernameEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://firbasetest-e3708-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("messages");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String username = usernameEditText.getText().toString();

                if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
                    registerUser(email, password, username);
                } else {
                    Toast.makeText(Register.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 註冊成功
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user != null ? ((FirebaseUser) user).getUid() : null;

                        // 儲存用戶資訊到 Realtime Database
                        if (uid != null) {
                            User userData = new User(username, email);
                            mDatabase.child("users").child(uid).setValue(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(Register.this, "註冊成功！", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Register.this, "儲存用戶資料失敗", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // 註冊失敗
                        Toast.makeText(Register.this, "註冊失敗：" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String username;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}