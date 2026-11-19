package com.ptit.muonphong.entity;

public class CanBoCSVC extends NguoiDung {
    private static final long serialVersionUID = 1L;

    private String viTriCongViec;

    @Override
    public String getVaiTroCode() {
        return "CAN_BO_CSVC";
    }

    @Override
    public String getVaiTroDisplay() {
        return "CÁN BỘ TRUNG TÂM";
    }

    public String getViTriCongViec() {
        return viTriCongViec;
    }

    public void setViTriCongViec(String viTriCongViec) {
        this.viTriCongViec = viTriCongViec;
    }
}

