package com.example.notifrdv.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.R;
import com.example.notifrdv.home.HomeActivity;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;

import android.util.Base64;
import android.util.Log;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginActivity extends AppCompatActivity {
    EditText loginEmailEDT;
    EditText loginPasswordEDT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatButton loginButton = findViewById(R.id.login_activity_button);
        loginEmailEDT = findViewById(R.id.login_activity_email);
        loginPasswordEDT = findViewById(R.id.login_activity_password);

        loginButton.setOnClickListener(view -> {
            if (areCredentialsValid()) {
                String email = loginEmailEDT.getText().toString().trim();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean areCredentialsValid() {
        String inputEmail = loginEmailEDT.getText().toString().trim();
        String inputPassword = loginPasswordEDT.getText().toString().trim();

        if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
            Log.e("LoginActivity", "Email ou mot de passe vide");
            return false;
        }

        Doctor doctor = Database.getInstance().getDoctorByEmail(inputEmail);
        if (doctor == null) {
            Log.e("LoginActivity", "Aucun docteur trouvé pour email: " + inputEmail);
            return false;
        }

        String salt = Database.getInstance().getSaltByEmail(inputEmail);
        if (salt == null) {
            Log.e("LoginActivity", "Sel non trouvé pour email: " + inputEmail);
            return false;
        }

        String hashedInputPassword = hashPassword(inputPassword, salt);
        if (hashedInputPassword == null) {
            Log.e("LoginActivity", "Échec du hachage du mot de passe pour email: " + inputEmail);
            return false;
        }

        boolean isValid = hashedInputPassword.equals(doctor.getPassword());
        Log.d("LoginActivity", "Validation des identifiants pour " + inputEmail + " : " + (isValid ? "Succès" : "Échec"));
        return isValid;
    }

    private String hashPassword(String password, String salt) {
        try {
            int iterations = 10000;
            char[] chars = password.toCharArray();
            byte[] saltBytes = Base64.decode(salt, Base64.DEFAULT);
            PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (Exception e) {
            Log.e("LoginActivity", "Erreur lors du hachage du mot de passe : " + e.getMessage());
            return null;
        }
    }
}