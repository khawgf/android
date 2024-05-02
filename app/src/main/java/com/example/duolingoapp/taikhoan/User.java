package com.example.duolingoapp.taikhoan;

public class User {
    private String iduser;
    private String hoTen;
    private String email;
    private String password;
    private String SDT;
    private byte[] img;

    private int network;

    public User() {
        //Nhận data từ Firebase
    }

    public User(String iduser, String hoTen, String email, String password, String SDT) {
        this.iduser = iduser;
        this.hoTen = hoTen;
        this.email = email;
        this.password = password;
        this.SDT = SDT;
    }

    public User(String iduser, String hoTen, String email, String password, String SDT, byte[] img, int network) {
        this.iduser = iduser;
        this.hoTen = hoTen;
        this.email = email;
        this.password = password;
        this.SDT = SDT;
        this.img = img;
        this.network = network;
    }

    public String getIduser() {
        return iduser;
    }

    public void setIduser(String iduser) {
        this.iduser = iduser;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }
}
