package com.example.notifrdv.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.R;
import com.example.notifrdv.home.HomeActivity;
import com.example.notifrdv.utils.FastToast;
import com.example.notifrdv.utils.database.Database;

public class RegisterActivity extends AppCompatActivity {
    EditText registerNameEDT;
    EditText registerEmailEDT;
    EditText registerPasswordEDT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatButton registerButton = findViewById(R.id.register_activity_button);
        registerNameEDT = findViewById(R.id.register_activity_name);
        registerEmailEDT = findViewById(R.id.register_activity_email);
        registerPasswordEDT = findViewById(R.id.register_activity_password);

        registerButton.setOnClickListener(view -> {
            String name = registerNameEDT.getText().toString().trim();
            String email = registerEmailEDT.getText().toString().trim();
            String password = registerPasswordEDT.getText().toString().trim();

            if (areFieldsValid(name, email, password)) {
                long doctorId = Database.getInstance().addDoctor(name, email, password, "");
                if (doctorId != -1) {
                    FastToast.show(getApplicationContext(), "Inscription réussie");
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                } else {
                    FastToast.show(getApplicationContext(), "Cet email existe déjà ou une erreur s'est produite");
                }
            } else {
                FastToast.show(getApplicationContext(), "Veuillez remplir tous les champs");
            }
        });
    }

    private boolean areFieldsValid(String name, String email, String password) {
        return !name.isEmpty() && !email.isEmpty() && !password.isEmpty();
    }
}