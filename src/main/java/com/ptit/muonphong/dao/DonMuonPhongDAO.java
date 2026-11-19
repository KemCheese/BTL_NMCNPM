package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
public class DonMuonPhongDAO {

    private static final String BASE_DETAIL_SELECT = """
            SELECT d.id, d.maDon, d.ngayTao, d.ngaySuDung, d.gioBatDau, d.gioKetThuc,
                   d.loaiPhongMongMuon, d.thietBiYeuCau, d.trangThai,
                   d.tenSuKien, d.mucDich, d.noiDung, d.soNguoiThamDu,
                   d.sinhVienId, d.donViId, d.phongMongMuonId,
                   sv.maSinhVien, sv.khoaHoc, sv.nganhHoc,
                   nd.tenDangNhap AS svTenDangNhap, nd.hoTen AS svHoTen,
                   nd.email AS svEmail, nd.soDienThoai AS svSoDienThoai, nd.trangThai AS svTrangThai,
                   dv.maDonVi, dv.tenCLB, dv.moTa AS dvMoTa,
                   pm.maPhong AS pmMaPhong, pm.tenPhong AS pmTenPhong, pm.toaNha AS pmToaNha,
                   pm.tang AS pmTang, pm.sucChuaToiDa AS pmSucChuaToiDa, pm.loaiPhong AS pmLoaiPhong,
                   pm.trangThietBi AS pmTrangThietBi, pm.tinhTrang AS pmTinhTrang, pm.moTa AS pmMoTa
            FROM tblDonMuonPhong d
            JOIN tblSinhVien sv ON d.sinhVienId = sv.id
            JOIN tblNguoiDung nd ON sv.id = nd.id
            JOIN tblDonVi dv ON d.donViId = dv.id
            LEFT JOIN tblPhong pm ON d.phongMongMuonId = pm.id
            """;

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

    public List<DonMuonPhong> timTheoTrangThai(TrangThaiDon trangThai) {
        if (trangThai == null) {
            return List.of();
        }
        String sql = BASE_DETAIL_SELECT + " WHERE d.trangThai = ? ORDER BY d.ngayTao ASC, d.id ASC";
        return jdbcTemplate.query(sql, new DonMuonPhongChiTietRowMapper(), trangThai.name());
    }

    public DonMuonPhong getChiTiet(int idDon) {
        String sql = BASE_DETAIL_SELECT + " WHERE d.id = ?";
        List<DonMuonPhong> rows = jdbcTemplate.query(sql, new DonMuonPhongChiTietRowMapper(), idDon);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int capNhatTrangThai(int idDon, TrangThaiDon trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái đơn không được để trống.");
        }
        String sql = "UPDATE tblDonMuonPhong SET trangThai = ? WHERE id = ?";
        return jdbcTemplate.update(sql, trangThai.name(), idDon);
    }

    private static DonMuonPhong mapCoreColumns(ResultSet rs) throws SQLException {
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
        return don;
    }

    private static class DonMuonPhongRowMapper implements RowMapper<DonMuonPhong> {
        @Override
        public DonMuonPhong mapRow(ResultSet rs, int rowNum) throws SQLException {
            DonMuonPhong don = mapCoreColumns(rs);
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

    private static class DonMuonPhongChiTietRowMapper implements RowMapper<DonMuonPhong> {
        @Override
        public DonMuonPhong mapRow(ResultSet rs, int rowNum) throws SQLException {
            DonMuonPhong don = mapCoreColumns(rs);

            SuKien suKien = new SuKien();
            suKien.setId(don.getId());
            suKien.setTenSuKien(don.getTenSuKien());
            suKien.setMucDich(don.getMucDich());
            suKien.setNoiDung(don.getNoiDung());
            suKien.setSoNguoiThamDu(don.getSoNguoiThamDu() != null ? don.getSoNguoiThamDu() : 0);
            don.setSuKien(suKien);

            DonVi donVi = new DonVi();
            donVi.setId(don.getDonViId());
            donVi.setMaDonVi(rs.getString("maDonVi"));
            donVi.setTenCLB(rs.getString("tenCLB"));
            donVi.setMoTa(rs.getString("dvMoTa"));
            don.setDonVi(donVi);

            SinhVien sv = new SinhVien();
            sv.setId(don.getSinhVienId());
            sv.setTenDangNhap(rs.getString("svTenDangNhap"));
            sv.setHoTen(rs.getString("svHoTen"));
            sv.setEmail(rs.getString("svEmail"));
            sv.setSoDienThoai(rs.getString("svSoDienThoai"));
            sv.setTrangThai(rs.getString("svTrangThai"));
            sv.setMaSinhVien(rs.getString("maSinhVien"));
            sv.setKhoaHoc(rs.getInt("khoaHoc"));
            sv.setNganhHoc(rs.getString("nganhHoc"));
            sv.setDonVi(donVi);
            don.setSinhVien(sv);

            Integer phongMongMuonId = don.getPhongMongMuonId();
            if (phongMongMuonId != null && phongMongMuonId > 0) {
                Phong phong = new Phong();
                phong.setId(phongMongMuonId);
                phong.setMaPhong(rs.getString("pmMaPhong"));
                phong.setTenPhong(rs.getString("pmTenPhong"));
                phong.setToaNha(rs.getString("pmToaNha"));
                phong.setTang(rs.getInt("pmTang"));
                phong.setSucChuaToiDa(rs.getInt("pmSucChuaToiDa"));
                phong.setLoaiPhong(rs.getString("pmLoaiPhong"));
                phong.setTrangThietBi(rs.getString("pmTrangThietBi"));
                phong.setTinhTrang(rs.getString("pmTinhTrang"));
                phong.setMoTa(rs.getString("pmMoTa"));
                don.setPhongMongMuon(phong);
            }

            return don;
        }
    }
}
