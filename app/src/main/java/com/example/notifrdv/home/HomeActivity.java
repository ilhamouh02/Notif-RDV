package com.example.notifrdv.home;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notifrdv.R;
import com.example.notifrdv.appointment.AppointmentActivity;
import com.example.notifrdv.doctorProfile.DoctorProfileActivity;
import com.example.notifrdv.patientRecords.PatientRecordsActivity;
import com.example.notifrdv.utils.database.Appointment;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeActivityAppointmentsAdapter.OnItemClickListener {

    private ImageView doctorImage;
    private TextView doctorName;
    private TextView appointmentsCounter;
    private TextView nextAppointment;
    private RecyclerView upcomingAppointmentsRecycler;

    private List<Appointment> appointments = new ArrayList<>();
    private HomeActivityAppointmentsAdapter adapter;
    private long currentDoctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerView();
        loadDoctorInfo();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume(); // Correction: appel à super
        loadDoctorInfo();
        loadUpcomingAppointments();
    }

    private void initViews() {
        doctorImage = findViewById(R.id.home_activity_doctor_picture);
        doctorName = findViewById(R.id.home_activity_doctor_name);
        appointmentsCounter = findViewById(R.id.home_activity_doctor_appointments_counter);
        nextAppointment = findViewById(R.id.home_activity_doctor_next_appointment);
        upcomingAppointmentsRecycler = findViewById(R.id.home_activity_upcoming_appointments_list);
    }

    private void setupRecyclerView() {
        adapter = new HomeActivityAppointmentsAdapter(appointments, this);
        upcomingAppointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        upcomingAppointmentsRecycler.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadDoctorInfo() {
        String email = getIntent().getStringExtra("email");
        if (email == null) return;

        new AsyncTask<Void, Void, Doctor>() {
            @Override
            protected Doctor doInBackground(Void... voids) {
                return Database.getInstance().getDoctorByEmail(email);
            }

            @Override
            protected void onPostExecute(Doctor doctor) {
                if (doctor != null) {
                    currentDoctorId = doctor.getId();
                    doctorName.setText("Dr. " + doctor.getName());

                    try {
                        if (doctor.getPicturePath() != null) {
                            File imgFile = new File(doctor.getPicturePath());
                            if (imgFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                doctorImage.setImageBitmap(bitmap);
                                return;
                            }
                        }
                        doctorImage.setImageResource(R.drawable.default_profile_picture);
                    } catch (Exception e) {
                        Log.e("HomeActivity", "Error loading doctor image", e);
                    }
                }
            }
        }.execute();
    }

    @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
    private void loadUpcomingAppointments() {
        new AsyncTask<Void, Void, List<Appointment>>() {
            @Override
            protected List<Appointment> doInBackground(Void... voids) {
                return Database.getInstance().getUpcomingAppointmentsForDoctor(currentDoctorId);
            }

            @Override
            protected void onPostExecute(List<Appointment> result) {
                appointments.clear();
                if (result != null) {
                    appointments.addAll(result);
                }
                adapter.notifyDataSetChanged();

                appointmentsCounter.setText(appointments.size() + " RDV");

                if (!appointments.isEmpty()) {
                    Appointment next = appointments.get(0);
                    String time = String.format("%02d:%02d",
                            next.getAppointmentTime() / 100,
                            next.getAppointmentTime() % 100);
                    nextAppointment.setText("Prochain: " + next.getPatient().getName() + " à " + time);
                } else {
                    nextAppointment.setText("Aucun rendez-vous à venir");
                }
            }
        }.execute();
    }

    private void setupClickListeners() {
        ImageButton editButton = findViewById(R.id.home_activity_edit_doctor_info);
        editButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorProfileActivity.class));
        });

        findViewById(R.id.home_activity_appointments_scheduling_layout)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, AppointmentActivity.class);
                    intent.putExtra("isNewAppointment", true);
                    startActivity(intent);
                });

        findViewById(R.id.home_activity_patients_records_layout)
                .setOnClickListener(v -> {
                    startActivity(new Intent(this, PatientRecordsActivity.class));
                });
    }

    @Override
    public void onItemClick(Appointment appointment) {
        Intent intent = new Intent(this, AppointmentActivity.class);
        intent.putExtra("appointment", appointment);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Pour désactiver le retour, ne pas appeler super
        // super.onBackPressed(); // Commenté pour désactiver le retour
    }
}