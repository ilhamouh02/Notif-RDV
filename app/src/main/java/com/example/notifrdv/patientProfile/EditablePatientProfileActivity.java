package com.example.notifrdv.patientProfile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.ImageView; // Import ajouté
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.notifrdv.R;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

import java.util.Calendar;

public class EditablePatientProfileActivity extends AppCompatActivity {
    private EditText patientName, patientEmail, patientPhone, patientHeight, patientWeight;
    private AppCompatButton patientBirthdate;
    private Spinner patientGender;
    private String selectedBirthdate;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable_patient_profile);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initializeViews();
        setupStatusBar();
        loadPatientData();
        setupGenderSpinner();
        setupBirthdatePicker();
        setupSaveButton();
    }

    private void initializeViews() {
        patientName = findViewById(R.id.editable_patient_profile_activity_patient_name);
        patientEmail = findViewById(R.id.editable_patient_profile_activity_patient_email);
        patientPhone = findViewById(R.id.editable_patient_profile_activity_patient_phone_number);
        patientHeight = findViewById(R.id.editable_patient_profile_activity_patient_height);
        patientWeight = findViewById(R.id.editable_patient_profile_activity_patient_weight);
        patientBirthdate = findViewById(R.id.editable_patient_profile_activity_patient_date_of_birth);
        patientGender = findViewById(R.id.editable_patient_profile_activity_patient_gender);
    }

    private void setupStatusBar() {
        findViewById(R.id.status_bar_back_arrow_icon).setOnClickListener(v -> finish());
        ((ImageView)findViewById(R.id.status_bar_icon)).setImageResource(R.drawable.patient_white);
        ((TextView)findViewById(R.id.status_bar_title)).setText(getString(R.string.patient_profile));
    }

    private void loadPatientData() {
        patient = getIntent().getParcelableExtra("patient");

        if (patient != null) {
            patientName.setText(patient.getName());
            patientEmail.setText(patient.getEmail());
            patientPhone.setText(patient.getPhone());
            patientHeight.setText(String.valueOf(patient.getHeight()));
            patientWeight.setText(String.valueOf(patient.getWeight()));

            int birthdate = patient.getBirthdate();
            selectedBirthdate = birthdate != 0 ?
                    String.format("%04d-%02d-%02d", birthdate/10000, (birthdate%10000)/100, birthdate%100) :
                    getCurrentDate();
            patientBirthdate.setText(selectedBirthdate);
        } else {
            selectedBirthdate = getCurrentDate();
            patientBirthdate.setText(selectedBirthdate);
        }
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, genders);
        patientGender.setAdapter(adapter);

        if (patient != null && patient.getGender() != null) {
            int position = patient.getGender().equalsIgnoreCase("Male") ? 0 : 1;
            patientGender.setSelection(position);
        }
    }

    private void setupBirthdatePicker() {
        patientBirthdate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (patient != null && patient.getBirthdate() != 0) {
            int birthdate = patient.getBirthdate();
            year = birthdate / 10000;
            month = (birthdate % 10000) / 100 - 1;
            day = birthdate % 100;
        }

        new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedBirthdate = String.format("%04d-%02d-%02d",
                            selectedYear, selectedMonth+1, selectedDay);
                    patientBirthdate.setText(selectedBirthdate);
                }, year, month, day).show();
    }

    private void setupSaveButton() {
        findViewById(R.id.editable_patient_profile_activity_save_button)
                .setOnClickListener(v -> savePatientData());
    }

    private void savePatientData() {
        if (!isFormValid()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (patient != null) {
                updateExistingPatient();
            } else {
                createNewPatient();
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isFormValid() {
        return !patientName.getText().toString().isEmpty() &&
                !patientEmail.getText().toString().isEmpty() &&
                !patientPhone.getText().toString().isEmpty() &&
                !patientHeight.getText().toString().isEmpty() &&
                !patientWeight.getText().toString().isEmpty();
    }

    private void updateExistingPatient() {
        patient.setName(patientName.getText().toString());
        patient.setEmail(patientEmail.getText().toString());
        patient.setPhone(patientPhone.getText().toString());
        patient.setHeight(Double.parseDouble(patientHeight.getText().toString()));
        patient.setWeight(Double.parseDouble(patientWeight.getText().toString()));
        patient.setBirthdate(Integer.parseInt(selectedBirthdate.replace("-", "")));
        patient.setGender(patientGender.getSelectedItem().toString());

        Database.getInstance().updatePatient(patient);
        Toast.makeText(this, "Patient mis à jour", Toast.LENGTH_SHORT).show();
    }

    private void createNewPatient() {
        Patient newPatient = new Patient(
                0,
                patientName.getText().toString(),
                Integer.parseInt(selectedBirthdate.replace("-", "")),
                patientEmail.getText().toString(),
                patientPhone.getText().toString(),
                null,
                Double.parseDouble(patientHeight.getText().toString()),
                Double.parseDouble(patientWeight.getText().toString()),
                patientGender.getSelectedItem().toString()
        );

        Database.getInstance().addPatient(newPatient);
        Toast.makeText(this, "Nouveau patient créé", Toast.LENGTH_SHORT).show();
    }
}