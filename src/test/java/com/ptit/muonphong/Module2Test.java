package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.DuyetCap1Controller;
import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.DuyetCap1Service;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
        sv.setKhoaHoc(2022);
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
                    .andExpect(content().string(containsString("Duyệt đơn cấp 1")))
                    .andExpect(content().string(containsString("DM-001")))
                    .andExpect(content().string(containsString("DM-004")))
                    .andExpect(content().string(containsString("Chờ duyệt cấp 1")))
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
}
