package com.ptit.muonphong.entity;

import java.io.Serializable;

public class SuKien implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String tenSuKien;
    private String mucDich;
    private String noiDung;
    private int soNguoiThamDu;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenSuKien() {
        return tenSuKien;
    }

    public void setTenSuKien(String tenSuKien) {
        this.tenSuKien = tenSuKien;
    }

    public String getMucDich() {
        return mucDich;
    }

    public void setMucDich(String mucDich) {
        this.mucDich = mucDich;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public int getSoNguoiThamDu() {
        return soNguoiThamDu;
    }

    public void setSoNguoiThamDu(int soNguoiThamDu) {
        this.soNguoiThamDu = soNguoiThamDu;
    }
}
