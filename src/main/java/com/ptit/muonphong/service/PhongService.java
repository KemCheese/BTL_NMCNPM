package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.DonMuonPhongDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.Phong;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PhongService {

    private final PhongDAO phongDAO;
    private final DonMuonPhongDAO donMuonPhongDAO;

    public PhongService(PhongDAO phongDAO, DonMuonPhongDAO donMuonPhongDAO) {
        this.phongDAO = phongDAO;
        this.donMuonPhongDAO = donMuonPhongDAO;
    }

    public List<Phong> timPhong(String tuKhoa) {
        return phongDAO.timPhong(tuKhoa);
    }

    public Phong getChiTiet(int idPhong) {
        return phongDAO.getChiTiet(idPhong);
    }

    public void themPhong(Phong phong) {
        if (phong == null) {
            throw new IllegalArgumentException("Thông tin phòng không được để trống.");
        }
        chuanHoaDuLieu(phong);
        phong.setTinhTrang(Phong.TINH_TRANG_HOAT_DONG);

        if (!phong.kiemTraHopLe()) {
            throw new IllegalArgumentException(
                    "Dữ liệu phòng không hợp lệ. Vui lòng kiểm tra mã, tên, loại, tòa nhà, tầng (>= 1) và sức chứa (>= 1).");
        }
        if (!phongDAO.kiemTraMaPhong(phong.getMaPhong())) {
            throw new IllegalArgumentException("Mã phòng \"" + phong.getMaPhong() + "\" đã tồn tại trong hệ thống!");
        }
        if (!phongDAO.themPhong(phong)) {
            throw new IllegalStateException("Không thể thêm phòng vào cơ sở dữ liệu.");
        }
    }

    public void capNhatPhong(Phong phong) {
        if (phong == null || phong.getId() <= 0) {
            throw new IllegalArgumentException("Không xác định được phòng cần cập nhật.");
        }
        Phong hienTai = phongDAO.getChiTiet(phong.getId());
        if (hienTai == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng cần cập nhật.");
        }

        phong.setMaPhong(hienTai.getMaPhong());
        phong.setTinhTrang(hienTai.getTinhTrang());
        chuanHoaDuLieu(phong);

        if (!phong.kiemTraHopLe()) {
            throw new IllegalArgumentException(
                    "Dữ liệu phòng không hợp lệ. Vui lòng kiểm tra tên, loại, tòa nhà, tầng (>= 1) và sức chứa (>= 1).");
        }
        if (!phongDAO.capNhatPhong(phong)) {
            throw new IllegalStateException("Không thể cập nhật thông tin phòng vào cơ sở dữ liệu.");
        }
    }

    public List<DonMuonPhong> layDonDaDuyetTuongLaiTheoPhong(int idPhong) {
        Phong phong = phongDAO.getChiTiet(idPhong);
        if (phong == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng cần ngưng sử dụng.");
        }
        return donMuonPhongDAO.timDonDaDuyetTheoPhong(phong, LocalDate.now());
    }

    public void ngungSuDung(int idPhong) {
        if (idPhong <= 0) {
            throw new IllegalArgumentException("Không xác định được phòng cần ngưng sử dụng.");
        }
        Phong phong = phongDAO.getChiTiet(idPhong);
        if (phong == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng cần ngưng sử dụng.");
        }
        if (!phong.isDangHoatDong()) {
            throw new IllegalArgumentException("Phòng \"" + phong.getMaPhong() + "\" đã ở trạng thái Ngưng sử dụng.");
        }
        if (!phongDAO.ngungSuDung(idPhong)) {
            throw new IllegalStateException("Không thể cập nhật trạng thái ngưng sử dụng phòng.");
        }
    }

    private void chuanHoaDuLieu(Phong phong) {
        phong.setMaPhong(trimToNull(phong.getMaPhong()));
        phong.setTenPhong(trimToNull(phong.getTenPhong()));
        phong.setToaNha(trimToNull(phong.getToaNha()));
        phong.setLoaiPhong(trimToNull(phong.getLoaiPhong()));
        phong.setTrangThietBi(trimToNull(phong.getTrangThietBi()));
        phong.setMoTa(trimToNull(phong.getMoTa()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
