package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class LichSuDonDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<LichSuDon> rowMapper = new LichSuDonRowMapper();

    public LichSuDonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int ghiThayDoi(LichSuDon lichSuDon) {
        if (lichSuDon == null
                || lichSuDon.getTrangThaiMoi() == null
                || lichSuDon.getDonMuonPhong() == null
                || lichSuDon.getNguoiThucHien() == null) {
            throw new IllegalArgumentException("Thông tin lịch sử đơn không hợp lệ.");
        }
        LocalDateTime thoiDiem = lichSuDon.getThoiDiem() != null ? lichSuDon.getThoiDiem() : LocalDateTime.now();
        String trangThaiCu = lichSuDon.getTrangThaiCu() != null ? lichSuDon.getTrangThaiCu().name() : null;
        String sql = """
                INSERT INTO tblLichSuDon (trangThaiCu, trangThaiMoi, ghiChu, thoiDiem, donMuonPhongId, nguoiThucHienId)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(
                sql,
                trangThaiCu,
                lichSuDon.getTrangThaiMoi().name(),
                lichSuDon.getGhiChu(),
                Timestamp.valueOf(thoiDiem),
                lichSuDon.getDonMuonPhong().getId(),
                lichSuDon.getNguoiThucHien().getId()
        );
    }

    public List<LichSuDon> getTheoDon(int idDon) {
        String sql = """
                SELECT ls.id, ls.trangThaiCu, ls.trangThaiMoi, ls.ghiChu, ls.thoiDiem,
                       ls.donMuonPhongId, ls.nguoiThucHienId,
                       nd.tenDangNhap, nd.hoTen,
                       sv.maSinhVien,
                       cbd.viTriCongViec AS cbdViTri,
                       cbcs.viTriCongViec AS cbcsViTri
                FROM tblLichSuDon ls
                JOIN tblNguoiDung nd ON ls.nguoiThucHienId = nd.id
                LEFT JOIN tblSinhVien sv ON nd.id = sv.id
                LEFT JOIN tblCanBoDoan cbd ON nd.id = cbd.id
                LEFT JOIN tblCanBoCSVC cbcs ON nd.id = cbcs.id
                WHERE ls.donMuonPhongId = ?
                ORDER BY ls.thoiDiem ASC, ls.id ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, idDon);
    }

    private static class LichSuDonRowMapper implements RowMapper<LichSuDon> {
        @Override
        public LichSuDon mapRow(ResultSet rs, int rowNum) throws SQLException {
            LichSuDon ls = new LichSuDon();
            ls.setId(rs.getInt("id"));
            ls.setTrangThaiCu(TrangThaiDon.fromCode(rs.getString("trangThaiCu")));
            ls.setTrangThaiMoi(TrangThaiDon.fromCode(rs.getString("trangThaiMoi")));
            ls.setGhiChu(rs.getString("ghiChu"));

            Timestamp thoiDiem = rs.getTimestamp("thoiDiem");
            if (thoiDiem != null) {
                ls.setThoiDiem(thoiDiem.toLocalDateTime());
            }

            DonMuonPhong don = new DonMuonPhong();
            don.setId(rs.getInt("donMuonPhongId"));
            ls.setDonMuonPhong(don);

            ls.setNguoiThucHien(DaoMappingHelper.mapNguoiDungTomTat(rs, "nguoiThucHienId"));
            return ls;
        }
    }
}
