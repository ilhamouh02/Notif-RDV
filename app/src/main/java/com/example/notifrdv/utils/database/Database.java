package com.example.notifrdv.utils.database;

// Importations des classes Android pour la gestion de la base SQLite
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

// Importations pour la génération de sel et le hachage
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Importations pour les spécifications de clé et d'hachage
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Classe singleton pour gérer la base de données SQLite
public class Database extends SQLiteOpenHelper {
    private static Database instance; // Instance unique de la base de données
    private static final String DATABASE_NAME = "notif_rdv.db"; // Nom de la base de données
    private static final int DATABASE_VERSION = 1; // Version de la base de données

    // Récupère l'instance de la base de données (seulement après initialisation)
    public static synchronized Database getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Database instance is not initialized. Call initializeInstance() first.");
        }
        return instance;
    }

    // Initialise l'instance de la base de données avec un contexte
    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new Database(context.getApplicationContext());
            Log.d("Database", "Database instance initialized successfully");
        }
    }

    // Constructeur privé pour le singleton
    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crée la table des médecins avec des champs pour l'identifiant, le nom, l'email, le mot de passe haché, le sel et le chemin de l'image
        db.execSQL("CREATE TABLE doctors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "picture_path TEXT)");

        // Crée la table des patients avec des champs pour l'identifiant, le nom, la date de naissance, l'email, le numéro de téléphone, etc.
        db.execSQL("CREATE TABLE patients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "date_of_birth INTEGER NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone_number TEXT NOT NULL, " +
                "picture_path TEXT, " +
                "height REAL, " +
                "weight REAL, " +
                "gender TEXT)");

        // Crée la table des rendez-vous avec des champs pour l'identifiant, les IDs du patient et du médecin, les dates, etc.
        db.execSQL("CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "doctor_id INTEGER NOT NULL, " +
                "appointment_date INTEGER NOT NULL, " +
                "appointment_time INTEGER NOT NULL, " +
                "notes TEXT, " +
                "medicines TEXT, " +
                "exams TEXT, " +
                "status TEXT, " +
                "created_at TEXT, " +
                "updated_at TEXT, " +
                "is_completed INTEGER NOT NULL DEFAULT 0, " +
                "is_done INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(patient_id) REFERENCES patients(id), " +
                "FOREIGN KEY(doctor_id) REFERENCES doctors(id))");

        // Crée la table des prescriptions avec des champs pour l'identifiant, l'ID du rendez-vous, le nom du médicament, etc.
        db.execSQL("CREATE TABLE prescriptions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER NOT NULL, " +
                "medicine_name TEXT NOT NULL, " +
                "dosage TEXT NOT NULL, " +
                "duration TEXT NOT NULL, " +
                "FOREIGN KEY(appointment_id) REFERENCES appointments(id))");

        // Crée la table des examens avec des champs pour l'identifiant, l'ID du rendez-vous, le type d'examen, etc.
        db.execSQL("CREATE TABLE examinations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appointment_id INTEGER NOT NULL, " +
                "exam_type TEXT NOT NULL, " +
                "result TEXT, " +
                "exam_date INTEGER NOT NULL, " +
                "FOREIGN KEY(appointment_id) REFERENCES appointments(id))");

        // Crée la table des antécédents médicaux avec des champs pour l'identifiant, l'ID du patient, la condition, etc.
        db.execSQL("CREATE TABLE medical_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "condition TEXT NOT NULL, " +
                "diagnosis_date INTEGER NOT NULL, " +
                "treatment TEXT, " +
                "FOREIGN KEY(patient_id) REFERENCES patients(id))");

        Log.d("Database", "Base de données et tables créées avec succès");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gère la mise à jour de la base de données si la version passe de 1 à 2
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE appointments ADD COLUMN medicines TEXT");
            db.execSQL("ALTER TABLE appointments ADD COLUMN exams TEXT");
            db.execSQL("ALTER TABLE appointments ADD COLUMN status TEXT");
            db.execSQL("ALTER TABLE appointments ADD COLUMN created_at TEXT");
            db.execSQL("ALTER TABLE appointments ADD COLUMN updated_at TEXT");
            db.execSQL("ALTER TABLE appointments ADD COLUMN is_completed INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE appointments ADD COLUMN is_done INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE patients ADD COLUMN height REAL");
            db.execSQL("ALTER TABLE patients ADD COLUMN weight REAL");
            db.execSQL("ALTER TABLE patients ADD COLUMN gender TEXT");
        }
        Log.d("Database", "Base de données mise à jour de la version " + oldVersion + " à " + newVersion);
    }

    // Génère un sel aléatoire de 16 octets pour sécuriser le hachage des mots de passe
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT).trim();
    }

    // Hache un mot de passe avec un sel donné en utilisant PBKDF2WithHmacSHA1
    private String hashPassword(String password, String salt) {
        try {
            int iterations = 10000; // Nombre d'itérations pour le hachage
            char[] chars = password.toCharArray();
            byte[] saltBytes = Base64.decode(salt, Base64.DEFAULT);
            PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (Exception e) {
            Log.e("Database", "Erreur lors du hachage du mot de passe : " + e.getMessage());
            return null;
        }
    }

    // Récupère le sel associé à un email de médecin
    public String getSaltByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("doctors", new String[]{"salt"}, "email = ?", new String[]{email}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("salt"));
            }
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du sel pour l'email " + email + " : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Méthodes pour les médecins
    public long addDoctor(String name, String email, String password, String picturePath) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);
            if (hashedPassword == null) {
                Log.e("Database", "Échec du hachage du mot de passe pour l'email " + email);
                return -1;
            }
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("email", email);
            values.put("password", hashedPassword);
            values.put("salt", salt);
            values.put("picture_path", picturePath);
            long result = db.insert("doctors", null, values);
            Log.d("Database", "addDoctor: Médecin inséré avec l'email " + email + ", résultat : " + result);
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout du médecin : " + e.getMessage());
            return -1;
        }
    }

    public Doctor getDoctorById(long doctorId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("doctors", null, "id = ?", new String[]{String.valueOf(doctorId)}, null, null, null);
            if (cursor.moveToFirst()) {
                Doctor doctor = new Doctor(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path"))
                );
                Log.d("Database", "getDoctorById: Médecin trouvé avec l'ID " + doctorId);
                return doctor;
            }
            Log.d("Database", "getDoctorById: Aucun médecin trouvé avec l'ID " + doctorId);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du médecin par ID : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Doctor getDoctorByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("doctors", null, "name = ?", new String[]{name}, null, null, null);
            if (cursor.moveToFirst()) {
                Doctor doctor = new Doctor(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path"))
                );
                Log.d("Database", "getDoctorByName: Médecin trouvé avec le nom " + name);
                return doctor;
            }
            Log.d("Database", "getDoctorByName: Aucun médecin trouvé avec le nom " + name);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du médecin par nom : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Doctor getDoctorByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("doctors", null, "email = ?", new String[]{email}, null, null, null);
            if (cursor.moveToFirst()) {
                Doctor doctor = new Doctor(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path"))
                );
                Log.d("Database", "getDoctorByEmail: Médecin trouvé avec l'email " + email);
                return doctor;
            }
            Log.d("Database", "getDoctorByEmail: Aucun médecin trouvé avec l'email " + email);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du médecin par email : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void updateDoctor(long doctorId, String name, String email, String password, String picturePath) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("email", email);
            if (password != null && !password.isEmpty()) {
                String salt = generateSalt();
                String hashedPassword = hashPassword(password, salt);
                if (hashedPassword == null) {
                    Log.e("Database", "Échec du hachage du mot de passe pour la mise à jour du médecin ID " + doctorId);
                    return;
                }
                values.put("password", hashedPassword);
                values.put("salt", salt);
            }
            values.put("picture_path", picturePath);
            int rows = db.update("doctors", values, "id = ?", new String[]{String.valueOf(doctorId)});
            Log.d("Database", "updateDoctor: " + rows + " lignes mises à jour pour le médecin ID " + doctorId);
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la mise à jour du médecin : " + e.getMessage());
        }
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("doctors", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                doctors.add(new Doctor(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path"))
                ));
            }
            Log.d("Database", "getAllDoctors: " + doctors.size() + " médecins récupérés");
            return doctors;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération de tous les médecins : " + e.getMessage());
            return doctors;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Méthodes pour les patients
    public long addPatient(String name, int dateOfBirth, String email, String phoneNumber, String picturePath, double height, double weight, String gender) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("date_of_birth", dateOfBirth);
            values.put("email", email);
            values.put("phone_number", phoneNumber);
            values.put("picture_path", picturePath);
            values.put("height", height);
            values.put("weight", weight);
            values.put("gender", gender);
            long result = db.insert("patients", null, values);
            Log.d("Database", "addPatient: Patient inséré avec l'email " + email + ", résultat : " + result);
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout du patient : " + e.getMessage());
            return -1;
        }
    }

    public long addPatient(Patient patient) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", patient.getName());
            values.put("date_of_birth", patient.getBirthdate());
            values.put("email", patient.getEmail());
            values.put("phone_number", patient.getPhone());
            values.put("picture_path", patient.getPicturePath());
            values.put("height", patient.getHeight());
            values.put("weight", patient.getWeight());
            values.put("gender", patient.getGender());
            long result = db.insert("patients", null, values);
            Log.d("Database", "addPatient: Patient inséré avec l'email " + patient.getEmail() + ", résultat : " + result);
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout du patient : " + e.getMessage());
            return -1;
        }
    }

    public Patient getPatientById(long patientId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("patients", null, "id = ?", new String[]{String.valueOf(patientId)}, null, null, null);
            if (cursor.moveToFirst()) {
                Patient patient = new Patient(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("date_of_birth")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("height")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
                Log.d("Database", "getPatientById: Patient trouvé avec l'ID " + patientId);
                return patient;
            }
            Log.d("Database", "getPatientById: Aucun patient trouvé avec l'ID " + patientId);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du patient par ID : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("patients", null, null, null, null, null, "name ASC");
            while (cursor.moveToNext()) {
                Patient patient = new Patient(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("date_of_birth")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("height")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
                patients.add(patient);
            }
            Log.d("Database", "getAllPatients: " + patients.size() + " patients récupérés");
            return patients;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération de tous les patients : " + e.getMessage());
            return patients;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Patient getPatientByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("patients", null, "email = ?", new String[]{email}, null, null, null);
            if (cursor.moveToFirst()) {
                Patient patient = new Patient(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("date_of_birth")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("height")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
                Log.d("Database", "getPatientByEmail: Patient trouvé avec l'email " + email);
                return patient;
            }
            Log.d("Database", "getPatientByEmail: Aucun patient trouvé avec l'email " + email);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du patient par email : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public Patient getPatientByName(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("patients", null, "name = ?", new String[]{name}, null, null, null);
            if (cursor.moveToFirst()) {
                Patient patient = new Patient(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("date_of_birth")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("picture_path")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("height")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("weight")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
                Log.d("Database", "getPatientByName: Patient trouvé avec le nom " + name);
                return patient;
            }
            Log.d("Database", "getPatientByName: Aucun patient trouvé avec le nom " + name);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du patient par nom : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void updatePatient(Patient patient) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", patient.getName());
            values.put("date_of_birth", patient.getBirthdate());
            values.put("email", patient.getEmail());
            values.put("phone_number", patient.getPhone());
            values.put("picture_path", patient.getPicturePath());
            values.put("height", patient.getHeight());
            values.put("weight", patient.getWeight());
            values.put("gender", patient.getGender());
            int rows = db.update("patients", values, "id = ?", new String[]{String.valueOf(patient.getId())});
            Log.d("Database", "updatePatient: " + rows + " lignes mises à jour pour le patient ID " + patient.getId());
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la mise à jour du patient : " + e.getMessage());
        }
    }

    public boolean deletePatient(long patientId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Supprime d'abord les dépendances (rendez-vous, etc.)
            db.delete("appointments", "patient_id = ?", new String[]{String.valueOf(patientId)});

            // Puis supprime le patient
            int rows = db.delete("patients", "id = ?", new String[]{String.valueOf(patientId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e("Database", "Erreur suppression patient: " + e.getMessage());
            return false;
        }
    }

    // Méthodes pour les rendez-vous
    public long addAppointment(Appointment appointment, long doctorId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Vérification des clés étrangères
            if (getPatientById(appointment.getPatient().getId()) == null) {
                Log.e("Database", "Patient ID " + appointment.getPatient().getId() + " non trouvé");
                return -1;
            }
            if (getDoctorById(doctorId) == null) {
                Log.e("Database", "Doctor ID " + doctorId + " non trouvé");
                return -1;
            }

            ContentValues values = new ContentValues();
            values.put("patient_id", appointment.getPatient().getId());
            values.put("doctor_id", doctorId);
            values.put("appointment_date", appointment.getAppointmentDate());
            values.put("appointment_time", appointment.getAppointmentTime());
            values.put("notes", appointment.getNotes());
            values.put("medicines", appointment.getMedicines());
            values.put("exams", appointment.getExams());
            values.put("status", appointment.getStatus());
            values.put("created_at", appointment.getCreatedAt());
            values.put("updated_at", appointment.getUpdatedAt());
            values.put("is_completed", appointment.isCompleted() ? 1 : 0);
            values.put("is_done", appointment.isDone() ? 1 : 0);
            long result = db.insert("appointments", null, values);
            if (result != -1) {
                Log.d("Database", "addAppointment: Rendez-vous inséré avec l'ID " + result);
            } else {
                Log.e("Database", "addAppointment: Échec de l'insertion du rendez-vous");
            }
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout du rendez-vous : " + e.getMessage());
            return -1;
        }
    }

    public Appointment getAppointmentById(long appointmentId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("appointments", null, "id = ?", new String[]{String.valueOf(appointmentId)}, null, null, null);
            if (cursor.moveToFirst()) {
                Patient patient = getPatientById(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                if (patient == null) {
                    Log.e("Database", "Patient non trouvé pour l'ID " + cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                    return null;
                }
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patient,
                        cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicines")),
                        cursor.getString(cursor.getColumnIndexOrThrow("exams")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
                );
                Log.d("Database", "getAppointmentById: Rendez-vous trouvé avec l'ID " + appointmentId);
                return appointment;
            }
            Log.d("Database", "getAppointmentById: Aucun rendez-vous trouvé avec l'ID " + appointmentId);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du rendez-vous par ID : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void updateAppointment(Appointment appointment, long doctorId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("patient_id", appointment.getPatient().getId());
            values.put("doctor_id", doctorId);
            values.put("appointment_date", appointment.getAppointmentDate());
            values.put("appointment_time", appointment.getAppointmentTime());
            values.put("notes", appointment.getNotes());
            values.put("medicines", appointment.getMedicines());
            values.put("exams", appointment.getExams());
            values.put("status", appointment.getStatus());
            values.put("created_at", appointment.getCreatedAt());
            values.put("updated_at", appointment.getUpdatedAt());
            values.put("is_completed", appointment.isCompleted() ? 1 : 0);
            values.put("is_done", appointment.isDone() ? 1 : 0);
            int rows = db.update("appointments", values, "id = ?", new String[]{String.valueOf(appointment.getId())});
            Log.d("Database", "updateAppointment: " + rows + " lignes mises à jour pour le rendez-vous ID " + appointment.getId());
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la mise à jour du rendez-vous : " + e.getMessage());
        }
    }

    public void deleteAppointment(long appointmentId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int rows = db.delete("appointments", "id = ?", new String[]{String.valueOf(appointmentId)});
            Log.d("Database", "deleteAppointment: " + rows + " lignes supprimées pour le rendez-vous ID " + appointmentId);
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la suppression du rendez-vous : " + e.getMessage());
        }
    }

    public Appointment getPreviousDoneAppointmentByPatientId(long patientId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "appointments",
                    null,
                    "patient_id = ? AND is_done = ?",
                    new String[]{String.valueOf(patientId), "1"},
                    null,
                    null,
                    "appointment_date DESC, appointment_time DESC",
                    "1"
            );
            if (cursor.moveToFirst()) {
                Patient patient = getPatientById(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                if (patient == null) {
                    Log.e("Database", "Patient non trouvé pour l'ID " + cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                    return null;
                }
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patient,
                        cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicines")),
                        cursor.getString(cursor.getColumnIndexOrThrow("exams")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
                );
                Log.d("Database", "getPreviousDoneAppointmentByPatientId: Rendez-vous précédent trouvé pour le patient ID " + patientId);
                return appointment;
            }
            Log.d("Database", "getPreviousDoneAppointmentByPatientId: Aucun rendez-vous précédent trouvé pour le patient ID " + patientId);
            return null;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération du rendez-vous précédent terminé : " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Appointment> getUpcomingAppointmentsForDoctor(long doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Formate la date actuelle au format YYYYMMDD
            Calendar cal = Calendar.getInstance();
            int currentDate = cal.get(Calendar.YEAR) * 10000 +
                    (cal.get(Calendar.MONTH) + 1) * 100 +
                    cal.get(Calendar.DAY_OF_MONTH);

            cursor = db.query("appointments",
                    null,
                    "doctor_id = ? AND appointment_date >= ? AND is_done = 0",
                    new String[]{String.valueOf(doctorId), String.valueOf(currentDate)},
                    null, null,
                    "appointment_date ASC, appointment_time ASC");

            while (cursor.moveToNext()) {
                Patient patient = getPatientById(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                if (patient == null) continue;

                appointments.add(new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patient,
                        cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicines")),
                        cursor.getString(cursor.getColumnIndexOrThrow("exams")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
                ));
            }
        } catch (Exception e) {
            Log.e("Database", "Error getting upcoming appointments", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return appointments;
    }

    public List<Appointment> getAllNotDoneAppointmentsByDate(int date) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    "appointments",
                    null,
                    "appointment_date = ? AND is_done = ?",
                    new String[]{String.valueOf(date), "0"},
                    null,
                    null,
                    "appointment_time ASC"
            );
            while (cursor.moveToNext()) {
                Patient patient = getPatientById(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                if (patient == null) {
                    Log.e("Database", "Patient non trouvé pour l'ID " + cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                    continue;
                }
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patient,
                        cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicines")),
                        cursor.getString(cursor.getColumnIndexOrThrow("exams")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
                );
                appointments.add(appointment);
            }
            Log.d("Database", "getAllNotDoneAppointmentsByDate: " + appointments.size() + " rendez-vous récupérés pour la date " + date);
            return appointments;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération des rendez-vous par date : " + e.getMessage());
            return appointments;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Appointment> getTodayAppointments() {
        List<Appointment> todayAppointments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int today = year * 10000 + month * 100 + day;
            cursor = db.query(
                    "appointments",
                    null,
                    "appointment_date = ? AND is_done = ?",
                    new String[]{String.valueOf(today), "0"},
                    null,
                    null,
                    "appointment_time ASC"
            );
            while (cursor.moveToNext()) {
                Patient patient = getPatientById(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                if (patient == null) {
                    Log.e("Database", "Patient non trouvé pour l'ID " + cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
                    continue;
                }
                Appointment appointment = new Appointment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        patient,
                        cursor.getLong(cursor.getColumnIndexOrThrow("doctor_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicines")),
                        cursor.getString(cursor.getColumnIndexOrThrow("exams")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("appointment_time")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
                );
                todayAppointments.add(appointment);
            }
            Log.d("Database", "getTodayAppointments: " + todayAppointments.size() + " rendez-vous récupérés pour aujourd'hui");
            return todayAppointments;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération des rendez-vous d'aujourd'hui : " + e.getMessage());
            return todayAppointments;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Méthodes pour les prescriptions
    public long addPrescription(long appointmentId, String medicineName, String dosage, String duration) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("appointment_id", appointmentId);
            values.put("medicine_name", medicineName);
            values.put("dosage", dosage);
            values.put("duration", duration);
            long result = db.insert("prescriptions", null, values);
            Log.d("Database", "addPrescription: Prescription insérée avec l'ID " + result);
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout de la prescription : " + e.getMessage());
            return -1;
        }
    }

    public List<String> getPrescriptionsForAppointment(long appointmentId) {
        List<String> prescriptions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("prescriptions", null, "appointment_id = ?", new String[]{String.valueOf(appointmentId)}, null, null, null);
            while (cursor.moveToNext()) {
                String medicine = cursor.getString(cursor.getColumnIndexOrThrow("medicine_name"));
                String dosage = cursor.getString(cursor.getColumnIndexOrThrow("dosage"));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"));
                prescriptions.add(medicine + " - " + dosage + " for " + duration);
            }
            Log.d("Database", "getPrescriptionsForAppointment: " + prescriptions.size() + " prescriptions récupérées pour le rendez-vous ID " + appointmentId);
            return prescriptions;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération des prescriptions : " + e.getMessage());
            return prescriptions;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Méthodes pour les examens
    public long addExamination(long appointmentId, String examType, String result, int examDate) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("appointment_id", appointmentId);
            values.put("exam_type", examType);
            values.put("result", result);
            values.put("exam_date", examDate);
            long resultId = db.insert("examinations", null, values);
            Log.d("Database", "addExamination: Examen inséré avec l'ID " + resultId);
            return resultId;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout de l'examen : " + e.getMessage());
            return -1;
        }
    }

    public List<String> getExaminationsForAppointment(long appointmentId) {
        List<String> examinations = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("examinations", null, "appointment_id = ?", new String[]{String.valueOf(appointmentId)}, null, null, null);
            while (cursor.moveToNext()) {
                String examType = cursor.getString(cursor.getColumnIndexOrThrow("exam_type"));
                String result = cursor.getString(cursor.getColumnIndexOrThrow("result"));
                int examDate = cursor.getInt(cursor.getColumnIndexOrThrow("exam_date"));
                examinations.add(examType + " on " + examDate + ": " + result);
            }
            Log.d("Database", "getExaminationsForAppointment: " + examinations.size() + " examens récupérés pour le rendez-vous ID " + appointmentId);
            return examinations;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération des examens : " + e.getMessage());
            return examinations;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Méthodes pour les antécédents médicaux
    public long addMedicalHistory(long patientId, String condition, int diagnosisDate, String treatment) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("patient_id", patientId);
            values.put("condition", condition);
            values.put("diagnosis_date", diagnosisDate);
            values.put("treatment", treatment);
            long result = db.insert("medical_history", null, values);
            Log.d("Database", "addMedicalHistory: Historique médical inséré avec l'ID " + result);
            return result;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de l'ajout de l'historique médical : " + e.getMessage());
            return -1;
        }
    }

    public List<String> getMedicalHistoryForPatient(long patientId) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("medical_history", null, "patient_id = ?", new String[]{String.valueOf(patientId)}, null, null, "diagnosis_date DESC");
            while (cursor.moveToNext()) {
                String condition = cursor.getString(cursor.getColumnIndexOrThrow("condition"));
                int diagnosisDate = cursor.getInt(cursor.getColumnIndexOrThrow("diagnosis_date"));
                String treatment = cursor.getString(cursor.getColumnIndexOrThrow("treatment"));
                history.add(condition + " diagnosed on " + diagnosisDate + ": " + treatment);
            }
            Log.d("Database", "getMedicalHistoryForPatient: " + history.size() + " entrées d'historique médical récupérées pour le patient ID " + patientId);
            return history;
        } catch (Exception e) {
            Log.e("Database", "Erreur lors de la récupération de l'historique médical : " + e.getMessage());
            return history;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}