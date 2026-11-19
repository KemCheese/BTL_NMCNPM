package com.ptit.muonphong.entity;

public enum TrangThaiDon {
    ChoDuyetCap1("Chờ duyệt cấp 1"),
    ChoDuyetCap2("Chờ duyệt cấp 2"),
    DaPheDuyet("Đã phê duyệt"),
    TuChoiCap1("Từ chối cấp 1"),
    TuChoiCap2("Từ chối cấp 2"),
    DaHuy("Đã hủy"),
    HoanThanh("Hoàn thành");

    private final String tenHienThi;

    TrangThaiDon(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getMoTa() {
        return tenHienThi;
    }

    public static TrangThaiDon fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (TrangThaiDon item : values()) {
            if (item.name().equals(code.trim())) {
                return item;
            }
        }
        return null;
    }
}
