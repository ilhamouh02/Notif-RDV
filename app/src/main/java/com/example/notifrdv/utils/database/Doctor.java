package com.example.notifrdv.utils.database;

import android.os.Parcel;
import android.os.Parcelable;

public class Doctor implements Parcelable {
    private long id;
    private String name;
    private String email;
    private String password;
    private String salt;
    private String picturePath;

    public Doctor(long id, String name, String email, String password, String salt, String picturePath) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.picturePath = picturePath;
    }

    protected Doctor(Parcel in) {
        id = in.readLong();
        name = in.readString();
        email = in.readString();
        password = in.readString();
        salt = in.readString();
        picturePath = in.readString();
    }

    public static final Creator<Doctor> CREATOR = new Creator<Doctor>() {
        @Override
        public Doctor createFromParcel(Parcel in) {
            return new Doctor(in);
        }

        @Override
        public Doctor[] newArray(int size) {
            return new Doctor[size];
        }
    };

    // Getters et Setters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getSalt() { return salt; }
    public String getPicturePath() { return picturePath; }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(salt);
        dest.writeString(picturePath);
    }
}