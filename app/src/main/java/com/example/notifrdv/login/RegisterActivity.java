package com.example.notifrdv.login;

// Importations des classes Android pour l'UI et la gestion des intents
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

// Importations pour les composants de l'UI et la gestion des marges
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.home.HomeActivity;
import com.example.notifrdv.utils.FastToast;
import com.example.notifrdv.utils.database.Database;

// Activité pour l'inscription des médecins
public class RegisterActivity extends AppCompatActivity {
    EditText registerNameEDT; // Champ de saisie pour le nom
    EditText registerEmailEDT; // Champ de saisie pour l'email
    EditText registerPasswordEDT; // Champ de saisie pour le mot de passe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_register); // Associe l'interface XML (activity_register.xml)

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des composants UI
        AppCompatButton registerButton = findViewById(R.id.register_activity_button); // Bouton d'inscription
        registerNameEDT = findViewById(R.id.register_activity_name); // Champ de nom
        registerEmailEDT = findViewById(R.id.register_activity_email); // Champ d'email
        registerPasswordEDT = findViewById(R.id.register_activity_password); // Champ de mot de passe

        // Configuration de l'écouteur de clic sur le bouton d'inscription
        registerButton.setOnClickListener(view -> {
            // Récupère et nettoie les entrées utilisateur
            String name = registerNameEDT.getText().toString().trim(); // Nom saisi
            String email = registerEmailEDT.getText().toString().trim(); // Email saisi
            String password = registerPasswordEDT.getText().toString().trim(); // Mot de passe saisi

            if (areFieldsValid(name, email, password)) { // Vérifie la validité des champs
                long doctorId = Database.getInstance().addDoctor(name, email, password, ""); // Ajoute le médecin à la base
                if (doctorId != -1) { // Vérifie si l'ajout a réussi
                    FastToast.show(getApplicationContext(), "Inscription réussie"); // Affiche un message de succès
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class)); // Redirige vers HomeActivity
                    finish(); // Ferme cette activité
                } else {
                    FastToast.show(getApplicationContext(), "Cet email existe déjà ou une erreur s'est produite"); // Affiche un message d'erreur
                }
            } else {
                FastToast.show(getApplicationContext(), "Veuillez remplir tous les champs"); // Affiche un message si les champs sont vides
            }
        });
    }

    private boolean areFieldsValid(String name, String email, String password) {
        // Vérifie que tous les champs ne sont pas vides
        return !name.isEmpty() && !email.isEmpty() && !password.isEmpty();
    }
}