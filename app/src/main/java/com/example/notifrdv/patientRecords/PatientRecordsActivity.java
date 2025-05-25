package com.example.notifrdv.patientRecords;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notifrdv.R;
import com.example.notifrdv.patientProfile.EditablePatientProfileActivity;
import com.example.notifrdv.patientProfile.PatientProfileActivity;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRecordsActivity extends AppCompatActivity implements PatientRecordsActivityPatientAdapter.OnItemClickListener {
    private RecyclerView patientList;
    private PatientRecordsActivityPatientAdapter adapter;
    private static final int REQUEST_CODE_ADD_PATIENT = 100;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_records);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon);
        TextView statusBarTitle = findViewById(R.id.status_bar_title);
        ImageView addButton = findViewById(R.id.patient_records_activity_add_appointment);

        statusBackIcon.setOnClickListener(v -> finish());
        statusBarIcon.setImageResource(R.drawable.patient_white);
        statusBarTitle.setText(getString(R.string.patient_records));
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientRecordsActivity.this, EditablePatientProfileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_PATIENT);
        });

        patientList = findViewById(R.id.patient_records_activity_patient_list);
        adapter = new PatientRecordsActivityPatientAdapter(new ArrayList<>(), this);
        patientList.setLayoutManager(new LinearLayoutManager(this));
        patientList.setAdapter(adapter);

        loadPatients();
    }

    private void loadPatients() {
        List<Patient> patients = Database.getInstance().getAllPatients();
        if (patients != null && !patients.isEmpty()) {
            adapter.updatePatientList(patients);
        } else {
            Toast.makeText(this, "Aucun patient trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_PATIENT && resultCode == RESULT_OK) {
            loadPatients(); // Rafraîchir la liste après ajout d'un patient
        }
    }

    @Override
    public void onItemClick(Patient patient) {
        Intent intent = new Intent(PatientRecordsActivity.this, PatientProfileActivity.class);
        intent.putExtra("patient", patient);
        startActivity(intent);
    }
}