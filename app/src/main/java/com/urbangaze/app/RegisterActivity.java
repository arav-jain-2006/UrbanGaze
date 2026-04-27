package com.urbangaze.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, lastName, email, password;
    private Button signupBtn;
    private TextView goToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        signupBtn = findViewById(R.id.signupBtn);
        goToLogin = findViewById(R.id.goLogin);

        signupBtn.setOnClickListener(v -> signupUser());

        goToLogin.setOnClickListener(v -> finish()); // go back to login
    }

    private void signupUser() {

        String fn = firstName.getText().toString().trim();
        String ln = lastName.getText().toString().trim();
        String em = email.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (!validate(fn, ln, em, pass)) return;

        auth.createUserWithEmailAndPassword(em, pass)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    String fullName = fn + " " + ln;

                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                    auth.getCurrentUser().updateProfile(profileUpdates);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", fullName);
                    userMap.put("email", em);
                    userMap.put("createdAt", System.currentTimeMillis());

                    db.collection("users")
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                goToApp();
                            });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean validate(String fn, String ln, String em, String pass) {

        if (TextUtils.isEmpty(fn)) {
            firstName.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(ln)) {
            lastName.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(em)) {
            email.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            password.setError("Required");
            return false;
        }

        if (pass.length() < 6) {
            password.setError("Min 6 characters");
            return false;
        }

        return true;
    }

    private void goToApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}