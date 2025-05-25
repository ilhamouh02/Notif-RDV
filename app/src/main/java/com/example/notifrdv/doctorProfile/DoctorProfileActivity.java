package com.example.notifrdv.doctorProfile;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.notifrdv.R;
import com.example.notifrdv.home.HomeActivity;
import com.example.notifrdv.utils.FastToast;
import com.example.notifrdv.utils.database.Database;
import com.example.notifrdv.utils.database.Doctor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Activité pour gérer le profil du médecin (création ou modification)
public class DoctorProfileActivity extends AppCompatActivity {
    EditText doctorNameInput;
    EditText doctorEmailInput;
    EditText doctorPasswordInput;
    ImageView doctorPicture;

    long doctorId = -1;
    String doctorPicturePath = "";
    static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    boolean isFirstTime = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_profile);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialisation des éléments de la barre de statut (via le layout inclus)
        ImageView statusBackIcon = findViewById(R.id.status_bar_back_arrow_icon);
        ImageView statusBarIcon = findViewById(R.id.status_bar_icon);
        TextView statusBarTitle = findViewById(R.id.status_bar_title);
        statusBackIcon.setOnClickListener(v -> finish());
        statusBarIcon.setImageResource(R.drawable.doctor_white);
        statusBarTitle.setText(getString(R.string.doctor_profile));

        doctorPicture = findViewById(R.id.doctor_profile_activity_doctor_picture);
        doctorNameInput = findViewById(R.id.doctor_profile_activity_doctor_name);
        doctorEmailInput = findViewById(R.id.doctor_profile_activity_doctor_email);
        doctorPasswordInput = findViewById(R.id.doctor_profile_activity_doctor_password);
        AppCompatButton saveButton = findViewById(R.id.doctor_profile_activity_save_button);

        // Vérifier si c'est la première connexion ou une modification
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);
        if (!isFirstTime) {
            // Charger les données du docteur existant
            String email = getIntent().getStringExtra("email");
            if (email != null) {
                Doctor doctor = Database.getInstance().getDoctorByEmail(email); // Corrigé : un seul paramètre
                if (doctor != null) {
                    doctorId = doctor.getId();
                    doctorNameInput.setText(doctor.getName());
                    doctorEmailInput.setText(doctor.getEmail());
                    doctorPasswordInput.setText(""); // Ne pas afficher le mot de passe pour des raisons de sécurité
                    doctorPicturePath = doctor.getPicturePath();
                    if (doctorPicturePath != null && !doctorPicturePath.isEmpty()) {
                        File imageFile = new File(doctorPicturePath);
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(doctorPicturePath);
                            doctorPicture.setImageBitmap(bitmap);
                        }
                    }
                } else {
                    FastToast.show(this, "Médecin non trouvé.");
                    finish();
                    return;
                }
            }
        }

        // Listener pour sélectionner une image
        doctorPicture.setOnClickListener(v -> openImagePicker());

        // Listener pour le bouton de sauvegarde
        saveButton.setOnClickListener(v -> {
            if (!isDoctorProfileValid()) {
                FastToast.show(this, "Veuillez remplir tous les champs.");
                return;
            }

            String name = doctorNameInput.getText().toString().trim();
            String email = doctorEmailInput.getText().toString().trim();
            String password = doctorPasswordInput.getText().toString().trim();
            String newPicturePath = imageUri != null ? saveImageLocally(imageUri) : doctorPicturePath;

            if (isFirstTime) {
                // Créer un nouveau docteur
                long result = Database.getInstance().addDoctor(name, email, password, newPicturePath);
                if (result != -1) {
                    doctorId = result;
                    FastToast.show(this, "Profil créé avec succès");
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    FastToast.show(this, "Cet email existe déjà ou une erreur s'est produite");
                }
            } else {
                // Mettre à jour le docteur existant
                if (doctorId != -1) {
                    Database.getInstance().updateDoctor(doctorId, name, email, password, newPicturePath);
                    FastToast.show(this, "Profil mis à jour avec succès");
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    FastToast.show(this, "Erreur lors de la mise à jour du profil : ID du médecin non trouvé");
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            doctorPicture.setImageURI(imageUri);
        }
    }

    private String saveImageLocally(Uri imageUri) {
        if (imageUri == null) return null;
        try {
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DoctorImages");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "doctor_profile_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("DoctorProfile", "Erreur lors de l'enregistrement de l'image : " + e.getMessage());
            return null;
        }
    }

    private boolean isDoctorProfileValid() {
        return !doctorNameInput.getText().toString().trim().isEmpty() &&
                !doctorEmailInput.getText().toString().trim().isEmpty() &&
                !doctorPasswordInput.getText().toString().trim().isEmpty();
    }
}