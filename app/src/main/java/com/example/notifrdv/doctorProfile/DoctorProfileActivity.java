package com.example.notifrdv.doctorProfile;

// Importations statiques pour des constantes et méthodes utilitaires
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

// Importations des classes Android pour l'UI, la gestion des intents, et les fichiers
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour les composants de l'UI et la gestion des marges
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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
import com.example.notifrdv.utils.database.Doctor;

// Importations pour la gestion des fichiers et des exceptions
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Activité pour gérer le profil du médecin (création ou modification)
public class DoctorProfileActivity extends AppCompatActivity {
    EditText doctorNameInput; // Champ de saisie pour le nom du médecin
    EditText doctorEmailInput; // Champ de saisie pour l'email du médecin
    EditText doctorPasswordInput; // Champ de saisie pour le mot de passe du médecin
    ImageView doctorPicture; // ImageView pour afficher la photo du médecin

    long doctorId = -1; // ID du médecin, initialisé à -1 (non défini)
    String doctorPicturePath = ""; // Chemin de la photo du médecin
    static final int PICK_IMAGE_REQUEST = 1; // Code de requête pour le sélecteur d'image
    Uri imageUri; // URI de l'image sélectionnée
    boolean isFirstTime = false; // Indicateur pour savoir si c'est la première connexion

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_doctor_profile); // Associe l'interface XML (activity_doctor_profile.xml)
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation en mode portrait

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des éléments de la barre de statut (via le layout inclus)
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon); // Bouton retour
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon); // Icône de la barre d'état
        TextView statusBarTitle = findViewById(R.id.status_bar_title); // Titre de la barre d'état
        statusBackIcon.setOnClickListener(v -> finish()); // Ferme l'activité et revient à l'écran précédent
        statusBarIcon.setImageResource(R.drawable.doctor_white); // Définit une icône de médecin (ressource drawable)
        statusBarTitle.setText(getString(R.string.doctor_profile)); // Définit le titre "Profil du médecin" (défini dans strings.xml)

        doctorPicture = findViewById(R.id.doctor_profile_activity_doctor_picture); // Associe l'ImageView de la photo
        doctorNameInput = findViewById(R.id.doctor_profile_activity_doctor_name); // Associe le champ de nom
        doctorEmailInput = findViewById(R.id.doctor_profile_activity_doctor_email); // Associe le champ d'email
        doctorPasswordInput = findViewById(R.id.doctor_profile_activity_doctor_password); // Associe le champ de mot de passe
        AppCompatButton saveButton = findViewById(R.id.doctor_profile_activity_save_button); // Associe le bouton de sauvegarde

        // Vérifier si c'est la première connexion ou une modification
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", false); // Récupère l'indicateur depuis l'intent
        if (!isFirstTime) {
            // Charger les données du docteur existant
            String email = getIntent().getStringExtra("email"); // Récupère l'email depuis l'intent
            if (email != null) {
                Doctor doctor = Database.getInstance().getDoctorByEmail(email); // Récupère le docteur par email
                if (doctor != null) {
                    doctorId = doctor.getId(); // Stocke l'ID du médecin
                    doctorNameInput.setText(doctor.getName()); // Pré-remplit le nom
                    doctorEmailInput.setText(doctor.getEmail()); // Pré-remplit l'email
                    doctorPasswordInput.setText(""); // Ne pas afficher le mot de passe pour des raisons de sécurité
                    doctorPicturePath = doctor.getPicturePath(); // Récupère le chemin de la photo
                    if (doctorPicturePath != null && !doctorPicturePath.isEmpty()) {
                        File imageFile = new File(doctorPicturePath); // Crée un objet File avec le chemin
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(doctorPicturePath); // Charge l'image depuis le fichier
                            doctorPicture.setImageBitmap(bitmap); // Affiche l'image dans l'ImageView
                        }
                    }
                } else {
                    FastToast.show(this, "Médecin non trouvé."); // Affiche un message d'erreur
                    finish(); // Ferme l'activité si le médecin n'est pas trouvé
                    return;
                }
            }
        }

        // Listener pour sélectionner une image
        doctorPicture.setOnClickListener(v -> openImagePicker()); // Ouvre le sélecteur d'image au clic sur la photo

        // Listener pour le bouton de sauvegarde
        saveButton.setOnClickListener(v -> {
            if (!isDoctorProfileValid()) { // Vérifie si les champs sont valides
                FastToast.show(this, "Veuillez remplir tous les champs."); // Affiche un message si invalide
                return;
            }

            String name = doctorNameInput.getText().toString().trim(); // Récupère et nettoie le nom
            String email = doctorEmailInput.getText().toString().trim(); // Récupère et nettoie l'email
            String password = doctorPasswordInput.getText().toString().trim(); // Récupère et nettoie le mot de passe
            String newPicturePath = imageUri != null ? saveImageLocally(imageUri) : doctorPicturePath; // Sauvegarde la nouvelle image si sélectionnée

            if (isFirstTime) {
                // Créer un nouveau docteur
                long result = Database.getInstance().addDoctor(name, email, password, newPicturePath); // Ajoute le médecin
                if (result != -1) {


                    doctorId = result; // Stocke l'ID généré
                    FastToast.show(this, "Profil créé avec succès"); // Confirme la création
                    Intent intent = new Intent(this, HomeActivity.class); // Redirige vers HomeActivity
                    intent.putExtra("email", email); // Passe l'email
                    startActivity(intent); // Lance l'activité
                    finish(); // Ferme cette activité
                } else {
                    FastToast.show(this, "Cet email existe déjà ou une erreur s'est produite"); // Affiche un message d'erreur
                }
            } else {
                // Mettre à jour le docteur existant
                if (doctorId != -1) {
                    Database.getInstance().updateDoctor(doctorId, name, email, password, newPicturePath); // Met à jour le médecin
                    FastToast.show(this, "Profil mis à jour avec succès"); // Confirme la mise à jour
                    Intent intent = new Intent(this, HomeActivity.class); // Redirige vers HomeActivity
                    intent.putExtra("email", email); // Passe l'email
                    startActivity(intent); // Lance l'activité
                    finish(); // Ferme cette activité
                } else {
                    FastToast.show(this, "Erreur lors de la mise à jour du profil : ID du médecin non trouvé"); // Affiche un message d'erreur
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(); // Crée un intent pour sélectionner une image
        intent.setType("image/*"); // Filtre pour les fichiers image
        intent.setAction(Intent.ACTION_GET_CONTENT); // Action pour ouvrir le sélecteur
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST); // Lance le sélecteur
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Récupère l'URI de l'image sélectionnée
            doctorPicture.setImageURI(imageUri); // Affiche l'image dans l'ImageView
        }
    }

    private String saveImageLocally(Uri imageUri) {
        if (imageUri == null) return null; // Retourne null si l'URI est null
        try {
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri); // Charge l'image
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DoctorImages"); // Crée un dossier pour les images
            if (!directory.exists()) {
                directory.mkdirs(); // Crée le dossier s'il n'existe pas
            }
            File file = new File(directory, "doctor_profile_" + System.currentTimeMillis() + ".jpg"); // Crée un fichier avec un nom unique
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // Sauvegarde l'image en JPEG à 100% de qualité
                return file.getAbsolutePath(); // Retourne le chemin absolu du fichier
            }
        } catch (IOException e) {
            Log.e("DoctorProfile", "Erreur lors de l'enregistrement de l'image : " + e.getMessage()); // Log de l'erreur
            return null; // Retourne null en cas d'échec
        }
    }

    private boolean isDoctorProfileValid() {
        // Vérifie que tous les champs sont remplis
        return !doctorNameInput.getText().toString().trim().isEmpty() &&
                !doctorEmailInput.getText().toString().trim().isEmpty() &&
                !doctorPasswordInput.getText().toString().trim().isEmpty();
    }
}