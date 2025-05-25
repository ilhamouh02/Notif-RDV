package com.example.notifrdv.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notifrdv.R;
import com.example.notifrdv.utils.database.Appointment;

import java.util.List;

public class HomeActivityAppointmentsAdapter extends RecyclerView.Adapter<HomeActivityAppointmentsAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointmentList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public HomeActivityAppointmentsAdapter(List<Appointment> appointmentList, OnItemClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_home_item, parent, false);
        return new AppointmentViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.patientName.setText(appointment.getPatient().getName());
        holder.patientAge.setText(appointment.getPatient().getAge() + " ans");
        holder.appointmentType.setText(appointment.getStatus());

        int hour = appointment.getAppointmentTime() / 100;
        int minute = appointment.getAppointmentTime() % 100;
        holder.appointmentTime.setText(String.format("%02d:%02d", hour, minute));

        // Alternance des couleurs
        int bgColor = position % 2 == 0 ? R.color.pastel_green : R.color.bondi_blue;
        holder.appointmentTime.setBackgroundTintList(
                AppCompatResources.getColorStateList(holder.itemView.getContext(), bgColor));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        ImageView patientImage;
        TextView patientName;
        TextView patientAge;
        TextView appointmentType;
        TextView appointmentTime;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            patientImage = itemView.findViewById(R.id.activity_home_item_patient_picture);
            patientName = itemView.findViewById(R.id.activity_home_item_patient_name);
            patientAge = itemView.findViewById(R.id.activity_home_item_patient_age);
            appointmentType = itemView.findViewById(R.id.activity_home_item_appointment_type);
            appointmentTime = itemView.findViewById(R.id.activity_home_item_appointment_time);
        }
    }
}