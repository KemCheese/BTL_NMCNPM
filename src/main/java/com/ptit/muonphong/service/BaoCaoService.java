package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.DonMuonPhongDAO;
import com.ptit.muonphong.dao.LichSuDungPhongDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.Phong;
import com.ptit.muonphong.entity.ThongKeDong;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaoCaoService {

    public static final String TIEU_CHI_THEO_PHONG = "Theo phòng";
    public static final long SO_GIO_HOAT_DONG_MOI_NGAY = Duration.between(
            DonMuonPhong.GIO_MO_CUA,
            DonMuonPhong.GIO_DONG_CUA).toHours();

    private final LichSuDungPhongDAO lichSuDungPhongDAO;
    private final PhongDAO phongDAO;
    private final DonMuonPhongDAO donMuonPhongDAO;

    public BaoCaoService(LichSuDungPhongDAO lichSuDungPhongDAO,
                         PhongDAO phongDAO,
                         DonMuonPhongDAO donMuonPhongDAO) {
        this.lichSuDungPhongDAO = lichSuDungPhongDAO;
        this.phongDAO = phongDAO;
        this.donMuonPhongDAO = donMuonPhongDAO;
    }

    public List<ThongKeDong> xemBaoCaoTheoPhong(String tieuChi, LocalDate tuNgay, LocalDate denNgay) {
        if (!laTieuChiTheoPhongHopLe(tieuChi)) {
            throw new IllegalArgumentException("Hệ thống hiện chỉ hỗ trợ tiêu chí thống kê Theo phòng.");
        }
        kiemTraKhoangNgay(tuNgay, denNgay);

        List<ThongKeDong> rawRows = lichSuDungPhongDAO.thongKe(TIEU_CHI_THEO_PHONG, tuNgay, denNgay);
        if (rawRows == null || rawRows.isEmpty()) {
            return List.of();
        }

        List<ThongKeDong> result = new ArrayList<>();
        for (ThongKeDong row : rawRows) {
            if (row == null) {
                continue;
            }
            double tongGio = lamTronHaiChuSo(row.getTongGio());
            double tyLe = tinhTyLeSuDung(tongGio, tuNgay, denNgay);
            result.add(new ThongKeDong(row.getPhong(), row.getSoLuot(), tongGio, tyLe));
        }
        result.sort(Comparator.comparingDouble(ThongKeDong::getTongGio).reversed()
                .thenComparing(Comparator.comparingInt(ThongKeDong::getSoLuot).reversed()));
        return result;
    }

    public ChiTietPhongBaoCao xemChiTietDong(int idPhong, LocalDate tuNgay, LocalDate denNgay) {
        kiemTraKhoangNgay(tuNgay, denNgay);
        if (idPhong <= 0) {
            throw new IllegalArgumentException("Không tìm thấy thông tin phòng cần xem chi tiết.");
        }
        Phong phong = phongDAO.getChiTiet(idPhong);
        if (phong == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin phòng cần xem chi tiết.");
        }

        List<DonMuonPhong> dsDon = donMuonPhongDAO.timDonDaDuyetTheoPhong(phong, tuNgay, denNgay);
        if (dsDon == null) {
            dsDon = List.of();
        }

        Map<Integer, Double> thoiLuongGioMap = new LinkedHashMap<>();
        double tongGio = 0.0;
        for (DonMuonPhong don : dsDon) {
            if (don == null) {
                continue;
            }
            double gio = tinhSoGioDon(don);
            thoiLuongGioMap.put(don.getId(), gio);
            tongGio += gio;
        }
        return new ChiTietPhongBaoCao(phong, dsDon, thoiLuongGioMap, lamTronHaiChuSo(tongGio));
    }

    private static void kiemTraKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ từ ngày và đến ngày.");
        }
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng Từ ngày.");
        }
    }

    public static double tinhSoGioDon(DonMuonPhong don) {
        if (don == null || don.getGioBatDau() == null || don.getGioKetThuc() == null) {
            return 0.0;
        }
        long phut = Duration.between(don.getGioBatDau(), don.getGioKetThuc()).toMinutes();
        if (phut <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(phut)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static class ChiTietPhongBaoCao {
        private final Phong phong;
        private final List<DonMuonPhong> dsDon;
        private final Map<Integer, Double> thoiLuongGioMap;
        private final double tongGio;

        public ChiTietPhongBaoCao(Phong phong,
                                   List<DonMuonPhong> dsDon,
                                   Map<Integer, Double> thoiLuongGioMap,
                                   double tongGio) {
            this.phong = phong;
            this.dsDon = dsDon;
            this.thoiLuongGioMap = thoiLuongGioMap;
            this.tongGio = tongGio;
        }

        public Phong getPhong() {
            return phong;
        }

        public List<DonMuonPhong> getDsDon() {
            return dsDon;
        }

        public Map<Integer, Double> getThoiLuongGioMap() {
            return thoiLuongGioMap;
        }

        public double getTongGio() {
            return tongGio;
        }
    }

    public static boolean laTieuChiTheoPhongHopLe(String tieuChi) {
        if (tieuChi == null || tieuChi.isBlank()) {
            return false;
        }
        String normalized = tieuChi.trim();
        return TIEU_CHI_THEO_PHONG.equalsIgnoreCase(normalized) || "THEO_PHONG".equalsIgnoreCase(normalized);
    }

    public static double tinhTyLeSuDung(double tongGio, LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || denNgay.isBefore(tuNgay) || tongGio <= 0) {
            return 0.0;
        }
        long soNgay = ChronoUnit.DAYS.between(tuNgay, denNgay) + 1;
        long tongGioToiDa = soNgay * SO_GIO_HOAT_DONG_MOI_NGAY;
        if (tongGioToiDa <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(tongGio)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(tongGioToiDa), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double lamTronHaiChuSo(double value) {
        if (value <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
