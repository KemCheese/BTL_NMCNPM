package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

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
