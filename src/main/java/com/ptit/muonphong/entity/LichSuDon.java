package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LichSuDon implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private TrangThaiDon trangThaiCu;
    private TrangThaiDon trangThaiMoi;
    private String ghiChu;
    private LocalDateTime thoiDiem;
    private DonMuonPhong donMuonPhong;
    private NguoiDung nguoiThucHien;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TrangThaiDon getTrangThaiCu() {
        return trangThaiCu;
    }

    public void setTrangThaiCu(TrangThaiDon trangThaiCu) {
        this.trangThaiCu = trangThaiCu;
    }

    public TrangThaiDon getTrangThaiMoi() {
        return trangThaiMoi;
    }

    public void setTrangThaiMoi(TrangThaiDon trangThaiMoi) {
        this.trangThaiMoi = trangThaiMoi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getThoiDiem() {
        return thoiDiem;
    }

    public void setThoiDiem(LocalDateTime thoiDiem) {
        this.thoiDiem = thoiDiem;
    }

    public DonMuonPhong getDonMuonPhong() {
        return donMuonPhong;
    }

    public void setDonMuonPhong(DonMuonPhong donMuonPhong) {
        this.donMuonPhong = donMuonPhong;
    }

    public NguoiDung getNguoiThucHien() {
        return nguoiThucHien;
    }

    public void setNguoiThucHien(NguoiDung nguoiThucHien) {
        this.nguoiThucHien = nguoiThucHien;
    }
}
