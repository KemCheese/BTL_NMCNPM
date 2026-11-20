package com.ptit.muonphong.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DonMuonPhong implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final LocalTime GIO_MO_CUA = LocalTime.of(7, 0);
    public static final LocalTime GIO_DONG_CUA = LocalTime.of(22, 0);
    public static final long SO_GIO_DAT_TRUOC_TOI_THIEU = 48L;

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
    private LichSuDungPhong lichSuDungPhong;

    public boolean kiemTraHopLe() {
        return kiemTraHopLe(LocalDateTime.now());
    }

    public boolean kiemTraHopLe(LocalDateTime thoiDiemHienTai) {
        return kiemTraChiTietHopLe(thoiDiemHienTai) == null;
    }

    public String kiemTraChiTietHopLe(LocalDateTime thoiDiemHienTai) {
        if (ngaySuDung == null || gioBatDau == null || gioKetThuc == null) {
            return "Vui lòng nhập đầy đủ ngày sử dụng, giờ bắt đầu và giờ kết thúc.";
        }
        if (!gioKetThuc.isAfter(gioBatDau)) {
            return "Giờ kết thúc phải sau giờ bắt đầu.";
        }
        if (gioBatDau.isBefore(GIO_MO_CUA) || gioKetThuc.isAfter(GIO_DONG_CUA)) {
            return "Khung giờ sử dụng phải nằm trong giờ hoạt động của Học viện (07:00 - 22:00).";
        }
        LocalDateTime mocHienTai = thoiDiemHienTai != null ? thoiDiemHienTai : LocalDateTime.now();
        LocalDateTime thoiDiemBatDau = LocalDateTime.of(ngaySuDung, gioBatDau);
        if (thoiDiemBatDau.isBefore(mocHienTai.plusHours(SO_GIO_DAT_TRUOC_TOI_THIEU))) {
            return "Thời gian sử dụng phải đặt trước tối thiểu 48 giờ so với thời điểm gửi đơn.";
        }
        if (loaiPhongMongMuon == null || loaiPhongMongMuon.isBlank()) {
            return "Vui lòng chọn loại phòng mong muốn.";
        }
        String ten = suKien != null ? suKien.getTenSuKien() : tenSuKien;
        String md = suKien != null ? suKien.getMucDich() : mucDich;
        String nd = suKien != null ? suKien.getNoiDung() : noiDung;
        Integer soNguoi = suKien != null ? suKien.getSoNguoiThamDu() : soNguoiThamDu;
        if (ten == null || ten.isBlank()) {
            return "Vui lòng nhập tên sự kiện.";
        }
        if (soNguoi == null || soNguoi <= 0) {
            return "Số người tham dự phải lớn hơn 0.";
        }
        if (md == null || md.isBlank()) {
            return "Vui lòng nhập mục đích mượn phòng.";
        }
        if (nd == null || nd.isBlank()) {
            return "Vui lòng nhập nội dung hoạt động.";
        }
        if ((sinhVien == null && sinhVienId <= 0) || (donVi == null && donViId <= 0)) {
            return "Không xác định được thông tin sinh viên hoặc đơn vị của người mượn.";
        }
        return null;
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

    public LichSuDungPhong getLichSuDungPhong() {
        return lichSuDungPhong;
    }

    public void setLichSuDungPhong(LichSuDungPhong lichSuDungPhong) {
        this.lichSuDungPhong = lichSuDungPhong;
    }
}
