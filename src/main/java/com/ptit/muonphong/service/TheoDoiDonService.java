package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TheoDoiDonService {

    private final DonMuonPhongDAO donMuonPhongDAO;
    private final LichSuDungPhongDAO lichSuDungPhongDAO;
    private final LuotDuyetDAO luotDuyetDAO;
    private final LichSuDonDAO lichSuDonDAO;

    public TheoDoiDonService(DonMuonPhongDAO donMuonPhongDAO,
                             LichSuDungPhongDAO lichSuDungPhongDAO,
                             LuotDuyetDAO luotDuyetDAO,
                             LichSuDonDAO lichSuDonDAO) {
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.lichSuDungPhongDAO = lichSuDungPhongDAO;
        this.luotDuyetDAO = luotDuyetDAO;
        this.lichSuDonDAO = lichSuDonDAO;
    }

    @Transactional
    public void capNhatTrangThaiHoanThanhQuaHan(NguoiDung nguoiDung) {
        List<DonMuonPhong> dsQuaHan = donMuonPhongDAO.timDonDaDuyetQuaHan(LocalDateTime.now());
        for (DonMuonPhong don : dsQuaHan) {
            int updated = donMuonPhongDAO.capNhatTrangThai(don.getId(), TrangThaiDon.HoanThanh);
            if (updated > 0) {
                NguoiDung nguoiThucHien = don.getSinhVien() != null ? don.getSinhVien() : nguoiDung;
                if (nguoiThucHien != null) {
                    LichSuDon lichSu = new LichSuDon();
                    lichSu.setTrangThaiCu(TrangThaiDon.DaPheDuyet);
                    lichSu.setTrangThaiMoi(TrangThaiDon.HoanThanh);
                    lichSu.setGhiChu("Hệ thống tự động cập nhật hoàn thành sau giờ sử dụng");
                    lichSu.setThoiDiem(LocalDateTime.now());
                    lichSu.setDonMuonPhong(don);
                    lichSu.setNguoiThucHien(nguoiThucHien);
                    lichSuDonDAO.ghiThayDoi(lichSu);
                }
            }
        }
    }

    @Transactional
    public DanhSachDonResult timDonCuaNguoiMuon(NguoiDung nguoiDung,
                                                 String maDon,
                                                 LocalDate tuNgay,
                                                 LocalDate denNgay,
                                                 String trangThaiCode) {
        if (!(nguoiDung instanceof SinhVien sv)) {
            return new DanhSachDonResult(Collections.emptyList(), Collections.emptyMap());
        }
        capNhatTrangThaiHoanThanhQuaHan(nguoiDung);

        TrangThaiDon trangThai = parseTrangThai(trangThaiCode);
        List<DonMuonPhong> dsDon = donMuonPhongDAO.timTheoNguoiMuon(sv, maDon, tuNgay, denNgay, trangThai);
        List<Integer> donIds = dsDon.stream().map(DonMuonPhong::getId).toList();
        Map<Integer, Phong> phongBoTriMap = lichSuDungPhongDAO.getPhongBoTriTheoCacDon(donIds);
        return new DanhSachDonResult(dsDon, phongBoTriMap);
    }

    @Transactional
    public ChiTietDonResult getChiTietDon(int idDon, NguoiDung nguoiDung) {
        if (!(nguoiDung instanceof SinhVien sv) || idDon <= 0) {
            return null;
        }
        capNhatTrangThaiHoanThanhQuaHan(nguoiDung);

        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null || don.getSinhVien() == null || don.getSinhVien().getId() != sv.getId()) {
            return null;
        }
        Phong phongBoTri = lichSuDungPhongDAO.getPhongBoTriTheoDon(idDon);
        List<LuotDuyet> dsLuotDuyet = luotDuyetDAO.getTheoDon(idDon);
        List<LichSuDon> dsLichSuDon = lichSuDonDAO.getTheoDon(idDon);
        boolean coTheTaoLai = don.getTrangThai() == TrangThaiDon.TuChoiCap1
                || don.getTrangThai() == TrangThaiDon.TuChoiCap2;
        return new ChiTietDonResult(don, phongBoTri, dsLuotDuyet, dsLichSuDon, coTheTaoLai);
    }

    private TrangThaiDon parseTrangThai(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return TrangThaiDon.fromCode(code.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static class DanhSachDonResult {
        private final List<DonMuonPhong> dsDon;
        private final Map<Integer, Phong> phongBoTriMap;

        public DanhSachDonResult(List<DonMuonPhong> dsDon, Map<Integer, Phong> phongBoTriMap) {
            this.dsDon = dsDon;
            this.phongBoTriMap = phongBoTriMap;
        }

        public List<DonMuonPhong> getDsDon() {
            return dsDon;
        }

        public Map<Integer, Phong> getPhongBoTriMap() {
            return phongBoTriMap;
        }
    }

    public static class ChiTietDonResult {
        private final DonMuonPhong don;
        private final Phong phongBoTri;
        private final List<LuotDuyet> dsLuotDuyet;
        private final List<LichSuDon> dsLichSuDon;
        private final boolean coTheTaoLai;

        public ChiTietDonResult(DonMuonPhong don,
                                Phong phongBoTri,
                                List<LuotDuyet> dsLuotDuyet,
                                List<LichSuDon> dsLichSuDon,
                                boolean coTheTaoLai) {
            this.don = don;
            this.phongBoTri = phongBoTri;
            this.dsLuotDuyet = dsLuotDuyet;
            this.dsLichSuDon = dsLichSuDon;
            this.coTheTaoLai = coTheTaoLai;
        }

        public DonMuonPhong getDon() {
            return don;
        }

        public Phong getPhongBoTri() {
            return phongBoTri;
        }

        public List<LuotDuyet> getDsLuotDuyet() {
            return dsLuotDuyet;
        }

        public List<LichSuDon> getDsLichSuDon() {
            return dsLichSuDon;
        }

        public boolean isCoTheTaoLai() {
            return coTheTaoLai;
        }
    }
}
