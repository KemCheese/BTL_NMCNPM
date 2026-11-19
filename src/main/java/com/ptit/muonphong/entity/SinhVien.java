package com.ptit.muonphong.entity;

public class SinhVien extends NguoiDung {
    private static final long serialVersionUID = 1L;

    private String maSinhVien;
    private int khoaHoc;
    private String nganhHoc;
    private DonVi donVi;

    @Override
    public String getVaiTroCode() {
        return "NGUOI_MUON";
    }

    @Override
    public String getVaiTroDisplay() {
        return "NGƯỜI MƯỢN";
    }

    public String getMaSinhVien() {
        return maSinhVien;
    }

    public void setMaSinhVien(String maSinhVien) {
        this.maSinhVien = maSinhVien;
    }

    public int getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(int khoaHoc) {
        this.khoaHoc = khoaHoc;
    }

    public String getNganhHoc() {
        return nganhHoc;
    }

    public void setNganhHoc(String nganhHoc) {
        this.nganhHoc = nganhHoc;
    }

    public DonVi getDonVi() {
        return donVi;
    }

    public void setDonVi(DonVi donVi) {
        this.donVi = donVi;
    }
}
