package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class NguoiDungDAO {

    private final JdbcTemplate jdbcTemplate;

    public NguoiDungDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NguoiDung dangNhap(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null || matKhau.isBlank()) {
            return null;
        }
        NguoiDung nguoiDung = findByUsername(tenDangNhap.trim());
        if (nguoiDung == null || !nguoiDung.isHoatDong() || !matKhau.equals(nguoiDung.getMatKhau())) {
            return null;
        }
        return nguoiDung;
    }

    public NguoiDung findByUsername(String tenDangNhap) {
        String sql = """
                SELECT nd.id, nd.tenDangNhap, nd.matKhau, nd.hoTen, nd.diaChi, nd.ngaySinh,
                       nd.email, nd.soDienThoai, nd.trangThai,
                       sv.maSinhVien, sv.khoaHoc, sv.nganhHoc, sv.donViId,
                       dv.maDonVi, dv.tenCLB, dv.moTa AS dvMoTa,
                       cbd.viTriCongViec AS cbdViTri,
                       cbcs.viTriCongViec AS cbcsViTri
                FROM tblNguoiDung nd
                LEFT JOIN tblSinhVien sv ON nd.id = sv.id
                LEFT JOIN tblDonVi dv ON sv.donViId = dv.id
                LEFT JOIN tblCanBoDoan cbd ON nd.id = cbd.id
                LEFT JOIN tblCanBoCSVC cbcs ON nd.id = cbcs.id
                WHERE nd.tenDangNhap = ?
                """;
        List<NguoiDung> rows = jdbcTemplate.query(sql, new NguoiDungRowMapper(), tenDangNhap);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static class NguoiDungRowMapper implements RowMapper<NguoiDung> {
        @Override
        public NguoiDung mapRow(ResultSet rs, int rowNum) throws SQLException {
            NguoiDung nd;
            if (rs.getString("maSinhVien") != null) {
                SinhVien sv = new SinhVien();
                sv.setMaSinhVien(rs.getString("maSinhVien"));
                sv.setKhoaHoc(rs.getInt("khoaHoc"));
                sv.setNganhHoc(rs.getString("nganhHoc"));
                int donViId = rs.getInt("donViId");
                if (!rs.wasNull()) {
                    DonVi dv = new DonVi();
                    dv.setId(donViId);
                    dv.setMaDonVi(rs.getString("maDonVi"));
                    dv.setTenCLB(rs.getString("tenCLB"));
                    dv.setMoTa(rs.getString("dvMoTa"));
                    sv.setDonVi(dv);
                }
                nd = sv;
            } else if (rs.getString("cbdViTri") != null) {
                CanBoDoan cbd = new CanBoDoan();
                cbd.setViTriCongViec(rs.getString("cbdViTri"));
                nd = cbd;
            } else if (rs.getString("cbcsViTri") != null) {
                CanBoCSVC cbcs = new CanBoCSVC();
                cbcs.setViTriCongViec(rs.getString("cbcsViTri"));
                nd = cbcs;
            } else {
                return null;
            }

            nd.setId(rs.getInt("id"));
            nd.setTenDangNhap(rs.getString("tenDangNhap"));
            nd.setMatKhau(rs.getString("matKhau"));
            nd.setHoTen(rs.getString("hoTen"));
            nd.setDiaChi(rs.getString("diaChi"));
            Date ngaySinh = rs.getDate("ngaySinh");
            if (ngaySinh != null) {
                nd.setNgaySinh(ngaySinh.toLocalDate());
            }
            nd.setEmail(rs.getString("email"));
            nd.setSoDienThoai(rs.getString("soDienThoai"));
            nd.setTrangThai(rs.getString("trangThai"));
            return nd;
        }
    }
}
