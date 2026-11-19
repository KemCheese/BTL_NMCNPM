package com.ptit.muonphong.entity;

import java.io.Serializable;

public class Phong implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String maPhong;
    private String tenPhong;
    private String toaNha;
    private int tang;
    private int sucChuaToiDa;
    private String loaiPhong;
    private String trangThietBi;
    private String tinhTrang;
    private String moTa;

    public boolean isDangHoatDong() {
        return "Đang hoạt động".equalsIgnoreCase(tinhTrang);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getToaNha() {
        return toaNha;
    }

    public void setToaNha(String toaNha) {
        this.toaNha = toaNha;
    }

    public int getTang() {
        return tang;
    }

    public void setTang(int tang) {
        this.tang = tang;
    }

    public int getSucChuaToiDa() {
        return sucChuaToiDa;
    }

    public void setSucChuaToiDa(int sucChuaToiDa) {
        this.sucChuaToiDa = sucChuaToiDa;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public String getTrangThietBi() {
        return trangThietBi;
    }

    public void setTrangThietBi(String trangThietBi) {
        this.trangThietBi = trangThietBi;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
