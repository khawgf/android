package com.example.duolingoapp.sapxepcau;

import java.io.Serializable;

public class ArrangeSentences  implements Serializable {
    private int idcau;
    private int idbo;
    private String dapan;
    private  String part1;
    private  String part2;
    private  String part3;
    private  String part4;

    public ArrangeSentences(int idcau, int idbo, String dapan, String part1, String part2, String part3, String part4) {
        this.idcau = idcau;
        this.idbo = idbo;
        this.dapan = dapan;
        this.part1 = part1;
        this.part2 = part2;
        this.part3 = part3;
        this.part4 = part4;
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

    public String getDapan() {
        return dapan;
    }

    public void setDapan(String dapan) {
        this.dapan = dapan;
    }

    public String getPart1() {
        return part1;
    }

    public void setPart1(String part1) {
        this.part1 = part1;
    }

    public String getPart2() {
        return part2;
    }

    public void setPart2(String part2) {
        this.part2 = part2;
    }

    public String getPart3() {
        return part3;
    }

    public void setPart3(String part3) {
        this.part3 = part3;
    }

    public String getPart4() {
        return part4;
    }

    public void setPart4(String part4) {
        this.part4 = part4;
    }
}
