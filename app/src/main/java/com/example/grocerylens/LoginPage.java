package com.example.grocerylens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    private EditText userInput, passInput;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        auth = FirebaseAuth.getInstance();

        userInput = findViewById(R.id.username_input);
        passInput = findViewById(R.id.password_input);
        Button signInBtn = findViewById(R.id.sign_in_button);
        Button signUpBtn = findViewById(R.id.sign_up_button);
        TextView forgotPass = findViewById(R.id.forgot_password);

        signInBtn.setOnClickListener(v -> {
            String email = userInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginPage.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            } else {
                signInUser(email, password);
            }
        });

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SignUpForm.class);
            startActivity(intent);
        });

        forgotPass.setOnClickListener(v ->
                Toast.makeText(LoginPage.this, "Forgot Password functionality coming soon.", Toast.LENGTH_SHORT).show());
    }

    private void signInUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginPage.this, "Login Successful",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginPage.this, LandingPage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginPage.this, "Login Failed " +
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
