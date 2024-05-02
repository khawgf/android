package com.example.duolingoapp.premium;

import java.io.Serializable;

public class Premium implements Serializable {
    private String idUser;
    private int idBo;

    public Premium(String idUser, int idBo) {
        this.idUser = idUser;
        this.idBo = idBo;
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
}