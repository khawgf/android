package com.example.duolingoapp.bocauhoi;

import java.io.Serializable;

public class QuestionList implements Serializable {
    private int idBo;
    private int stt;
    private String tenBo_ENG;
    private String tenBo_VIE;
    private byte[] img;

    public QuestionList(int idBo, int stt, String tenBo_ENG, String tenBo_VIE, byte[] img) {
        this.idBo = idBo;
        this.stt = stt;
        this.tenBo_VIE = tenBo_VIE;
        this.tenBo_ENG = tenBo_ENG;
        this.img = img;
    }

    public int getIdBo() {
        return idBo;
    }

    public void setIdBo(int idBo) {
        this.idBo = idBo;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public String getTenBo_VIE() {
        return tenBo_VIE;
    }

    public void setTenBo_VIE(String tenBo_VIE) {
        this.tenBo_VIE = tenBo_VIE;
    }

    public String getTenBo_ENG() {
        return tenBo_ENG;
    }

    public void setTenBo_ENG(String tenBo_ENG) {
        this.tenBo_ENG = tenBo_ENG;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }
}
