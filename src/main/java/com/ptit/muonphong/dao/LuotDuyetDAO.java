package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.LuotDuyet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class LuotDuyetDAO {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<LuotDuyet> rowMapper = new LuotDuyetRowMapper();

    public LuotDuyetDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int ghiLuotDuyet(LuotDuyet luotDuyet) {
        if (luotDuyet == null || luotDuyet.getDonMuonPhong() == null || luotDuyet.getNguoiDuyet() == null) {
            throw new IllegalArgumentException("Thông tin lượt duyệt không hợp lệ.");
        }
        LocalDateTime thoiDiem = luotDuyet.getThoiDiem() != null ? luotDuyet.getThoiDiem() : LocalDateTime.now();
        String sql = """
                INSERT INTO tblLuotDuyet (capDuyet, ketQua, lyDo, thoiDiem, donMuonPhongId, nguoiDuyetId)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(
                sql,
                luotDuyet.getCapDuyet(),
                luotDuyet.getKetQua(),
                luotDuyet.getLyDo(),
                Timestamp.valueOf(thoiDiem),
                luotDuyet.getDonMuonPhong().getId(),
                luotDuyet.getNguoiDuyet().getId()
        );
    }

    public List<LuotDuyet> getTheoDon(int idDon) {
        String sql = """
                SELECT ld.id, ld.capDuyet, ld.ketQua, ld.lyDo, ld.thoiDiem,
                       ld.donMuonPhongId, ld.nguoiDuyetId,
                       nd.tenDangNhap, nd.hoTen,
                       sv.maSinhVien,
                       cbd.viTriCongViec AS cbdViTri,
                       cbcs.viTriCongViec AS cbcsViTri
                FROM tblLuotDuyet ld
                JOIN tblNguoiDung nd ON ld.nguoiDuyetId = nd.id
                LEFT JOIN tblSinhVien sv ON nd.id = sv.id
                LEFT JOIN tblCanBoDoan cbd ON nd.id = cbd.id
                LEFT JOIN tblCanBoCSVC cbcs ON nd.id = cbcs.id
                WHERE ld.donMuonPhongId = ?
                ORDER BY ld.thoiDiem ASC, ld.id ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, idDon);
    }

    private static class LuotDuyetRowMapper implements RowMapper<LuotDuyet> {
        @Override
        public LuotDuyet mapRow(ResultSet rs, int rowNum) throws SQLException {
            LuotDuyet ld = new LuotDuyet();
            ld.setId(rs.getInt("id"));
            ld.setCapDuyet(rs.getInt("capDuyet"));
            ld.setKetQua(rs.getString("ketQua"));
            ld.setLyDo(rs.getString("lyDo"));

            Timestamp thoiDiem = rs.getTimestamp("thoiDiem");
            if (thoiDiem != null) {
                ld.setThoiDiem(thoiDiem.toLocalDateTime());
            }

            DonMuonPhong don = new DonMuonPhong();
            don.setId(rs.getInt("donMuonPhongId"));
            ld.setDonMuonPhong(don);

            ld.setNguoiDuyet(DaoMappingHelper.mapNguoiDungTomTat(rs, "nguoiDuyetId"));
            return ld;
        }
    }
}
