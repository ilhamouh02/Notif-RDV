package com.example.notifrdv.patientProfile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notifrdv.R;
import com.example.notifrdv.utils.ConfirmationDialog;
import com.example.notifrdv.utils.FastToast;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

public class PatientProfileActivity extends AppCompatActivity {
    private ImageView patientPicture;
    private TextView patientName, patientEmail, patientPhone, patientBirthdateAge;
    private TextView patientHeight, patientWeight, patientGender;
    private ImageView deleteButton; // Ajouté
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        initializeViews();
        setupStatusBar();
        loadPatientData();
        setupEditButton();
        setupDeleteButton(); // Ajouté
    }

    private void initializeViews() {
        patientPicture = findViewById(R.id.patient_profile_activity_patient_picture);
        patientName = findViewById(R.id.patient_profile_activity_patient_name);
        patientEmail = findViewById(R.id.patient_profile_activity_patient_email);
        patientPhone = findViewById(R.id.patient_profile_activity_patient_phone_number);
        patientBirthdateAge = findViewById(R.id.patient_profile_activity_patient_birth_date_age);
        patientHeight = findViewById(R.id.patient_profile_activity_patient_height);
        patientWeight = findViewById(R.id.patient_profile_activity_patient_weight);
        patientGender = findViewById(R.id.patient_profile_activity_patient_gender);
        deleteButton = findViewById(R.id.patient_profile_activity_delete_button); // Ajouté
    }

    private void setupStatusBar() {
        ImageView backIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusIcon = findViewById(R.id.status_bar_icon);
        TextView title = findViewById(R.id.status_bar_title);

        backIcon.setOnClickListener(v -> finish());
        statusIcon.setImageResource(R.drawable.patient_white);
        title.setText(getString(R.string.patient_profile));
    }

    private void loadPatientData() {
        patient = getIntent().getParcelableExtra("patient");
        if (patient == null) {
            showErrorAndFinish("Données du patient non trouvées");
            return;
        }
        updateUI();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        patientPicture.setImageResource(R.drawable.default_profile_picture);
        patientName.setText(patient.getName());
        patientEmail.setText(patient.getEmail());
        patientPhone.setText(patient.getPhone());
        patientHeight.setText("Taille\n" + formatMeasurement(patient.getHeight()) + " cm");
        patientWeight.setText("Poids\n" + formatMeasurement(patient.getWeight()) + " Kg");
        patientGender.setText("Genre\n" + patient.getGender());

        String birthdate = formatBirthdate(patient.getBirthdate());
        patientBirthdateAge.setText(birthdate + " (" + patient.getAge() + " ans)");
    }

    // Ajout de la configuration du bouton de suppression
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
        );
    }

    private void deletePatient() {
        if (patient != null) {
            boolean success = Database.getInstance().deletePatient(patient.getId());
            if (success) {
                FastToast.show(this, "Patient supprimé");
                finish();
            } else {
                FastToast.show(this, "Échec de la suppression");
            }
        }
    }

    private String formatBirthdate(int birthdate) {
        return String.format("%04d-%02d-%02d",
                birthdate / 10000,
                (birthdate % 10000) / 100,
                birthdate % 100);
    }

    private String formatMeasurement(double value) {
        return value == (int) value ?
                String.valueOf((int) value) :
                String.format("%.2f", value);
    }

    private void setupEditButton() {
        findViewById(R.id.patient_profile_activity_edit_patient_info)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditablePatientProfileActivity.class);
                    intent.putExtra("patient", patient);
                    startActivity(intent);
                });
    }

    private void showErrorAndFinish(String message) {
        Log.e("PatientProfile", message);
        FastToast.show(this, "Erreur: " + message);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patient != null) {
            patient = Database.getInstance().getPatientById(patient.getId());
            if (patient == null) {
                showErrorAndFinish("Patient non trouvé dans la base");
            } else {
                updateUI();
            }
        }
    }
}