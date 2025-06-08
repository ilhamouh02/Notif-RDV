package com.example.notifrdv.appointment;

// Importations statiques pour des constantes et méthodes utilitaires
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; // Orientation portrait
import static android.view.View.GONE; // Visibilité invisible
import static android.view.View.VISIBLE; // Visibilité visible
import static com.example.notifrdv.utils.ConfirmationDialog.showConfirmationDialog; // Boîte de dialogue de confirmation

// Importations des classes Android pour l'UI et la gestion des intents
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

// Importations pour les composants de l'UI et la gestion des marges
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.currentAppointment.CurrentAppointmentActivity;
import com.example.notifrdv.utils.database.Appointment;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;
import com.example.notifrdv.utils.database.Patient;

// Importations pour les collections et la gestion du temps
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
    // Déclaration des composants UI
    private AutoCompleteTextView patientNameInput; // Champ de saisie auto-complétant pour le nom du patient
    private AppCompatButton dateButton; // Bouton pour sélectionner la date du rendez-vous
    private AppCompatButton timeButton; // Bouton pour sélectionner l'heure du rendez-vous

    // Déclaration des données du rendez-vous
    private Appointment appointment; // Objet rendez-vous (null si création d'un nouveau rendez-vous)
    private String appointmentDate; // Date formatée sous forme de chaîne (YYYY-MM-DD)
    private String appointmentTime; // Heure formatée sous forme de chaîne (HH:MM)
    private long patientId; // ID du patient sélectionné
    private long doctorId; // ID du médecin (récupéré depuis la base de données)
    private List<String> patientNames = new ArrayList<>(); // Liste des noms des patients pour l'auto-complétion

    @SuppressLint({"SourceLockedOrientationActivity", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_appointment); // Associe l'interface XML (activity_appointment.xml)
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation en mode portrait

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews(); // Appelle la méthode pour initialiser les composants UI
        setupStatusBar(); // Appelle la méthode pour configurer la barre d'état personnalisée
        getInitialData(); // Appelle la méthode pour charger les données initiales
        setupListeners(); // Appelle la méthode pour configurer les écouteurs d'événements
    }

    /**
     * Initialise les vues en les liant aux éléments XML définis dans activity_appointment.xml
     */
    private void initViews() {
        patientNameInput = findViewById(R.id.appointment_activity_patient_name); // Associe le champ auto-complétant
        dateButton = findViewById(R.id.appointment_activity_appointment_date); // Associe le bouton de sélection de date
        timeButton = findViewById(R.id.appointment_activity_appointment_time); // Associe le bouton de sélection d'heure
    }

    /**
     * Configure la barre d'état personnalisée
     * - Bouton retour
     * - Icône
     * - Titre
     */
    private void setupStatusBar() {
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon); // Bouton retour
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon); // Icône de la barre d'état
        TextView statusBarTitle = findViewById(R.id.status_bar_title); // Titre de la barre d'état

        statusBackIcon.setOnClickListener(v -> finish()); // Ferme l'activité et revient à l'écran précédent
        statusBarIcon.setImageResource(R.drawable.scheduling_white); // Définit une icône de calendrier (ressource drawable)
        statusBarTitle.setText(getString(R.string.appointment)); // Définit le titre "Rendez-vous" (défini dans strings.xml)
    }

    /**
     * Charge les données initiales :
     * - Liste des patients pour l'auto-complétion
     * - Rendez-vous existant (si modification)
     * - Valeurs par défaut (date/heure actuelle)
     */
    private void getInitialData() {
        // 1. Récupération de tous les patients depuis la base de données
        List<Patient> patients = Database.getInstance().getAllPatients();
        for (Patient patient : patients) {
            patientNames.add(patient.getName()); // Ajoute chaque nom de patient à la liste pour l'auto-complétion
        }

        // 2. Configuration de l'auto-complétion pour le champ patientNameInput
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, // Layout par défaut pour les suggestions
                patientNames // Liste des noms des patients
        );
        patientNameInput.setAdapter(adapter); // Associe l'adaptateur au champ auto-complétant

        // 3. Récupération du rendez-vous existant (si passé via l'intent pour modification)
        appointment = getIntent().getParcelableExtra("appointment");

        // 4. Initialisation des valeurs par défaut (date et heure actuelles)
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR); // Année actuelle
        int month = calendar.get(Calendar.MONTH); // Mois actuel (0-11)
        int day = calendar.get(Calendar.DAY_OF_MONTH); // Jour actuel
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Heure actuelle (format 24h)
        int minute = calendar.get(Calendar.MINUTE); // Minute actuelle

        // 5. Remplissage des champs selon le contexte (création ou modification)
        if (appointment != null) {
            // Mode modification : pré-remplissage des champs avec les données du rendez-vous existant
            patientNameInput.setText(appointment.getPatient().getName()); // Affiche le nom du patient
            patientId = appointment.getPatient().getId(); // Récupère l'ID du patient
            doctorId = appointment.getDoctorId(); // Récupère l'ID du médecin

            // Conversion de la date (format numérique YYYYMMDD) en chaîne affichable (YYYY-MM-DD)
            appointmentDate = String.format("%04d-%02d-%02d",
                    appointment.getAppointmentDate() / 10000, // Extrait l'année
                    (appointment.getAppointmentDate() % 10000) / 100, // Extrait le mois
                    appointment.getAppointmentDate() % 100); // Extrait le jour

            // Conversion de l'heure (format numérique HHMM) en chaîne affichable (HH:MM)
            appointmentTime = String.format("%02d:%02d",
                    appointment.getAppointmentTime() / 100, // Extrait l'heure
                    appointment.getAppointmentTime() % 100); // Extrait les minutes
        } else {
            // Mode création : initialise avec les valeurs actuelles
            appointmentDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Date actuelle (YYYY-MM-DD)
            appointmentTime = String.format("%02d:%02d", hour, minute); // Heure actuelle (HH:MM)

            // Sélection automatique du premier médecin disponible dans la base
            List<Doctor> doctors = Database.getInstance().getAllDoctors();
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId(); // Prend l'ID du premier médecin
            }
        }

        // Mise à jour des boutons avec les valeurs initiales de date et heure
        dateButton.setText(appointmentDate);
        timeButton.setText(appointmentTime);

        // Gestion de l'état des champs et boutons selon le mode (création ou modification)
        boolean isNewAppointment = getIntent().getBooleanExtra("isNewAppointment", false);
        ImageView cancelAppointment = findViewById(R.id.appointment_activity_cancel_appointment); // Bouton "Supprimer"
        AppCompatButton cancelButton = findViewById(R.id.appointment_activity_cancel_button); // Bouton "Annuler"

        if (isNewAppointment) {
            // Mode création : le champ patient est modifiable, bouton "Annuler" visible
            patientNameInput.setEnabled(true);
            cancelAppointment.setVisibility(GONE); // Cache le bouton "Supprimer"
            cancelButton.setVisibility(VISIBLE); // Affiche le bouton "Annuler"
        } else {
            // Mode modification : le champ patient est verrouillé, bouton "Supprimer" visible
            patientNameInput.setEnabled(false);
            cancelAppointment.setVisibility(VISIBLE); // Affiche le bouton "Supprimer"
            cancelButton.setVisibility(GONE); // Cache le bouton "Annuler"
        }
    }

    /**
     * Configure les écouteurs d'événements pour :
     * - Sélection du patient
     * - Sélection de la date/heure
     * - Boutons d'actions (sauvegarde, annulation, etc.)
     */
    private void setupListeners() {
        // 1. Écouteur pour la sélection d'un patient dans l'auto-complétion
        patientNameInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position); // Récupère le nom sélectionné
            Patient patient = Database.getInstance().getPatientByName(selectedName); // Récupère l'objet patient
            if (patient != null) {
                patientId = patient.getId(); // Stocke l'ID du patient pour la sauvegarde
                Log.d("Appointment", "Patient sélectionné: " + selectedName + " (ID: " + patientId + ")"); // Log pour débogage
            } else {
                Toast.makeText(this, "Patient non trouvé", Toast.LENGTH_SHORT).show(); // Message d'erreur si patient non trouvé
            }
        });

        // 2. Écouteur pour le bouton de sélection de la date
        dateButton.setOnClickListener(v -> showDatePicker()); // Ouvre un dialogue pour choisir la date

        // 3. Écouteur pour le bouton de sélection de l'heure
        timeButton.setOnClickListener(v -> showTimePicker()); // Ouvre un dialogue pour choisir l'heure

        // 4. Configuration des écouteurs pour les boutons d'actions
        ImageView startAppointment = findViewById(R.id.appointment_activity_start_appointment); // Bouton "Démarrer consultation"
        AppCompatButton saveButton = findViewById(R.id.appointment_activity_save_button); // Bouton "Sauvegarder"
        AppCompatButton cancelButton = findViewById(R.id.appointment_activity_cancel_button); // Bouton "Annuler"
        ImageView cancelAppointment = findViewById(R.id.appointment_activity_cancel_appointment); // Bouton "Supprimer"

        startAppointment.setOnClickListener(v -> startAppointment()); // Action pour démarrer une consultation
        saveButton.setOnClickListener(v -> saveAppointment()); // Action pour sauvegarder le rendez-vous
        cancelButton.setOnClickListener(v -> showCancelConfirmation()); // Action pour annuler (mode création)
        cancelAppointment.setOnClickListener(v -> showDeleteConfirmation()); // Action pour supprimer (mode modification)
    }

    /**
     * Affiche un DatePickerDialog pour sélectionner la date du rendez-vous
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // Récupère la date actuelle
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, yearSelected, monthSelected, dayOfMonth) -> {
                    // Formatage de la date sélectionnée en YYYY-MM-DD
                    appointmentDate = String.format("%04d-%02d-%02d", yearSelected, monthSelected + 1, dayOfMonth);
                    dateButton.setText(appointmentDate); // Met à jour le texte du bouton avec la date choisie
                },
                calendar.get(Calendar.YEAR), // Année initiale
                calendar.get(Calendar.MONTH), // Mois initial
                calendar.get(Calendar.DAY_OF_MONTH) // Jour initial
        );
        datePicker.show(); // Affiche le dialogue de sélection de date
    }

    /**
     * Affiche un TimePickerDialog pour sélectionner l'heure du rendez-vous
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance(); // Récupère l'heure actuelle
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfHour) -> {
                    // Formatage de l'heure sélectionnée en HH:MM
                    appointmentTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    timeButton.setText(appointmentTime); // Met à jour le texte du bouton avec l'heure choisie
                },
                calendar.get(Calendar.HOUR_OF_DAY), // Heure initiale
                calendar.get(Calendar.MINUTE), // Minute initiale
                true // Format 24h
        );
        timePicker.show(); // Affiche le dialogue de sélection d'heure
    }

    /**
     * Valide les entrées avant sauvegarde
     * @return true si toutes les entrées sont valides, false sinon
     */
    private boolean validateInputs() {
        // Vérifie si un patient a été sélectionné
        if (patientId == 0) {
            Toast.makeText(this, "Veuillez sélectionner un patient valide", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Vérifie si une date a été sélectionnée
        if (appointmentDate == null || appointmentDate.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Vérifie si une heure a été sélectionnée
        if (appointmentTime == null || appointmentTime.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une heure", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Toutes les validations sont passées
    }

    /**
     * Sauvegarde le rendez-vous en base (création ou mise à jour)
     */
    private void saveAppointment() {
        if (!validateInputs()) return; // Arrête si les entrées ne sont pas valides

        try {
            // Conversion des formats de date et heure en entiers pour le stockage
            int dateInt = Integer.parseInt(appointmentDate.replace("-", "")); // Convertit YYYY-MM-DD en YYYYMMDD
            int timeInt = Integer.parseInt(appointmentTime.replace(":", "")); // Convertit HH:MM en HHMM

            // Horodatage actuel pour les champs created_at/updated_at
            String currentDateTime = String.format("%04d%02d%02d%02d%02d",
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));

            // Récupération de l'objet patient complet à partir de l'ID
            Patient patient = Database.getInstance().getPatientById(patientId);
            if (patient == null) {
                Toast.makeText(this, "Patient non trouvé", Toast.LENGTH_SHORT).show();
                return;
            }

            if (appointment == null) {
                // Mode création : création d'un nouveau rendez-vous
                appointment = new Appointment(
                        0L, // ID temporaire (sera généré par la base)
                        patient, // Patient associé
                        doctorId, // ID du médecin
                        "", "", "", // Champs vides pour diagnostic, prescription, notes (non utilisés ici)
                        dateInt, // Date au format YYYYMMDD
                        timeInt, // Heure au format HHMM
                        "scheduled", // Statut initial du rendez-vous
                        currentDateTime, // Date/heure de création
                        currentDateTime, // Date/heure de mise à jour
                        false, // notified (pas de notification pour l'instant)
                        false  // completed (rendez-vous non terminé)
                );

                // Insertion du rendez-vous dans la base
                long result = Database.getInstance().addAppointment(appointment, doctorId);
                if (result != -1) {
                    appointment.setId(result); // Met à jour l'ID du rendez-vous avec celui généré par la base
                    Toast.makeText(this, "Rendez-vous créé avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // Ferme l'activité et retourne à l'écran précédent
                } else {
                    Toast.makeText(this, "Échec de la création du rendez-vous", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Mode modification : mise à jour du rendez-vous existant
                appointment.setPatient(patient); // Met à jour le patient
                appointment.setAppointmentDate(dateInt); // Met à jour la date
                appointment.setAppointmentTime(timeInt); // Met à jour l'heure
                appointment.setUpdatedAt(currentDateTime); // Met à jour la date de modification

                Database.getInstance().updateAppointment(appointment, doctorId); // Met à jour dans la base
                Toast.makeText(this, "Rendez-vous mis à jour avec succès", Toast.LENGTH_SHORT).show();
                finish(); // Ferme l'activité
            }
        } catch (Exception e) {
            Log.e("Appointment", "Erreur lors de la sauvegarde", e); // Log de l'erreur pour débogage
            Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show(); // Message d'erreur à l'utilisateur
        }
    }

    /**
     * Lance l'activité de consultation en cours
     */
    private void startAppointment() {
        if (!validateInputs()) return; // Arrête si les entrées ne sont pas valides

        saveAppointment(); // Sauvegarde d'abord les modifications ou crée le rendez-vous

        if (appointment != null && appointment.getId() != 0) {
            // Si le rendez-vous existe et a un ID valide, lance l'activité de consultation
            Intent intent = new Intent(this, CurrentAppointmentActivity.class);
            intent.putExtra("appointment", appointment); // Passe le rendez-vous à l'activité suivante
            startActivity(intent); // Lance l'activité
            finish(); // Ferme cette activité
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
                this::finish, // Si l'utilisateur confirme, ferme l'activité
                () -> {}      // Si l'utilisateur annule, ne fait rien
        );
    }

    /**
     * Affiche une boîte de dialogue de confirmation pour la suppression
     */
    private void showDeleteConfirmation() {
        if (appointment == null) return; // Arrête si aucun rendez-vous n'est chargé

        showConfirmationDialog(
                this,
                "Supprimer le rendez-vous",
                "Voulez-vous vraiment supprimer ce rendez-vous?",
                "Oui",
                "Non",
                () -> {
                    // Si l'utilisateur confirme, supprime le rendez-vous de la base
                    Database.getInstance().deleteAppointment(appointment.getId());
                    finish(); // Ferme l'activité et retourne à l'écran précédent
                },
                () -> {} // Si l'utilisateur annule, ne fait rien
        );
    }
}