package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.DonMuonPhongDAO;
import com.ptit.muonphong.dao.LichSuDonDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class NopDonService {

    private final PhongDAO phongDAO;
    private final DonMuonPhongDAO donMuonPhongDAO;
    private final LichSuDonDAO lichSuDonDAO;

    public NopDonService(PhongDAO phongDAO, DonMuonPhongDAO donMuonPhongDAO, LichSuDonDAO lichSuDonDAO) {
        this.phongDAO = phongDAO;
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.lichSuDonDAO = lichSuDonDAO;
    }

    public List<String> getDanhSachLoaiPhong() {
        return phongDAO.findAllLoaiPhong();
    }

    public Phong getPhongById(int idPhong) {
        return phongDAO.getChiTiet(idPhong);
    }

    public DonMuonPhong getDonMauDeTaoLai(int idDonCu, NguoiDung nguoiDung) {
        if (idDonCu <= 0 || !(nguoiDung instanceof SinhVien sv)) {
            return null;
        }
        DonMuonPhong donCu = donMuonPhongDAO.getChiTiet(idDonCu);
        if (donCu == null || donCu.getSinhVien() == null || donCu.getSinhVien().getId() != sv.getId()) {
            return null;
        }
        return donCu;
    }

    public String kiemTraThoiGianTimKiem(LocalDate ngaySuDung, LocalTime gioBatDau, LocalTime gioKetThuc, Integer sucChua) {
        if (ngaySuDung == null || gioBatDau == null || gioKetThuc == null) {
            return "Vui lòng nhập đầy đủ ngày mượn, giờ bắt đầu và giờ kết thúc.";
        }
        if (!gioKetThuc.isAfter(gioBatDau)) {
            return "Giờ kết thúc phải sau giờ bắt đầu.";
        }
        if (gioBatDau.isBefore(DonMuonPhong.GIO_MO_CUA) || gioKetThuc.isAfter(DonMuonPhong.GIO_DONG_CUA)) {
            return "Khung giờ sử dụng phải nằm trong giờ hoạt động của Học viện (07:00 - 22:00).";
        }
        LocalDateTime thoiDiemBatDau = LocalDateTime.of(ngaySuDung, gioBatDau);
        if (thoiDiemBatDau.isBefore(LocalDateTime.now().plusHours(DonMuonPhong.SO_GIO_DAT_TRUOC_TOI_THIEU))) {
            return "Thời gian sử dụng phải đặt trước tối thiểu 48 giờ so với thời điểm hiện tại.";
        }
        if (sucChua != null && sucChua <= 0) {
            return "Sức chứa tối thiểu phải lớn hơn 0.";
        }
        return null;
    }

    public TimPhongResult timPhongTrong(LocalDate ngaySuDung, LocalTime gioBatDau, LocalTime gioKetThuc,
                                        String loaiPhong, Integer sucChua) {
        String error = kiemTraThoiGianTimKiem(ngaySuDung, gioBatDau, gioKetThuc, sucChua);
        if (error != null) {
            return new TimPhongResult(false, error, List.of());
        }
        List<Phong> dsPhong = phongDAO.timPhongTrong(ngaySuDung, gioBatDau, gioKetThuc, loaiPhong, sucChua, null);
        return new TimPhongResult(true, null, dsPhong);
    }

    @Transactional
    public KetQuaNopDon guiDon(NopDonCommand command, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || sv.getDonVi() == null) {
            return KetQuaNopDon.thatBai("Chỉ tài khoản sinh viên thuộc đơn vị/CLB mới được phép nộp đơn mượn phòng.");
        }
        if (command == null) {
            return KetQuaNopDon.thatBai("Dữ liệu đơn mượn phòng không hợp lệ.");
        }

        Phong phongMongMuon = null;
        if (command.getPhongId() != null && command.getPhongId() > 0) {
            phongMongMuon = phongDAO.getChiTiet(command.getPhongId());
            if (phongMongMuon == null) {
                return KetQuaNopDon.thatBai("Không tìm thấy phòng mong muốn đã chọn.");
            }
            if (!phongMongMuon.isDangHoatDong()) {
                return KetQuaNopDon.thatBai("Phòng đã chọn hiện không ở trạng thái đang hoạt động.");
            }
        }

        String loaiPhong = command.getLoaiPhongMongMuon();
        if ((loaiPhong == null || loaiPhong.isBlank()) && phongMongMuon != null) {
            loaiPhong = phongMongMuon.getLoaiPhong();
        }

        DonMuonPhong don = new DonMuonPhong();
        don.setNgayTao(LocalDate.now());
        don.setNgaySuDung(command.getNgaySuDung());
        don.setGioBatDau(command.getGioBatDau());
        don.setGioKetThuc(command.getGioKetThuc());
        don.setLoaiPhongMongMuon(loaiPhong);
        don.setThietBiYeuCau(command.getThietBiYeuCau());
        don.setTrangThai(TrangThaiDon.ChoDuyetCap1);
        don.setTenSuKien(command.getTenSuKien());
        don.setMucDich(command.getMucDich());
        don.setNoiDung(command.getNoiDung());
        don.setSoNguoiThamDu(command.getSoNguoiThamDu() != null ? command.getSoNguoiThamDu() : 0);
        don.setSinhVien(sv);
        don.setDonVi(sv.getDonVi());
        don.setPhongMongMuon(phongMongMuon);

        String validationError = don.kiemTraChiTietHopLe(LocalDateTime.now());
        if (validationError != null) {
            return KetQuaNopDon.thatBai(validationError);
        }

        if (phongMongMuon != null) {
            Integer sucChuaPhong = phongMongMuon.getSucChuaToiDa();
            if (sucChuaPhong == null || sucChuaPhong < don.getSoNguoiThamDu()) {
                return KetQuaNopDon.thatBai("Sức chứa tối đa của phòng " + phongMongMuon.getMaPhong()
                        + " (" + sucChuaPhong + " chỗ) không đáp ứng số người tham dự ("
                        + don.getSoNguoiThamDu() + ").");
            }
            if (donMuonPhongDAO.kiemTraTrungLich(don, phongMongMuon)) {
                return KetQuaNopDon.thatBai("Phòng " + phongMongMuon.getMaPhong()
                        + " đã có lịch sử dụng trùng với khung giờ đã chọn.");
            }
        } else {
            if (donMuonPhongDAO.kiemTraTrungLich(don, null)) {
                return KetQuaNopDon.thatBai("Không còn phòng " + don.getLoaiPhongMongMuon()
                        + " phù hợp sức chứa " + don.getSoNguoiThamDu()
                        + " chỗ còn trống trong khung giờ đã chọn.");
            }
        }

        DonMuonPhong daLuu = donMuonPhongDAO.taoDon(don);

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(null);
        lichSuDon.setTrangThaiMoi(TrangThaiDon.ChoDuyetCap1);
        String ghiChu = command.getGhiChu();
        lichSuDon.setGhiChu(ghiChu != null && !ghiChu.isBlank()
                ? "Tạo đơn: " + ghiChu.trim()
                : "Tạo đơn mượn phòng");
        lichSuDon.setThoiDiem(LocalDateTime.now());
        lichSuDon.setDonMuonPhong(daLuu);
        lichSuDon.setNguoiThucHien(sv);
        lichSuDonDAO.ghiThayDoi(lichSuDon);

        return KetQuaNopDon.thanhCong(daLuu);
    }

    public static class TimPhongResult {
        private final boolean hopLe;
        private final String errorMessage;
        private final List<Phong> dsPhongTrong;

        public TimPhongResult(boolean hopLe, String errorMessage, List<Phong> dsPhongTrong) {
            this.hopLe = hopLe;
            this.errorMessage = errorMessage;
            this.dsPhongTrong = dsPhongTrong != null ? dsPhongTrong : List.of();
        }

        public boolean isHopLe() {
            return hopLe;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<Phong> getDsPhongTrong() {
            return dsPhongTrong;
        }
    }

    public static class NopDonCommand {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate ngaySuDung;
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime gioBatDau;
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime gioKetThuc;
        private String loaiPhongMongMuon;
        private Integer sucChuaToiThieu;
        private Integer phongId;
        private String tenSuKien;
        private Integer soNguoiThamDu;
        private String thietBiYeuCau;
        private String mucDich;
        private String noiDung;
        private String ghiChu;

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

        public Integer getSucChuaToiThieu() {
            return sucChuaToiThieu;
        }

        public void setSucChuaToiThieu(Integer sucChuaToiThieu) {
            this.sucChuaToiThieu = sucChuaToiThieu;
        }

        public Integer getPhongId() {
            return phongId;
        }

        public void setPhongId(Integer phongId) {
            this.phongId = phongId;
        }

        public String getTenSuKien() {
            return tenSuKien;
        }

        public void setTenSuKien(String tenSuKien) {
            this.tenSuKien = tenSuKien;
        }

        public Integer getSoNguoiThamDu() {
            return soNguoiThamDu;
        }

        public void setSoNguoiThamDu(Integer soNguoiThamDu) {
            this.soNguoiThamDu = soNguoiThamDu;
        }

        public String getThietBiYeuCau() {
            return thietBiYeuCau;
        }

        public void setThietBiYeuCau(String thietBiYeuCau) {
            this.thietBiYeuCau = thietBiYeuCau;
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

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }
    }

    public static class KetQuaNopDon {
        private final boolean thanhCong;
        private final String errorMessage;
        private final DonMuonPhong donDaLuu;

        private KetQuaNopDon(boolean thanhCong, String errorMessage, DonMuonPhong donDaLuu) {
            this.thanhCong = thanhCong;
            this.errorMessage = errorMessage;
            this.donDaLuu = donDaLuu;
        }

        public static KetQuaNopDon thanhCong(DonMuonPhong donDaLuu) {
            return new KetQuaNopDon(true, null, donDaLuu);
        }

        public static KetQuaNopDon thatBai(String errorMessage) {
            return new KetQuaNopDon(false, errorMessage, null);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public DonMuonPhong getDonDaLuu() {
            return donDaLuu;
        }
    }
}
