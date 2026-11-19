package com.ptit.muonphong.dao;

import com.ptit.muonphong.entity.CanBoCSVC;
import com.ptit.muonphong.entity.CanBoDoan;
import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.entity.SinhVien;

import java.sql.ResultSet;
import java.sql.SQLException;

final class DaoMappingHelper {

    private DaoMappingHelper() {
    }

    static NguoiDung mapNguoiDungTomTat(ResultSet rs, String idColumn) throws SQLException {
        NguoiDung nguoiDung;
        String maSinhVien = rs.getString("maSinhVien");
        String cbdViTri = rs.getString("cbdViTri");
        String cbcsViTri = rs.getString("cbcsViTri");

        if (cbdViTri != null) {
            CanBoDoan cbd = new CanBoDoan();
            cbd.setViTriCongViec(cbdViTri);
            nguoiDung = cbd;
        } else if (cbcsViTri != null) {
            CanBoCSVC cbcs = new CanBoCSVC();
            cbcs.setViTriCongViec(cbcsViTri);
            nguoiDung = cbcs;
        } else {
            SinhVien sv = new SinhVien();
            sv.setMaSinhVien(maSinhVien);
            nguoiDung = sv;
        }

        nguoiDung.setId(rs.getInt(idColumn));
        nguoiDung.setTenDangNhap(rs.getString("tenDangNhap"));
        nguoiDung.setHoTen(rs.getString("hoTen"));
        return nguoiDung;
    }
}
