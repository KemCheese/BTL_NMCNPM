package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LuotDuyet implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int capDuyet;
    private String ketQua;
    private String lyDo;
    private LocalDateTime thoiDiem;
    private DonMuonPhong donMuonPhong;
    private NguoiDung nguoiDuyet;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCapDuyet() {
        return capDuyet;
    }

    public void setCapDuyet(int capDuyet) {
        this.capDuyet = capDuyet;
    }

    public String getKetQua() {
        return ketQua;
    }

    public void setKetQua(String ketQua) {
        this.ketQua = ketQua;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
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

    public NguoiDung getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(NguoiDung nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }
}
