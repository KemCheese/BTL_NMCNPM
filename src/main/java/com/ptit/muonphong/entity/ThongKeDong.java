package com.ptit.muonphong.entity;

import java.io.Serializable;

public class ThongKeDong implements Serializable {
    private static final long serialVersionUID = 1L;

    private Phong phong;
    private int soLuot;
    private double tongGio;
    private double tyLeSuDung;

    public ThongKeDong() {
    }

    public ThongKeDong(Phong phong, int soLuot, double tongGio, double tyLeSuDung) {
        this.phong = phong;
        this.soLuot = soLuot;
        this.tongGio = tongGio;
        this.tyLeSuDung = tyLeSuDung;
    }

    public Phong getPhong() {
        return phong;
    }

    public void setPhong(Phong phong) {
        this.phong = phong;
    }

    public int getSoLuot() {
        return soLuot;
    }

    public void setSoLuot(int soLuot) {
        this.soLuot = soLuot;
    }

    public double getTongGio() {
        return tongGio;
    }

    public void setTongGio(double tongGio) {
        this.tongGio = tongGio;
    }

    public double getTyLeSuDung() {
        return tyLeSuDung;
    }

    public void setTyLeSuDung(double tyLeSuDung) {
        this.tyLeSuDung = tyLeSuDung;
    }
}
