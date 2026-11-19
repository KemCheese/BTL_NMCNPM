package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.DonVi;
import com.ptit.muonphong.entity.Phong;
import com.ptit.muonphong.entity.TrangThaiDon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
public class DonMuonPhongDAO {

    private final JdbcTemplate jdbcTemplate;

    public DonMuonPhongDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DonMuonPhong> timDonDaDuyetTheoPhong(Phong phong, LocalDate tuNgayHienTai) {
        return timDonDaDuyetTheoPhong(phong, tuNgayHienTai, null);
    }

    public List<DonMuonPhong> timDonDaDuyetTheoPhong(Phong phong, LocalDate tuNgay, LocalDate denNgay) {
        if (phong == null || phong.getId() <= 0 || tuNgay == null) {
            return Collections.emptyList();
        }

        String baseSql = "SELECT d.id, d.maDon, d.ngayTao, d.ngaySuDung, d.gioBatDau, d.gioKetThuc, "
                + "d.loaiPhongMongMuon, d.thietBiYeuCau, d.trangThai, d.tenSuKien, d.mucDich, d.noiDung, "
                + "d.soNguoiThamDu, d.sinhVienId, d.donViId, d.phongMongMuonId, "
                + "dv.maDonVi, dv.tenCLB "
                + "FROM tblDonMuonPhong d "
                + "INNER JOIN tblLichSuDungPhong lsp ON lsp.donMuonPhongId = d.id "
                + "LEFT JOIN tblDonVi dv ON d.donViId = dv.id "
                + "WHERE lsp.phongId = ? AND d.trangThai = ? ";

        if (denNgay == null) {
            String sql = baseSql + "AND d.ngaySuDung >= ? ORDER BY d.ngaySuDung ASC, d.gioBatDau ASC, d.id ASC";
            return jdbcTemplate.query(
                    sql,
                    new DonMuonPhongRowMapper(),
                    phong.getId(),
                    TrangThaiDon.DaPheDuyet.name(),
                    Date.valueOf(tuNgay));
        }

        String sql = baseSql + "AND d.ngaySuDung BETWEEN ? AND ? ORDER BY d.ngaySuDung ASC, d.gioBatDau ASC, d.id ASC";
        return jdbcTemplate.query(
                sql,
                new DonMuonPhongRowMapper(),
                phong.getId(),
                TrangThaiDon.DaPheDuyet.name(),
                Date.valueOf(tuNgay),
                Date.valueOf(denNgay));
    }

    private static class DonMuonPhongRowMapper implements RowMapper<DonMuonPhong> {
        @Override
        public DonMuonPhong mapRow(ResultSet rs, int rowNum) throws SQLException {
            DonMuonPhong don = new DonMuonPhong();
            don.setId(rs.getInt("id"));
            don.setMaDon(rs.getString("maDon"));

            Date ngayTao = rs.getDate("ngayTao");
            don.setNgayTao(ngayTao != null ? ngayTao.toLocalDate() : null);

            Date ngaySuDung = rs.getDate("ngaySuDung");
            don.setNgaySuDung(ngaySuDung != null ? ngaySuDung.toLocalDate() : null);

            Time gioBatDau = rs.getTime("gioBatDau");
            don.setGioBatDau(gioBatDau != null ? gioBatDau.toLocalTime() : null);

            Time gioKetThuc = rs.getTime("gioKetThuc");
            don.setGioKetThuc(gioKetThuc != null ? gioKetThuc.toLocalTime() : null);

            don.setLoaiPhongMongMuon(rs.getString("loaiPhongMongMuon"));
            don.setThietBiYeuCau(rs.getString("thietBiYeuCau"));

            String trangThaiStr = rs.getString("trangThai");
            if (trangThaiStr != null && !trangThaiStr.isBlank()) {
                don.setTrangThai(TrangThaiDon.valueOf(trangThaiStr.trim()));
            }

            don.setTenSuKien(rs.getString("tenSuKien"));
            don.setMucDich(rs.getString("mucDich"));
            don.setNoiDung(rs.getString("noiDung"));

            int soNguoi = rs.getInt("soNguoiThamDu");
            don.setSoNguoiThamDu(rs.wasNull() ? null : soNguoi);

            don.setSinhVienId(rs.getInt("sinhVienId"));
            don.setDonViId(rs.getInt("donViId"));

            int phongMongMuonId = rs.getInt("phongMongMuonId");
            don.setPhongMongMuonId(rs.wasNull() ? null : phongMongMuonId);

            String tenCLB = rs.getString("tenCLB");
            String maDonVi = rs.getString("maDonVi");
            if (tenCLB != null || maDonVi != null) {
                DonVi donVi = new DonVi();
                donVi.setId(don.getDonViId());
                donVi.setMaDonVi(maDonVi);
                donVi.setTenCLB(tenCLB);
                don.setDonVi(donVi);
            }
            return don;
        }
    }
}
