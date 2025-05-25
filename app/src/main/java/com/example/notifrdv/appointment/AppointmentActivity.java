package com.example.notifrdv.appointment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.notifrdv.utils.ConfirmationDialog.showConfirmationDialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.R;
import com.example.notifrdv.currentAppointment.CurrentAppointmentActivity;
import com.example.notifrdv.utils.database.Appointment;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;
import com.example.notifrdv.utils.database.Patient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activity pour la gestion des rendez-vous (création/modification)
 * Permet au médecin de :
 * - Créer un nouveau rendez-vous
 * - Modifier un rendez-vous existant
 * - Démarrer une consultation
 * - Annuler/supprimer un rendez-vous
 */
public class AppointmentActivity extends AppCompatActivity {
    // Composants UI
    private AutoCompleteTextView patientNameInput; // Champ de saisie auto-complétant pour le nom du patient
    private AppCompatButton dateButton; // Bouton pour sélectionner la date
    private AppCompatButton timeButton; // Bouton pour sélectionner l'heure

    // Données du rendez-vous
    private Appointment appointment; // Objet rendez-vous (null si création)
    private String appointmentDate; // Date formatée (YYYY-MM-DD)
    private String appointmentTime; // Heure formatée (HH:MM)
    private long patientId; // ID du patient sélectionné
    private long doctorId; // ID du médecin (récupéré depuis la base)
    private List<String> patientNames = new ArrayList<>(); // Liste des noms patients pour l'auto-complétion

    @SuppressLint({"SourceLockedOrientationActivity", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge (plein écran)
        setContentView(R.layout.activity_appointment);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation portrait

        // Gestion des marges système (barre de statut/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews(); // Initialise les composants UI
        setupStatusBar(); // Configure la barre d'état personnalisée
        getInitialData(); // Charge les données initiales
        setupListeners(); // Configure les écouteurs d'événements
    }

    /**
     * Initialise les vues en les liant aux éléments XML
     */
    private void initViews() {
        patientNameInput = findViewById(R.id.appointment_activity_patient_name);
        dateButton = findViewById(R.id.appointment_activity_appointment_date);
        timeButton = findViewById(R.id.appointment_activity_appointment_time);
    }

    /**
     * Configure la barre d'état personnalisée
     * - Bouton retour
     * - Icône
     * - Titre
     */
    private void setupStatusBar() {
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon);
        TextView statusBarTitle = findViewById(R.id.status_bar_title);

        statusBackIcon.setOnClickListener(v -> finish()); // Retour à l'écran précédent
        statusBarIcon.setImageResource(R.drawable.scheduling_white); // Icône de calendrier
        statusBarTitle.setText(getString(R.string.appointment)); // Titre "Rendez-vous"
    }

    /**
     * Charge les données initiales :
     * - Liste des patients pour l'auto-complétion
     * - Rendez-vous existant (si modification)
     * - Valeurs par défaut (date/heure actuelle)
     */
    private void getInitialData() {
        // 1. Récupération de tous les patients depuis la base
        List<Patient> patients = Database.getInstance().getAllPatients();
        for (Patient patient : patients) {
            patientNames.add(patient.getName()); // Remplit la liste des noms
        }

        // 2. Configuration de l'auto-complétion
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                patientNames
        );
        patientNameInput.setAdapter(adapter);

        // 3. Récupération du rendez-vous existant (passé en extra de l'intent)
        appointment = getIntent().getParcelableExtra("appointment");

