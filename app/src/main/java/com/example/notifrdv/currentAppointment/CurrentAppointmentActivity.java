package com.example.notifrdv.currentAppointment;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.R;
import com.example.notifrdv.patientProfile.PatientProfileActivity;
import com.example.notifrdv.utils.database.Appointment;
import com.example.notifrdv.utils.database.Database;

/**
 * Activité pour gérer une consultation médicale en cours
 * Permet au médecin de :
 * - Visualiser les informations du patient et du rendez-vous
 * - Saisir les notes de consultation, médicaments et examens
 * - Consulter le précédent rendez-vous du patient
 * - Marquer la consultation comme terminée
 */
public class CurrentAppointmentActivity extends AppCompatActivity {

    @SuppressLint({"SourceLockedOrientationActivity", "SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge
        setContentView(R.layout.activity_current_appointment);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation portrait

        // Gestion des marges système (barre de statut/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des composants UI
        // Barre d'état personnalisée
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon);
        TextView statusBarTitle = findViewById(R.id.status_bar_title);

        // Infos patient
        ConstraintLayout patientInfo = findViewById(R.id.current_appointment_activity_patient_info_layout);
        ImageView patientPicture = findViewById(R.id.current_appointment_activity_appointment_item_image);
        TextView patientName = findViewById(R.id.current_appointment_activity_appointment_item_patient_name);
        TextView patientAge = findViewById(R.id.current_appointment_activity_appointment_item_patient_age);
        TextView appointmentType = findViewById(R.id.current_appointment_activity_appointment_item_appointment_type);
        TextView appointmentTime = findViewById(R.id.current_appointment_activity_appointment_item_appointment_time);

        // Champs de saisie
        EditText notes = findViewById(R.id.current_appointment_activity_notes);
        EditText medicines = findViewById(R.id.current_appointment_activity_medicines);
        EditText exams = findViewById(R.id.current_appointment_activity_exams);
        AppCompatButton save = findViewById(R.id.current_appointment_activity_save_button);

        // Section rendez-vous précédent
        TextView previousAppointmentTitle = findViewById(R.id.current_appointment_activity_previous_appointment_title);
        ConstraintLayout previousAppointmentLayout = findViewById(R.id.current_appointment_activity_previous_appointment_info_layout);
        TextView previousAppointmentDateTime = findViewById(R.id.current_appointment_activity_previous_appointment_date_time);
        TextView previousAppointmentType = findViewById(R.id.current_appointment_activity_previous_appointment_type);

        // Configuration de la barre d'état
        statusBackIcon.setOnClickListener(v -> finish()); // Bouton retour
        statusBarIcon.setImageResource(R.drawable.stethoscope_white); // Icône stéthoscope
        statusBarTitle.setText(getString(R.string.current_appointment)); // Titre "Consultation en cours"

        // Récupération du rendez-vous depuis l'intent
        Appointment appointment = getIntent().getParcelableExtra("appointment");
        if (appointment == null) {
            finish(); // Si aucun rendez-vous, ferme l'activité
            return;
        }

        // Affichage des informations du patient
        patientPicture.setImageResource(R.drawable.default_profile_picture); // Photo par défaut
        patientName.setText(appointment.getPatient().getName()); // Nom du patient
        patientAge.setText(appointment.getPatient().getAge() + " ans"); // Âge
        appointmentType.setText("Consultation régulière"); // Type de consultation
        // Formatage de l'heure (HH:MM)
        appointmentTime.setText(String.format("%02d:%02d",
                appointment.getAppointmentTime() / 100,
                appointment.getAppointmentTime() % 100));

        // Remplissage des champs de saisie (s'ils contiennent déjà des données)
        notes.setText(appointment.getNotes() != null ? appointment.getNotes() : "");
        medicines.setText(appointment.getMedicines() != null ? appointment.getMedicines() : "");
        exams.setText(appointment.getExams() != null ? appointment.getExams() : "");

        // Récupération du précédent rendez-vous du patient
        Appointment previousAppointment = Database.getInstance()
                .getPreviousDoneAppointmentByPatientId(appointment.getPatient().getId());

        if (previousAppointment != null) {
            // Affichage des infos du précédent rendez-vous si existant
            previousAppointmentTitle.setVisibility(VISIBLE);
            previousAppointmentLayout.setVisibility(VISIBLE);
            // Formatage date/heure (YYYY-MM-DD HH:MM)
            previousAppointmentDateTime.setText(String.format("%04d-%02d-%02d %02d:%02d",
                    previousAppointment.getAppointmentDate() / 10000,
                    (previousAppointment.getAppointmentDate() % 10000) / 100,
                    previousAppointment.getAppointmentDate() % 100,
                    previousAppointment.getAppointmentTime() / 100,
                    previousAppointment.getAppointmentTime() % 100));
            previousAppointmentType.setText(
                    previousAppointment.getNotes() != null ?
                            previousAppointment.getNotes() : "Non spécifié");
        } else {
            // Masquage de la section si aucun précédent rendez-vous
            previousAppointmentTitle.setVisibility(GONE);
            previousAppointmentLayout.setVisibility(GONE);
        }

        // Clic sur la fiche patient -> redirection vers le profil complet
        patientInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientProfileActivity.class);
            intent.putExtra("patient", appointment.getPatient());
            startActivity(intent);
        });

        // Sauvegarde des modifications
        save.setOnClickListener(v -> {
            // Mise à jour des données du rendez-vous
            appointment.setNotes(notes.getText().toString());
            appointment.setMedicines(medicines.getText().toString());
            appointment.setExams(exams.getText().toString());
            appointment.setDone(true); // Marque comme terminé

            // Enregistrement en base de données
            Database.getInstance().updateAppointment(appointment, appointment.getDoctorId());
            finish(); // Retour à l'écran précédent
        });
    }
}