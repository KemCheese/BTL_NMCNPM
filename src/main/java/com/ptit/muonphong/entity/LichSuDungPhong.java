package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

public class LichSuDungPhong implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private Phong phong;
    private DonMuonPhong donMuonPhong;

    public String getKhungGioText() {
        if (donMuonPhong == null
                || donMuonPhong.getGioBatDau() == null
                || donMuonPhong.getGioKetThuc() == null) {
            return "";
        }
        return donMuonPhong.getGioBatDau().format(TIME_FMT)
                + " - "
                + donMuonPhong.getGioKetThuc().format(TIME_FMT);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Phong getPhong() {
        return phong;
    }

    public void setPhong(Phong phong) {
        this.phong = phong;
    }

    public DonMuonPhong getDonMuonPhong() {
        return donMuonPhong;
    }

    public void setDonMuonPhong(DonMuonPhong donMuonPhong) {
        this.donMuonPhong = donMuonPhong;
    }
}
