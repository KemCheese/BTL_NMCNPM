package com.ptit.muonphong;

import com.ptit.muonphong.config.CustomUserDetails;
import com.ptit.muonphong.config.CustomUserDetailsService;
import com.ptit.muonphong.config.SecurityConfig;
import com.ptit.muonphong.controller.BaoCaoController;
import com.ptit.muonphong.dao.DonMuonPhongDAO;
import com.ptit.muonphong.dao.LichSuDungPhongDAO;
import com.ptit.muonphong.dao.NguoiDungDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.CanBoCSVC;
import com.ptit.muonphong.entity.CanBoDoan;
import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.DonVi;
import com.ptit.muonphong.entity.Phong;
import com.ptit.muonphong.entity.SinhVien;
import com.ptit.muonphong.entity.TrangThaiDon;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.BaoCaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaoCaoController.class)
@Import({ SecurityConfig.class, CustomUserDetailsService.class, AuthService.class })
class Module4Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BaoCaoService baoCaoService;

    @MockBean
    private NguoiDungDAO nguoiDungDAO;

    private CanBoCSVC createCanBoCSVC() {
        CanBoCSVC cbcs = new CanBoCSVC();
        cbcs.setId(3);
        cbcs.setTenDangNhap("cb_cuong");
        cbcs.setMatKhau("123456");
        cbcs.setHoTen("Lê Văn Cường");
        cbcs.setTrangThai("Hoạt động");
        cbcs.setViTriCongViec("Chuyên viên Trung tâm Quản lý phòng");
        return cbcs;
    }

    private SinhVien createSinhVien() {
        SinhVien sv = new SinhVien();
        sv.setId(1);
        sv.setTenDangNhap("sv_an");
        sv.setMatKhau("123456");
        sv.setHoTen("Nguyễn Văn An");
        sv.setTrangThai("Hoạt động");
        sv.setMaSinhVien("B22DCCN001");
        return sv;
    }

    private CanBoDoan createCanBoDoan() {
        CanBoDoan cbd = new CanBoDoan();
        cbd.setId(2);
        cbd.setTenDangNhap("cb_binh");
        cbd.setMatKhau("123456");
        cbd.setHoTen("Trần Thị Bình");
        cbd.setTrangThai("Hoạt động");
        cbd.setViTriCongViec("Cán bộ Văn phòng Đoàn");
        return cbd;
    }

    private Phong createSamplePhong(int id, String maPhong, String tenPhong) {
        Phong p = new Phong();
        p.setId(id);
        p.setMaPhong(maPhong);
        p.setTenPhong(tenPhong);
        p.setToaNha("A2");
        p.setTang(1);
        p.setSucChuaToiDa(300);
        p.setLoaiPhong("Hội trường");
        p.setTinhTrang(Phong.TINH_TRANG_HOAT_DONG);
        return p;
    }

    @Test
    void unauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/bao-cao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void nonCsvRolesReceive403() throws Exception {
        mockMvc.perform(get("/bao-cao").with(user(new CustomUserDetails(createSinhVien()))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));

        mockMvc.perform(get("/bao-cao").with(user(new CustomUserDetails(createCanBoDoan()))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void canBoCsvViewsDefaultReportAndChart() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        BaoCaoService.ThongKeDon row = new BaoCaoService.ThongKeDon(htA2, 2, 6.0, 1.29);
        when(baoCaoService.xemBaoCaoTheoPhong(BaoCaoService.TIEU_CHI_THEO_PHONG, firstDay, lastDay))
                .thenReturn(List.of(row));

        mockMvc.perform(get("/bao-cao").with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Hệ thống mượn phòng trực tuyến PTIT")))
                .andExpect(content().string(containsString("href=\"/xac-nhan-dang-xuat\"")))
                .andExpect(content().string(not(containsString("id=\"logoutModal\""))))
                .andExpect(content().string(not(containsString("openLogoutModal"))))
                .andExpect(content().string(containsString("HT-A2")))
                .andExpect(content().string(containsString("Hội trường A2")))
                .andExpect(content().string(containsString("6.00 giờ")))
                .andExpect(content().string(containsString("1.29%")))
                .andExpect(content().string(containsString("Biểu đồ tổng giờ sử dụng theo phòng")))
                .andExpect(content().string(containsString("height: 100.00%")));
    }

    @Test
    void canBoCsvFiltersByDateRangeAndShowsSortedChart() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);

        Phong htB1 = createSamplePhong(2, "HT-B1", "Hội trường B1");
        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        BaoCaoService.ThongKeDon row1 = new BaoCaoService.ThongKeDon(htB1, 3, 9.0, 1.94);
        BaoCaoService.ThongKeDon row2 = new BaoCaoService.ThongKeDon(htA2, 1, 3.0, 0.65);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay))
                .thenReturn(List.of(row1, row2));

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-01")
                .param("denNgay", "2026-12-31")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("HT-B1")))
                .andExpect(content().string(containsString("9.00h")))
                .andExpect(content().string(containsString("3.00h")))
                .andExpect(content().string(containsString("height: 33.33%")));
    }

    @Test
    void canBoCsvShowsErrorMessageWhenInvalidDateRangeOrDbError() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 12, 31);
        LocalDate denNgay = LocalDate.of(2026, 12, 1);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay))
                .thenThrow(new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng Từ ngày."));

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-31")
                .param("denNgay", "2026-12-01")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Đến ngày phải lớn hơn hoặc bằng Từ ngày.")))
                .andExpect(content().string(not(containsString("Biểu đồ tổng giờ sử dụng theo phòng"))));

        LocalDate validTu = LocalDate.of(2026, 12, 1);
        LocalDate validDen = LocalDate.of(2026, 12, 30);
        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", validTu, validDen))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-01")
                .param("denNgay", "2026-12-30")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Lỗi kết nối cơ sở dữ liệu khi tải báo cáo thống kê")));
    }

    @Test
    void canBoCsvShowsEmptyNoticeWhenNoUsageData() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 11, 1);
        LocalDate denNgay = LocalDate.of(2026, 11, 30);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay))
                .thenReturn(List.of());

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-11-01")
                .param("denNgay", "2026-11-30")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Không có dữ liệu sử dụng phòng trong khoảng thời gian đã chọn.")))
                .andExpect(content().string(not(containsString("Biểu đồ tổng giờ sử dụng theo phòng"))));
    }

    @Test
    void canBoCsvViewsChiTietDongForSelectedRoom() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);

        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        BaoCaoService.ThongKeDon row = new BaoCaoService.ThongKeDon(htA2, 1, 3.0, 0.65);

        DonVi clb = new DonVi();
        clb.setId(1);
        clb.setTenCLB("CLB Tin học");

        DonMuonPhong don = new DonMuonPhong();
        don.setId(3);
        don.setMaDon("DM-003");
        don.setTenSuKien("Sinh hoạt chuyên đề cuối năm");
        don.setDonVi(clb);
        don.setNgaySuDung(LocalDate.of(2026, 12, 30));
        don.setGioBatDau(LocalTime.of(8, 0));
        don.setGioKetThuc(LocalTime.of(11, 0));
        don.setTrangThai(TrangThaiDon.DaPheDuyet);

        BaoCaoService.ChiTietPhongBaoCao chiTiet = new BaoCaoService.ChiTietPhongBaoCao(
                htA2, List.of(don), Map.of(3, 3.0), 3.0);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay)).thenReturn(List.of(row));
        when(baoCaoService.xemChiTietDong(1, tuNgay, denNgay)).thenReturn(chiTiet);

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-01")
                .param("denNgay", "2026-12-31")
                .param("chiTietPhongId", "1")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Chi tiết đơn đã phê duyệt — HT-A2 (Hội trường A2)")))
                .andExpect(content().string(containsString("DM-003")))
                .andExpect(content().string(containsString("Sinh hoạt chuyên đề cuối năm")))
                .andExpect(content().string(containsString("CLB Tin học")))
                .andExpect(content().string(containsString("2026-12-30")))
                .andExpect(content().string(containsString("08:00 - 11:00")))
                .andExpect(content().string(containsString("Đóng chi tiết")));
    }

    @Test
    void canBoCsvShowsChiTietErrorWhenRoomNotFound() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);

        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        BaoCaoService.ThongKeDon row = new BaoCaoService.ThongKeDon(htA2, 1, 3.0, 0.65);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay)).thenReturn(List.of(row));
        when(baoCaoService.xemChiTietDong(999, tuNgay, denNgay))
                .thenThrow(new IllegalArgumentException("Không tìm thấy thông tin phòng cần xem chi tiết."));

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-01")
                .param("denNgay", "2026-12-31")
                .param("chiTietPhongId", "999")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("HT-A2")))
                .andExpect(content().string(containsString("Không tìm thấy thông tin phòng cần xem chi tiết.")));
    }

    @Test
    void canBoCsvSkipsChiTietWhenDateRangeInvalid() throws Exception {
        LocalDate tuNgay = LocalDate.of(2026, 12, 31);
        LocalDate denNgay = LocalDate.of(2026, 12, 1);

        when(baoCaoService.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay))
                .thenThrow(new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng Từ ngày."));

        mockMvc.perform(get("/bao-cao")
                .param("tieuChi", "Theo phòng")
                .param("tuNgay", "2026-12-31")
                .param("denNgay", "2026-12-01")
                .param("chiTietPhongId", "1")
                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                .andExpect(status().isOk())
                .andExpect(view().name("bao-cao"))
                .andExpect(content().string(containsString("Đến ngày phải lớn hơn hoặc bằng Từ ngày.")));

        verify(baoCaoService, never()).xemChiTietDong(anyInt(), any(), any());
    }

    @Test
    void baoCaoServiceRejectsInvalidDatesAndCriteria() {
        LichSuDungPhongDAO daoMock = mock(LichSuDungPhongDAO.class);
        PhongDAO phongDaoMock = mock(PhongDAO.class);
        DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
        BaoCaoService service = new BaoCaoService(daoMock, phongDaoMock, donDaoMock);

        LocalDate d1 = LocalDate.of(2026, 12, 10);
        LocalDate d2 = LocalDate.of(2026, 12, 1);

        assertThrows(IllegalArgumentException.class,
                () -> service.xemBaoCaoTheoPhong("Theo phòng", d1, d2));
        assertThrows(IllegalArgumentException.class,
                () -> service.xemBaoCaoTheoPhong("Theo phòng", d1, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.xemBaoCaoTheoPhong("Theo phòng", null, d2));
        assertThrows(IllegalArgumentException.class,
                () -> service.xemBaoCaoTheoPhong("Theo đơn vị", d2, d1));

        verifyNoInteractions(daoMock, phongDaoMock, donDaoMock);
    }

    @Test
    void baoCaoServiceComputes15HoursPerDayAndSortsDesc() {
        LichSuDungPhongDAO daoMock = mock(LichSuDungPhongDAO.class);
        PhongDAO phongDaoMock = mock(PhongDAO.class);
        DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
        BaoCaoService service = new BaoCaoService(daoMock, phongDaoMock, donDaoMock);

        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 2);

        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        Phong htB1 = createSamplePhong(2, "HT-B1", "Hội trường B1");

        BaoCaoService.ThongKeDon low = new BaoCaoService.ThongKeDon(htA2, 1, 3.0, 0.0);
        BaoCaoService.ThongKeDon high = new BaoCaoService.ThongKeDon(htB1, 2, 7.5, 0.0);

        when(daoMock.thongKe("Theo phòng", tuNgay, denNgay)).thenReturn(List.of(low, high));

        List<BaoCaoService.ThongKeDon> result = service.xemBaoCaoTheoPhong("Theo phòng", tuNgay, denNgay);
        assertEquals(2, result.size());
        assertEquals("HT-B1", result.get(0).getPhong().getMaPhong());
        assertEquals(25.0, result.get(0).getTyLeSuDung(), 0.001);
        assertEquals("HT-A2", result.get(1).getPhong().getMaPhong());
        assertEquals(10.0, result.get(1).getTyLeSuDung(), 0.001);
    }

    @Test
    void baoCaoServiceXemChiTietDongReturnsOrdersAndDurations() {
        LichSuDungPhongDAO daoMock = mock(LichSuDungPhongDAO.class);
        PhongDAO phongDaoMock = mock(PhongDAO.class);
        DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
        BaoCaoService service = new BaoCaoService(daoMock, phongDaoMock, donDaoMock);

        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);
        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");

        DonMuonPhong d1 = new DonMuonPhong();
        d1.setId(3);
        d1.setMaDon("DM-003");
        d1.setGioBatDau(LocalTime.of(8, 30));
        d1.setGioKetThuc(LocalTime.of(10, 0));

        DonMuonPhong d2 = new DonMuonPhong();
        d2.setId(4);
        d2.setMaDon("DM-004");
        d2.setGioBatDau(LocalTime.of(14, 0));
        d2.setGioKetThuc(LocalTime.of(17, 0));

        when(phongDaoMock.getChiTiet(1)).thenReturn(htA2);
        when(donDaoMock.timDonDaDuyetTheoPhong(htA2, tuNgay, denNgay)).thenReturn(List.of(d1, d2));

        BaoCaoService.ChiTietPhongBaoCao detail = service.xemChiTietDong(1, tuNgay, denNgay);
        assertEquals("HT-A2", detail.getPhong().getMaPhong());
        assertEquals(2, detail.getDsDon().size());
        assertEquals(1.5, detail.getThoiLuongGioMap().get(3), 0.001);
        assertEquals(3.0, detail.getThoiLuongGioMap().get(4), 0.001);
        assertEquals(4.5, detail.getTongGio(), 0.001);

        verify(phongDaoMock).getChiTiet(1);
        verify(donDaoMock).timDonDaDuyetTheoPhong(htA2, tuNgay, denNgay);
    }

    @Test
    void baoCaoServiceXemChiTietDongRejectsMissingRoomOrInvalidDates() {
        LichSuDungPhongDAO daoMock = mock(LichSuDungPhongDAO.class);
        PhongDAO phongDaoMock = mock(PhongDAO.class);
        DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
        BaoCaoService service = new BaoCaoService(daoMock, phongDaoMock, donDaoMock);

        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);

        assertThrows(IllegalArgumentException.class,
                () -> service.xemChiTietDong(1, denNgay, tuNgay));
        assertThrows(IllegalArgumentException.class,
                () -> service.xemChiTietDong(0, tuNgay, denNgay));

        when(phongDaoMock.getChiTiet(99)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.xemChiTietDong(99, tuNgay, denNgay));

        verifyNoInteractions(donDaoMock);
    }

    @Test
    void tinhTyLeSuDungMatches15HourOperatingWindow() {
        assertEquals(15L, BaoCaoService.SO_GIO_HOAT_DONG_MOI_NGAY);
        assertEquals(20.0, BaoCaoService.tinhTyLeSuDung(
                3.0, LocalDate.of(2026, 12, 30), LocalDate.of(2026, 12, 30)), 0.001);
        assertEquals(100.0, BaoCaoService.tinhTyLeSuDung(
                15.0, LocalDate.of(2026, 12, 30), LocalDate.of(2026, 12, 30)), 0.001);
        assertEquals(0.65, BaoCaoService.tinhTyLeSuDung(
                3.0, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)), 0.001);
        assertEquals(0.0, BaoCaoService.tinhTyLeSuDung(
                3.0, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 12, 1)), 0.001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lichSuDungPhongDaoThongKeValidatesInputsAndQueriesDaPheDuyet() {
        JdbcTemplate jdbcMock = mock(JdbcTemplate.class);
        LichSuDungPhongDAO dao = new LichSuDungPhongDAO(jdbcMock);

        LocalDate tuNgay = LocalDate.of(2026, 12, 1);
        LocalDate denNgay = LocalDate.of(2026, 12, 31);

        assertTrue(dao.thongKe("Theo phòng", null, denNgay).isEmpty());
        assertTrue(dao.thongKe("Theo phòng", tuNgay, null).isEmpty());
        assertTrue(dao.thongKe("Theo phòng", denNgay, tuNgay).isEmpty());
        assertTrue(dao.thongKe("Theo đơn vị", tuNgay, denNgay).isEmpty());
        verifyNoInteractions(jdbcMock);

        Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2");
        BaoCaoService.ThongKeDon expectedRow = new BaoCaoService.ThongKeDon(htA2, 1, 3.0, 0.65);

        when(jdbcMock.query(anyString(), any(RowMapper.class),
                eq("DaPheDuyet"), eq(Date.valueOf(tuNgay)), eq(Date.valueOf(denNgay))))
                .thenReturn(List.of(expectedRow));

        List<BaoCaoService.ThongKeDon> rows = dao.thongKe("Theo phòng", tuNgay, denNgay);
        assertEquals(1, rows.size());
        assertEquals("HT-A2", rows.get(0).getPhong().getMaPhong());
        assertEquals(3.0, rows.get(0).getTongGio(), 0.001);
        assertEquals(0.65, rows.get(0).getTyLeSuDung(), 0.001);
    }
}
