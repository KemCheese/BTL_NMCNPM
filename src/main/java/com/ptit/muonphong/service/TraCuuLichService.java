package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.LichSuDungPhongDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.LichSuDungPhong;
import com.ptit.muonphong.entity.Phong;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TraCuuLichService {

    private final PhongDAO phongDAO;
    private final LichSuDungPhongDAO lichSuDungPhongDAO;

    public TraCuuLichService(PhongDAO phongDAO, LichSuDungPhongDAO lichSuDungPhongDAO) {
        this.phongDAO = phongDAO;
        this.lichSuDungPhongDAO = lichSuDungPhongDAO;
    }

    public List<PhongLichItem> traCuuLich(LocalDate ngay, String loaiPhong, Integer sucChua, String tinhTrang) {
        LocalDate ngayTraCuu = ngay != null ? ngay : LocalDate.now();
        List<Phong> dsPhong = phongDAO.locPhong(ngayTraCuu, loaiPhong, sucChua, tinhTrang);
        List<PhongLichItem> ketQua = new ArrayList<>();
        for (Phong phong : dsPhong) {
            List<LichSuDungPhong> lichTrongNgay = lichSuDungPhongDAO.getLichPhong(phong, ngayTraCuu);
            ketQua.add(new PhongLichItem(phong, lichTrongNgay));
        }
        return ketQua;
    }

    public PhongLichItem getChiTietPhong(int idPhong, LocalDate ngay) {
        Phong phong = phongDAO.getChiTiet(idPhong);
        if (phong == null) {
            return null;
        }
        LocalDate ngayTraCuu = ngay != null ? ngay : LocalDate.now();
        List<LichSuDungPhong> lichTrongNgay = lichSuDungPhongDAO.getLichPhong(phong, ngayTraCuu);
        return new PhongLichItem(phong, lichTrongNgay);
    }

    public List<String> getDanhSachLoaiPhong() {
        return phongDAO.findAllLoaiPhong();
    }

    public List<String> getDanhSachTinhTrang() {
        return phongDAO.findAllTinhTrang();
    }

    public static class PhongLichItem {
        private final Phong phong;
        private final List<LichSuDungPhong> lichTrongNgay;

        public PhongLichItem(Phong phong, List<LichSuDungPhong> lichTrongNgay) {
            this.phong = phong;
            this.lichTrongNgay = lichTrongNgay != null ? lichTrongNgay : List.of();
        }

        public Phong getPhong() {
            return phong;
        }

        public List<LichSuDungPhong> getLichTrongNgay() {
            return lichTrongNgay;
        }

        public boolean isCoLichTrongNgay() {
            return !lichTrongNgay.isEmpty();
        }

        public boolean isSanSangMuon() {
            return phong != null && phong.isDangHoatDong();
        }
    }
}