        // 4. Initialisation des valeurs par défaut (date/heure actuelle)
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 5. Remplissage des champs selon le contexte (création/modification)
        if (appointment != null) {
            // Mode modification - pré-remplissage avec les données existantes
            patientNameInput.setText(appointment.getPatient().getName());
            patientId = appointment.getPatient().getId();
            doctorId = appointment.getDoctorId();

            // Conversion des formats numériques en strings affichables
            appointmentDate = String.format("%04d-%02d-%02d",
                    appointment.getAppointmentDate() / 10000,
                    (appointment.getAppointmentDate() % 10000) / 100,
                    appointment.getAppointmentDate() % 100);

            appointmentTime = String.format("%02d:%02d",
                    appointment.getAppointmentTime() / 100,
                    appointment.getAppointmentTime() % 100);
        } else {
            // Mode création - valeurs par défaut
            appointmentDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            appointmentTime = String.format("%02d:%02d", hour, minute);

            // Sélection automatique du premier médecin disponible
            List<Doctor> doctors = Database.getInstance().getAllDoctors();
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId();
            }
        }

        // Mise à jour des boutons avec les valeurs
        dateButton.setText(appointmentDate);
        timeButton.setText(appointmentTime);

        // Gestion de l'état des boutons selon le mode (création/modification)
        boolean isNewAppointment = getIntent().getBooleanExtra("isNewAppointment", false);
        ImageView cancelAppointment = findViewById(R.id.appointment_activity_cancel_appointment);
        AppCompatButton cancelButton = findViewById(R.id.appointment_activity_cancel_button);

        if (isNewAppointment) {
            // Mode création : champ patient modifiable, bouton "Annuler" visible
            patientNameInput.setEnabled(true);
            cancelAppointment.setVisibility(GONE);
            cancelButton.setVisibility(VISIBLE);
        } else {
            // Mode modification : champ patient verrouillé, bouton "Supprimer" visible
            patientNameInput.setEnabled(false);
            cancelAppointment.setVisibility(VISIBLE);
            cancelButton.setVisibility(GONE);
        }
    }

    /**
     * Configure les écouteurs d'événements pour :
     * - Sélection du patient
     * - Sélection de la date/heure
     * - Boutons d'actions (sauvegarde, annulation, etc.)
     */
    private void setupListeners() {
        // 1. Sélection d'un patient dans l'auto-complétion
        patientNameInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            Patient patient = Database.getInstance().getPatientByName(selectedName);
            if (patient != null) {
                patientId = patient.getId(); // Stocke l'ID pour la sauvegarde
                Log.d("Appointment", "Patient sélectionné: " + selectedName + " (ID: " + patientId + ")");
            } else {
                Toast.makeText(this, "Patient non trouvé", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Sélection de la date via un DatePickerDialog
        dateButton.setOnClickListener(v -> showDatePicker());

        // 3. Sélection de l'heure via un TimePickerDialog
        timeButton.setOnClickListener(v -> showTimePicker());

        // 4. Configuration des boutons d'actions
        ImageView startAppointment = findViewById(R.id.appointment_activity_start_appointment);
        AppCompatButton saveButton = findViewById(R.id.appointment_activity_save_button);
        AppCompatButton cancelButton = findViewById(R.id.appointment_activity_cancel_button);
        ImageView cancelAppointment = findViewById(R.id.appointment_activity_cancel_appointment);

        startAppointment.setOnClickListener(v -> startAppointment()); // Démarrer consultation
        saveButton.setOnClickListener(v -> saveAppointment()); // Sauvegarder
        cancelButton.setOnClickListener(v -> showCancelConfirmation()); // Annuler (mode création)
        cancelAppointment.setOnClickListener(v -> showDeleteConfirmation()); // Supprimer (mode modification)
    }

    /**
     * Affiche un DatePickerDialog pour sélectionner la date du rendez-vous
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, yearSelected, monthSelected, dayOfMonth) -> {
                    // Formatage de la date sélectionnée (YYYY-MM-DD)
                    appointmentDate = String.format("%04d-%02d-%02d", yearSelected, monthSelected + 1, dayOfMonth);
                    dateButton.setText(appointmentDate); // Mise à jour du bouton
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    /**
     * Affiche un TimePickerDialog pour sélectionner l'heure du rendez-vous
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfHour) -> {
                    // Formatage de l'heure sélectionnée (HH:MM)
                    appointmentTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    timeButton.setText(appointmentTime); // Mise à jour du bouton
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // Format 24h
        );
        timePicker.show();
    }

    /**
     * Valide les entrées avant sauvegarde
     * @return true si toutes les entrées sont valides
     */
    private boolean validateInputs() {
        if (patientId == 0) {
            Toast.makeText(this, "Veuillez sélectionner un patient valide", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (appointmentDate == null || appointmentDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (appointmentTime == null || appointmentTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une heure", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Sauvegarde le rendez-vous en base (création ou mise à jour)
     */
    private void saveAppointment() {
        if (!validateInputs()) return; // Validation préalable

        try {
            // Conversion des formats de date/heure en entiers pour la base
            int dateInt = Integer.parseInt(appointmentDate.replace("-", ""));
            int timeInt = Integer.parseInt(appointmentTime.replace(":", ""));

            // Horodatage actuel pour created_at/updated_at
            String currentDateTime = String.format("%04d%02d%02d%02d%02d",
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));

            // Récupération du patient complet
            Patient patient = Database.getInstance().getPatientById(patientId);
            if (patient == null) {
                Toast.makeText(this, "Patient non trouvé", Toast.LENGTH_SHORT).show();
                return;
            }

            if (appointment == null) {
                // Création d'un nouveau rendez-vous
                appointment = new Appointment(
                        0L, // ID temporaire
                        patient,
                        doctorId,
                        "", "", "", // Champs vides (diagnostic, prescription, notes)
                        dateInt,
                        timeInt,
                        "scheduled", // Statut initial
                        currentDateTime, // created_at
                        currentDateTime, // updated_at
                        false, // notified
                        false  // completed
                );

                // Insertion en base
                long result = Database.getInstance().addAppointment(appointment, doctorId);
                if (result != -1) {
                    appointment.setId(result); // Met à jour avec l'ID généré
                    Toast.makeText(this, "Rendez-vous créé avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // Retour à l'écran précédent
                } else {
                    Toast.makeText(this, "Échec de la création du rendez-vous", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Mise à jour d'un rendez-vous existant
                appointment.setPatient(patient);
                appointment.setAppointmentDate(dateInt);
                appointment.setAppointmentTime(timeInt);
                appointment.setUpdatedAt(currentDateTime);

                Database.getInstance().updateAppointment(appointment, doctorId);
                Toast.makeText(this, "Rendez-vous mis à jour avec succès", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("Appointment", "Erreur lors de la sauvegarde", e);
            Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lance l'activité de consultation en cours
     */
    private void startAppointment() {
        if (!validateInputs()) return;

        saveAppointment(); // Sauvegarde d'abord les modifications

        if (appointment != null && appointment.getId() != 0) {
            Intent intent = new Intent(this, CurrentAppointmentActivity.class);
            intent.putExtra("appointment", appointment);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Impossible de démarrer le rendez-vous", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Affiche une boîte de dialogue de confirmation pour l'annulation
     */
    private void showCancelConfirmation() {
        showConfirmationDialog(
                this,
                "Annuler",
                "Voulez-vous vraiment annuler? Les modifications ne seront pas sauvegardées.",
                "Oui",
                "Non",
                this::finish, // Action si confirmation
                () -> {}      // Action si annulation
        );
    }

    /**
     * Affiche une boîte de dialogue de confirmation pour la suppression
     */
    private void showDeleteConfirmation() {
        if (appointment == null) return;

        showConfirmationDialog(
                this,
                "Supprimer le rendez-vous",
                "Voulez-vous vraiment supprimer ce rendez-vous?",
                "Oui",
                "Non",
                () -> {
                    // Action si confirmation : suppression en base
                    Database.getInstance().deleteAppointment(appointment.getId());
                    finish(); // Retour à l'écran précédent
                },
                () -> {} // Action si annulation
        );
    }
}