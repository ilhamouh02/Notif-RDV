package com.example.notifrdv.home;

// Importations des classes Android pour l'UI et le RecyclerView
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Importations pour le RecyclerView et les annotations
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

// Importations des ressources et classes internes de l'application
import com.example.notifrdv.R;
import com.example.notifrdv.utils.database.Appointment;

// Importations pour les collections
import java.util.List;

// Adaptateur pour afficher les rendez-vous dans un RecyclerView sur l'écran d'accueil
public class HomeActivityAppointmentsAdapter extends RecyclerView.Adapter<HomeActivityAppointmentsAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointmentList; // Liste des rendez-vous à afficher
    private final OnItemClickListener listener; // Écouteur pour gérer les clics sur les éléments

    // Interface pour gérer les clics sur les éléments de la liste
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment); // Méthode appelée lors d'un clic sur un rendez-vous
    }

    // Constructeur de l'adaptateur
    public HomeActivityAppointmentsAdapter(List<Appointment> appointmentList, OnItemClickListener listener) {
        this.appointmentList = appointmentList; // Initialise la liste des rendez-vous
        this.listener = listener; // Initialise l'écouteur de clics
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crée une nouvelle vue pour un élément de la liste
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_home_item, parent, false); // Charge le layout XML (activity_home_item.xml)
        return new AppointmentViewHolder(view); // Retourne un nouveau ViewHolder avec la vue
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        // Lie les données d'un rendez-vous à une vue
        Appointment appointment = appointmentList.get(position); // Récupère le rendez-vous à la position donnée

        holder.patientName.setText(appointment.getPatient().getName()); // Affiche le nom du patient
        holder.patientAge.setText(appointment.getPatient().getAge() + " ans"); // Affiche l'âge du patient
        holder.appointmentType.setText(appointment.getStatus()); // Affiche le statut du rendez-vous (par exemple, "scheduled")

        // Formatage de l'heure (de HHMM à HH:MM)
        int hour = appointment.getAppointmentTime() / 100; // Extrait l'heure
        int minute = appointment.getAppointmentTime() % 100; // Extrait les minutes
        holder.appointmentTime.setText(String.format("%02d:%02d", hour, minute)); // Affiche l'heure formatée

        // Alternance des couleurs pour l'arrière-plan de l'heure
        int bgColor = position % 2 == 0 ? R.color.pastel_green : R.color.bondi_blue; // Vert pastel ou bleu bondi
        holder.appointmentTime.setBackgroundTintList(
                AppCompatResources.getColorStateList(holder.itemView.getContext(), bgColor)); // Applique la couleur

        // Configure le clic sur l'élément pour appeler l'écouteur
        holder.itemView.setOnClickListener(v -> listener.onItemClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size(); // Retourne le nombre total d'éléments dans la liste
    }

    // Classe ViewHolder pour gérer les vues de chaque élément de la liste
    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        ImageView patientImage; // Image du patient (non utilisée ici)
        TextView patientName; // Nom du patient
        TextView patientAge; // Âge du patient
        TextView appointmentType; // Statut ou type du rendez-vous
        TextView appointmentTime; // Heure du rendez-vous

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            // Associe les composants UI aux éléments XML
            patientImage = itemView.findViewById(R.id.activity_home_item_patient_picture); // Image du patient
            patientName = itemView.findViewById(R.id.activity_home_item_patient_name); // Nom du patient
            patientAge = itemView.findViewById(R.id.activity_home_item_patient_age); // Âge du patient
            appointmentType = itemView.findViewById(R.id.activity_home_item_appointment_type); // Statut du rendez-vous
            appointmentTime = itemView.findViewById(R.id.activity_home_item_appointment_time); // Heure du rendez-vous
        }
    }
}