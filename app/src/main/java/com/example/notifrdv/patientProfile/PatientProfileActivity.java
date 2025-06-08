package com.example.notifrdv.patientProfile;

// Importations des classes Android pour l'UI et la gestion des intents
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour l'activité et les composants
import androidx.appcompat.app.AppCompatActivity;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.utils.ConfirmationDialog;
import com.example.notifrdv.utils.FastToast;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

// Activité pour afficher et gérer le profil d'un patient
public class PatientProfileActivity extends AppCompatActivity {
    private ImageView patientPicture; // ImageView pour la photo du patient
    private TextView patientName; // TextView pour le nom
    private TextView patientEmail; // TextView pour l'email
    private TextView patientPhone; // TextView pour le téléphone
    private TextView patientBirthdateAge; // TextView pour la date de naissance et l'âge
    private TextView patientHeight; // TextView pour la taille
    private TextView patientWeight; // TextView pour le poids
    private TextView patientGender; // TextView pour le genre
    private ImageView deleteButton; // Bouton pour supprimer le patient
    private Patient patient; // Objet patient à afficher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile); // Associe l'interface XML

        initializeViews(); // Initialise les composants UI
        setupStatusBar(); // Configure la barre d'état
        loadPatientData(); // Charge les données du patient
        setupEditButton(); // Configure le bouton d'édition
        setupDeleteButton(); // Configure le bouton de suppression
    }

    private void initializeViews() {
        // Associe les composants UI aux éléments XML
        patientPicture = findViewById(R.id.patient_profile_activity_patient_picture);
        patientName = findViewById(R.id.patient_profile_activity_patient_name);
        patientEmail = findViewById(R.id.patient_profile_activity_patient_email);
        patientPhone = findViewById(R.id.patient_profile_activity_patient_phone_number);
        patientBirthdateAge = findViewById(R.id.patient_profile_activity_patient_birth_date_age);
        patientHeight = findViewById(R.id.patient_profile_activity_patient_height);
        patientWeight = findViewById(R.id.patient_profile_activity_patient_weight);
        patientGender = findViewById(R.id.patient_profile_activity_patient_gender);
        deleteButton = findViewById(R.id.patient_profile_activity_delete_button); // Bouton de suppression
    }

    private void setupStatusBar() {
        // Configure la barre d'état personnalisée
        ImageView backIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusIcon = findViewById(R.id.status_bar_icon);
        TextView title = findViewById(R.id.status_bar_title);

        backIcon.setOnClickListener(v -> finish()); // Ferme l'activité au clic sur le retour
        statusIcon.setImageResource(R.drawable.patient_white); // Icône patient
        title.setText(getString(R.string.patient_profile)); // Titre "Profil patient"
    }

    private void loadPatientData() {
        // Récupère le patient passé via l'Intent
        patient = getIntent().getParcelableExtra("patient");
        if (patient == null) {
            showErrorAndFinish("Données du patient non trouvées"); // Affiche une erreur et ferme si null
            return;
        }
        updateUI(); // Met à jour l'interface avec les données
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        // Met à jour l'interface avec les données du patient
        patientPicture.setImageResource(R.drawable.default_profile_picture); // Image par défaut
        patientName.setText(patient.getName()); // Affiche le nom
        patientEmail.setText(patient.getEmail()); // Affiche l'email
        patientPhone.setText(patient.getPhone()); // Affiche le téléphone
        patientHeight.setText("Taille\n" + formatMeasurement(patient.getHeight()) + " cm"); // Affiche la taille
        patientWeight.setText("Poids\n" + formatMeasurement(patient.getWeight()) + " Kg"); // Affiche le poids
        patientGender.setText("Genre\n" + patient.getGender()); // Affiche le genre

        String birthdate = formatBirthdate(patient.getBirthdate()); // Formate la date
        patientBirthdateAge.setText(birthdate + " (" + patient.getAge() + " ans)"); // Affiche date et âge
    }

    // Configure le bouton de suppression
    private void setupDeleteButton() {
        deleteButton.setOnClickListener(v ->
                ConfirmationDialog.showConfirmationDialog(
                        this,
                        "Confirmer la suppression",
                        "Voulez-vous vraiment supprimer ce patient ?",
                        "Supprimer",
                        "Annuler",
                        this::deletePatient,
                        null
                )
        ); // Ouvre un dialogue de confirmation
    }

    private void deletePatient() {
        // Supprime le patient de la base de données
        if (patient != null) {
            boolean success = Database.getInstance().deletePatient(patient.getId()); // Tente la suppression
            if (success) {
                FastToast.show(this, "Patient supprimé"); // Confirme la suppression
                finish(); // Ferme l'activité
            } else {
                FastToast.show(this, "Échec de la suppression"); // Affiche un message d'échec
            }
        }
    }

    private String formatBirthdate(int birthdate) {
        // Formate la date de naissance (YYYYMMDD en YYYY-MM-DD)
        return String.format("%04d-%02d-%02d",
                birthdate / 10000, // Année
                (birthdate % 10000) / 100, // Mois
                birthdate % 100); // Jour
    }

    private String formatMeasurement(double value) {
        // Formate une mesure (entier ou décimal avec 2 chiffres)
        return value == (int) value ?
                String.valueOf((int) value) :
                String.format("%.2f", value);
    }

    private void setupEditButton() {
        // Configure le bouton d'édition
        findViewById(R.id.patient_profile_activity_edit_patient_info)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditablePatientProfileActivity.class); // Crée un intent
                    intent.putExtra("patient", patient); // Passe le patient
                    startActivity(intent); // Redirige vers EditablePatientProfileActivity
                });
    }

    private void showErrorAndFinish(String message) {
        // Affiche une erreur et ferme l'activité
        Log.e("PatientProfile", message); // Log l'erreur
        FastToast.show(this, "Erreur: " + message); // Affiche un message
        finish(); // Ferme l'activité
    }

    @Override
    protected void onResume() {
        super.onResume(); // Appelle la méthode parent
        if (patient != null) {
            patient = Database.getInstance().getPatientById(patient.getId()); // Recharge les données
            if (patient == null) {
                showErrorAndFinish("Patient non trouvé dans la base"); // Erreur si non trouvé
            } else {
                updateUI(); // Met à jour l'interface
            }
        }
    }
}