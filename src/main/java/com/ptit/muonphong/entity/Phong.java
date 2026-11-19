package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.util.List;

public class Phong implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TINH_TRANG_HOAT_DONG = "Đang hoạt động";
    public static final String TINH_TRANG_NGUNG = "Ngưng sử dụng";
    public static final List<String> LOAI_PHONG_HOP_LE = List.of(
            "Phòng học thường",
            "Hội trường",
            "Phòng máy");

    private int id;
    private String maPhong;
    private String tenPhong;
    private String toaNha;
    private Integer tang;
    private Integer sucChuaToiDa;
    private String loaiPhong;
    private String trangThietBi;
    private String tinhTrang;
    private String moTa;

    public boolean kiemTraHopLe() {
        if (isBlank(maPhong) || maPhong.trim().length() > 50) {
            return false;
        }
        if (isBlank(tenPhong) || tenPhong.trim().length() > 150) {
            return false;
        }
        if (isBlank(toaNha) || toaNha.trim().length() > 50) {
            return false;
        }
        if (tang == null || tang < 1) {
            return false;
        }
        if (sucChuaToiDa == null || sucChuaToiDa < 1) {
            return false;
        }
        if (isBlank(loaiPhong) || !LOAI_PHONG_HOP_LE.contains(loaiPhong.trim())) {
            return false;
        }
        return true;
    }

    public boolean isDangHoatDong() {
        return TINH_TRANG_HOAT_DONG.equals(tinhTrang);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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

    public Integer getTang() {
        return tang;
    }

    public void setTang(Integer tang) {
        this.tang = tang;
    }

    public Integer getSucChuaToiDa() {
        return sucChuaToiDa;
    }

    public void setSucChuaToiDa(Integer sucChuaToiDa) {
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
