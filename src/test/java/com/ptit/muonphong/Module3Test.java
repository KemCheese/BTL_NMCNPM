package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.QuanLyPhongController;
import com.ptit.muonphong.dao.DonMuonPhongDAO;
import com.ptit.muonphong.dao.NguoiDungDAO;
import com.ptit.muonphong.dao.PhongDAO;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.PhongService;
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
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuanLyPhongController.class)
@Import({ SecurityConfig.class, CustomUserDetailsService.class, AuthService.class })
class Module3Test {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PhongService phongService;

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

        private Phong createSamplePhong(int id, String maPhong, String tenPhong, String tinhTrang) {
                Phong p = new Phong();
                p.setId(id);
                p.setMaPhong(maPhong);
                p.setTenPhong(tenPhong);
                p.setToaNha("A2");
                p.setTang(1);
                p.setSucChuaToiDa(300);
                p.setLoaiPhong("Hội trường");
                p.setTrangThietBi("máy chiếu, âm thanh");
                p.setTinhTrang(tinhTrang);
                p.setMoTa("Phòng mẫu kiểm thử");
                return p;
        }

        @Test
        void unauthenticatedRedirectsToLogin() throws Exception {
                mockMvc.perform(get("/quan-ly-phong"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        void nonCsvRolesReceive403() throws Exception {
                mockMvc.perform(get("/quan-ly-phong").with(user(new CustomUserDetails(createSinhVien()))))
                                .andExpect(status().isForbidden())
                                .andExpect(forwardedUrl("/403"));

                mockMvc.perform(get("/quan-ly-phong").with(user(new CustomUserDetails(createCanBoDoan()))))
                                .andExpect(status().isForbidden())
                                .andExpect(forwardedUrl("/403"));
        }

        @Test
        void canBoCsvViewsRoomListAndSearch() throws Exception {
                Phong p1 = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_HOAT_DONG);
                Phong p2 = createSamplePhong(3, "HT-A3", "Hội trường A3", Phong.TINH_TRANG_NGUNG);
                when(phongService.timPhong("")).thenReturn(List.of(p1, p2));
                when(phongService.timPhong("A2")).thenReturn(List.of(p1));

                mockMvc.perform(get("/quan-ly-phong").with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(view().name("quan-ly-phong"))
                                .andExpect(content().string(containsString("Hệ thống mượn phòng trực tuyến PTIT")))
                                .andExpect(content().string(containsString("id=\"logoutModal\"")))
                                .andExpect(content().string(containsString("Quản lý danh mục phòng")))
                                .andExpect(content().string(containsString("HT-A2")))
                                .andExpect(content().string(containsString("Hội trường A3")))
                                .andExpect(content().string(containsString("href=\"/quan-ly-phong?suaId=1&amp;tuKhoa=#formPhongSection\"")))
                                .andExpect(content().string(containsString("href=\"/quan-ly-phong?ngungId=1&amp;tuKhoa=\"")))
                                .andExpect(content().string(containsString("Form thêm phòng mới")))
                                .andExpect(content().string(containsString(">Lưu<")))
                                .andExpect(content().string(not(containsString(">Làm mới<"))))
                                .andExpect(content().string(not(containsString(">Hủy sửa<"))));

                mockMvc.perform(get("/quan-ly-phong")
                                .param("tuKhoa", "A2")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("HT-A2")))
                                .andExpect(content().string(containsString("href=\"/quan-ly-phong?suaId=1&amp;tuKhoa=A2#formPhongSection\"")))
                                .andExpect(content().string(containsString("href=\"/quan-ly-phong?ngungId=1&amp;tuKhoa=A2\"")))
                                .andExpect(content().string(not(containsString("HT-A3"))));

                verify(phongService).timPhong("A2");
        }

        @Test
        void emptySearchShowsNotFoundMessage() throws Exception {
                when(phongService.timPhong("KHONG_TON_TAI")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/quan-ly-phong")
                                .param("tuKhoa", "KHONG_TON_TAI")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Không tìm thấy phòng phù hợp.")));
        }

        @Test
        void addValidRoomRedirectsWithSuccessMessage() throws Exception {
                mockMvc.perform(post("/quan-ly-phong/them")
                                .with(csrf())
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .param("maPhong", "A2-301")
                                .param("tenPhong", "Phòng học A2-301")
                                .param("loaiPhong", "Phòng học thường")
                                .param("toaNha", "A2")
                                .param("tang", "3")
                                .param("sucChuaToiDa", "60")
                                .param("trangThietBi", "máy chiếu, bảng")
                                .param("moTa", "Phòng tầng 3"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/quan-ly-phong"))
                                .andExpect(flash().attribute("successMessage", "Thêm phòng thành công."));

                verify(phongService).themPhong(any(Phong.class));
        }

        @Test
        void addDuplicateOrInvalidRoomKeepsFormAndShowsError() throws Exception {
                doThrow(new IllegalArgumentException("Mã phòng \"HT-A2\" đã tồn tại trong hệ thống!"))
                                .when(phongService).themPhong(any(Phong.class));
                when(phongService.timPhong("")).thenReturn(Collections.emptyList());

                mockMvc.perform(post("/quan-ly-phong/them")
                                .with(csrf())
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .param("maPhong", "HT-A2")
                                .param("tenPhong", "Hội trường A2 mới")
                                .param("loaiPhong", "Hội trường")
                                .param("toaNha", "A2")
                                .param("tang", "1")
                                .param("sucChuaToiDa", "200"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("quan-ly-phong"))
                                .andExpect(model().attributeExists("errorMessage"))
                                .andExpect(content().string(containsString("đã tồn tại trong hệ thống")))
                                .andExpect(content().string(containsString("Hội trường A2 mới")));
        }

        @Test
        void addRoomWithNonNumericFloorOrCapacityShowsFriendlyError() throws Exception {
                when(phongService.timPhong("")).thenReturn(Collections.emptyList());

                mockMvc.perform(post("/quan-ly-phong/them")
                                .with(csrf())
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .param("maPhong", "PM-101")
                                .param("tenPhong", "Phòng máy 101")
                                .param("loaiPhong", "Phòng máy")
                                .param("toaNha", "A3")
                                .param("tang", "abc")
                                .param("sucChuaToiDa", "40"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("quan-ly-phong"))
                                .andExpect(content().string(
                                                containsString("Tầng và sức chứa tối đa phải là số nguyên hợp lệ.")));

                verify(phongService, never()).themPhong(any(Phong.class));
        }

        @Test
        void addRoomDatabaseErrorShowsFriendlyError() throws Exception {
                doThrow(new DataAccessResourceFailureException("DB down"))
                                .when(phongService).themPhong(any(Phong.class));
                when(phongService.timPhong("")).thenReturn(Collections.emptyList());

                mockMvc.perform(post("/quan-ly-phong/them")
                                .with(csrf())
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .param("maPhong", "PM-102")
                                .param("tenPhong", "Phòng máy 102")
                                .param("loaiPhong", "Phòng máy")
                                .param("toaNha", "A3")
                                .param("tang", "2")
                                .param("sucChuaToiDa", "40"))
                                .andExpect(status().isOk())
                                .andExpect(content()
                                                .string(containsString("Lỗi kết nối cơ sở dữ liệu khi thêm phòng.")));
        }

        @Test
        void phongKiemTraHopLeChecksRequiredFieldsAndRanges() {
                Phong valid = createSamplePhong(0, "PM-201", "Phòng máy 201", Phong.TINH_TRANG_HOAT_DONG);
                valid.setLoaiPhong("Phòng máy");
                assertTrue(valid.kiemTraHopLe());

                Phong invalidMa = createSamplePhong(0, "   ", "Tên", Phong.TINH_TRANG_HOAT_DONG);
                assertFalse(invalidMa.kiemTraHopLe());

                Phong invalidLoai = createSamplePhong(0, "PM-202", "Tên", Phong.TINH_TRANG_HOAT_DONG);
                invalidLoai.setLoaiPhong("Loại lạ");
                assertFalse(invalidLoai.kiemTraHopLe());

                Phong invalidTang = createSamplePhong(0, "PM-203", "Tên", Phong.TINH_TRANG_HOAT_DONG);
                invalidTang.setTang(0);
                assertFalse(invalidTang.kiemTraHopLe());

                Phong invalidSucChua = createSamplePhong(0, "PM-204", "Tên", Phong.TINH_TRANG_HOAT_DONG);
                invalidSucChua.setSucChuaToiDa(0);
                assertFalse(invalidSucChua.kiemTraHopLe());
        }

        @Test
        void phongServiceValidatesAndDefaultsTinhTrangHoatDong() {
                PhongDAO daoMock = mock(PhongDAO.class);
                DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
                PhongService service = new PhongService(daoMock, donDaoMock);

                Phong phong = new Phong();
                phong.setMaPhong("  PM-301  ");
                phong.setTenPhong("  Phòng máy 301 ");
                phong.setToaNha(" A3 ");
                phong.setTang(3);
                phong.setSucChuaToiDa(50);
                phong.setLoaiPhong("Phòng máy");
                phong.setTrangThietBi("   ");
                phong.setMoTa("");
                phong.setTinhTrang(Phong.TINH_TRANG_NGUNG);

                when(daoMock.kiemTraMaPhong("PM-301")).thenReturn(true);
                when(daoMock.themPhong(phong)).thenReturn(true);

                service.themPhong(phong);

                assertEquals("PM-301", phong.getMaPhong());
                assertEquals(Phong.TINH_TRANG_HOAT_DONG, phong.getTinhTrang());
                assertNull(phong.getTrangThietBi());
                assertNull(phong.getMoTa());
                verify(daoMock).themPhong(phong);

                Phong duplicate = createSamplePhong(0, "HT-A2", "Hội trường", Phong.TINH_TRANG_HOAT_DONG);
                when(daoMock.kiemTraMaPhong("HT-A2")).thenReturn(false);
                assertThrows(IllegalArgumentException.class, () -> service.themPhong(duplicate));
                verify(daoMock, never()).themPhong(duplicate);
        }

        @Test
        @SuppressWarnings("unchecked")
        void phongDaoUsesJdbcTemplateProperlyForSlice1() {
                JdbcTemplate jdbcMock = mock(JdbcTemplate.class);
                PhongDAO dao = new PhongDAO(jdbcMock);

                when(jdbcMock.queryForObject(anyString(), eq(Integer.class), eq("NEW-01"))).thenReturn(0);
                when(jdbcMock.queryForObject(anyString(), eq(Integer.class), eq("HT-A2"))).thenReturn(1);
                assertTrue(dao.kiemTraMaPhong("NEW-01"));
                assertFalse(dao.kiemTraMaPhong("HT-A2"));
                assertFalse(dao.kiemTraMaPhong(" "));

                Phong phong = createSamplePhong(0, "A2-302", "Phòng A2-302", null);
                when(jdbcMock.update(
                                anyString(),
                                eq("A2-302"),
                                eq("Phòng A2-302"),
                                eq("A2"),
                                eq(1),
                                eq(300),
                                eq("Hội trường"),
                                eq("máy chiếu, âm thanh"),
                                eq(Phong.TINH_TRANG_HOAT_DONG),
                                eq("Phòng mẫu kiểm thử"))).thenReturn(1);
                assertTrue(dao.themPhong(phong));

                when(jdbcMock.query(anyString(), any(RowMapper.class))).thenReturn(List.of(phong));
                assertEquals(1, dao.timPhong("").size());

                when(jdbcMock.query(anyString(), any(RowMapper.class),
                                eq("%A2%"), eq("%A2%"), eq("%A2%"), eq("%A2%"), eq("%A2%")))
                                .thenReturn(List.of(phong));
                assertEquals(1, dao.timPhong("A2").size());

                when(jdbcMock.query(anyString(), any(RowMapper.class), anyInt()))
                                .thenReturn(Collections.emptyList());
                assertNull(dao.getChiTiet(999));
        }

        @Test
        void canBoCsvLoadsEditFormWithReadOnlyMaPhong() throws Exception {
                Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_NGUNG);
                when(phongService.timPhong("")).thenReturn(List.of(htA2));
                when(phongService.getChiTiet(1)).thenReturn(htA2);

                mockMvc.perform(get("/quan-ly-phong")
                                .param("suaId", "1")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(view().name("quan-ly-phong"))
                                .andExpect(model().attribute("editMode", true))
                                .andExpect(content().string(containsString("Form sửa thông tin phòng")))
                                .andExpect(content().string(containsString("readonly=\"readonly\"")))
                                .andExpect(content().string(containsString("Ngưng sử dụng")))
                                .andExpect(content().string(containsString(">Lưu<")))
                                .andExpect(content().string(not(containsString(">Hủy sửa<"))))
                                .andExpect(content().string(not(containsString(">Làm mới<"))));

                when(phongService.getChiTiet(999)).thenReturn(null);
                mockMvc.perform(get("/quan-ly-phong")
                                .param("suaId", "999")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("editMode", false))
                                .andExpect(content().string(containsString("Không tìm thấy phòng cần sửa.")));
        }

        @Test
        void canBoCsvSubmitsValidEditAndHandlesValidationErrors() throws Exception {
                when(phongService.timPhong("A2")).thenReturn(Collections.emptyList());
                when(phongService.timPhong("")).thenReturn(Collections.emptyList());

                mockMvc.perform(post("/quan-ly-phong/sua")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("id", "1")
                                .param("maPhong", "HT-A2")
                                .param("tenPhong", "Hội trường A2 mới")
                                .param("loaiPhong", "Hội trường")
                                .param("toaNha", "A2")
                                .param("tang", "1")
                                .param("sucChuaToiDa", "350")
                                .param("tuKhoa", "A2"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/quan-ly-phong?tuKhoa=A2"))
                                .andExpect(flash().attribute("successMessage", "Cập nhật phòng thành công."));

                verify(phongService).capNhatPhong(any(Phong.class));

                Phong hienTai = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_NGUNG);
                when(phongService.getChiTiet(1)).thenReturn(hienTai);

                mockMvc.perform(post("/quan-ly-phong/sua")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("id", "1")
                                .param("maPhong", "HACK")
                                .param("tenPhong", "Tên mới")
                                .param("loaiPhong", "Hội trường")
                                .param("toaNha", "A2")
                                .param("tang", "abc")
                                .param("sucChuaToiDa", "350"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("editMode", true))
                                .andExpect(content().string(
                                                containsString("Tầng và sức chứa tối đa phải là số nguyên hợp lệ.")))
                                .andExpect(content().string(containsString("value=\"HT-A2\"")));

                doThrow(new IllegalArgumentException("Dữ liệu phòng không hợp lệ.")).when(phongService)
                                .capNhatPhong(any(Phong.class));
                mockMvc.perform(post("/quan-ly-phong/sua")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("id", "1")
                                .param("maPhong", "HT-A2")
                                .param("tenPhong", "")
                                .param("loaiPhong", "Hội trường")
                                .param("toaNha", "A2")
                                .param("tang", "1")
                                .param("sucChuaToiDa", "300"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("editMode", true))
                                .andExpect(content().string(containsString("Dữ liệu phòng không hợp lệ.")));
        }

        @Test
        void phongServiceCapNhatPhongPreservesMaPhongAndTinhTrang() {
                PhongDAO daoMock = mock(PhongDAO.class);
                DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
                PhongService service = new PhongService(daoMock, donDaoMock);

                Phong hienTai = createSamplePhong(3, "HT-A3", "Hội trường A3", Phong.TINH_TRANG_NGUNG);
                when(daoMock.getChiTiet(3)).thenReturn(hienTai);
                when(daoMock.capNhatPhong(any(Phong.class))).thenReturn(true);

                Phong submitted = createSamplePhong(3, "MA-MOI", "  Hội trường A3 sửa  ", Phong.TINH_TRANG_HOAT_DONG);
                submitted.setTrangThietBi("  máy chiếu mới  ");
                submitted.setMoTa("   ");

                service.capNhatPhong(submitted);

                assertEquals("HT-A3", submitted.getMaPhong());
                assertEquals(Phong.TINH_TRANG_NGUNG, submitted.getTinhTrang());
                assertEquals("Hội trường A3 sửa", submitted.getTenPhong());
                assertEquals("máy chiếu mới", submitted.getTrangThietBi());
                assertNull(submitted.getMoTa());
                verify(daoMock).capNhatPhong(submitted);

                Phong missing = createSamplePhong(99, "NOPE", "Tên", Phong.TINH_TRANG_HOAT_DONG);
                when(daoMock.getChiTiet(99)).thenReturn(null);
                assertThrows(IllegalArgumentException.class, () -> service.capNhatPhong(missing));
                verify(daoMock, never()).capNhatPhong(missing);
        }

        @Test
        void phongDaoCapNhatPhongUpdatesEditableColumnsOnly() {
                JdbcTemplate jdbcMock = mock(JdbcTemplate.class);
                PhongDAO dao = new PhongDAO(jdbcMock);

                Phong phong = createSamplePhong(2, "HT-B1", "Hội trường B1 mới", Phong.TINH_TRANG_HOAT_DONG);
                when(jdbcMock.update(
                                anyString(),
                                eq("Hội trường B1 mới"),
                                eq("A2"),
                                eq(1),
                                eq(300),
                                eq("Hội trường"),
                                eq("máy chiếu, âm thanh"),
                                eq("Phòng mẫu kiểm thử"),
                                eq(2))).thenReturn(1);

                assertTrue(dao.capNhatPhong(phong));
                assertFalse(dao.capNhatPhong(null));

                Phong zeroId = createSamplePhong(0, "X", "X", Phong.TINH_TRANG_HOAT_DONG);
                assertFalse(dao.capNhatPhong(zeroId));
        }

        @Test
        void canBoCsvOpensNgungSuDungModalWithAndWithoutFutureOrders() throws Exception {
                Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_HOAT_DONG);
                Phong htB1 = createSamplePhong(2, "HT-B1", "Hội trường B1", Phong.TINH_TRANG_HOAT_DONG);
                Phong htA3 = createSamplePhong(3, "HT-A3", "Hội trường A3", Phong.TINH_TRANG_NGUNG);

                DonMuonPhong don = new DonMuonPhong();
                don.setId(3);
                don.setMaDon("DM-003");
                don.setTenSuKien("Sinh hoạt chuyên đề cuối năm");
                don.setNgaySuDung(LocalDate.of(2026, 12, 30));
                don.setGioBatDau(LocalTime.of(8, 0));
                don.setGioKetThuc(LocalTime.of(11, 0));
                don.setTrangThai(TrangThaiDon.DaPheDuyet);
                DonVi clb = new DonVi();
                clb.setTenCLB("CLB Tin học");
                don.setDonVi(clb);

                when(phongService.timPhong("")).thenReturn(List.of(htA2, htB1, htA3));
                when(phongService.getChiTiet(1)).thenReturn(htA2);
                when(phongService.layDonDaDuyetTuongLaiTheoPhong(1)).thenReturn(List.of(don));

                mockMvc.perform(get("/quan-ly-phong")
                                .param("ngungId", "1")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeExists("ngungTarget", "donLienQuanNgung"))
                                .andExpect(content().string(containsString("Xác nhận ngưng sử dụng phòng HT-A2")))
                                .andExpect(content().string(containsString("DM-003")))
                                .andExpect(content().string(containsString("Sinh hoạt chuyên đề cuối năm")))
                                .andExpect(content().string(containsString("CLB Tin học")))
                                .andExpect(content().string(containsString("Xác nhận ngưng")));

                when(phongService.getChiTiet(2)).thenReturn(htB1);
                when(phongService.layDonDaDuyetTuongLaiTheoPhong(2)).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/quan-ly-phong")
                                .param("ngungId", "2")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                "Phòng hiện không có đơn đã phê duyệt nào chưa đến ngày sử dụng")));

                when(phongService.getChiTiet(3)).thenReturn(htA3);
                mockMvc.perform(get("/quan-ly-phong")
                                .param("ngungId", "3")
                                .with(user(new CustomUserDetails(createCanBoCSVC()))))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeDoesNotExist("ngungTarget"))
                                .andExpect(content().string(containsString("đã ở trạng thái Ngưng sử dụng.")));
        }

        @Test
        void canBoCsvConfirmsNgungSuDungRedirectsAndHandlesErrors() throws Exception {
                when(phongService.timPhong("")).thenReturn(Collections.emptyList());

                mockMvc.perform(post("/quan-ly-phong/ngung-su-dung")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("idPhong", "1")
                                .param("tuKhoa", "HT"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/quan-ly-phong?tuKhoa=HT"))
                                .andExpect(flash().attribute("successMessage", "Ngưng sử dụng phòng thành công."));

                verify(phongService).ngungSuDung(1);

                doThrow(new IllegalArgumentException("Không tìm thấy phòng cần ngưng sử dụng.")).when(phongService)
                                .ngungSuDung(999);
                mockMvc.perform(post("/quan-ly-phong/ngung-su-dung")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("idPhong", "999"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Không tìm thấy phòng cần ngưng sử dụng.")));

                doThrow(new DataAccessResourceFailureException("DB down")).when(phongService).ngungSuDung(2);
                mockMvc.perform(post("/quan-ly-phong/ngung-su-dung")
                                .with(user(new CustomUserDetails(createCanBoCSVC())))
                                .with(csrf())
                                .param("idPhong", "2"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(
                                                containsString("Lỗi kết nối cơ sở dữ liệu khi ngưng sử dụng phòng.")));
        }

        @Test
        void phongServiceNgungSuDungValidatesAndFindsFutureApprovedOrders() {
                PhongDAO daoMock = mock(PhongDAO.class);
                DonMuonPhongDAO donDaoMock = mock(DonMuonPhongDAO.class);
                PhongService service = new PhongService(daoMock, donDaoMock);

                Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_HOAT_DONG);
                DonMuonPhong don = new DonMuonPhong();
                don.setMaDon("DM-003");
                when(daoMock.getChiTiet(1)).thenReturn(htA2);
                when(donDaoMock.timDonDaDuyetTheoPhong(eq(htA2), any(LocalDate.class))).thenReturn(List.of(don));
                when(daoMock.ngungSuDung(1)).thenReturn(true);

                List<DonMuonPhong> futureOrders = service.layDonDaDuyetTuongLaiTheoPhong(1);
                assertEquals(1, futureOrders.size());
                assertEquals("DM-003", futureOrders.get(0).getMaDon());
                verify(donDaoMock).timDonDaDuyetTheoPhong(htA2, LocalDate.now());

                service.ngungSuDung(1);
                verify(daoMock).ngungSuDung(1);

                Phong htA3 = createSamplePhong(3, "HT-A3", "Hội trường A3", Phong.TINH_TRANG_NGUNG);
                when(daoMock.getChiTiet(3)).thenReturn(htA3);
                assertThrows(IllegalArgumentException.class, () -> service.ngungSuDung(3));
                verify(daoMock, never()).ngungSuDung(3);

                when(daoMock.getChiTiet(99)).thenReturn(null);
                assertThrows(IllegalArgumentException.class, () -> service.ngungSuDung(99));
        }

        @Test
        @SuppressWarnings("unchecked")
        void phongDaoAndDonMuonPhongDaoWorkForSlice3() {
                JdbcTemplate jdbcMock = mock(JdbcTemplate.class);
                PhongDAO phongDAO = new PhongDAO(jdbcMock);
                DonMuonPhongDAO donDAO = new DonMuonPhongDAO(jdbcMock);

                when(jdbcMock.update(anyString(), eq(Phong.TINH_TRANG_NGUNG), eq(1))).thenReturn(1);
                assertTrue(phongDAO.ngungSuDung(1));
                assertFalse(phongDAO.ngungSuDung(0));

                Phong htA2 = createSamplePhong(1, "HT-A2", "Hội trường A2", Phong.TINH_TRANG_HOAT_DONG);
                LocalDate fromDate = LocalDate.of(2026, 11, 19);
                LocalDate toDate = LocalDate.of(2026, 12, 31);

                DonMuonPhong sample = new DonMuonPhong();
                sample.setMaDon("DM-003");

                when(jdbcMock.query(anyString(), any(RowMapper.class),
                                eq(1), eq("DaPheDuyet"), eq(Date.valueOf(fromDate))))
                                .thenReturn(List.of(sample));
                assertEquals(1, donDAO.timDonDaDuyetTheoPhong(htA2, fromDate).size());

                when(jdbcMock.query(anyString(), any(RowMapper.class),
                                eq(1), eq("DaPheDuyet"), eq(Date.valueOf(fromDate)), eq(Date.valueOf(toDate))))
                                .thenReturn(List.of(sample));
                assertEquals(1, donDAO.timDonDaDuyetTheoPhong(htA2, fromDate, toDate).size());

                assertTrue(donDAO.timDonDaDuyetTheoPhong(null, fromDate).isEmpty());
                assertTrue(donDAO.timDonDaDuyetTheoPhong(htA2, null).isEmpty());
        }
}
