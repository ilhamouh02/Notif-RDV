package com.example.notifrdv.currentAppointment;

// Importations statiques pour des constantes et méthodes utilitaires
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; // Orientation portrait
import static android.view.View.GONE; // Visibilité invisible
import static android.view.View.VISIBLE; // Visibilité visible

// Importations des classes Android pour l'UI et la gestion des intents
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour les composants de l'UI et la gestion des marges
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importations des ressources et classes internes de l'application
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
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_current_appointment); // Associe l'interface XML (activity_current_appointment.xml)
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation en mode portrait

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des composants UI
        // Barre d'état personnalisée
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon); // Bouton retour
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon); // Icône de la barre d'état
        TextView statusBarTitle = findViewById(R.id.status_bar_title); // Titre de la barre d'état

        // Informations du patient
        ConstraintLayout patientInfo = findViewById(R.id.current_appointment_activity_patient_info_layout); // Layout des infos patient
        ImageView patientPicture = findViewById(R.id.current_appointment_activity_appointment_item_image); // Photo du patient
        TextView patientName = findViewById(R.id.current_appointment_activity_appointment_item_patient_name); // Nom du patient
        TextView patientAge = findViewById(R.id.current_appointment_activity_appointment_item_patient_age); // Âge du patient
        TextView appointmentType = findViewById(R.id.current_appointment_activity_appointment_item_appointment_type); // Type de rendez-vous
        TextView appointmentTime = findViewById(R.id.current_appointment_activity_appointment_item_appointment_time); // Heure du rendez-vous

        // Champs de saisie pour la consultation
        EditText notes = findViewById(R.id.current_appointment_activity_notes); // Notes de consultation
        EditText medicines = findViewById(R.id.current_appointment_activity_medicines); // Médicaments prescrits
        EditText exams = findViewById(R.id.current_appointment_activity_exams); // Examens à réaliser
        AppCompatButton save = findViewById(R.id.current_appointment_activity_save_button); // Bouton de sauvegarde

        // Section du précédent rendez-vous
        TextView previousAppointmentTitle = findViewById(R.id.current_appointment_activity_previous_appointment_title); // Titre de la section
        ConstraintLayout previousAppointmentLayout = findViewById(R.id.current_appointment_activity_previous_appointment_info_layout); // Layout des infos
        TextView previousAppointmentDateTime = findViewById(R.id.current_appointment_activity_previous_appointment_date_time); // Date/heure
        TextView previousAppointmentType = findViewById(R.id.current_appointment_activity_previous_appointment_type); // Type ou notes

        // Configuration de la barre d'état
        statusBackIcon.setOnClickListener(v -> finish()); // Ferme l'activité et revient à l'écran précédent
        statusBarIcon.setImageResource(R.drawable.stethoscope_white); // Définit une icône de stéthoscope (ressource drawable)
        statusBarTitle.setText(getString(R.string.current_appointment)); // Définit le titre "Consultation en cours" (défini dans strings.xml)

        // Récupération du rendez-vous passé via l'intent
        Appointment appointment = getIntent().getParcelableExtra("appointment");
        if (appointment == null) {
            finish(); // Ferme l'activité si aucun rendez-vous n'est fourni
            return;
        }

        // Affichage des informations du patient
        patientPicture.setImageResource(R.drawable.default_profile_picture); // Définit une image par défaut pour le patient
        patientName.setText(appointment.getPatient().getName()); // Affiche le nom du patient
        patientAge.setText(appointment.getPatient().getAge() + " ans"); // Affiche l'âge avec l'unité
        appointmentType.setText("Consultation régulière"); // Définit un type par défaut
        // Formatage de l'heure (convertit HHMM en HH:MM)
        appointmentTime.setText(String.format("%02d:%02d",
                appointment.getAppointmentTime() / 100, // Extrait l'heure
                appointment.getAppointmentTime() % 100)); // Extrait les minutes

        // Remplissage des champs de saisie avec les données existantes (si présentes)
        notes.setText(appointment.getNotes() != null ? appointment.getNotes() : ""); // Notes ou vide
        medicines.setText(appointment.getMedicines() != null ? appointment.getMedicines() : ""); // Médicaments ou vide
        exams.setText(appointment.getExams() != null ? appointment.getExams() : ""); // Examens ou vide

        // Récupération du précédent rendez-vous terminé du patient
        Appointment previousAppointment = Database.getInstance()
                .getPreviousDoneAppointmentByPatientId(appointment.getPatient().getId());

        if (previousAppointment != null) {
            // Affiche les informations du précédent rendez-vous si existant
            previousAppointmentTitle.setVisibility(VISIBLE); // Rend le titre visible
            previousAppointmentLayout.setVisibility(VISIBLE); // Rend le layout visible
            // Formatage de la date/heure (YYYYMMDD HHMM en YYYY-MM-DD HH:MM)
            previousAppointmentDateTime.setText(String.format("%04d-%02d-%02d %02d:%02d",
                    previousAppointment.getAppointmentDate() / 10000, // Année
                    (previousAppointment.getAppointmentDate() % 10000) / 100, // Mois
                    previousAppointment.getAppointmentDate() % 100, // Jour
                    previousAppointment.getAppointmentTime() / 100, // Heure
                    previousAppointment.getAppointmentTime() % 100)); // Minutes
            previousAppointmentType.setText(
                    previousAppointment.getNotes() != null ?
                            previousAppointment.getNotes() : "Non spécifié"); // Notes ou texte par défaut
        } else {
            // Masque la section si aucun précédent rendez-vous
            previousAppointmentTitle.setVisibility(GONE); // Cache le titre
            previousAppointmentLayout.setVisibility(GONE); // Cache le layout
        }

        // Clic sur les infos patient -> redirection vers le profil complet
        patientInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientProfileActivity.class); // Crée un intent pour PatientProfileActivity
            intent.putExtra("patient", appointment.getPatient()); // Passe l'objet patient
            startActivity(intent); // Lance l'activité
        });

        // Sauvegarde des modifications lors du clic sur le bouton
        save.setOnClickListener(v -> {
            // Met à jour les champs du rendez-vous avec les saisies
            appointment.setNotes(notes.getText().toString()); // Enregistre les notes
            appointment.setMedicines(medicines.getText().toString()); // Enregistre les médicaments
            appointment.setExams(exams.getText().toString()); // Enregistre les examens
            appointment.setDone(true); // Marque la consultation comme terminée

            // Enregistre les modifications dans la base de données
            Database.getInstance().updateAppointment(appointment, appointment.getDoctorId());
            finish(); // Ferme l'activité et retourne à l'écran précédent
        });
    }
}