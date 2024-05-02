package com.example.duolingoapp.dienkhuyet;

import java.io.Serializable;

public class FillBlank implements Serializable {
    private int idcau;
    private int idbo;
    private String noidung;
    private String dapan;
    private String goiy;

    public FillBlank(int idcau, int idbo, String noidung, String dapan, String goiy) {
        this.idcau = idcau;
        this.idbo = idbo;
        this.noidung = noidung;
        this.dapan = dapan;
        this.goiy = goiy;
    }

    public int getIdcau() {
        return idcau;
    }

    public void setIdcau(int idcau) {
        this.idcau = idcau;
    }

    public int getIdbo() {
        return idbo;
    }

    public void setIdbo(int idbo) {
        this.idbo = idbo;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }

    public String getDapan() {
        return dapan;
    }

    public void setDapan(String dapan) {
        this.dapan = dapan;
    }

    public String getGoiy() {
        return goiy;
    }

    public void setGoiy(String goiy) {
        this.goiy = goiy;
    }
}