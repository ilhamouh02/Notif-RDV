package com.example.notifrdv.utils.database;

import android.os.Parcel;
import android.os.Parcelable;

public class Patient implements Parcelable {
    private long id;
    private String name;
    private int birthdate;
    private String email;
    private String phone;
    private String picturePath;
    private double height;
    private double weight;
    private String gender;

    // Constructeurs
    public Patient(long id, String name, int birthdate, String email, String phone,
                   String picturePath, double height, double weight, String gender) {
        this.id = id;
        this.name = name;
        this.birthdate = birthdate;
        this.email = email;
        this.phone = phone;
        this.picturePath = picturePath;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    protected Patient(Parcel in) {
        id = in.readLong();
        name = in.readString();
        birthdate = in.readInt();
        email = in.readString();
        phone = in.readString();
        picturePath = in.readString();
        height = in.readDouble();
        weight = in.readDouble();
        gender = in.readString();
    }

    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBirthdate() { return birthdate; }
    public void setBirthdate(int birthdate) { this.birthdate = birthdate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPicturePath() { return picturePath; }
    public void setPicturePath(String picturePath) { this.picturePath = picturePath; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() {
        int birthYear = birthdate / 10000;
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        return currentYear - birthYear;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(birthdate);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(picturePath);
        dest.writeDouble(height);
        dest.writeDouble(weight);
        dest.writeString(gender);
    }
}