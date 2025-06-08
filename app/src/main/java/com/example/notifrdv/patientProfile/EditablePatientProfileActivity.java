package com.example.notifrdv.patientProfile;

// Importations des classes Android pour l'UI et les dialogues
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

// Importations ajoutées pour ImageView et TextView
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour les composants de l'UI
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

// Importations pour la gestion du calendrier
import java.util.Calendar;

// Activité pour créer ou modifier le profil d'un patient
public class EditablePatientProfileActivity extends AppCompatActivity {
    private EditText patientName; // Champ de saisie pour le nom du patient
    private EditText patientEmail; // Champ de saisie pour l'email du patient
    private EditText patientPhone; // Champ de saisie pour le numéro de téléphone
    private EditText patientHeight; // Champ de saisie pour la taille
    private EditText patientWeight; // Champ de saisie pour le poids
    private AppCompatButton patientBirthdate; // Bouton pour sélectionner la date de naissance
    private Spinner patientGender; // Spinner pour sélectionner le genre
    private String selectedBirthdate; // Date de naissance sélectionnée
    private Patient patient; // Objet patient à modifier (null si création)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable_patient_profile); // Associe l'interface XML
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation portrait

        initializeViews(); // Initialise les composants UI
        setupStatusBar(); // Configure la barre d'état
        loadPatientData(); // Charge les données du patient
        setupGenderSpinner(); // Configure le Spinner pour le genre
        setupBirthdatePicker(); // Configure le sélecteur de date
        setupSaveButton(); // Configure le bouton de sauvegarde
    }

    private void initializeViews() {
        // Associe les composants UI aux éléments XML
        patientName = findViewById(R.id.editable_patient_profile_activity_patient_name);
        patientEmail = findViewById(R.id.editable_patient_profile_activity_patient_email);
        patientPhone = findViewById(R.id.editable_patient_profile_activity_patient_phone_number);
        patientHeight = findViewById(R.id.editable_patient_profile_activity_patient_height);
        patientWeight = findViewById(R.id.editable_patient_profile_activity_patient_weight);
        patientBirthdate = findViewById(R.id.editable_patient_profile_activity_patient_date_of_birth);
        patientGender = findViewById(R.id.editable_patient_profile_activity_patient_gender);
    }

    private void setupStatusBar() {
        // Configure la barre d'état personnalisée
        findViewById(R.id.status_bar_back_arrow_icon).setOnClickListener(v -> finish()); // Bouton retour
        ((ImageView) findViewById(R.id.status_bar_icon)).setImageResource(R.drawable.patient_white); // Icône patient
        ((TextView) findViewById(R.id.status_bar_title)).setText(getString(R.string.patient_profile)); // Titre "Profil patient"
    }

    private void loadPatientData() {
        // Récupère le patient passé via l'Intent
        patient = getIntent().getParcelableExtra("patient");

        if (patient != null) {
            // Pré-remplit les champs si un patient existe
            patientName.setText(patient.getName());
            patientEmail.setText(patient.getEmail());
            patientPhone.setText(patient.getPhone());
            patientHeight.setText(String.valueOf(patient.getHeight()));
            patientWeight.setText(String.valueOf(patient.getWeight()));

            // Formatage de la date de naissance (YYYYMMDD en YYYY-MM-DD)
            int birthdate = patient.getBirthdate();
            selectedBirthdate = birthdate != 0 ?
                    String.format("%04d-%02d-%02d", birthdate / 10000, (birthdate % 10000) / 100, birthdate % 100) :
                    getCurrentDate();
            patientBirthdate.setText(selectedBirthdate);
        } else {
            // Initialise avec la date actuelle si c'est un nouveau patient
            selectedBirthdate = getCurrentDate();
            patientBirthdate.setText(selectedBirthdate);
        }
    }

    private String getCurrentDate() {
        // Récupère la date actuelle au format YYYY-MM-DD
        Calendar cal = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private void setupGenderSpinner() {
        // Configure le Spinner pour le genre
        String[] genders = {"Male", "Female"}; // Options de genre
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, genders); // Adaptateur pour le Spinner
        patientGender.setAdapter(adapter); // Associe l'adaptateur

        if (patient != null && patient.getGender() != null) {
            // Sélectionne le genre du patient existant
            int position = patient.getGender().equalsIgnoreCase("Male") ? 0 : 1;
            patientGender.setSelection(position);
        }
    }

    private void setupBirthdatePicker() {
        // Configure l'écouteur pour ouvrir le DatePickerDialog
        patientBirthdate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        // Affiche un dialogue pour sélectionner la date de naissance
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (patient != null && patient.getBirthdate() != 0) {
            // Initialise avec la date de naissance du patient si existante
            int birthdate = patient.getBirthdate();
            year = birthdate / 10000;
            month = (birthdate % 10000) / 100 - 1;
            day = birthdate % 100;
        }

        new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Met à jour la date sélectionnée et l'affiche
                    selectedBirthdate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    patientBirthdate.setText(selectedBirthdate);
                }, year, month, day).show(); // Affiche le dialogue
    }

    private void setupSaveButton() {
        // Configure l'écouteur pour le bouton de sauvegarde
        findViewById(R.id.editable_patient_profile_activity_save_button)
                .setOnClickListener(v -> savePatientData());
    }

    private void savePatientData() {
        // Sauvegarde les données du patient
        if (!isFormValid()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show(); // Message d'erreur
            return;
        }

        try {
            if (patient != null) {
                updateExistingPatient(); // Met à jour un patient existant
            } else {
                createNewPatient(); // Crée un nouveau patient
            }
            finish(); // Ferme l'activité
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Affiche l'erreur
        }
    }

    private boolean isFormValid() {
        // Vérifie que tous les champs sont remplis
        return !patientName.getText().toString().isEmpty() &&
                !patientEmail.getText().toString().isEmpty() &&
                !patientPhone.getText().toString().isEmpty() &&
                !patientHeight.getText().toString().isEmpty() &&
                !patientWeight.getText().toString().isEmpty();
    }

    private void updateExistingPatient() {
        // Met à jour les données d'un patient existant
        patient.setName(patientName.getText().toString());
        patient.setEmail(patientEmail.getText().toString());
        patient.setPhone(patientPhone.getText().toString());
        patient.setHeight(Double.parseDouble(patientHeight.getText().toString()));
        patient.setWeight(Double.parseDouble(patientWeight.getText().toString()));
        patient.setBirthdate(Integer.parseInt(selectedBirthdate.replace("-", ""))); // Convertit en YYYYMMDD
        patient.setGender(patientGender.getSelectedItem().toString());

        Database.getInstance().updatePatient(patient); // Sauvegarde dans la base
        Toast.makeText(this, "Patient mis à jour", Toast.LENGTH_SHORT).show(); // Confirmation
    }

    private void createNewPatient() {
        // Crée un nouveau patient
        Patient newPatient = new Patient(
                0, // ID temporaire (généré par la base)
                patientName.getText().toString(),
                Integer.parseInt(selectedBirthdate.replace("-", "")), // Date en YYYYMMDD
                patientEmail.getText().toString(),
                patientPhone.getText().toString(),
                null, // Champ notes ou autre non utilisé ici
                Double.parseDouble(patientHeight.getText().toString()),
                Double.parseDouble(patientWeight.getText().toString()),
                patientGender.getSelectedItem().toString()
        );

        Database.getInstance().addPatient(newPatient); // Ajoute à la base
        Toast.makeText(this, "Nouveau patient créé", Toast.LENGTH_SHORT).show(); // Confirmation
    }
}