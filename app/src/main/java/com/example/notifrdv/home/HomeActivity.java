package com.example.notifrdv.home;

// Importations des classes Android pour l'UI, la gestion des intents, et les fichiers
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour le RecyclerView et les activités
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.appointment.AppointmentActivity;
import com.example.notifrdv.doctorProfile.DoctorProfileActivity;
import com.example.notifrdv.patientRecords.PatientRecordsActivity;
import com.example.notifrdv.utils.database.Appointment;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;

// Importations pour les collections
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Activité principale (tableau de bord) de l'application
public class HomeActivity extends AppCompatActivity implements HomeActivityAppointmentsAdapter.OnItemClickListener {

    // Déclaration des composants UI
    private ImageView doctorImage; // ImageView pour la photo du médecin
    private TextView doctorName; // TextView pour le nom du médecin
    private TextView appointmentsCounter; // TextView pour le compteur de rendez-vous
    private TextView nextAppointment; // TextView pour le prochain rendez-vous
    private RecyclerView upcomingAppointmentsRecycler; // RecyclerView pour la liste des rendez-vous

    // Déclaration des données
    private List<Appointment> appointments = new ArrayList<>(); // Liste des rendez-vous à venir
    private HomeActivityAppointmentsAdapter adapter; // Adaptateur pour le RecyclerView




    private long currentDoctorId; // ID du médecin connecté

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Associe l'interface XML (activity_home.xml)

        initViews(); // Initialise les composants UI
        setupRecyclerView(); // Configure le RecyclerView
        loadDoctorInfo(); // Charge les informations du médecin
        setupClickListeners(); // Configure les écouteurs de clics
    }

    @Override
    protected void onResume() {
        super.onResume(); // Appelle la méthode parent pour le cycle de vie
        loadDoctorInfo(); // Recharge les informations du médecin
        loadUpcomingAppointments(); // Recharge les rendez-vous à venir
    }

    private void initViews() {
        // Associe les composants UI aux éléments XML
        doctorImage = findViewById(R.id.home_activity_doctor_picture); // Image du médecin
        doctorName = findViewById(R.id.home_activity_doctor_name); // Nom du médecin
        appointmentsCounter = findViewById(R.id.home_activity_doctor_appointments_counter); // Compteur de rendez-vous
        nextAppointment = findViewById(R.id.home_activity_doctor_next_appointment); // Prochain rendez-vous
        upcomingAppointmentsRecycler = findViewById(R.id.home_activity_upcoming_appointments_list); // Liste des rendez-vous
    }

    private void setupRecyclerView() {
        // Configure le RecyclerView pour afficher les rendez-vous
        adapter = new HomeActivityAppointmentsAdapter(appointments, this); // Crée l'adaptateur avec la liste et l'écouteur de clics
        upcomingAppointmentsRecycler.setLayoutManager(new LinearLayoutManager(this)); // Définit un layout linéaire
        upcomingAppointmentsRecycler.setAdapter(adapter); // Associe l'adaptateur au RecyclerView
    }

    @SuppressLint("StaticFieldLeak")
    private void loadDoctorInfo() {
        String email = getIntent().getStringExtra("email"); // Récupère l'email du médecin depuis l'intent
        if (email == null) return; // Arrête si l'email est null

        // Utilise AsyncTask pour charger les informations du médecin de manière asynchrone
        new AsyncTask<Void, Void, Doctor>() {
            @Override
            protected Doctor doInBackground(Void... voids) {
                return Database.getInstance().getDoctorByEmail(email); // Récupère le médecin par email
            }

            @Override
            protected void onPostExecute(Doctor doctor) {
                if (doctor != null) {
                    currentDoctorId = doctor.getId(); // Stocke l'ID du médecin
                    doctorName.setText("Dr. " + doctor.getName()); // Affiche le nom avec le préfixe "Dr."

                    try {
                        if (doctor.getPicturePath() != null) { // Vérifie si une photo existe
                            File imgFile = new File(doctor.getPicturePath()); // Crée un objet File avec le chemin
                            if (imgFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath()); // Charge l'image
                                doctorImage.setImageBitmap(bitmap); // Affiche l'image dans l'ImageView
                                return;
                            }
                        }
                        doctorImage.setImageResource(R.drawable.default_profile_picture); // Image par défaut si aucune photo
                    } catch (Exception e) {
                        Log.e("HomeActivity", "Error loading doctor image", e); // Log de l'erreur en cas d'échec
                    }
                }
            }
        }.execute(); // Lance la tâche asynchrone
    }

    @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
    private void loadUpcomingAppointments() {
        // Utilise AsyncTask pour charger les rendez-vous à venir de manière asynchrone
        new AsyncTask<Void, Void, List<Appointment>>() {
            @Override
            protected List<Appointment> doInBackground(Void... voids) {
                return Database.getInstance().getUpcomingAppointmentsForDoctor(currentDoctorId); // Récupère les rendez-vous
            }

            @Override
            protected void onPostExecute(List<Appointment> result) {
                appointments.clear(); // Vide la liste actuelle
                if (result != null) {
                    appointments.addAll(result); // Ajoute les nouveaux rendez-vous
                }
                adapter.notifyDataSetChanged(); // Met à jour l'affichage du RecyclerView

                appointmentsCounter.setText(appointments.size() + " RDV"); // Met à jour le compteur

                if (!appointments.isEmpty()) { // Si des rendez-vous existent
                    Appointment next = appointments.get(0); // Prend le premier rendez-vous (prochain)
                    String time = String.format("%02d:%02d",
                            next.getAppointmentTime() / 100, // Extrait l'heure
                            next.getAppointmentTime() % 100); // Extrait les minutes
                    nextAppointment.setText("Prochain: " + next.getPatient().getName() + " à " + time); // Affiche le prochain rendez-vous
                } else {
                    nextAppointment.setText("Aucun rendez-vous à venir"); // Message si aucun rendez-vous
                }
            }
        }.execute(); // Lance la tâche asynchrone
    }

    private void setupClickListeners() {
        // Bouton pour modifier le profil du médecin
        ImageButton editButton = findViewById(R.id.home_activity_edit_doctor_info);
        editButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorProfileActivity.class)); // Redirige vers DoctorProfileActivity
        });

        // Bouton pour planifier un nouveau rendez-vous
        findViewById(R.id.home_activity_appointments_scheduling_layout)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, AppointmentActivity.class);
                    intent.putExtra("isNewAppointment", true); // Indique qu'il s'agit d'un nouveau rendez-vous
                    startActivity(intent); // Redirige vers AppointmentActivity
                });

        // Bouton pour accéder à la liste des patients
        findViewById(R.id.home_activity_patients_records_layout)
                .setOnClickListener(v -> {
                    startActivity(new Intent(this, PatientRecordsActivity.class)); // Redirige vers PatientRecordsActivity
                });
    }

    @Override
    public void onItemClick(Appointment appointment) {
        // Gère le clic sur un rendez-vous dans la liste
        Intent intent = new Intent(this, AppointmentActivity.class);
        intent.putExtra("appointment", appointment); // Passe le rendez-vous sélectionné
        startActivity(intent); // Redirige vers AppointmentActivity pour modification
    }

    @Override
    public void onBackPressed() {
        // Pour désactiver le retour, ne pas appeler super
        // super.onBackPressed(); // Commenté pour désactiver le retour
    }
}