package com.ptit.muonphong.service;

import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DuyetCap2Service {

    private final DonMuonPhongDAO donMuonPhongDAO;
    private final PhongDAO phongDAO;
    private final LichSuDungPhongDAO lichSuDungPhongDAO;
    private final LuotDuyetDAO luotDuyetDAO;
    private final LichSuDonDAO lichSuDonDAO;

    public DuyetCap2Service(DonMuonPhongDAO donMuonPhongDAO,
                            PhongDAO phongDAO,
                            LichSuDungPhongDAO lichSuDungPhongDAO,
                            LuotDuyetDAO luotDuyetDAO,
                            LichSuDonDAO lichSuDonDAO) {
        this.donMuonPhongDAO = donMuonPhongDAO;
        this.phongDAO = phongDAO;
        this.lichSuDungPhongDAO = lichSuDungPhongDAO;
        this.luotDuyetDAO = luotDuyetDAO;
        this.lichSuDonDAO = lichSuDonDAO;
    }

    public List<DonMuonPhong> layDanhSachChoDuyetCap2() {
        return donMuonPhongDAO.timTheoTrangThai(TrangThaiDon.ChoDuyetCap2);
    }

    public List<DonMuonPhong> layDanhSachDaPheDuyetChuaSuDung() {
        LocalDateTime now = LocalDateTime.now();
        List<DonMuonPhong> danhSach = donMuonPhongDAO.timDonDaDuyetChuaSuDung(now.toLocalDate(), now.toLocalTime());
        if (danhSach.isEmpty()) {
            return danhSach;
        }
        List<Integer> donIds = danhSach.stream().map(DonMuonPhong::getId).toList();
        Map<Integer, Phong> phongMap = lichSuDungPhongDAO.getPhongBoTriTheoCacDon(donIds);
        if (phongMap != null) {
            for (DonMuonPhong don : danhSach) {
                Phong phongBoTri = phongMap.get(don.getId());
                if (phongBoTri != null) {
                    LichSuDungPhong lich = new LichSuDungPhong();
                    lich.setPhong(phongBoTri);
                    lich.setDonMuonPhong(don);
                    don.setLichSuDungPhong(lich);
                }
            }
        }
        return danhSach;
    }

    public DonMuonPhong xemChiTietDon(int idDon) {
        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        don.setLuotDuyets(luotDuyetDAO.getTheoDon(idDon));
        don.setLichSuDons(lichSuDonDAO.getTheoDon(idDon));
        Phong phongBoTri = lichSuDungPhongDAO.getPhongBoTriTheoDon(idDon);
        if (phongBoTri != null) {
            LichSuDungPhong lich = new LichSuDungPhong();
            lich.setPhong(phongBoTri);
            lich.setDonMuonPhong(don);
            don.setLichSuDungPhong(lich);
        }
        return don;
    }

    public List<Phong> kiemTraPhongTrong(int idDon) {
        DonMuonPhong don = donMuonPhongDAO.getChiTiet(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        int sucChua = resolveSoNguoiThamDu(don);
        return phongDAO.timPhongTrong(
                don.getNgaySuDung(),
                don.getGioBatDau(),
                don.getGioKetThuc(),
                don.getLoaiPhongMongMuon(),
                sucChua,
                null
        );
    }

    @Transactional
    public void pheDuyetVaBoTriPhong(int idDon, Integer idPhong, NguoiDung canBoCSVC) {
        validateCanBoCSVC(canBoCSVC);
        if (idPhong == null || idPhong <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn phòng phù hợp để bố trí.");
        }

        DonMuonPhong don = donMuonPhongDAO.lockDonById(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        if (don.getTrangThai() != TrangThaiDon.ChoDuyetCap2) {
            throw new IllegalStateException("Chỉ có thể phê duyệt đơn ở trạng thái Chờ duyệt cấp 2.");
        }

        Phong phong = phongDAO.lockPhongById(idPhong);
        validatePhongPhuHopVaKhongTrung(don, phong);

        LichSuDungPhong lichSuDungPhong = new LichSuDungPhong();
        lichSuDungPhong.setPhong(phong);
        lichSuDungPhong.setDonMuonPhong(don);
        lichSuDungPhongDAO.themKhungGio(lichSuDungPhong);

        int updated = donMuonPhongDAO.capNhatTrangThai(idDon, TrangThaiDon.DaPheDuyet);
        if (updated == 0) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn mượn phòng.");
        }

        LocalDateTime now = LocalDateTime.now();

        LuotDuyet luotDuyet = new LuotDuyet();
        luotDuyet.setCapDuyet(2);
        luotDuyet.setKetQua("Phê duyệt");
        luotDuyet.setLyDo(null);
        luotDuyet.setThoiDiem(now);
        luotDuyet.setDonMuonPhong(don);
        luotDuyet.setNguoiDuyet(canBoCSVC);
        luotDuyetDAO.ghiLuotDuyet(luotDuyet);

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(TrangThaiDon.ChoDuyetCap2);
        lichSuDon.setTrangThaiMoi(TrangThaiDon.DaPheDuyet);
        lichSuDon.setGhiChu("Duyệt cấp 2 và bố trí phòng " + phong.getMaPhong());
        lichSuDon.setThoiDiem(now);
        lichSuDon.setDonMuonPhong(don);
        lichSuDon.setNguoiThucHien(canBoCSVC);
        lichSuDonDAO.ghiThayDoi(lichSuDon);
    }

    @Transactional
    public void tuChoiDon(int idDon, String lyDo, NguoiDung canBoCSVC) {
        validateCanBoCSVC(canBoCSVC);
        if (lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        }
        String lyDoChuan = lyDo.trim();

        DonMuonPhong don = donMuonPhongDAO.lockDonById(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        if (don.getTrangThai() != TrangThaiDon.ChoDuyetCap2) {
            throw new IllegalStateException("Chỉ có thể từ chối đơn ở trạng thái Chờ duyệt cấp 2.");
        }

        int updated = donMuonPhongDAO.capNhatTrangThai(idDon, TrangThaiDon.TuChoiCap2);
        if (updated == 0) {
            throw new IllegalStateException("Không thể cập nhật trạng thái đơn mượn phòng.");
        }

        LocalDateTime now = LocalDateTime.now();

        LuotDuyet luotDuyet = new LuotDuyet();
        luotDuyet.setCapDuyet(2);
        luotDuyet.setKetQua("Từ chối");
        luotDuyet.setLyDo(lyDoChuan);
        luotDuyet.setThoiDiem(now);
        luotDuyet.setDonMuonPhong(don);
        luotDuyet.setNguoiDuyet(canBoCSVC);
        luotDuyetDAO.ghiLuotDuyet(luotDuyet);

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(TrangThaiDon.ChoDuyetCap2);
        lichSuDon.setTrangThaiMoi(TrangThaiDon.TuChoiCap2);
        lichSuDon.setGhiChu("Duyệt cấp 2: Từ chối - " + lyDoChuan);
        lichSuDon.setThoiDiem(now);
        lichSuDon.setDonMuonPhong(don);
        lichSuDon.setNguoiThucHien(canBoCSVC);
        lichSuDonDAO.ghiThayDoi(lichSuDon);
    }

    @Transactional
    public void doiPhong(int idDon, Integer idPhongMoi, NguoiDung canBoCSVC) {
        validateCanBoCSVC(canBoCSVC);
        if (idPhongMoi == null || idPhongMoi <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn phòng mới để thực hiện đổi phòng.");
        }

        DonMuonPhong don = donMuonPhongDAO.lockDonById(idDon);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn mượn phòng.");
        }
        if (don.getTrangThai() != TrangThaiDon.DaPheDuyet) {
            throw new IllegalStateException("Chỉ được đổi phòng đối với đơn ở trạng thái Đã phê duyệt.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (don.getNgaySuDung() == null || don.getGioBatDau() == null
                || !LocalDateTime.of(don.getNgaySuDung(), don.getGioBatDau()).isAfter(now)) {
            throw new IllegalStateException("Không thể đổi phòng khi đơn đã đến hoặc qua giờ bắt đầu sử dụng.");
        }

        Phong phongCu = lichSuDungPhongDAO.getPhongBoTriTheoDon(idDon);
        if (phongCu == null) {
            throw new IllegalStateException("Đơn chưa có thông tin phòng được bố trí để đổi phòng.");
        }
        if (phongCu.getId() == idPhongMoi) {
            throw new IllegalArgumentException("Vui lòng chọn phòng mới khác với phòng đang được bố trí.");
        }

        Phong phongMoi = phongDAO.lockPhongById(idPhongMoi);
        validatePhongPhuHopVaKhongTrung(don, phongMoi);

        int updated = lichSuDungPhongDAO.doiPhongChoDon(idDon, idPhongMoi);
        if (updated == 0) {
            throw new IllegalStateException("Không thể cập nhật phòng mới cho đơn mượn phòng.");
        }

        LichSuDon lichSuDon = new LichSuDon();
        lichSuDon.setTrangThaiCu(TrangThaiDon.DaPheDuyet);
        lichSuDon.setTrangThaiMoi(TrangThaiDon.DaPheDuyet);
        lichSuDon.setGhiChu("Đổi phòng từ " + phongCu.getMaPhong() + " sang " + phongMoi.getMaPhong());
        lichSuDon.setThoiDiem(now);
        lichSuDon.setDonMuonPhong(don);
        lichSuDon.setNguoiThucHien(canBoCSVC);
        lichSuDonDAO.ghiThayDoi(lichSuDon);
    }

    private void validateCanBoCSVC(NguoiDung nguoiDuyet) {
        if (!(nguoiDuyet instanceof CanBoCSVC) || nguoiDuyet.getId() <= 0) {
            throw new IllegalStateException("Chỉ Cán bộ Trung tâm quản lý phòng mới có quyền duyệt đơn cấp 2.");
        }
    }

    private void validatePhongPhuHopVaKhongTrung(DonMuonPhong don, Phong phong) {
        if (phong == null) {
            throw new IllegalArgumentException("Không tìm thấy phòng được chọn.");
        }
        if (!phong.isDangHoatDong()) {
            throw new IllegalStateException("Phòng " + phong.getMaPhong() + " hiện không ở trạng thái Đang hoạt động.");
        }
        int soNguoi = resolveSoNguoiThamDu(don);
        if (phong.getSucChuaToiDa() == null || phong.getSucChuaToiDa() < soNguoi) {
            throw new IllegalStateException("Phòng " + phong.getMaPhong() + " không đủ sức chứa cho sự kiện.");
        }
        if (donMuonPhongDAO.kiemTraTrungLich(don, phong)) {
            throw new IllegalStateException("Phòng " + phong.getMaPhong()
                    + " vừa bị giữ chỗ hoặc trùng lịch trong khung giờ này. Vui lòng chọn phòng khác.");
        }
    }

    private int resolveSoNguoiThamDu(DonMuonPhong don) {
        if (don.getSoNguoiThamDu() != null) {
            return don.getSoNguoiThamDu();
        }
        if (don.getSuKien() != null) {
            return don.getSuKien().getSoNguoiThamDu();
        }
        return 0;
    }
}
