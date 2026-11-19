package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.Phong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PhongDAO {

    private static final String BASE_SELECT = """
            SELECT id, maPhong, tenPhong, toaNha, tang, sucChuaToiDa,
                   loaiPhong, trangThietBi, tinhTrang, moTa
            FROM tblPhong
            """;

    private final JdbcTemplate jdbcTemplate;

    public PhongDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Phong> locPhong(LocalDate ngay, String loaiPhong, Integer sucChua, String tinhTrang) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (loaiPhong != null && !loaiPhong.isBlank()) {
            sql.append(" AND loaiPhong = ?");
            params.add(loaiPhong.trim());
        }
        if (sucChua != null && sucChua > 0) {
            sql.append(" AND sucChuaToiDa >= ?");
            params.add(sucChua);
        }
        if (tinhTrang != null && !tinhTrang.isBlank()) {
            sql.append(" AND tinhTrang = ?");
            params.add(tinhTrang.trim());
        }

        sql.append(" ORDER BY toaNha ASC, tang ASC, maPhong ASC");
        return jdbcTemplate.query(sql.toString(), new PhongRowMapper(), params.toArray());
    }

    public Phong getChiTiet(int idPhong) {
        String sql = BASE_SELECT + " WHERE id = ?";
        List<Phong> rows = jdbcTemplate.query(sql, new PhongRowMapper(), idPhong);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<String> findAllLoaiPhong() {
        String sql = "SELECT DISTINCT loaiPhong FROM tblPhong ORDER BY loaiPhong ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findAllTinhTrang() {
        String sql = "SELECT DISTINCT tinhTrang FROM tblPhong ORDER BY tinhTrang ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private static class PhongRowMapper implements RowMapper<Phong> {
        @Override
        public Phong mapRow(ResultSet rs, int rowNum) throws SQLException {
            Phong phong = new Phong();
            phong.setId(rs.getInt("id"));
            phong.setMaPhong(rs.getString("maPhong"));
            phong.setTenPhong(rs.getString("tenPhong"));
            phong.setToaNha(rs.getString("toaNha"));
            phong.setTang(rs.getInt("tang"));
            phong.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
            phong.setLoaiPhong(rs.getString("loaiPhong"));
            phong.setTrangThietBi(rs.getString("trangThietBi"));
            phong.setTinhTrang(rs.getString("tinhTrang"));
            phong.setMoTa(rs.getString("moTa"));
            return phong;
        }
    }
}
