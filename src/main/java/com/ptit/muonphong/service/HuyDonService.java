package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class HuyDonService {

    private final DonMuonPhongDAO donMuonPhongDAO;
    private final LichSuDungPhongDAO lichSuDungPhongDAO;
    private final LichSuDonDAO lichSuDonDAO;
    private final Clock clock;

    @Autowired
    public HuyDonService(DonMuonPhongDAO donMuonPhongDAO,
            LichSuDungPhongDAO lichSuDungPhongDAO,
            LichSuDonDAO lichSuDonDAO) {
        this(donMuonPhongDAO, lichSuDungPhongDAO, lichSuDonDAO, Clock.systemDefaultZone());
    }

    public HuyDonService(DonMuonPhongDAO donMuonPhongDAO,
            LichSuDungPhongDAO lichSuDungPhongDAO,
            LichSuDonDAO lichSuDonDAO,
            Clock clock) {
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.lichSuDungPhongDAO = lichSuDungPhongDAO;
        this.lichSuDonDAO = lichSuDonDAO;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    public ThongTinHuyDonResult getChiTietDonDeHuy(int idDon, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || idDon <= 0) {
            return ThongTinHuyDonResult
                    .khongChoPhep("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền hủy đơn này.");
        }
        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (!laChuDon(don, sv)) {
            return ThongTinHuyDonResult
                    .khongChoPhep("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền hủy đơn này.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!don.coTheHuy(now)) {
            return ThongTinHuyDonResult.khongChoPhep(thongBaoKhongDuDieuKienHuy(don));
        }
        Phong phongBoTri = lichSuDungPhongDAO.getPhongBoTriTheoDon(idDon);
        return ThongTinHuyDonResult.choPhep(don, phongBoTri);
    }

    @Transactional
    public KetQuaHuyDon huyDon(int idDon, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || idDon <= 0) {
            return KetQuaHuyDon.thatBai("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền hủy đơn này.");
        }
        DonMuonPhong don = donMuonPhongDAO.lockDonById(idDon);
        if (!laChuDon(don, sv)) {
            return KetQuaHuyDon.thatBai("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền hủy đơn này.");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!don.coTheHuy(now)) {
            return KetQuaHuyDon.thatBai(thongBaoKhongDuDieuKienHuy(don));
        }

        TrangThaiDon trangThaiCu = don.getTrangThai();
        int updated = donMuonPhongDAO.huyDon(idDon);
        if (updated <= 0) {
            return KetQuaHuyDon.thatBai("Không thể hủy đơn mượn phòng vào lúc này.");
        }

        Phong phongBoTri = lichSuDungPhongDAO.getPhongBoTriTheoDon(idDon);
        if (phongBoTri != null || trangThaiCu == TrangThaiDon.DaPheDuyet) {
            lichSuDungPhongDAO.giaiPhongKhungGio(idDon);
        }

        LichSuDon lichSu = new LichSuDon();
        lichSu.setDonMuonPhong(don);
        lichSu.setTrangThaiCu(trangThaiCu);
        lichSu.setTrangThaiMoi(TrangThaiDon.DaHuy);
        lichSu.setNguoiThucHien(sv);
        lichSu.setGhiChu("Người mượn hủy đơn");
        lichSu.setThoiDiem(now);
        lichSuDonDAO.ghiThayDoi(lichSu);

        don.setTrangThai(TrangThaiDon.DaHuy);
        return KetQuaHuyDon.thanhCong(don);
    }

    private boolean laChuDon(DonMuonPhong don, SinhVien sv) {
        if (don == null || sv == null) {
            return false;
        }
        int ownerId = don.getSinhVien() != null ? don.getSinhVien().getId() : don.getSinhVienId();
        return ownerId == sv.getId();
    }

    private String thongBaoKhongDuDieuKienHuy(DonMuonPhong don) {
        if (don.getTrangThai() == TrangThaiDon.DaPheDuyet) {
            return "Không thể hủy đơn đã phê duyệt khi đã đến hoặc qua giờ sử dụng.";
        }
        return "Chỉ được hủy đơn ở trạng thái Chờ duyệt cấp 1, Chờ duyệt cấp 2 hoặc Đã phê duyệt chưa đến giờ sử dụng.";
    }

    public static class ThongTinHuyDonResult {
        private final boolean choPhep;
        private final String errorMessage;
        private final DonMuonPhong don;
        private final Phong phongBoTri;

        private ThongTinHuyDonResult(boolean choPhep, String errorMessage, DonMuonPhong don, Phong phongBoTri) {
            this.choPhep = choPhep;
            this.errorMessage = errorMessage;
            this.don = don;
            this.phongBoTri = phongBoTri;
        }

        public static ThongTinHuyDonResult choPhep(DonMuonPhong don, Phong phongBoTri) {
            return new ThongTinHuyDonResult(true, null, don, phongBoTri);
        }

        public static ThongTinHuyDonResult khongChoPhep(String errorMessage) {
            return new ThongTinHuyDonResult(false, errorMessage, null, null);
        }

        public boolean isChoPhep() {
            return choPhep;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public DonMuonPhong getDon() {
            return don;
        }

        public Phong getPhongBoTri() {
            return phongBoTri;
        }
    }

    public static class KetQuaHuyDon {
        private final boolean thanhCong;
        private final String errorMessage;
        private final DonMuonPhong don;

        private KetQuaHuyDon(boolean thanhCong, String errorMessage, DonMuonPhong don) {
            this.thanhCong = thanhCong;
            this.errorMessage = errorMessage;
            this.don = don;
        }

        public static KetQuaHuyDon thanhCong(DonMuonPhong don) {
            return new KetQuaHuyDon(true, null, don);
        }

        public static KetQuaHuyDon thatBai(String errorMessage) {
            return new KetQuaHuyDon(false, errorMessage, null);
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public DonMuonPhong getDon() {
            return don;
        }
    }
}
