package com.example.notifrdv.patientRecords;

// Importations statiques pour des constantes
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

// Importations des classes Android pour l'UI et la gestion des intents
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// Importations pour les composants de l'UI et la gestion des marges
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.patientProfile.EditablePatientProfileActivity;
import com.example.notifrdv.patientProfile.PatientProfileActivity;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

// Importations pour les collections
import java.util.ArrayList;
import java.util.List;

// Activité pour afficher et gérer la liste des patients
public class PatientRecordsActivity extends AppCompatActivity implements PatientRecordsActivityPatientAdapter.OnItemClickListener {
    private RecyclerView patientList; // RecyclerView pour afficher la liste des patients
    private PatientRecordsActivityPatientAdapter adapter; // Adaptateur pour le RecyclerView
    private static final int REQUEST_CODE_ADD_PATIENT = 100; // Code de requête pour l'ajout d'un patient

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Active le mode edge-to-edge pour un affichage plein écran
        setContentView(R.layout.activity_patient_records); // Associe l'interface XML
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT); // Force l'orientation portrait

        // Gestion des marges système (barre de statut et barre de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des composants de la barre d'état et du bouton d'ajout
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon);
        TextView statusBarTitle = findViewById(R.id.status_bar_title);
        ImageView addButton = findViewById(R.id.patient_records_activity_add_appointment);

        statusBackIcon.setOnClickListener(v -> finish()); // Ferme l'activité au clic sur le retour
        statusBarIcon.setImageResource(R.drawable.patient_white); // Icône patient
        statusBarTitle.setText(getString(R.string.patient_records)); // Titre "Dossiers patients"
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientRecordsActivity.this, EditablePatientProfileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_PATIENT); // Ouvre l'activité pour ajouter un patient
        });

        // Configuration du RecyclerView
        patientList = findViewById(R.id.patient_records_activity_patient_list);
        adapter = new PatientRecordsActivityPatientAdapter(new ArrayList<>(), this); // Crée l'adaptateur
        patientList.setLayoutManager(new LinearLayoutManager(this)); // Définit un layout linéaire
        patientList.setAdapter(adapter); // Associe l'adaptateur

        loadPatients(); // Charge la liste des patients
    }

    private void loadPatients() {
        // Charge tous les patients depuis la base de données
        List<Patient> patients = Database.getInstance().getAllPatients();
        if (patients != null && !patients.isEmpty()) {
            adapter.updatePatientList(patients); // Met à jour l'adaptateur avec la liste
        } else {
            Toast.makeText(this, "Aucun patient trouvé", Toast.LENGTH_SHORT).show(); // Affiche un message si vide
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_PATIENT && resultCode == RESULT_OK) {
            loadPatients(); // Rafraîchit la liste après ajout d'un patient
        }
    }

    @Override
    public void onItemClick(Patient patient) {
        // Gère le clic sur un patient dans la liste
        Intent intent = new Intent(PatientRecordsActivity.this, PatientProfileActivity.class);
        intent.putExtra("patient", patient); // Passe le patient sélectionné
        startActivity(intent); // Redirige vers PatientProfileActivity
    }
}