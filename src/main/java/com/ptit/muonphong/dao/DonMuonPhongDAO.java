package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class DonMuonPhongDAO {

    private static final String PREFIX_MA_DON = "DM-";

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

    public DonMuonPhong lockDonById(int idDon) {
        String sql = BASE_DETAIL_SELECT + " WHERE d.id = ? FOR UPDATE";
        List<DonMuonPhong> rows = jdbcTemplate.query(sql, new DonMuonPhongChiTietRowMapper(), idDon);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<DonMuonPhong> timDonDaDuyetChuaSuDung(LocalDate ngayHienTai, LocalTime gioHienTai) {
        if (ngayHienTai == null || gioHienTai == null) {
            return List.of();
        }
        String sql = BASE_DETAIL_SELECT
                + " WHERE d.trangThai = ? AND (d.ngaySuDung > ? OR (d.ngaySuDung = ? AND d.gioBatDau > ?))"
                + " ORDER BY d.ngaySuDung ASC, d.gioBatDau ASC, d.id ASC";
        Date ngaySql = Date.valueOf(ngayHienTai);
        Time gioSql = Time.valueOf(gioHienTai);
        return jdbcTemplate.query(
                sql,
                new DonMuonPhongChiTietRowMapper(),
                TrangThaiDon.DaPheDuyet.name(),
                ngaySql,
                ngaySql,
                gioSql
        );
    }

    public List<DonMuonPhong> timTheoNguoiMuon(SinhVien sinhVien, TrangThaiDon trangThai) {
        return timTheoNguoiMuon(sinhVien, null, null, null, trangThai);
    }

    public List<DonMuonPhong> timTheoNguoiMuon(SinhVien sinhVien, String maDon, LocalDate tuNgay,
                                               LocalDate denNgay, TrangThaiDon trangThai) {
        if (sinhVien == null || sinhVien.getId() <= 0) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder(BASE_DETAIL_SELECT)
                .append(" WHERE d.sinhVienId = ?");
        List<Object> params = new ArrayList<>();
        params.add(sinhVien.getId());

        if (maDon != null && !maDon.isBlank()) {
            sql.append(" AND LOWER(d.maDon) LIKE ?");
            params.add("%" + maDon.trim().toLowerCase() + "%");
        }
        if (tuNgay != null) {
            sql.append(" AND d.ngaySuDung >= ?");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append(" AND d.ngaySuDung <= ?");
            params.add(Date.valueOf(denNgay));
        }
        if (trangThai != null) {
            sql.append(" AND d.trangThai = ?");
            params.add(trangThai.name());
        }
        sql.append(" ORDER BY d.ngayTao DESC, d.id DESC");
        return jdbcTemplate.query(sql.toString(), new DonMuonPhongChiTietRowMapper(), params.toArray());
    }

    public List<DonMuonPhong> timDonDaDuyetQuaHan(LocalDateTime thoiDiemHienTai) {
        LocalDateTime moc = thoiDiemHienTai != null ? thoiDiemHienTai : LocalDateTime.now();
        String sql = BASE_DETAIL_SELECT
                + " WHERE d.trangThai = 'DaPheDuyet' AND TIMESTAMP(d.ngaySuDung, d.gioKetThuc) <= ?";
        return jdbcTemplate.query(sql, new DonMuonPhongChiTietRowMapper(), Timestamp.valueOf(moc));
    }

    public int capNhatTrangThai(int idDon, TrangThaiDon trangThai) {
        if (trangThai == null) {
            throw new IllegalArgumentException("Trạng thái đơn không được để trống.");
        }
        String sql = "UPDATE tblDonMuonPhong SET trangThai = ? WHERE id = ?";
        return jdbcTemplate.update(sql, trangThai.name(), idDon);
    }

    public boolean kiemTraTrungLich(DonMuonPhong don, Phong phong) {
        if (don == null || don.getNgaySuDung() == null || don.getGioBatDau() == null || don.getGioKetThuc() == null) {
            return true;
        }
        Date ngaySql = Date.valueOf(don.getNgaySuDung());
        Time gioKetThucSql = Time.valueOf(don.getGioKetThuc());
        Time gioBatDauSql = Time.valueOf(don.getGioBatDau());

        if (phong != null && phong.getId() > 0) {
            StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM tblLichSuDungPhong ls
                    INNER JOIN tblDonMuonPhong d ON ls.donMuonPhongId = d.id
                    WHERE ls.phongId = ?
                      AND d.ngaySuDung = ?
                      AND d.trangThai <> 'DaHuy'
                      AND d.gioBatDau < ?
                      AND d.gioKetThuc > ?
                    """);
            List<Object> params = new ArrayList<>();
            params.add(phong.getId());
            params.add(ngaySql);
            params.add(gioKetThucSql);
            params.add(gioBatDauSql);

            if (don.getId() > 0) {
                sql.append(" AND d.id <> ?");
                params.add(don.getId());
            }

            Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
            return count != null && count > 0;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM tblPhong p
                WHERE p.tinhTrang = 'Đang hoạt động'
                """);
        List<Object> params = new ArrayList<>();
        if (don.getLoaiPhongMongMuon() != null && !don.getLoaiPhongMongMuon().isBlank()) {
            sql.append(" AND p.loaiPhong = ?");
            params.add(don.getLoaiPhongMongMuon().trim());
        }
        if (don.getSoNguoiThamDu() != null && don.getSoNguoiThamDu() > 0) {
            sql.append(" AND p.sucChuaToiDa >= ?");
            params.add(don.getSoNguoiThamDu());
        }
        sql.append("""
                 AND NOT EXISTS (
                     SELECT 1
                     FROM tblLichSuDungPhong ls
                     INNER JOIN tblDonMuonPhong d ON ls.donMuonPhongId = d.id
                     WHERE ls.phongId = p.id
                       AND d.ngaySuDung = ?
                       AND d.trangThai <> 'DaHuy'
                       AND d.gioBatDau < ?
                       AND d.gioKetThuc > ?
                """);
        params.add(ngaySql);
        params.add(gioKetThucSql);
        params.add(gioBatDauSql);
        if (don.getId() > 0) {
            sql.append(" AND d.id <> ?");
            params.add(don.getId());
        }
        sql.append(" )");

        Integer availableCount = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return availableCount == null || availableCount == 0;
    }

    public synchronized String sinhMaDonMoi() {
        List<String> existingCodes = jdbcTemplate.queryForList(
                "SELECT maDon FROM tblDonMuonPhong WHERE maDon LIKE 'DM-%'", String.class);
        int maxNumber = 0;
        for (String code : existingCodes) {
            if (code == null || !code.startsWith(PREFIX_MA_DON)) {
                continue;
            }
            String numberPart = code.substring(PREFIX_MA_DON.length()).trim();
            try {
                int value = Integer.parseInt(numberPart);
                if (value > maxNumber) {
                    maxNumber = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("DM-%03d", maxNumber + 1);
    }

    public synchronized DonMuonPhong taoDon(DonMuonPhong don) {
        if (don.getMaDon() == null || don.getMaDon().isBlank()) {
            don.setMaDon(sinhMaDonMoi());
        }
        if (don.getNgayTao() == null) {
            don.setNgayTao(LocalDate.now());
        }
        if (don.getTrangThai() == null) {
            don.setTrangThai(TrangThaiDon.ChoDuyetCap1);
        }

        int svId = don.getSinhVien() != null && don.getSinhVien().getId() > 0
                ? don.getSinhVien().getId() : don.getSinhVienId();
        int dvId = don.getDonVi() != null && don.getDonVi().getId() > 0
                ? don.getDonVi().getId() : don.getDonViId();
        Integer phongId = don.getPhongMongMuon() != null && don.getPhongMongMuon().getId() > 0
                ? don.getPhongMongMuon().getId() : don.getPhongMongMuonId();
        don.setSinhVienId(svId);
        don.setDonViId(dvId);
        don.setPhongMongMuonId(phongId);

        String sql = """
                INSERT INTO tblDonMuonPhong (
                    maDon, ngayTao, ngaySuDung, gioBatDau, gioKetThuc,
                    loaiPhongMongMuon, thietBiYeuCau, trangThai,
                    tenSuKien, mucDich, noiDung, soNguoiThamDu,
                    sinhVienId, donViId, phongMongMuonId
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, don.getMaDon());
            ps.setDate(2, Date.valueOf(don.getNgayTao()));
            ps.setDate(3, Date.valueOf(don.getNgaySuDung()));
            ps.setTime(4, Time.valueOf(don.getGioBatDau()));
            ps.setTime(5, Time.valueOf(don.getGioKetThuc()));
            ps.setString(6, don.getLoaiPhongMongMuon().trim());
            if (don.getThietBiYeuCau() != null && !don.getThietBiYeuCau().isBlank()) {
                ps.setString(7, don.getThietBiYeuCau().trim());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setString(8, don.getTrangThai().name());
            ps.setString(9, don.getTenSuKien().trim());
            ps.setString(10, don.getMucDich().trim());
            ps.setString(11, don.getNoiDung().trim());
            ps.setInt(12, don.getSoNguoiThamDu() != null ? don.getSoNguoiThamDu() : 0);
            ps.setInt(13, svId);
            ps.setInt(14, dvId);
            if (phongId != null && phongId > 0) {
                ps.setInt(15, phongId);
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            don.setId(generatedId.intValue());
        }
        return don;
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
