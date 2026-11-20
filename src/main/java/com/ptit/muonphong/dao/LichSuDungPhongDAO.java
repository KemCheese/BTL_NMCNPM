package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.BaoCaoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class LichSuDungPhongDAO {

    private final JdbcTemplate jdbcTemplate;

    public LichSuDungPhongDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LichSuDungPhong> getLichPhong(Phong phong, LocalDate ngay) {
        if (phong == null || phong.getId() <= 0 || ngay == null) {
            return List.of();
        }
        String sql = """
                SELECT ls.id AS lsId, ls.phongId, ls.donMuonPhongId,
                       d.maDon, d.ngayTao, d.ngaySuDung, d.gioBatDau, d.gioKetThuc,
                       d.loaiPhongMongMuon, d.thietBiYeuCau, d.trangThai,
                       d.tenSuKien, d.mucDich, d.noiDung, d.soNguoiThamDu,
                       dv.id AS donViId, dv.maDonVi, dv.tenCLB, dv.moTa AS dvMoTa
                FROM tblLichSuDungPhong ls
                INNER JOIN tblDonMuonPhong d ON ls.donMuonPhongId = d.id
                LEFT JOIN tblDonVi dv ON d.donViId = dv.id
                WHERE ls.phongId = ?
                  AND d.ngaySuDung = ?
                  AND d.trangThai <> 'DaHuy'
                ORDER BY d.gioBatDau ASC, ls.id ASC
                """;
        return jdbcTemplate.query(sql, new LichSuDungPhongRowMapper(phong), phong.getId(), Date.valueOf(ngay));
    }

    public Phong getPhongBoTriTheoDon(int idDon) {
        if (idDon <= 0) {
            return null;
        }
        return getPhongBoTriTheoCacDon(List.of(idDon)).get(idDon);
    }

    public Map<Integer, Phong> getPhongBoTriTheoCacDon(List<Integer> donIds) {
        if (donIds == null || donIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(donIds.size(), "?"));
        String sql = "SELECT ls.donMuonPhongId, p.id AS pId, p.maPhong, p.tenPhong, p.toaNha, p.tang, "
                + "p.sucChuaToiDa, p.loaiPhong, p.trangThietBi, p.tinhTrang, p.moTa AS pMoTa "
                + "FROM tblLichSuDungPhong ls "
                + "INNER JOIN tblPhong p ON ls.phongId = p.id "
                + "WHERE ls.donMuonPhongId IN (" + placeholders + ")";
        Map<Integer, Phong> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            int donId = rs.getInt("donMuonPhongId");
            result.putIfAbsent(donId, mapPhongColumns(rs));
        }, donIds.toArray());
        return result;
    }

    public int themKhungGio(LichSuDungPhong lichSuDungPhong) {
        if (lichSuDungPhong == null
                || lichSuDungPhong.getPhong() == null || lichSuDungPhong.getPhong().getId() <= 0
                || lichSuDungPhong.getDonMuonPhong() == null || lichSuDungPhong.getDonMuonPhong().getId() <= 0) {
            throw new IllegalArgumentException("Thông tin lịch sử dụng phòng không hợp lệ.");
        }
        String sql = "INSERT INTO tblLichSuDungPhong (phongId, donMuonPhongId) VALUES (?, ?)";
        return jdbcTemplate.update(sql, lichSuDungPhong.getPhong().getId(), lichSuDungPhong.getDonMuonPhong().getId());
    }

    public int doiPhongChoDon(int idDon, int idPhongMoi) {
        if (idDon <= 0 || idPhongMoi <= 0) {
            throw new IllegalArgumentException("Thông tin đổi phòng không hợp lệ.");
        }
        String sql = "UPDATE tblLichSuDungPhong SET phongId = ? WHERE donMuonPhongId = ?";
        return jdbcTemplate.update(sql, idPhongMoi, idDon);
    }

    public List<ThongKeDong> thongKe(String tieuChi, LocalDate tuNgay, LocalDate denNgay) {
        if (!BaoCaoService.laTieuChiTheoPhongHopLe(tieuChi)
                || tuNgay == null
                || denNgay == null
                || denNgay.isBefore(tuNgay)) {
            return List.of();
        }
        String sql = """
                SELECT p.id AS pId, p.maPhong, p.tenPhong, p.toaNha, p.tang,
                       p.sucChuaToiDa, p.loaiPhong, p.trangThietBi, p.tinhTrang, p.moTa AS pMoTa,
                       COUNT(DISTINCT d.id) AS soLuot,
                       COALESCE(SUM(TIMESTAMPDIFF(MINUTE, d.gioBatDau, d.gioKetThuc)) / 60.0, 0) AS tongGio
                FROM tblLichSuDungPhong ls
                INNER JOIN tblPhong p ON ls.phongId = p.id
                INNER JOIN tblDonMuonPhong d ON ls.donMuonPhongId = d.id
                WHERE d.trangThai = ?
                  AND d.ngaySuDung BETWEEN ? AND ?
                GROUP BY p.id, p.maPhong, p.tenPhong, p.toaNha, p.tang,
                         p.sucChuaToiDa, p.loaiPhong, p.trangThietBi, p.tinhTrang, p.moTa
                ORDER BY tongGio DESC, soLuot DESC, p.maPhong ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Phong phong = mapPhongColumns(rs);
            int soLuot = rs.getInt("soLuot");
            double tongGio = BaoCaoService.lamTronHaiChuSo(rs.getDouble("tongGio"));
            double tyLe = BaoCaoService.tinhTyLeSuDung(tongGio, tuNgay, denNgay);
            return new ThongKeDong(phong, soLuot, tongGio, tyLe);
        }, TrangThaiDon.DaPheDuyet.name(), Date.valueOf(tuNgay), Date.valueOf(denNgay));
    }

    private static Phong mapPhongColumns(ResultSet rs) throws SQLException {
        Phong phong = new Phong();
        phong.setId(rs.getInt("pId"));
        phong.setMaPhong(rs.getString("maPhong"));
        phong.setTenPhong(rs.getString("tenPhong"));
        phong.setToaNha(rs.getString("toaNha"));
        phong.setTang(rs.getInt("tang"));
        phong.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
        phong.setLoaiPhong(rs.getString("loaiPhong"));
        phong.setTrangThietBi(rs.getString("trangThietBi"));
        phong.setTinhTrang(rs.getString("tinhTrang"));
        phong.setMoTa(rs.getString("pMoTa"));
        return phong;
    }

    private static class LichSuDungPhongRowMapper implements RowMapper<LichSuDungPhong> {
        private final Phong phong;

        private LichSuDungPhongRowMapper(Phong phong) {
            this.phong = phong;
        }

        @Override
        public LichSuDungPhong mapRow(ResultSet rs, int rowNum) throws SQLException {
            DonMuonPhong don = new DonMuonPhong();
            don.setId(rs.getInt("donMuonPhongId"));
            don.setMaDon(rs.getString("maDon"));

            Date ngayTao = rs.getDate("ngayTao");
            if (ngayTao != null) {
                don.setNgayTao(ngayTao.toLocalDate());
            }
            Date ngaySuDung = rs.getDate("ngaySuDung");
            if (ngaySuDung != null) {
                don.setNgaySuDung(ngaySuDung.toLocalDate());
            }
            Time gioBatDau = rs.getTime("gioBatDau");
            if (gioBatDau != null) {
                don.setGioBatDau(gioBatDau.toLocalTime());
            }
            Time gioKetThuc = rs.getTime("gioKetThuc");
            if (gioKetThuc != null) {
                don.setGioKetThuc(gioKetThuc.toLocalTime());
            }

            don.setLoaiPhongMongMuon(rs.getString("loaiPhongMongMuon"));
            don.setThietBiYeuCau(rs.getString("thietBiYeuCau"));
            don.setTrangThai(TrangThaiDon.fromCode(rs.getString("trangThai")));
            don.setTenSuKien(rs.getString("tenSuKien"));
            don.setMucDich(rs.getString("mucDich"));
            don.setNoiDung(rs.getString("noiDung"));
            don.setSoNguoiThamDu(rs.getInt("soNguoiThamDu"));

            int donViId = rs.getInt("donViId");
            if (!rs.wasNull()) {
                DonVi donVi = new DonVi();
                donVi.setId(donViId);
                donVi.setMaDonVi(rs.getString("maDonVi"));
                donVi.setTenCLB(rs.getString("tenCLB"));
                donVi.setMoTa(rs.getString("dvMoTa"));
                don.setDonVi(donVi);
            }

            LichSuDungPhong lich = new LichSuDungPhong();
            lich.setId(rs.getInt("lsId"));
            lich.setPhong(phong);
            lich.setDonMuonPhong(don);
            return lich;
        }
    }
}
