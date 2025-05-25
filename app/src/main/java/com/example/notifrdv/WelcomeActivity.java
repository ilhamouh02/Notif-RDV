package com.example.notifrdv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.login.LoginActivity;
import com.example.notifrdv.login.RegisterActivity;
import com.example.notifrdv.utils.database.Database;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class WelcomeActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation de la base de données
        Database.initializeInstance(this);

        // Initialisation des boutons
        Button loginButton = findViewById(R.id.welcome_login_button);
        Button registerButton = findViewById(R.id.welcome_register_button);

        // Listener pour le bouton "Login"
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            });
        } else {
            Log.e("WelcomeActivity", "Login button not found");
        }

        // Listener pour le bouton "Register"
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
            });
        } else {
            Log.e("WelcomeActivity", "Register button not found");
        }
    }
}