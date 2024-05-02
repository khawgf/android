package com.example.duolingoapp.trangthai;

import java.io.Serializable;

public class Status implements Serializable {
    private String idUser;
    private int idBo;
    private int idSkill;
    private int score;
    private String timedone;

    public Status() {
    }

    public Status(String idUser, int idBo, int idSkill, int score, String timedone) {
        this.idUser = idUser;
        this.idBo = idBo;
        this.idSkill = idSkill;
        this.score = score;
        this.timedone = timedone;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public int getIdBo() {
        return idBo;
    }

    public void setIdBo(int idBo) {
        this.idBo = idBo;
    }

    public int getIdSkill() {
        return idSkill;
    }

    public void setIdSkill(int idSkill) {
        this.idSkill = idSkill;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTimedone() {
        return timedone;
    }

    public void setTimedone(String timedone) {
        this.timedone = timedone;
    }
}