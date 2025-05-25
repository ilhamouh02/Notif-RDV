package com.example.notifrdv.utils.database;

import android.os.Parcel;
import android.os.Parcelable;

public class Appointment implements Parcelable {
    private long id;
    private Patient patient;
    private long doctorId;
    private String notes;
    private String medicines;
    private String exams;
    private int appointmentDate;
    private int appointmentTime;
    private String status;
    private String createdAt;
    private String updatedAt;
    private boolean isCompleted;
    private boolean isDone;

    public Appointment(long id, Patient patient, long doctorId, String notes, int appointmentDate, int appointmentTime, String status, String createdAt, String updatedAt, boolean isCompleted) {
        this(id, patient, doctorId, notes, "", "", appointmentDate, appointmentTime, status, createdAt, updatedAt, isCompleted, false);
    }

    public Appointment(long id, Patient patient, long doctorId, String notes, String medicines, String exams, int appointmentDate, int appointmentTime, String status, String createdAt, String updatedAt, boolean isCompleted, boolean isDone) {
        this.id = id;
        this.patient = patient;
        this.doctorId = doctorId;
        this.notes = notes;
        this.medicines = medicines;
        this.exams = exams;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isCompleted = isCompleted;
        this.isDone = isDone;
    }

    protected Appointment(Parcel in) {
        id = in.readLong();
        patient = in.readParcelable(Patient.class.getClassLoader());
        doctorId = in.readLong();
        notes = in.readString();
        medicines = in.readString();
        exams = in.readString();
        appointmentDate = in.readInt();
        appointmentTime = in.readInt();
        status = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        isCompleted = in.readByte() != 0;
        isDone = in.readByte() != 0;
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeParcelable(patient, flags);
        dest.writeLong(doctorId);
        dest.writeString(notes);
        dest.writeString(medicines);
        dest.writeString(exams);
        dest.writeInt(appointmentDate);
        dest.writeInt(appointmentTime);
        dest.writeString(status);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeByte((byte) (isDone ? 1 : 0));
    }

    // Getters
    public long getId() { return id; }
    public Patient getPatient() { return patient; }
    public long getDoctorId() { return doctorId; }
    public String getNotes() { return notes; }
    public String getMedicines() { return medicines; }
    public String getExams() { return exams; }
    public int getAppointmentDate() { return appointmentDate; }
    public int getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isDone() { return isDone; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public void setDoctorId(long doctorId) { this.doctorId = doctorId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setMedicines(String medicines) { this.medicines = medicines; }
    public void setExams(String exams) { this.exams = exams; }
    public void setAppointmentDate(int appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(int appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
    public void setDone(boolean done) { this.isDone = done; }

    public String getAppointmentType() {
        return "Regular Consultation";
    }
}