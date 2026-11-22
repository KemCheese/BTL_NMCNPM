package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.DuyetCap1Controller;
import com.ptit.muonphong.controller.DuyetCap2Controller;
import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.DuyetCap1Service;
import com.ptit.muonphong.service.DuyetCap2Service;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class Module2Test {

    private static CanBoDoan createCanBoDoan() {
        CanBoDoan cbd = new CanBoDoan();
        cbd.setId(2);
        cbd.setTenDangNhap("cb_binh");
        cbd.setMatKhau("123456");
        cbd.setHoTen("Trần Thị Bình");
        cbd.setTrangThai("Hoạt động");
        cbd.setViTriCongViec("Cán bộ Văn phòng Đoàn Thanh niên");
        return cbd;
    }

    private static SinhVien createSinhVien(int id, String tenCLB, String maDonVi) {
        DonVi dv = new DonVi();
        dv.setId(id);
        dv.setMaDonVi(maDonVi);
        dv.setTenCLB(tenCLB);

        SinhVien sv = new SinhVien();
        sv.setId(id);
        sv.setTenDangNhap(id == 1 ? "sv_an" : "sv_khoa");
        sv.setHoTen(id == 1 ? "Nguyễn Văn An" : "Phạm Minh Khóa");
        sv.setMaSinhVien(id == 1 ? "B22DCCN001" : "B22DCCN002");
        sv.setKhoaHoc("2022");
        sv.setNganhHoc("Công nghệ thông tin");
        sv.setTrangThai("Hoạt động");
        sv.setDonVi(dv);
        return sv;
    }

    private static CanBoCSVC createCanBoCSVC() {
        CanBoCSVC cbcs = new CanBoCSVC();
        cbcs.setId(3);
        cbcs.setTenDangNhap("cb_cuong");
        cbcs.setHoTen("Lê Văn Cường");
        cbcs.setTrangThai("Hoạt động");
        cbcs.setViTriCongViec("Chuyên viên Trung tâm Quản lý phòng");
        return cbcs;
    }

    private static DonMuonPhong createDonChoDuyetCap1(int id, String maDon, LocalDate ngayTao,
                                                      String tenSuKien, String tenCLB, String maDonVi) {
        SinhVien sv = createSinhVien(id == 1 ? 1 : 4, tenCLB, maDonVi);

        SuKien sk = new SuKien();
        sk.setId(id);
        sk.setTenSuKien(tenSuKien);
        sk.setMucDich("Mục đích sự kiện " + maDon);
        sk.setNoiDung("Nội dung chi tiết " + maDon);
        sk.setSoNguoiThamDu(120);

        Phong phong = new Phong();
        phong.setId(1);
        phong.setMaPhong("HT-A2");
        phong.setTenPhong("Hội trường A2");

        DonMuonPhong don = new DonMuonPhong();
        don.setId(id);
        don.setMaDon(maDon);
        don.setNgayTao(ngayTao);
        don.setNgaySuDung(LocalDate.of(2026, 12, 25));
        don.setGioBatDau(LocalTime.of(18, 0));
        don.setGioKetThuc(LocalTime.of(21, 0));
        don.setLoaiPhongMongMuon("Hội trường");
        don.setThietBiYeuCau("micro và máy chiếu");
        don.setTrangThai(TrangThaiDon.ChoDuyetCap1);
        don.setSinhVien(sv);
        don.setDonVi(sv.getDonVi());
        don.setSuKien(sk);
        don.setPhongMongMuon(phong);
        return don;
    }

    @Nested
    class Uc08DaoAndServiceUnitTest {

        @Test
        @SuppressWarnings("unchecked")
        void donMuonPhongDao_timTheoTrangThaiQueriesFifoOrder() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            DonMuonPhongDAO dao = new DonMuonPhongDAO(jdbcTemplate);

            DonMuonPhong dm1 = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("ChoDuyetCap1")))
                    .thenReturn(List.of(dm1));

            List<DonMuonPhong> result = dao.timTheoTrangThai(TrangThaiDon.ChoDuyetCap1);

            assertEquals(1, result.size());
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), eq("ChoDuyetCap1"));
            String sql = sqlCaptor.getValue();
            assertEquals(true, sql.contains("WHERE d.trangThai = ? ORDER BY d.ngayTao ASC, d.id ASC"));
        }

        @Test
        void donMuonPhongDao_capNhatTrangThaiUpdatesRow() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            DonMuonPhongDAO dao = new DonMuonPhongDAO(jdbcTemplate);
            when(jdbcTemplate.update(anyString(), eq("ChoDuyetCap2"), eq(1))).thenReturn(1);

            int updated = dao.capNhatTrangThai(1, TrangThaiDon.ChoDuyetCap2);

            assertEquals(1, updated);
            verify(jdbcTemplate).update("UPDATE tblDonMuonPhong SET trangThai = ? WHERE id = ?", "ChoDuyetCap2", 1);
        }

        @Test
        void luotDuyetAndLichSuDonDao_insertRecords() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            LuotDuyetDAO luotDuyetDAO = new LuotDuyetDAO(jdbcTemplate);
            LichSuDonDAO lichSuDonDAO = new LichSuDonDAO(jdbcTemplate);

            DonMuonPhong don = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            CanBoDoan cbd = createCanBoDoan();

            LuotDuyet ld = new LuotDuyet();
            ld.setCapDuyet(1);
            ld.setKetQua("Chấp thuận");
            ld.setLyDo(null);
            ld.setDonMuonPhong(don);
            ld.setNguoiDuyet(cbd);

            luotDuyetDAO.ghiLuotDuyet(ld);
            verify(jdbcTemplate).update(
                    anyString(),
                    eq(1),
                    eq("Chấp thuận"),
                    eq(null),
                    any(Timestamp.class),
                    eq(1),
                    eq(2)
            );

            LichSuDon ls = new LichSuDon();
            ls.setTrangThaiCu(TrangThaiDon.ChoDuyetCap1);
            ls.setTrangThaiMoi(TrangThaiDon.ChoDuyetCap2);
            ls.setGhiChu("Duyệt cấp 1: Chấp thuận");
            ls.setDonMuonPhong(don);
            ls.setNguoiThucHien(cbd);

            lichSuDonDAO.ghiThayDoi(ls);
            verify(jdbcTemplate).update(
                    anyString(),
                    eq("ChoDuyetCap1"),
                    eq("ChoDuyetCap2"),
                    eq("Duyệt cấp 1: Chấp thuận"),
                    any(Timestamp.class),
                    eq(1),
                    eq(2)
            );
        }

        @Test
        void duyetCap1Service_chapThuanChuyenChoDuyetCap2() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap1Service service = new DuyetCap1Service(donDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            CanBoDoan cbd = createCanBoDoan();

            when(donDAO.getChiTiet(1)).thenReturn(don);
            when(donDAO.capNhatTrangThai(1, TrangThaiDon.ChoDuyetCap2)).thenReturn(1);

            service.chapThuanDon(1, cbd);

            verify(donDAO).capNhatTrangThai(1, TrangThaiDon.ChoDuyetCap2);

            ArgumentCaptor<LuotDuyet> luotCaptor = ArgumentCaptor.forClass(LuotDuyet.class);
            verify(luotDAO).ghiLuotDuyet(luotCaptor.capture());
            assertEquals(1, luotCaptor.getValue().getCapDuyet());
            assertEquals("Chấp thuận", luotCaptor.getValue().getKetQua());
            assertNull(luotCaptor.getValue().getLyDo());

            ArgumentCaptor<LichSuDon> lsCaptor = ArgumentCaptor.forClass(LichSuDon.class);
            verify(lichSuDAO).ghiThayDoi(lsCaptor.capture());
            assertEquals(TrangThaiDon.ChoDuyetCap1, lsCaptor.getValue().getTrangThaiCu());
            assertEquals(TrangThaiDon.ChoDuyetCap2, lsCaptor.getValue().getTrangThaiMoi());
        }

        @Test
        void duyetCap1Service_tuChoiRequiresLyDo() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap1Service service = new DuyetCap1Service(donDAO, luotDAO, lichSuDAO);

            assertThrows(IllegalArgumentException.class,
                    () -> service.tuChoiDon(1, "   ", createCanBoDoan()));

            verify(donDAO, never()).capNhatTrangThai(anyInt(), any());
            verify(luotDAO, never()).ghiLuotDuyet(any());
            verify(lichSuDAO, never()).ghiThayDoi(any());
        }

        @Test
        void duyetCap1Service_tuChoiWithLyDoUpdatesToTuChoiCap1() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap1Service service = new DuyetCap1Service(donDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            when(donDAO.getChiTiet(1)).thenReturn(don);
            when(donDAO.capNhatTrangThai(1, TrangThaiDon.TuChoiCap1)).thenReturn(1);

            service.tuChoiDon(1, "  Trùng lịch sự kiện toàn trường  ", createCanBoDoan());

            verify(donDAO).capNhatTrangThai(1, TrangThaiDon.TuChoiCap1);

            ArgumentCaptor<LuotDuyet> luotCaptor = ArgumentCaptor.forClass(LuotDuyet.class);
            verify(luotDAO).ghiLuotDuyet(luotCaptor.capture());
            assertEquals("Từ chối", luotCaptor.getValue().getKetQua());
            assertEquals("Trùng lịch sự kiện toàn trường", luotCaptor.getValue().getLyDo());

            ArgumentCaptor<LichSuDon> lsCaptor = ArgumentCaptor.forClass(LichSuDon.class);
            verify(lichSuDAO).ghiThayDoi(lsCaptor.capture());
            assertEquals(TrangThaiDon.TuChoiCap1, lsCaptor.getValue().getTrangThaiMoi());
        }
    }

    @Nested
    @WebMvcTest(DuyetCap1Controller.class)
    @Import({SecurityConfig.class, CustomUserDetailsService.class, AuthService.class, DuyetCap1Service.class})
    class Uc08WebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NguoiDungDAO nguoiDungDAO;

        @MockBean
        private DonMuonPhongDAO donMuonPhongDAO;

        @MockBean
        private LuotDuyetDAO luotDuyetDAO;

        @MockBean
        private LichSuDonDAO lichSuDonDAO;

        @Test
        void unauthorizedRolesCannotAccessDuyetCap1() throws Exception {
            mockMvc.perform(get("/duyet-cap-1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/login"));

            mockMvc.perform(get("/duyet-cap-1")
                            .with(user(new CustomUserDetails(createSinhVien(1, "CLB Tin học", "CLB-IT")))))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/403"));

            mockMvc.perform(get("/duyet-cap-1")
                            .with(user(new CustomUserDetails(createCanBoCSVC()))))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/403"));
        }

        @Test
        void canBoDoanViewsListFiltersByClbAndViewsDetailWithoutGhiChu() throws Exception {
            DonMuonPhong dm1 = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            DonMuonPhong dm4 = createDonChoDuyetCap1(4, "DM-004", LocalDate.of(2026, 11, 18),
                    "Giải giao hữu nội bộ", "CLB Bóng rổ", "CLB-BR");

            when(donMuonPhongDAO.timTheoTrangThai(TrangThaiDon.ChoDuyetCap1)).thenReturn(List.of(dm1, dm4));
            when(donMuonPhongDAO.getChiTiet(1)).thenReturn(dm1);
            when(luotDuyetDAO.getTheoDon(1)).thenReturn(List.of());
            when(lichSuDonDAO.getTheoDon(1)).thenReturn(List.of());

            CustomUserDetails cbBinh = new CustomUserDetails(createCanBoDoan());

            mockMvc.perform(get("/duyet-cap-1").with(user(cbBinh)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hệ thống mượn phòng trực tuyến PTIT")))
                    .andExpect(content().string(containsString("href=\"/xac-nhan-dang-xuat\"")))
                    .andExpect(content().string(not(containsString("id=\"logoutModal\""))))
                    .andExpect(content().string(not(containsString("openLogoutModal"))))
                    .andExpect(content().string(containsString("Duyệt đơn cấp 1")))
                    .andExpect(content().string(containsString("DM-001")))
                    .andExpect(content().string(containsString("DM-004")))
                    .andExpect(content().string(containsString("25/12/2026")))
                    .andExpect(content().string(containsString("18:00 - 21:00")))
                    .andExpect(content().string(containsString("Chờ duyệt cấp 1")))
                    .andExpect(content().string(not(containsString("<th>Khung giờ</th>"))))
                    .andExpect(content().string(not(containsString(">Đặt lại<"))))
                    .andExpect(content().string(not(containsString("Ghi chú"))));

            mockMvc.perform(get("/duyet-cap-1").param("tenCLB", "CLB Tin học").with(user(cbBinh)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("DM-001")))
                    .andExpect(content().string(not(containsString("Giải giao hữu nội bộ"))));

            mockMvc.perform(get("/duyet-cap-1").param("selectedId", "1").with(user(cbBinh)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Chi tiết đơn mượn phòng:")))
                    .andExpect(content().string(containsString("Đêm nhạc chào tân sinh viên")))
                    .andExpect(content().string(containsString("Chấp thuận")))
                    .andExpect(content().string(containsString("Từ chối")))
                    .andExpect(content().string(not(containsString("Ghi chú"))));
        }

        @Test
        void canBoDoanApprovesAndRejectsWithValidation() throws Exception {
            DonMuonPhong dm1 = createDonChoDuyetCap1(1, "DM-001", LocalDate.of(2026, 11, 17),
                    "Đêm nhạc chào tân sinh viên", "CLB Tin học", "CLB-IT");
            when(donMuonPhongDAO.getChiTiet(1)).thenReturn(dm1);
            when(donMuonPhongDAO.capNhatTrangThai(1, TrangThaiDon.ChoDuyetCap2)).thenReturn(1);
            when(donMuonPhongDAO.capNhatTrangThai(1, TrangThaiDon.TuChoiCap1)).thenReturn(1);

            CustomUserDetails cbBinh = new CustomUserDetails(createCanBoDoan());

            mockMvc.perform(post("/duyet-cap-1/1/chap-thuan")
                            .with(csrf())
                            .with(user(cbBinh)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-1"))
                    .andExpect(flash().attributeExists("successMessage"));
            verify(donMuonPhongDAO).capNhatTrangThai(1, TrangThaiDon.ChoDuyetCap2);

            mockMvc.perform(post("/duyet-cap-1/1/tu-choi")
                            .with(csrf())
                            .with(user(cbBinh))
                            .param("lyDo", "  "))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-1?selectedId=1"))
                    .andExpect(flash().attributeExists("errorMessage"));
            verify(donMuonPhongDAO, never()).capNhatTrangThai(1, TrangThaiDon.TuChoiCap1);

            mockMvc.perform(post("/duyet-cap-1/1/tu-choi")
                            .with(csrf())
                            .with(user(cbBinh))
                            .param("lyDo", "Chưa đủ thông tin sự kiện"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-1"))
                    .andExpect(flash().attributeExists("successMessage"));
            verify(donMuonPhongDAO).capNhatTrangThai(1, TrangThaiDon.TuChoiCap1);
        }
    }

    private static Phong createPhong(int id, String maPhong, String tenPhong, int sucChua,
                                     String loaiPhong, String thietBi, String tinhTrang) {
        Phong p = new Phong();
        p.setId(id);
        p.setMaPhong(maPhong);
        p.setTenPhong(tenPhong);
        p.setToaNha("A2");
        p.setTang(1);
        p.setSucChuaToiDa(sucChua);
        p.setLoaiPhong(loaiPhong);
        p.setTrangThietBi(thietBi);
        p.setTinhTrang(tinhTrang);
        return p;
    }

    private static DonMuonPhong createDonChoDuyetCap2(int id, String maDon, LocalDate ngaySuDung) {
        DonMuonPhong don = createDonChoDuyetCap1(id, maDon, LocalDate.of(2026, 11, 17),
                "Hội thảo AI ứng dụng", "CLB Tin học", "CLB-IT");
        don.setTrangThai(TrangThaiDon.ChoDuyetCap2);
        don.setNgaySuDung(ngaySuDung);
        don.setGioBatDau(LocalTime.of(14, 0));
        don.setGioKetThuc(LocalTime.of(17, 0));
        don.setSoNguoiThamDu(150);
        don.getSuKien().setSoNguoiThamDu(150);
        don.setThietBiYeuCau("máy chiếu, âm thanh");
        return don;
    }

    @Nested
    class Uc09DaoAndServiceUnitTest {

        @Test
        @SuppressWarnings("unchecked")
        void phongDao_lockPhongByIdUsesForUpdate() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            PhongDAO phongDAO = new PhongDAO(jdbcTemplate);
            Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);
            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(List.of(htA2));

            Phong locked = phongDAO.lockPhongById(1);

            assertNotNull(locked);
            assertEquals("HT-A2", locked.getMaPhong());
            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), eq(1));
            assertTrue(sqlCaptor.getValue().contains("WHERE id = ? FOR UPDATE"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void donMuonPhongDao_lockAndFindUpcomingApproved() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            DonMuonPhongDAO dao = new DonMuonPhongDAO(jdbcTemplate);
            DonMuonPhong dm3 = createDonChoDuyetCap2(3, "DM-003", LocalDate.of(2027, 12, 30));
            dm3.setTrangThai(TrangThaiDon.DaPheDuyet);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3))).thenReturn(List.of(dm3));
            when(jdbcTemplate.query(anyString(), any(RowMapper.class),
                    eq("DaPheDuyet"), any(Date.class), any(Date.class), any(Time.class)))
                    .thenReturn(List.of(dm3));

            DonMuonPhong locked = dao.lockDonById(3);
            List<DonMuonPhong> upcoming = dao.timDonDaDuyetChuaSuDung(LocalDate.of(2026, 11, 20), LocalTime.of(9, 0));

            assertNotNull(locked);
            assertEquals("DM-003", upcoming.get(0).getMaDon());
        }

        @Test
        void lichSuDungPhongDao_themKhungGioAndDoiPhongChoDon() {
            JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
            LichSuDungPhongDAO dao = new LichSuDungPhongDAO(jdbcTemplate);
            when(jdbcTemplate.update(anyString(), eq(1), eq(2))).thenReturn(1);
            when(jdbcTemplate.update(anyString(), eq(2), eq(3))).thenReturn(1);

            Phong phong = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);
            DonMuonPhong don = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));

            LichSuDungPhong lich = new LichSuDungPhong();
            lich.setPhong(phong);
            lich.setDonMuonPhong(don);

            assertEquals(1, dao.themKhungGio(lich));
            assertEquals(1, dao.doiPhongChoDon(3, 2));
            verify(jdbcTemplate).update("INSERT INTO tblLichSuDungPhong (phongId, donMuonPhongId) VALUES (?, ?)", 1, 2);
            verify(jdbcTemplate).update("UPDATE tblLichSuDungPhong SET phongId = ? WHERE donMuonPhongId = ?", 2, 3);
        }

        @Test
        void duyetCap2Service_kiemTraPhongTrongAppliesPhuongAnA() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            PhongDAO phongDAO = mock(PhongDAO.class);
            LichSuDungPhongDAO lichPhongDAO = mock(LichSuDungPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap2Service service = new DuyetCap2Service(donDAO, phongDAO, lichPhongDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));
            Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu, âm thanh", Phong.TINH_TRANG_HOAT_DONG);
            when(donDAO.getChiTiet(2)).thenReturn(don);
            when(phongDAO.timPhongTrong(don.getNgaySuDung(), don.getGioBatDau(), don.getGioKetThuc(),
                    "Hội trường", 150, null)).thenReturn(List.of(htA2));

            List<Phong> rooms = service.kiemTraPhongTrong(2);

            assertEquals(1, rooms.size());
            verify(phongDAO).timPhongTrong(don.getNgaySuDung(), don.getGioBatDau(), don.getGioKetThuc(),
                    "Hội trường", 150, null);
        }

        @Test
        void duyetCap2Service_pheDuyetVaBoTriPhongReservesRoomAndAudits() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            PhongDAO phongDAO = mock(PhongDAO.class);
            LichSuDungPhongDAO lichPhongDAO = mock(LichSuDungPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap2Service service = new DuyetCap2Service(donDAO, phongDAO, lichPhongDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));
            Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu, âm thanh", Phong.TINH_TRANG_HOAT_DONG);

            when(donDAO.lockDonById(2)).thenReturn(don);
            when(phongDAO.lockPhongById(1)).thenReturn(htA2);
            when(donDAO.kiemTraTrungLich(don, htA2)).thenReturn(false);
            when(donDAO.capNhatTrangThai(2, TrangThaiDon.DaPheDuyet)).thenReturn(1);

            service.pheDuyetVaBoTriPhong(2, 1, createCanBoCSVC());

            verify(lichPhongDAO).themKhungGio(any(LichSuDungPhong.class));
            verify(donDAO).capNhatTrangThai(2, TrangThaiDon.DaPheDuyet);

            ArgumentCaptor<LuotDuyet> luotCaptor = ArgumentCaptor.forClass(LuotDuyet.class);
            verify(luotDAO).ghiLuotDuyet(luotCaptor.capture());
            assertEquals(2, luotCaptor.getValue().getCapDuyet());
            assertEquals("Phê duyệt", luotCaptor.getValue().getKetQua());
            assertNull(luotCaptor.getValue().getLyDo());

            ArgumentCaptor<LichSuDon> lsCaptor = ArgumentCaptor.forClass(LichSuDon.class);
            verify(lichSuDAO).ghiThayDoi(lsCaptor.capture());
            assertEquals(TrangThaiDon.ChoDuyetCap2, lsCaptor.getValue().getTrangThaiCu());
            assertEquals(TrangThaiDon.DaPheDuyet, lsCaptor.getValue().getTrangThaiMoi());
            assertTrue(lsCaptor.getValue().getGhiChu().contains("HT-A2"));
        }

        @Test
        void duyetCap2Service_pheDuyetRejectsConflictInactiveAndSmallCapacity() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            PhongDAO phongDAO = mock(PhongDAO.class);
            LichSuDungPhongDAO lichPhongDAO = mock(LichSuDungPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap2Service service = new DuyetCap2Service(donDAO, phongDAO, lichPhongDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));
            Phong conflictRoom = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);
            Phong inactiveRoom = createPhong(3, "HT-A3", "Hội trường A3", 200, "Hội trường", "máy chiếu", Phong.TINH_TRANG_NGUNG);
            Phong smallRoom = createPhong(4, "A2-201", "Phòng A2-201", 60, "Phòng học thường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);

            when(donDAO.lockDonById(2)).thenReturn(don);
            when(phongDAO.lockPhongById(1)).thenReturn(conflictRoom);
            when(donDAO.kiemTraTrungLich(don, conflictRoom)).thenReturn(true);
            when(phongDAO.lockPhongById(3)).thenReturn(inactiveRoom);
            when(phongDAO.lockPhongById(4)).thenReturn(smallRoom);

            CanBoCSVC cbcs = createCanBoCSVC();
            assertThrows(IllegalStateException.class, () -> service.pheDuyetVaBoTriPhong(2, 1, cbcs));
            assertThrows(IllegalStateException.class, () -> service.pheDuyetVaBoTriPhong(2, 3, cbcs));
            assertThrows(IllegalStateException.class, () -> service.pheDuyetVaBoTriPhong(2, 4, cbcs));

            verify(lichPhongDAO, never()).themKhungGio(any());
            verify(donDAO, never()).capNhatTrangThai(anyInt(), any());
            verify(luotDAO, never()).ghiLuotDuyet(any());
        }

        @Test
        void duyetCap2Service_tuChoiRequiresLyDoAndUpdatesStatus() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            PhongDAO phongDAO = mock(PhongDAO.class);
            LichSuDungPhongDAO lichPhongDAO = mock(LichSuDungPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap2Service service = new DuyetCap2Service(donDAO, phongDAO, lichPhongDAO, luotDAO, lichSuDAO);

            CanBoCSVC cbcs = createCanBoCSVC();
            assertThrows(IllegalArgumentException.class, () -> service.tuChoiDon(2, "   ", cbcs));

            DonMuonPhong don = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));
            when(donDAO.lockDonById(2)).thenReturn(don);
            when(donDAO.capNhatTrangThai(2, TrangThaiDon.TuChoiCap2)).thenReturn(1);

            service.tuChoiDon(2, "Không còn hội trường trống", cbcs);

            verify(donDAO).capNhatTrangThai(2, TrangThaiDon.TuChoiCap2);
            ArgumentCaptor<LuotDuyet> luotCaptor = ArgumentCaptor.forClass(LuotDuyet.class);
            verify(luotDAO).ghiLuotDuyet(luotCaptor.capture());
            assertEquals(2, luotCaptor.getValue().getCapDuyet());
            assertEquals("Từ chối", luotCaptor.getValue().getKetQua());
            assertEquals("Không còn hội trường trống", luotCaptor.getValue().getLyDo());
        }

        @Test
        void duyetCap2Service_doiPhongUpdatesOnlyLichSuDonAndBlocksStartedOrder() {
            DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
            PhongDAO phongDAO = mock(PhongDAO.class);
            LichSuDungPhongDAO lichPhongDAO = mock(LichSuDungPhongDAO.class);
            LuotDuyetDAO luotDAO = mock(LuotDuyetDAO.class);
            LichSuDonDAO lichSuDAO = mock(LichSuDonDAO.class);
            DuyetCap2Service service = new DuyetCap2Service(donDAO, phongDAO, lichPhongDAO, luotDAO, lichSuDAO);

            DonMuonPhong don = createDonChoDuyetCap2(3, "DM-003", LocalDate.of(2027, 12, 30));
            don.setTrangThai(TrangThaiDon.DaPheDuyet);
            Phong phongCu = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);
            Phong phongMoi = createPhong(2, "HT-B1", "Hội trường B1", 200, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);

            when(donDAO.lockDonById(3)).thenReturn(don);
            when(lichPhongDAO.getPhongBoTriTheoDon(3)).thenReturn(phongCu);
            when(phongDAO.lockPhongById(2)).thenReturn(phongMoi);
            when(donDAO.kiemTraTrungLich(don, phongMoi)).thenReturn(false);
            when(lichPhongDAO.doiPhongChoDon(3, 2)).thenReturn(1);

            CanBoCSVC cbcs = createCanBoCSVC();
            service.doiPhong(3, 2, cbcs);

            verify(lichPhongDAO).doiPhongChoDon(3, 2);
            verify(luotDAO, never()).ghiLuotDuyet(any());
            verify(donDAO, never()).capNhatTrangThai(anyInt(), any());

            ArgumentCaptor<LichSuDon> lsCaptor = ArgumentCaptor.forClass(LichSuDon.class);
            verify(lichSuDAO).ghiThayDoi(lsCaptor.capture());
            assertEquals(TrangThaiDon.DaPheDuyet, lsCaptor.getValue().getTrangThaiCu());
            assertEquals(TrangThaiDon.DaPheDuyet, lsCaptor.getValue().getTrangThaiMoi());
            assertEquals("Đổi phòng từ HT-A2 sang HT-B1", lsCaptor.getValue().getGhiChu());

            DonMuonPhong started = createDonChoDuyetCap2(5, "DM-005", LocalDate.of(2020, 1, 1));
            started.setTrangThai(TrangThaiDon.DaPheDuyet);
            when(donDAO.lockDonById(5)).thenReturn(started);
            assertThrows(IllegalStateException.class, () -> service.doiPhong(5, 2, cbcs));
        }
    }

    @Nested
    @WebMvcTest(DuyetCap2Controller.class)
    @Import({SecurityConfig.class, CustomUserDetailsService.class, AuthService.class, DuyetCap2Service.class})
    class Uc09WebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NguoiDungDAO nguoiDungDAO;

        @MockBean
        private DonMuonPhongDAO donMuonPhongDAO;

        @MockBean
        private PhongDAO phongDAO;

        @MockBean
        private LichSuDungPhongDAO lichSuDungPhongDAO;

        @MockBean
        private LuotDuyetDAO luotDuyetDAO;

        @MockBean
        private LichSuDonDAO lichSuDonDAO;

        @Test
        void unauthorizedRolesCannotAccessDuyetCap2() throws Exception {
            mockMvc.perform(get("/duyet-cap-2"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/login"));

            mockMvc.perform(get("/duyet-cap-2")
                            .with(user(new CustomUserDetails(createSinhVien(1, "CLB Tin học", "CLB-IT")))))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/403"));

            mockMvc.perform(get("/duyet-cap-2")
                            .with(user(new CustomUserDetails(createCanBoDoan()))))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/403"));
        }

        @Test
        void canBoCsvcViewsChecksRoomsApprovesAndRejects() throws Exception {
            DonMuonPhong dm2 = createDonChoDuyetCap2(2, "DM-002", LocalDate.of(2027, 12, 28));
            Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu, âm thanh", Phong.TINH_TRANG_HOAT_DONG);

            when(donMuonPhongDAO.timTheoTrangThai(TrangThaiDon.ChoDuyetCap2)).thenReturn(List.of(dm2));
            when(donMuonPhongDAO.getChiTiet(2)).thenReturn(dm2);
            when(donMuonPhongDAO.lockDonById(2)).thenReturn(dm2);
            when(luotDuyetDAO.getTheoDon(2)).thenReturn(List.of());
            when(lichSuDonDAO.getTheoDon(2)).thenReturn(List.of());
            when(lichSuDungPhongDAO.getPhongBoTriTheoDon(2)).thenReturn(null);
            when(phongDAO.timPhongTrong(dm2.getNgaySuDung(), dm2.getGioBatDau(), dm2.getGioKetThuc(),
                    "Hội trường", 150, null)).thenReturn(List.of(htA2));
            when(phongDAO.lockPhongById(1)).thenReturn(htA2);
            when(donMuonPhongDAO.kiemTraTrungLich(dm2, htA2)).thenReturn(false);
            when(donMuonPhongDAO.capNhatTrangThai(2, TrangThaiDon.DaPheDuyet)).thenReturn(1);
            when(donMuonPhongDAO.capNhatTrangThai(2, TrangThaiDon.TuChoiCap2)).thenReturn(1);

            CustomUserDetails cbCuong = new CustomUserDetails(createCanBoCSVC());

            mockMvc.perform(get("/duyet-cap-2")
                            .param("selectedId", "2")
                            .param("checkRooms", "true")
                            .with(user(cbCuong)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hệ thống mượn phòng trực tuyến PTIT")))
                    .andExpect(content().string(containsString("href=\"/xac-nhan-dang-xuat\"")))
                    .andExpect(content().string(not(containsString("id=\"logoutModal\""))))
                    .andExpect(content().string(not(containsString("openLogoutModal"))))
                    .andExpect(content().string(containsString("Duyệt đơn cấp 2 và bố trí phòng")))
                    .andExpect(content().string(containsString("DM-002")))
                    .andExpect(content().string(containsString("14:00 - 17:00")))
                    .andExpect(content().string(not(containsString("<th>Khung giờ</th>"))))
                    .andExpect(content().string(containsString("HT-A2")))
                    .andExpect(content().string(containsString("máy chiếu, âm thanh")))
                    .andExpect(content().string(containsString("Phê duyệt")))
                    .andExpect(content().string(containsString("Từ chối")));

            mockMvc.perform(post("/duyet-cap-2/2/phe-duyet")
                            .with(csrf())
                            .with(user(cbCuong)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-2?tab=cho-duyet&selectedId=2&checkRooms=true"))
                    .andExpect(flash().attributeExists("errorMessage"));

            mockMvc.perform(post("/duyet-cap-2/2/phe-duyet")
                            .with(csrf())
                            .with(user(cbCuong))
                            .param("idPhong", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-2?tab=cho-duyet"))
                    .andExpect(flash().attributeExists("successMessage"));
            verify(lichSuDungPhongDAO).themKhungGio(any(LichSuDungPhong.class));
            verify(donMuonPhongDAO).capNhatTrangThai(2, TrangThaiDon.DaPheDuyet);

            mockMvc.perform(post("/duyet-cap-2/2/tu-choi")
                            .with(csrf())
                            .with(user(cbCuong))
                            .param("lyDo", " "))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-2?tab=cho-duyet&selectedId=2"))
                    .andExpect(flash().attributeExists("errorMessage"));

            mockMvc.perform(post("/duyet-cap-2/2/tu-choi")
                            .with(csrf())
                            .with(user(cbCuong))
                            .param("lyDo", "Không có phòng phù hợp"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-2?tab=cho-duyet"))
                    .andExpect(flash().attributeExists("successMessage"));
            verify(donMuonPhongDAO).capNhatTrangThai(2, TrangThaiDon.TuChoiCap2);
        }

        @Test
        void canBoCsvcSwitchesToApprovedTabAndChangesRoom() throws Exception {
            DonMuonPhong dm3 = createDonChoDuyetCap2(3, "DM-003", LocalDate.of(2027, 12, 30));
            dm3.setTrangThai(TrangThaiDon.DaPheDuyet);
            Phong phongCu = createPhong(1, "HT-A2", "Hội trường A2", 300, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);
            Phong phongMoi = createPhong(2, "HT-B1", "Hội trường B1", 200, "Hội trường", "máy chiếu", Phong.TINH_TRANG_HOAT_DONG);

            when(donMuonPhongDAO.timDonDaDuyetChuaSuDung(any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(List.of(dm3));
            when(lichSuDungPhongDAO.getPhongBoTriTheoCacDon(List.of(3))).thenReturn(Map.of(3, phongCu));
            when(donMuonPhongDAO.getChiTiet(3)).thenReturn(dm3);
            when(donMuonPhongDAO.lockDonById(3)).thenReturn(dm3);
            when(luotDuyetDAO.getTheoDon(3)).thenReturn(List.of());
            when(lichSuDonDAO.getTheoDon(3)).thenReturn(List.of());
            when(lichSuDungPhongDAO.getPhongBoTriTheoDon(3)).thenReturn(phongCu);
            when(phongDAO.timPhongTrong(dm3.getNgaySuDung(), dm3.getGioBatDau(), dm3.getGioKetThuc(),
                    "Hội trường", 150, null)).thenReturn(List.of(phongMoi));
            when(phongDAO.lockPhongById(2)).thenReturn(phongMoi);
            when(donMuonPhongDAO.kiemTraTrungLich(dm3, phongMoi)).thenReturn(false);
            when(lichSuDungPhongDAO.doiPhongChoDon(3, 2)).thenReturn(1);

            CustomUserDetails cbCuong = new CustomUserDetails(createCanBoCSVC());

            mockMvc.perform(get("/duyet-cap-2")
                            .param("tab", "da-phe-duyet")
                            .param("selectedId", "3")
                            .param("checkRooms", "true")
                            .with(user(cbCuong)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("DM-003")))
                    .andExpect(content().string(containsString("30/12/2027")))
                    .andExpect(content().string(containsString("14:00 - 17:00")))
                    .andExpect(content().string(not(containsString("<th>Khung giờ</th>"))))
                    .andExpect(content().string(containsString("HT-B1")))
                    .andExpect(content().string(containsString("Đổi phòng")));

            mockMvc.perform(post("/duyet-cap-2/3/doi-phong")
                            .with(csrf())
                            .with(user(cbCuong))
                            .param("idPhongMoi", "2"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/duyet-cap-2?tab=da-phe-duyet"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(lichSuDungPhongDAO).doiPhongChoDon(3, 2);
            verify(lichSuDonDAO).ghiThayDoi(any(LichSuDon.class));
            verify(luotDuyetDAO, never()).ghiLuotDuyet(any());
        }
    }
}
