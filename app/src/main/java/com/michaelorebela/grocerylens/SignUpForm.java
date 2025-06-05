package com.michaelorebela.grocerylens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// manages the new user sign-up using Firebase Authentication.
// also saves user info like name and recipe goal to Firebase Realtime Database.

public class SignUpForm extends AppCompatActivity {

    private EditText nameInput, emailInput, passInput, confirmPassInput, goalInput;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);

        // get Firebase instances
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // link input fields
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        passInput = findViewById(R.id.password_input);
        confirmPassInput = findViewById(R.id.confirm_password_input);
        goalInput = findViewById(R.id.goal_input);
        Button signUpBtn = findViewById(R.id.sign_up_button);

        // trigger registration when button is clicked
        signUpBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();
            String confirmPassword = confirmPassInput.getText().toString().trim();
            String goal = goalInput.getText().toString().trim();

            // check all fields before creating account
            if (validateInputs(name, email, password, confirmPassword, goal)) {
                registerUser(name, email, password, Integer.parseInt(goal));
            }
        });
    }

    // Checks that no input is empty and password is valid
    private boolean validateInputs(String name, String email, String password, String confirmPassword, String goal) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(goal) || Integer.parseInt(goal) <= 0) {
            Toast.makeText(this, "Enter a valid recipe goal", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Creates user in Firebase Auth and stores their info in Realtime Database
    private void registerUser(final String name, final String email, String password, int goal) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // store user details like name and recipe goal
                            User user = new User(name, email, 0, goal);
                            databaseReference.child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(SignUpForm.this, "Sign-up Successful!",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpForm.this, LoginPage.class));
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpForm.this, "Failed to save user data. Try again.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpForm.this, "Sign-up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Simple model to store user data in Firebase DB
    public static class User {
        public String name;
        public String email;
        public int recipesViewed;
        public int recipeGoal;

        public User() {
            // required for Firebase
        }

        public User(String name, String email, int recipesViewed, int recipeGoal) {
            this.name = name;
            this.email = email;
            this.recipesViewed = recipesViewed;
            this.recipeGoal = recipeGoal;
        }
    }
}
