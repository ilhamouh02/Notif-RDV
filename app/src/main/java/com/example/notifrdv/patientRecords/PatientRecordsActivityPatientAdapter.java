package com.example.notifrdv.patientRecords;

// Importations des classes Android pour l'UI et le RecyclerView
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// Importations pour les annotations et les dialogues
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

// Importations des classes internes de l'application
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Patient;

// Importations pour les collections
import java.util.ArrayList;
import java.util.List;

// Adaptateur pour afficher la liste des patients dans un RecyclerView
public class PatientRecordsActivityPatientAdapter extends RecyclerView.Adapter<PatientRecordsActivityPatientAdapter.PatientViewHolder> {
    private List<Patient> patients; // Liste des patients à afficher
    private final OnItemClickListener listener; // Écouteur pour gérer les clics sur les patients

    // Interface pour gérer les clics sur les éléments de la liste
    public interface OnItemClickListener {
        void onItemClick(Patient patient); // Méthode appelée lors d'un clic sur un patient
    }

    // Constructeur de l'adaptateur
    public PatientRecordsActivityPatientAdapter(List<Patient> patients, OnItemClickListener listener) {
        this.patients = patients != null ? patients : new ArrayList<>(); // Initialise avec la liste ou une liste vide si null
        this.listener = listener; // Initialise l'écouteur de clics
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crée une nouvelle vue pour un élément de la liste
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false); // Utilise un layout simple
        return new PatientViewHolder(view); // Retourne un nouveau ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        // Lie les données d'un patient à une vue
        Patient patient = patients.get(position); // Récupère le patient à la position donnée
        holder.patientName.setText(patient.getName()); // Affiche le nom du patient
        holder.itemView.setOnClickListener(v -> listener.onItemClick(patient)); // Gère le clic simple
        holder.itemView.setOnLongClickListener(v -> {
            // Ouvre un dialogue de confirmation pour la suppression
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer le patient") // Titre du dialogue
                    .setMessage("Êtes-vous sûr de vouloir supprimer " + patient.getName() + " ?") // Message avec le nom
                    .setPositiveButton("Oui", (dialog, which) -> {
                        Database.getInstance().deletePatient(patient.getId()); // Supprime le patient de la base
                        patients.remove(position); // Supprime de la liste locale
                        notifyItemRemoved(position); // Notifie la suppression
                    })
                    .setNegativeButton("Non", null) // Bouton d'annulation
                    .show(); // Affiche le dialogue
            return true; // Indique que l'événement est consommé
        });
    }

    @Override
    public int getItemCount() {
        return patients != null ? patients.size() : 0; // Retourne le nombre d'éléments dans la liste
    }

    public void updatePatientList(List<Patient> newPatients) {
        // Met à jour la liste des patients
        this.patients = newPatients != null ? newPatients : new ArrayList<>(); // Initialise avec la nouvelle liste ou une liste vide
        notifyDataSetChanged(); // Notifie l'adaptateur pour rafraîchir la vue
    }

    // Classe ViewHolder pour gérer les vues de chaque élément de la liste
    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientName; // TextView pour afficher le nom du patient

        PatientViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(android.R.id.text1); // Associe le TextView du layout simple
        }
    }
}