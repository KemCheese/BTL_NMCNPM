package com.ptit.muonphong.entity;

public class CanBoDoan extends NguoiDung {
    private static final long serialVersionUID = 1L;

    private String viTriCongViec;

    @Override
    public String getVaiTroCode() {
        return "CAN_BO_DOAN";
    }

    @Override
    public String getVaiTroDisplay() {
        return "CÁN BỘ ĐOÀN";
    }

    public String getViTriCongViec() {
        return viTriCongViec;
    }

    public void setViTriCongViec(String viTriCongViec) {
        this.viTriCongViec = viTriCongViec;
    }
}

