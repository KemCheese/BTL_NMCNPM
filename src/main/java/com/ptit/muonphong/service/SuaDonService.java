package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class SuaDonService {

    private final DonMuonPhongDAO donMuonPhongDAO;
    private final PhongDAO phongDAO;
    private final LichSuDonDAO lichSuDonDAO;

    public SuaDonService(DonMuonPhongDAO donMuonPhongDAO, PhongDAO phongDAO, LichSuDonDAO lichSuDonDAO) {
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.phongDAO = phongDAO;
        this.lichSuDonDAO = lichSuDonDAO;
    }

    public List<String> getDanhSachLoaiPhong() {
        return phongDAO.findAllLoaiPhong();
    }

    public MoFormSuaResult moFormSua(int idDon, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || idDon <= 0) {
            return MoFormSuaResult.khongChoPhep("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền chỉnh sửa đơn này.");
        }
        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null || don.getSinhVien() == null || don.getSinhVien().getId() != sv.getId()) {
            return MoFormSuaResult.khongChoPhep("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền chỉnh sửa đơn này.");
        }
        if (don.getTrangThai() != TrangThaiDon.ChoDuyetCap1) {
            return MoFormSuaResult.khongChoPhep("Chỉ được chỉnh sửa đơn khi đơn đang ở trạng thái Chờ duyệt cấp 1.");
        }
        return MoFormSuaResult.choPhep(don, SuaDonCommand.fromDon(don));
    }

    @Transactional
    public KetQuaSuaDon capNhatDon(int idDon, SuaDonCommand command, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || idDon <= 0) {
            return KetQuaSuaDon.thatBai("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền chỉnh sửa đơn này.");
        }
        if (command == null) {
            return KetQuaSuaDon.thatBai("Dữ liệu chỉnh sửa đơn không hợp lệ.");
        }
        DonMuonPhong donGoc = donMuonPhongDAO.getChiTiet(idDon);
        if (donGoc == null || donGoc.getSinhVien() == null || donGoc.getSinhVien().getId() != sv.getId()) {
            return KetQuaSuaDon.thatBai("Không tìm thấy đơn mượn phòng hoặc bạn không có quyền chỉnh sửa đơn này.");
        }
        if (donGoc.getTrangThai() != TrangThaiDon.ChoDuyetCap1) {
            return KetQuaSuaDon.thatBai("Chỉ được chỉnh sửa đơn khi đơn đang ở trạng thái Chờ duyệt cấp 1.");
        }

        DonMuonPhong donCapNhat = taoBanCapNhat(donGoc, command);
        String validationError = donCapNhat.kiemTraChiTietHopLe(LocalDateTime.now());
        if (validationError != null) {
            return KetQuaSuaDon.thatBai(validationError);
        }

        PhongEvaluation phongEval = danhGiaPhongMongMuon(donGoc, donCapNhat);
        if (phongEval.errorMessage != null) {
            return KetQuaSuaDon.thatBai(phongEval.errorMessage);
        }

        donCapNhat.setPhongMongMuon(phongEval.phongMongMuon);
        donCapNhat.setPhongMongMuonId(phongEval.phongMongMuon != null ? phongEval.phongMongMuon.getId() : null);

        int updated = donMuonPhongDAO.suaDon(donCapNhat);
        if (updated <= 0) {
            return KetQuaSuaDon.thatBai("Không thể cập nhật đơn vì đơn không còn ở trạng thái Chờ duyệt cấp 1.");
        }

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(TrangThaiDon.ChoDuyetCap1);
        lichSuDon.setTrangThaiMoi(TrangThaiDon.ChoDuyetCap1);
        lichSuDon.setGhiChu(xayDungGhiChu(command.getGhiChu(), phongEval.maPhongCuDaBo));
        lichSuDon.setThoiDiem(LocalDateTime.now());
        lichSuDon.setDonMuonPhong(donCapNhat);
        lichSuDon.setNguoiThucHien(sv);
        lichSuDonDAO.ghiThayDoi(lichSuDon);

        return KetQuaSuaDon.thanhCong(donCapNhat, phongEval.maPhongCuDaBo != null);
    }

    private DonMuonPhong taoBanCapNhat(DonMuonPhong donGoc, SuaDonCommand command) {
        DonMuonPhong don = new DonMuonPhong();
        don.setId(donGoc.getId());
        don.setMaDon(donGoc.getMaDon());
        don.setNgayTao(donGoc.getNgayTao());
        don.setTrangThai(TrangThaiDon.ChoDuyetCap1);
        don.setSinhVien(donGoc.getSinhVien());
        don.setSinhVienId(donGoc.getSinhVienId());
        don.setDonVi(donGoc.getDonVi());
        don.setDonViId(donGoc.getDonViId());
        don.setNgaySuDung(command.getNgaySuDung());
        don.setGioBatDau(command.getGioBatDau());
        don.setGioKetThuc(command.getGioKetThuc());
        don.setLoaiPhongMongMuon(command.getLoaiPhongMongMuon());
        don.setTenSuKien(command.getTenSuKien());
        don.setSoNguoiThamDu(command.getSoNguoiThamDu() != null ? command.getSoNguoiThamDu() : 0);
        don.setThietBiYeuCau(command.getThietBiYeuCau());
        don.setMucDich(command.getMucDich());
        don.setNoiDung(command.getNoiDung());
        return don;
    }

    private PhongEvaluation danhGiaPhongMongMuon(DonMuonPhong donGoc, DonMuonPhong donCapNhat) {
        Phong phongGoc = donGoc.getPhongMongMuon();
        String maPhongCuDaBo = null;

        if (phongGoc != null && phongGoc.getId() > 0) {
            Phong phongKiemTra = phongDAO.getChiTiet(phongGoc.getId());
            if (phongKiemTra == null) {
                phongKiemTra = phongGoc;
            }
            if (isPhongConPhuHop(phongKiemTra, donCapNhat)
                    && !donMuonPhongDAO.kiemTraTrungLich(donCapNhat, phongKiemTra)) {
                return new PhongEvaluation(phongKiemTra, null, null);
            }
            maPhongCuDaBo = phongKiemTra.getMaPhong() != null ? phongKiemTra.getMaPhong() : String.valueOf(phongKiemTra.getId());
        }

        if (donMuonPhongDAO.kiemTraTrungLich(donCapNhat, null)) {
            return new PhongEvaluation(null, null,
                    "Không còn phòng " + donCapNhat.getLoaiPhongMongMuon()
                            + " phù hợp sức chứa " + donCapNhat.getSoNguoiThamDu()
                            + " chỗ còn trống trong khung giờ đã chọn.");
        }
        return new PhongEvaluation(null, maPhongCuDaBo, null);
    }

    private boolean isPhongConPhuHop(Phong phong, DonMuonPhong don) {
        if (phong == null || !phong.isDangHoatDong()) {
            return false;
        }
        String loaiMoi = don.getLoaiPhongMongMuon() != null ? don.getLoaiPhongMongMuon().trim() : "";
        if (!loaiMoi.equalsIgnoreCase(phong.getLoaiPhong() != null ? phong.getLoaiPhong().trim() : "")) {
            return false;
        }
        Integer sucChua = phong.getSucChuaToiDa();
        return sucChua != null && don.getSoNguoiThamDu() != null && sucChua >= don.getSoNguoiThamDu();
    }

    private String xayDungGhiChu(String ghiChuForm, String maPhongCuDaBo) {
        StringBuilder sb = new StringBuilder("Sửa đơn");
        if (ghiChuForm != null && !ghiChuForm.isBlank()) {
            sb.append(": ").append(ghiChuForm.trim());
        }
        if (maPhongCuDaBo != null && !maPhongCuDaBo.isBlank()) {
            sb.append(" (chuyển phòng mong muốn từ ").append(maPhongCuDaBo).append(" sang Trung tâm tự bố trí)");
        }
        return sb.toString();
    }

    private static class PhongEvaluation {
        private final Phong phongMongMuon;
        private final String maPhongCuDaBo;
        private final String errorMessage;

        private PhongEvaluation(Phong phongMongMuon, String maPhongCuDaBo, String errorMessage) {
            this.phongMongMuon = phongMongMuon;
            this.maPhongCuDaBo = maPhongCuDaBo;
            this.errorMessage = errorMessage;
        }
    }

    public static class SuaDonCommand {
        private String maDon;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate ngaySuDung;

        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime gioBatDau;

        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime gioKetThuc;

        private String loaiPhongMongMuon;
        private String tenSuKien;
        private Integer soNguoiThamDu;
        private String thietBiYeuCau;
        private String mucDich;
        private String noiDung;
        private String ghiChu;

        public static SuaDonCommand fromDon(DonMuonPhong don) {
            SuaDonCommand cmd = new SuaDonCommand();
            cmd.setMaDon(don.getMaDon());
            cmd.setNgaySuDung(don.getNgaySuDung());
            cmd.setGioBatDau(don.getGioBatDau());
            cmd.setGioKetThuc(don.getGioKetThuc());
            cmd.setLoaiPhongMongMuon(don.getLoaiPhongMongMuon());
            cmd.setTenSuKien(don.getTenSuKien());
            cmd.setSoNguoiThamDu(don.getSoNguoiThamDu());
            cmd.setThietBiYeuCau(don.getThietBiYeuCau());
            cmd.setMucDich(don.getMucDich());
            cmd.setNoiDung(don.getNoiDung());
            return cmd;
        }

        public String getMaDon() {
            return maDon;
        }

        public void setMaDon(String maDon) {
            this.maDon = maDon;
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

    public static class MoFormSuaResult {
        private final boolean choPhep;
        private final String errorMessage;
        private final DonMuonPhong don;
        private final SuaDonCommand command;

        private MoFormSuaResult(boolean choPhep, String errorMessage, DonMuonPhong don, SuaDonCommand command) {
            this.choPhep = choPhep;
            this.errorMessage = errorMessage;
            this.don = don;
            this.command = command;
        }

        public static MoFormSuaResult choPhep(DonMuonPhong don, SuaDonCommand command) {
            return new MoFormSuaResult(true, null, don, command);
        }

        public static MoFormSuaResult khongChoPhep(String errorMessage) {
            return new MoFormSuaResult(false, errorMessage, null, null);
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

        public SuaDonCommand getCommand() {
            return command;
        }
    }

    public static class KetQuaSuaDon {
        private final boolean thanhCong;
        private final String errorMessage;
        private final DonMuonPhong don;
        private final boolean daChuyenTuBoTri;

        private KetQuaSuaDon(boolean thanhCong, String errorMessage, DonMuonPhong don, boolean daChuyenTuBoTri) {
            this.thanhCong = thanhCong;
            this.errorMessage = errorMessage;
            this.don = don;
            this.daChuyenTuBoTri = daChuyenTuBoTri;
        }

        public static KetQuaSuaDon thanhCong(DonMuonPhong don, boolean daChuyenTuBoTri) {
            return new KetQuaSuaDon(true, null, don, daChuyenTuBoTri);
        }

        public static KetQuaSuaDon thatBai(String errorMessage) {
            return new KetQuaSuaDon(false, errorMessage, null, false);
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

        public boolean isDaChuyenTuBoTri() {
            return daChuyenTuBoTri;
        }
    }
}
