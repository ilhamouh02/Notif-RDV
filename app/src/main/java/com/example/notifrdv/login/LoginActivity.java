package com.example.notifrdv.login;

// Importations des classes Android pour l'UI et la gestion des intents
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

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
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;

// Importations pour la gestion des hachages et des logs
import android.util.Base64;
import android.util.Log;

// Importations pour les spécifications de clé et d'hachage
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Activité pour la connexion des médecins
public class LoginActivity extends AppCompatActivity {
    EditText loginEmailEDT; // Champ de saisie pour l'email
    EditText loginPasswordEDT; // Champ de saisie pour le mot de passe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_login); // Associe l'interface XML (activity_login.xml)

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des composants UI
        AppCompatButton loginButton = findViewById(R.id.login_activity_button); // Bouton de connexion
        loginEmailEDT = findViewById(R.id.login_activity_email); // Champ d'email
        loginPasswordEDT = findViewById(R.id.login_activity_password); // Champ de mot de passe

        // Configuration de l'écouteur de clic sur le bouton de connexion
        loginButton.setOnClickListener(view -> {
            if (areCredentialsValid()) { // Vérifie la validité des identifiants
                String email = loginEmailEDT.getText().toString().trim(); // Récupère l'email
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class); // Crée un intent pour HomeActivity
                intent.putExtra("email", email); // Passe l'email comme extra
                startActivity(intent); // Lance l'activité HomeActivity
                finish(); // Ferme cette activité
            } else {
                Toast.makeText(getApplicationContext(), "Invalid credentials", Toast.LENGTH_SHORT).show(); // Affiche un message d'erreur
            }
        });
    }

    private boolean areCredentialsValid() {
        // Récupère et nettoie les entrées utilisateur
        String inputEmail = loginEmailEDT.getText().toString().trim(); // Email saisi
        String inputPassword = loginPasswordEDT.getText().toString().trim(); // Mot de passe saisi

        // Vérifie que les champs ne sont pas vides
        if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
            Log.e("LoginActivity", "Email ou mot de passe vide"); // Log d'erreur
            return false;
        }

        // Récupère le médecin correspondant à l'email
        Doctor doctor = Database.getInstance().getDoctorByEmail(inputEmail);
        if (doctor == null) {
            Log.e("LoginActivity", "Aucun docteur trouvé pour email: " + inputEmail); // Log d'erreur
            return false;
        }

        // Récupère le sel associé à l'email
        String salt = Database.getInstance().getSaltByEmail(inputEmail);
        if (salt == null) {
            Log.e("LoginActivity", "Sel non trouvé pour email: " + inputEmail); // Log d'erreur
            return false;
        }

        // Hache le mot de passe saisi avec le sel
        String hashedInputPassword = hashPassword(inputPassword, salt);
        if (hashedInputPassword == null) {
            Log.e("LoginActivity", "Échec du hachage du mot de passe pour email: " + inputEmail); // Log d'erreur
            return false;
        }

        // Compare le mot de passe haché avec celui stocké
        boolean isValid = hashedInputPassword.equals(doctor.getPassword());
        Log.d("LoginActivity", "Validation des identifiants pour " + inputEmail + " : " + (isValid ? "Succès" : "Échec")); // Log de débogage
        return isValid; // Retourne le résultat de la validation
    }

    private String hashPassword(String password, String salt) {
        try {
            int iterations = 10000; // Nombre d'itérations pour le hachage
            char[] chars = password.toCharArray(); // Convertit le mot de passe en tableau de caractères
            byte[] saltBytes = Base64.decode(salt, Base64.DEFAULT); // Décode le sel en bytes
            PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, 256); // Spécifie les paramètres du hachage
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); // Utilise l'algorithme PBKDF2
            byte[] hash = skf.generateSecret(spec).getEncoded(); // Génère le hachage
            return Base64.encodeToString(hash, Base64.DEFAULT).trim(); // Encode et retourne le hachage en Base64
        } catch (Exception e) {
            Log.e("LoginActivity", "Erreur lors du hachage du mot de passe : " + e.getMessage()); // Log de l'erreur
            return null; // Retourne null en cas d'échec
        }
    }
}