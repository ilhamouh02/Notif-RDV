package com.example.notifrdv.patientRecords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRecordsActivityPatientAdapter extends RecyclerView.Adapter<PatientRecordsActivityPatientAdapter.PatientViewHolder> {
    private List<Patient> patients;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Patient patient);
    }

    public PatientRecordsActivityPatientAdapter(List<Patient> patients, OnItemClickListener listener) {
        this.patients = patients != null ? patients : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientName.setText(patient.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(patient));
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer le patient")
                    .setMessage("Êtes-vous sûr de vouloir supprimer " + patient.getName() + " ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        Database.getInstance().deletePatient(patient.getId());
                        patients.remove(position);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton("Non", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return patients != null ? patients.size() : 0;
    }

    public void updatePatientList(List<Patient> newPatients) {
        this.patients = newPatients != null ? newPatients : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientName;

        PatientViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(android.R.id.text1);
        }
    }
}