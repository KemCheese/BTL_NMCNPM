package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class DuyetCap1Service {

    private final DonMuonPhongDAO donMuonPhongDAO;
    private final LuotDuyetDAO luotDuyetDAO;
    private final LichSuDonDAO lichSuDonDAO;

    public DuyetCap1Service(DonMuonPhongDAO donMuonPhongDAO,
                            LuotDuyetDAO luotDuyetDAO,
                            LichSuDonDAO lichSuDonDAO) {
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.luotDuyetDAO = luotDuyetDAO;
        this.lichSuDonDAO = lichSuDonDAO;
    }

    public List<DonMuonPhong> layDanhSachChoDuyetCap1() {
        return donMuonPhongDAO.timTheoTrangThai(TrangThaiDon.ChoDuyetCap1);
    }

    public List<DonMuonPhong> layDanhSachChoDuyetCap1(String tenCLB) {
        return locTheoTenClb(layDanhSachChoDuyetCap1(), tenCLB);
    }

    public List<DonMuonPhong> locTheoTenClb(List<DonMuonPhong> danhSachDon, String tenCLB) {
        if (tenCLB == null || tenCLB.isBlank()) {
            return danhSachDon;
        }
        String tuKhoa = tenCLB.trim().toLowerCase(Locale.ROOT);
        return danhSachDon.stream()
                .filter(don -> don.getDonVi() != null
                        && don.getDonVi().getTenCLB() != null
                        && don.getDonVi().getTenCLB().toLowerCase(Locale.ROOT).contains(tuKhoa))
                .toList();
    }

    public List<String> layDanhSachTenClb(List<DonMuonPhong> danhSachDon) {
        return danhSachDon.stream()
                .filter(don -> don.getDonVi() != null && don.getDonVi().getTenCLB() != null)
                .map(don -> don.getDonVi().getTenCLB())
                .distinct()
                .sorted()
                .toList();
    }

    public DonMuonPhong xemChiTietDon(int idDon) {
        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        don.setLuotDuyets(luotDuyetDAO.getTheoDon(idDon));
        don.setLichSuDons(lichSuDonDAO.getTheoDon(idDon));
        return don;
    }

    @Transactional
    public void chapThuanDon(int idDon, NguoiDung canBoDoan) {
        xuLyDuyetCap1(
                idDon,
                TrangThaiDon.ChoDuyetCap2,
                "Chấp thuận",
                null,
                "Duyệt cấp 1: Chấp thuận",
                canBoDoan
        );
    }

    @Transactional
    public void tuChoiDon(int idDon, String lyDo, NguoiDung canBoDoan) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        String lyDoChuan = lyDo.trim();
        xuLyDuyetCap1(
                idDon,
                TrangThaiDon.TuChoiCap1,
                "Từ chối",
                lyDoChuan,
                "Duyệt cấp 1: Từ chối - " + lyDoChuan,
                canBoDoan
        );
    }

    private void xuLyDuyetCap1(int idDon,
                               TrangThaiDon trangThaiMoi,
                               String ketQua,
                               String lyDo,
                               String ghiChuLichSu,
                               NguoiDung nguoiDuyet) {
        if (!(nguoiDuyet instanceof CanBoDoan) || nguoiDuyet.getId() <= 0) {
            throw new IllegalStateException("Chỉ Cán bộ Đoàn mới có quyền duyệt đơn cấp 1.");
        }

        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        if (don.getTrangThai() != TrangThaiDon.ChoDuyetCap1) {
            throw new IllegalStateException("Chỉ có thể xử lý đơn ở trạng thái Chờ duyệt cấp 1.");
        }

        int updated = donMuonPhongDAO.capNhatTrangThai(idDon, trangThaiMoi);
        if (updated == 0) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn mượn phòng.");
        }

        LocalDateTime now = LocalDateTime.now();

        LuotDuyet luotDuyet = new LuotDuyet();
        luotDuyet.setCapDuyet(1);
        luotDuyet.setKetQua(ketQua);
        luotDuyet.setLyDo(lyDo);
        luotDuyet.setThoiDiem(now);
        luotDuyet.setDonMuonPhong(don);
        luotDuyet.setNguoiDuyet(nguoiDuyet);
        luotDuyetDAO.ghiLuotDuyet(luotDuyet);

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(TrangThaiDon.ChoDuyetCap1);
        lichSuDon.setTrangThaiMoi(trangThaiMoi);
        lichSuDon.setGhiChu(ghiChuLichSu);
        lichSuDon.setThoiDiem(now);
        lichSuDon.setDonMuonPhong(don);
        lichSuDon.setNguoiThucHien(nguoiDuyet);
        lichSuDonDAO.ghiThayDoi(lichSuDon);
    }
}
