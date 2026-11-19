package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DonMuonPhong implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String maDon;
    private LocalDate ngayTao;
    private LocalDate ngaySuDung;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String loaiPhongMongMuon;
    private String thietBiYeuCau;
    private TrangThaiDon trangThai;
    private String tenSuKien;
    private String mucDich;
    private String noiDung;
    private Integer soNguoiThamDu;
    private int sinhVienId;
    private int donViId;
    private Integer phongMongMuonId;
    private SinhVien sinhVien;
    private DonVi donVi;
    private Phong phongMongMuon;
    private SuKien suKien;
    private List<LuotDuyet> luotDuyets = new ArrayList<>();
    private List<LichSuDon> lichSuDons = new ArrayList<>();

    public boolean kiemTraHopLe() {
        if (ngaySuDung == null || gioBatDau == null || gioKetThuc == null || !gioKetThuc.isAfter(gioBatDau)) {
            return false;
        }
        if (loaiPhongMongMuon == null || loaiPhongMongMuon.isBlank()) {
            return false;
        }
        String ten = suKien != null ? suKien.getTenSuKien() : tenSuKien;
        String md = suKien != null ? suKien.getMucDich() : mucDich;
        String nd = suKien != null ? suKien.getNoiDung() : noiDung;
        Integer soNguoi = suKien != null ? suKien.getSoNguoiThamDu() : soNguoiThamDu;
        return ten != null && !ten.isBlank()
                && md != null && !md.isBlank()
                && nd != null && !nd.isBlank()
                && soNguoi != null && soNguoi > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaDon() {
        return maDon;
    }

    public void setMaDon(String maDon) {
        this.maDon = maDon;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDate getNgaySuDung() {
        return ngaySuDung;
    }

    public void setNgaySuDung(LocalDate ngaySuDung) {
        this.ngaySuDung = ngaySuDung;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getLoaiPhongMongMuon() {
        return loaiPhongMongMuon;
    }

    public void setLoaiPhongMongMuon(String loaiPhongMongMuon) {
        this.loaiPhongMongMuon = loaiPhongMongMuon;
    }

    public String getThietBiYeuCau() {
        return thietBiYeuCau;
    }

    public void setThietBiYeuCau(String thietBiYeuCau) {
        this.thietBiYeuCau = thietBiYeuCau;
    }

    public TrangThaiDon getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDon trangThai) {
        this.trangThai = trangThai;
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

    public Integer getSoNguoiThamDu() {
        return soNguoiThamDu;
    }

    public void setSoNguoiThamDu(Integer soNguoiThamDu) {
        this.soNguoiThamDu = soNguoiThamDu;
    }

    public int getSinhVienId() {
        return sinhVienId;
    }

    public void setSinhVienId(int sinhVienId) {
        this.sinhVienId = sinhVienId;
    }

    public int getDonViId() {
        return donViId;
    }

    public void setDonViId(int donViId) {
        this.donViId = donViId;
    }

    public Integer getPhongMongMuonId() {
        return phongMongMuonId;
    }

    public void setPhongMongMuonId(Integer phongMongMuonId) {
        this.phongMongMuonId = phongMongMuonId;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public DonVi getDonVi() {
        return donVi;
    }

    public void setDonVi(DonVi donVi) {
        this.donVi = donVi;
    }

    public Phong getPhongMongMuon() {
        return phongMongMuon;
    }

    public void setPhongMongMuon(Phong phongMongMuon) {
        this.phongMongMuon = phongMongMuon;
    }

    public SuKien getSuKien() {
        return suKien;
    }

    public void setSuKien(SuKien suKien) {
        this.suKien = suKien;
    }

    public List<LuotDuyet> getLuotDuyets() {
        return luotDuyets;
    }

    public void setLuotDuyets(List<LuotDuyet> luotDuyets) {
        this.luotDuyets = luotDuyets;
    }

    public List<LichSuDon> getLichSuDons() {
        return lichSuDons;
    }

    public void setLichSuDons(List<LichSuDon> lichSuDons) {
        this.lichSuDons = lichSuDons;
    }
}
