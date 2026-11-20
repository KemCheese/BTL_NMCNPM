package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.NopDonController;
import com.ptit.muonphong.controller.TheoDoiDonController;
import com.ptit.muonphong.controller.TraCuuLichController;
import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.NopDonService;
import com.ptit.muonphong.service.NopDonService.KetQuaNopDon;
import com.ptit.muonphong.service.NopDonService.NopDonCommand;
import com.ptit.muonphong.service.NopDonService.TimPhongResult;
import com.ptit.muonphong.service.TheoDoiDonService;
import com.ptit.muonphong.service.TheoDoiDonService.ChiTietDonResult;
import com.ptit.muonphong.service.TheoDoiDonService.DanhSachDonResult;
import com.ptit.muonphong.service.TraCuuLichService;
import com.ptit.muonphong.service.TraCuuLichService.PhongLichItem;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TraCuuLichController.class, NopDonController.class, TheoDoiDonController.class})
@Import({SecurityConfig.class, CustomUserDetailsService.class, AuthService.class})
class Module1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NguoiDungDAO nguoiDungDAO;

    @MockBean
    private TraCuuLichService traCuuLichService;

    @MockBean
    private NopDonService nopDonService;

    @MockBean
    private TheoDoiDonService theoDoiDonService;

    private SinhVien createActiveSinhVien() {
        DonVi dv = new DonVi();
        dv.setId(1);
        dv.setMaDonVi("CLB-IT");
        dv.setTenCLB("CLB Tin học");

        SinhVien sv = new SinhVien();
        sv.setId(1);
        sv.setTenDangNhap("sv_an");
        sv.setMatKhau("123456");
        sv.setHoTen("Nguyễn Văn An");
        sv.setTrangThai("Hoạt động");
        sv.setMaSinhVien("B22DCCN001");
        sv.setDonVi(dv);
        return sv;
    }

    private CanBoDoan createActiveCanBoDoan() {
        CanBoDoan cbd = new CanBoDoan();
        cbd.setId(2);
        cbd.setTenDangNhap("cb_binh");
        cbd.setMatKhau("123456");
        cbd.setHoTen("Trần Thị Bình");
        cbd.setTrangThai("Hoạt động");
        cbd.setViTriCongViec("Cán bộ Văn phòng Đoàn");
        return cbd;
    }

    private Phong createPhong(int id, String maPhong, String tenPhong, String tinhTrang) {
        Phong p = new Phong();
        p.setId(id);
        p.setMaPhong(maPhong);
        p.setTenPhong(tenPhong);
        p.setToaNha("A2");
        p.setTang(1);
        p.setSucChuaToiDa(300);
        p.setLoaiPhong("Hội trường");
        p.setTrangThietBi("máy chiếu, âm thanh, micro");
        p.setTinhTrang(tinhTrang);
        p.setMoTa("Hội trường lớn");
        return p;
    }

    private LichSuDungPhong createLich(Phong phong, LocalDate ngay) {
        DonVi dv = new DonVi();
        dv.setId(1);
        dv.setMaDonVi("CLB-IT");
        dv.setTenCLB("CLB Tin học");

        DonMuonPhong don = new DonMuonPhong();
        don.setId(3);
        don.setMaDon("DM-003");
        don.setNgaySuDung(ngay);
        don.setGioBatDau(LocalTime.of(8, 0));
        don.setGioKetThuc(LocalTime.of(11, 0));
        don.setTenSuKien("Sinh hoạt chuyên đề cuối năm");
        don.setTrangThai(TrangThaiDon.DaPheDuyet);
        don.setDonVi(dv);

        LichSuDungPhong lich = new LichSuDungPhong();
        lich.setId(1);
        lich.setPhong(phong);
        lich.setDonMuonPhong(don);
        return lich;
    }

    private RequestPostProcessor asSinhVien() {
        SinhVien sv = createActiveSinhVien();
        return authentication(new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(sv),
                sv.getMatKhau(),
                List.of(new SimpleGrantedAuthority("ROLE_NGUOI_MUON"))
        ));
    }

    private RequestPostProcessor asCanBoDoan() {
        CanBoDoan cbd = createActiveCanBoDoan();
        return authentication(new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(cbd),
                cbd.getMatKhau(),
                List.of(new SimpleGrantedAuthority("ROLE_CAN_BO_DOAN"))
        ));
    }

    @Test
    void unauthenticatedUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/tra-cuu-lich"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void canBoDoanCannotAccessTraCuuLich() throws Exception {
        mockMvc.perform(get("/tra-cuu-lich").with(asCanBoDoan()))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void nguoiMuonCanFilterRoomsAndSeeScheduleAndNopDonLink() throws Exception {
        LocalDate ngay = LocalDate.of(2026, 12, 30);

        Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", "Đang hoạt động");
        Phong htA3 = createPhong(3, "HT-A3", "Hội trường A3", "Ngưng sử dụng");

        when(traCuuLichService.getDanhSachLoaiPhong()).thenReturn(List.of("Hội trường", "Phòng học thường"));
        when(traCuuLichService.getDanhSachTinhTrang()).thenReturn(List.of("Đang hoạt động", "Ngưng sử dụng"));
        when(traCuuLichService.traCuuLich(ngay, "Hội trường", 100, ""))
                .thenReturn(List.of(
                        new PhongLichItem(htA2, List.of(createLich(htA2, ngay))),
                        new PhongLichItem(htA3, List.of())
                ));

        mockMvc.perform(get("/tra-cuu-lich")
                        .with(asSinhVien())
                        .param("ngay", "2026-12-30")
                        .param("loaiPhong", "Hội trường")
                        .param("sucChua", "100")
                        .param("tinhTrang", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tra cứu lịch phòng")))
                .andExpect(content().string(containsString("Hội trường A2")))
                .andExpect(content().string(containsString("08:00 - 11:00")))
                .andExpect(content().string(containsString("/nop-don?phongId=1&amp;ngaySuDung=2026-12-30")))
                .andExpect(content().string(containsString("Hội trường A3")))
                .andExpect(content().string(not(containsString("/nop-don?phongId=3"))));
    }

    @Test
    void invalidCapacityShowsValidationMessage() throws Exception {
        LocalDate ngay = LocalDate.of(2026, 12, 30);

        when(traCuuLichService.getDanhSachLoaiPhong()).thenReturn(List.of("Hội trường"));
        when(traCuuLichService.getDanhSachTinhTrang()).thenReturn(List.of("Đang hoạt động"));
        when(traCuuLichService.traCuuLich(ngay, "", null, "")).thenReturn(List.of());

        mockMvc.perform(get("/tra-cuu-lich")
                        .with(asSinhVien())
                        .param("ngay", "2026-12-30")
                        .param("sucChua", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sức chứa tối thiểu phải lớn hơn 0.")));
    }

    @Test
    void viewRoomDetailShowsScheduleAndNotFoundRedirects() throws Exception {
        LocalDate ngay = LocalDate.of(2026, 12, 30);
        Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", "Đang hoạt động");

        when(traCuuLichService.getChiTietPhong(1, ngay))
                .thenReturn(new PhongLichItem(htA2, List.of(createLich(htA2, ngay))));
        when(traCuuLichService.getChiTietPhong(999, ngay)).thenReturn(null);

        mockMvc.perform(get("/tra-cuu-lich/1")
                        .with(asSinhVien())
                        .param("ngay", "2026-12-30"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hội trường A2")))
                .andExpect(content().string(containsString("DM-003")))
                .andExpect(content().string(containsString("Sinh hoạt chuyên đề cuối năm")))
                .andExpect(content().string(containsString("08:00 - 11:00")))
                .andExpect(content().string(containsString("/nop-don?phongId=1&amp;ngaySuDung=2026-12-30")));

        mockMvc.perform(get("/tra-cuu-lich/999")
                        .with(asSinhVien())
                        .param("ngay", "2026-12-30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tra-cuu-lich?notFound=true"));
    }

    @Test
    void phongDaoBuildsDynamicFilterQuery() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        PhongDAO dao = new PhongDAO(jdbc);
        dao.locPhong(LocalDate.of(2026, 12, 30), "Hội trường", 100, "Đang hoạt động");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sqlCaptor.capture(), any(RowMapper.class),
                eq("Hội trường"), eq(100), eq("Đang hoạt động"));
        assertTrue(sqlCaptor.getValue().contains("loaiPhong = ?"));
        assertTrue(sqlCaptor.getValue().contains("sucChuaToiDa >= ?"));
        assertTrue(sqlCaptor.getValue().contains("tinhTrang = ?"));
    }

    @Test
    void lichSuDungPhongDaoSkipsInvalidInputs() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LichSuDungPhongDAO dao = new LichSuDungPhongDAO(jdbc);

        assertEquals(0, dao.getLichPhong(null, LocalDate.now()).size());
        assertEquals(0, dao.getLichPhong(new Phong(), LocalDate.now()).size());
        verify(jdbc, never()).query(anyString(), any(RowMapper.class), any());
    }

    @Test
    void uc04SecurityBlocksAnonymousAndCanBoDoan() throws Exception {
        mockMvc.perform(get("/nop-don"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/nop-don").with(asCanBoDoan()))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    void uc04DonMuonPhongKiemTraHopLeEnforcesBusinessRules() {
        LocalDateTime mocHienTai = LocalDateTime.of(2026, 12, 1, 10, 0);
        SinhVien sv = createActiveSinhVien();

        DonMuonPhong donHopLe = new DonMuonPhong();
        donHopLe.setNgaySuDung(LocalDate.of(2026, 12, 4));
        donHopLe.setGioBatDau(LocalTime.of(8, 0));
        donHopLe.setGioKetThuc(LocalTime.of(11, 0));
        donHopLe.setLoaiPhongMongMuon("Hội trường");
        donHopLe.setTenSuKien("Hội thảo CNTT");
        donHopLe.setSoNguoiThamDu(120);
        donHopLe.setMucDich("Giao lưu học thuật");
        donHopLe.setNoiDung("Chia sẻ kinh nghiệm");
        donHopLe.setSinhVien(sv);
        donHopLe.setDonVi(sv.getDonVi());
        assertTrue(donHopLe.kiemTraHopLe(mocHienTai));

        DonMuonPhong donSomQua = new DonMuonPhong();
        donSomQua.setNgaySuDung(LocalDate.of(2026, 12, 2));
        donSomQua.setGioBatDau(LocalTime.of(10, 0));
        donSomQua.setGioKetThuc(LocalTime.of(12, 0));
        donSomQua.setLoaiPhongMongMuon("Hội trường");
        donSomQua.setTenSuKien("Sự kiện gấp");
        donSomQua.setSoNguoiThamDu(50);
        donSomQua.setMucDich("Họp");
        donSomQua.setNoiDung("Họp CLB");
        donSomQua.setSinhVien(sv);
        donSomQua.setDonVi(sv.getDonVi());
        assertFalse(donSomQua.kiemTraHopLe(mocHienTai));

        donHopLe.setGioBatDau(LocalTime.of(6, 30));
        assertFalse(donHopLe.kiemTraHopLe(mocHienTai));

        donHopLe.setGioBatDau(LocalTime.of(19, 0));
        donHopLe.setGioKetThuc(LocalTime.of(22, 30));
        assertFalse(donHopLe.kiemTraHopLe(mocHienTai));
    }

    @Test
    void uc04PhongDaoAndDonMuonPhongDaoBuildQueriesAndSequentialMaDon() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        PhongDAO phongDAO = new PhongDAO(jdbc);
        phongDAO.timPhongTrong(LocalDate.of(2026, 12, 30), LocalTime.of(8, 0), LocalTime.of(11, 0),
                "Hội trường", 100, null);

        ArgumentCaptor<String> timSqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(timSqlCaptor.capture(), any(RowMapper.class),
                eq("Hội trường"), eq(100),
                eq(java.sql.Date.valueOf(LocalDate.of(2026, 12, 30))),
                eq(java.sql.Time.valueOf(LocalTime.of(11, 0))),
                eq(java.sql.Time.valueOf(LocalTime.of(8, 0))));
        assertTrue(timSqlCaptor.getValue().contains("tinhTrang = 'Đang hoạt động'"));
        assertTrue(timSqlCaptor.getValue().contains("NOT EXISTS"));

        when(jdbc.queryForList(contains("SELECT maDon FROM tblDonMuonPhong"), eq(String.class)))
                .thenReturn(List.of("DM-001", "DM-002", "DM-004"));
        DonMuonPhongDAO donDAO = new DonMuonPhongDAO(jdbc);
        assertEquals("DM-005", donDAO.sinhMaDonMoi());

        DonMuonPhong donCheck = new DonMuonPhong();
        donCheck.setNgaySuDung(LocalDate.of(2026, 12, 30));
        donCheck.setGioBatDau(LocalTime.of(8, 0));
        donCheck.setGioKetThuc(LocalTime.of(11, 0));
        donCheck.setLoaiPhongMongMuon("Hội trường");
        donCheck.setSoNguoiThamDu(100);

        Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", "Đang hoạt động");
        when(jdbc.queryForObject(contains("ls.phongId = ?"), eq(Integer.class), any(Object[].class)))
                .thenReturn(1);
        assertTrue(donDAO.kiemTraTrungLich(donCheck, htA2));

        when(jdbc.queryForObject(contains("FROM tblPhong p"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        assertTrue(donDAO.kiemTraTrungLich(donCheck, null));
    }

    @Test
    void uc04NopDonServiceValidatesAndSavesOrderWithHistory() {
        PhongDAO phongDAO = mock(PhongDAO.class);
        DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
        LichSuDonDAO lichSuDonDAO = mock(LichSuDonDAO.class);
        NopDonService service = new NopDonService(phongDAO, donDAO, lichSuDonDAO);

        SinhVien sv = createActiveSinhVien();
        Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", "Đang hoạt động");
        htA2.setSucChuaToiDa(100);

        NopDonCommand cmd = new NopDonCommand();
        cmd.setNgaySuDung(LocalDate.now().plusDays(5));
        cmd.setGioBatDau(LocalTime.of(14, 0));
        cmd.setGioKetThuc(LocalTime.of(17, 0));
        cmd.setLoaiPhongMongMuon("Hội trường");
        cmd.setPhongId(1);
        cmd.setTenSuKien("Workshop Spring Boot");
        cmd.setSoNguoiThamDu(150);
        cmd.setMucDich("Chia sẻ kỹ thuật");
        cmd.setNoiDung("Thực hành viết web");

        when(phongDAO.getChiTiet(1)).thenReturn(htA2);
        KetQuaNopDon quaSucChua = service.guiDon(cmd, sv);
        assertFalse(quaSucChua.isThanhCong());
        assertTrue(quaSucChua.getErrorMessage().contains("Sức chứa"));
        verify(donDAO, never()).taoDon(any());

        cmd.setSoNguoiThamDu(80);
        when(donDAO.kiemTraTrungLich(any(DonMuonPhong.class), eq(htA2))).thenReturn(true);
        KetQuaNopDon trungLich = service.guiDon(cmd, sv);
        assertFalse(trungLich.isThanhCong());
        assertTrue(trungLich.getErrorMessage().contains("trùng với khung giờ"));
        verify(donDAO, never()).taoDon(any());

        when(donDAO.kiemTraTrungLich(any(DonMuonPhong.class), eq(htA2))).thenReturn(false);
        when(donDAO.taoDon(any(DonMuonPhong.class))).thenAnswer(inv -> {
            DonMuonPhong d = inv.getArgument(0);
            d.setId(10);
            d.setMaDon("DM-005");
            return d;
        });

        cmd.setGhiChu("Cần mở điều hòa sớm");
        KetQuaNopDon thanhCong = service.guiDon(cmd, sv);
        assertTrue(thanhCong.isThanhCong());
        assertEquals("DM-005", thanhCong.getDonDaLuu().getMaDon());
        assertEquals(TrangThaiDon.ChoDuyetCap1, thanhCong.getDonDaLuu().getTrangThai());

        ArgumentCaptor<LichSuDon> lsCaptor = ArgumentCaptor.forClass(LichSuDon.class);
        verify(lichSuDonDAO).ghiThayDoi(lsCaptor.capture());
        assertNull(lsCaptor.getValue().getTrangThaiCu());
        assertEquals(TrangThaiDon.ChoDuyetCap1, lsCaptor.getValue().getTrangThaiMoi());
        assertEquals("Tạo đơn: Cần mở điều hòa sớm", lsCaptor.getValue().getGhiChu());
    }

    @Test
    void uc04ControllerHandlesTwoStepFlowAndSuccessRedirect() throws Exception {
        LocalDate ngayHopLe = LocalDate.now().plusDays(5);
        Phong htA2 = createPhong(1, "HT-A2", "Hội trường A2", "Đang hoạt động");

        when(nopDonService.getDanhSachLoaiPhong()).thenReturn(List.of("Hội trường", "Phòng học thường"));
        when(nopDonService.getPhongById(1)).thenReturn(htA2);
        when(nopDonService.timPhongTrong(ngayHopLe, LocalTime.of(8, 0), LocalTime.of(11, 0), "Hội trường", 100))
                .thenReturn(new TimPhongResult(true, null, List.of(htA2)));
        when(nopDonService.kiemTraThoiGianTimKiem(ngayHopLe, LocalTime.of(8, 0), LocalTime.of(11, 0), 100))
                .thenReturn(null);

        mockMvc.perform(get("/nop-don")
                        .with(asSinhVien())
                        .param("phongId", "1")
                        .param("ngaySuDung", ngayHopLe.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nộp đơn mượn phòng")))
                .andExpect(content().string(containsString("HT-A2 - Hội trường A2")));

        mockMvc.perform(post("/nop-don/tim-phong")
                        .with(asSinhVien())
                        .with(csrf())
                        .param("ngaySuDung", ngayHopLe.toString())
                        .param("gioBatDau", "08:00")
                        .param("gioKetThuc", "11:00")
                        .param("loaiPhongMongMuon", "Hội trường")
                        .param("sucChuaToiThieu", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"" + ngayHopLe.toString() + "\"")))
                .andExpect(content().string(containsString("Hội trường A2")))
                .andExpect(content().string(containsString("bỏ qua việc chọn phòng")));

        mockMvc.perform(post("/nop-don/buoc-2")
                        .with(asSinhVien())
                        .with(csrf())
                        .param("ngaySuDung", ngayHopLe.toString())
                        .param("gioBatDau", "08:00")
                        .param("gioKetThuc", "11:00")
                        .param("loaiPhongMongMuon", "Hội trường")
                        .param("phongId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CLB Tin học")))
                .andExpect(content().string(containsString("Nguyễn Văn An")))
                .andExpect(content().string(containsString("Gửi đơn")));

        DonMuonPhong donDaLuu = new DonMuonPhong();
        donDaLuu.setId(5);
        donDaLuu.setMaDon("DM-005");
        when(nopDonService.guiDon(any(NopDonCommand.class), any(NguoiDung.class)))
                .thenReturn(KetQuaNopDon.thanhCong(donDaLuu));

        mockMvc.perform(post("/nop-don")
                        .with(asSinhVien())
                        .with(csrf())
                        .param("ngaySuDung", ngayHopLe.toString())
                        .param("gioBatDau", "08:00")
                        .param("gioKetThuc", "11:00")
                        .param("loaiPhongMongMuon", "Hội trường")
                        .param("phongId", "1")
                        .param("tenSuKien", "Workshop AI")
                        .param("soNguoiThamDu", "100")
                        .param("mucDich", "Học thuật")
                        .param("noiDung", "Chia sẻ kiến thức AI"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nop-don?thanhCong=true&maDon=DM-005"));

        mockMvc.perform(get("/nop-don")
                        .with(asSinhVien())
                        .param("thanhCong", "true")
                        .param("maDon", "DM-005"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gửi đơn thành công!")))
                .andExpect(content().string(containsString("DM-005")));
    }

    @Test
    void uc07_serviceCapNhatHoanThanhQuaHan_vaLayChiTietDon() {
        DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
        LichSuDungPhongDAO lichDAO = mock(LichSuDungPhongDAO.class);
        LuotDuyetDAO luotDuyetDAO = mock(LuotDuyetDAO.class);
        LichSuDonDAO lichSuDonDAO = mock(LichSuDonDAO.class);
        TheoDoiDonService service = new TheoDoiDonService(donDAO, lichDAO, luotDuyetDAO, lichSuDonDAO);

        SinhVien sv = createActiveSinhVien();
        DonMuonPhong donQuaHan = new DonMuonPhong();
        donQuaHan.setId(8);
        donQuaHan.setMaDon("DM-008");
        donQuaHan.setTrangThai(TrangThaiDon.DaPheDuyet);
        donQuaHan.setSinhVien(sv);

        when(donDAO.timDonDaDuyetQuaHan(any(LocalDateTime.class))).thenReturn(List.of(donQuaHan));
        when(donDAO.capNhatTrangThai(8, TrangThaiDon.HoanThanh)).thenReturn(1);

        DonMuonPhong donTuChoi = new DonMuonPhong();
        donTuChoi.setId(10);
        donTuChoi.setMaDon("DM-010");
        donTuChoi.setTrangThai(TrangThaiDon.TuChoiCap1);
        donTuChoi.setSinhVien(sv);

        when(donDAO.getChiTiet(10)).thenReturn(donTuChoi);
        when(lichDAO.getPhongBoTriTheoDon(10)).thenReturn(null);
        when(luotDuyetDAO.getTheoDon(10)).thenReturn(List.of());
        when(lichSuDonDAO.getTheoDon(10)).thenReturn(List.of());

        ChiTietDonResult result = service.getChiTietDon(10, sv);
        assertNotNull(result);
        assertEquals("DM-010", result.getDon().getMaDon());
        assertTrue(result.isCoTheTaoLai());

        verify(donDAO).capNhatTrangThai(8, TrangThaiDon.HoanThanh);
        ArgumentCaptor<LichSuDon> lichSuCaptor = ArgumentCaptor.forClass(LichSuDon.class);
        verify(lichSuDonDAO).ghiThayDoi(lichSuCaptor.capture());
        assertEquals(TrangThaiDon.DaPheDuyet, lichSuCaptor.getValue().getTrangThaiCu());
        assertEquals(TrangThaiDon.HoanThanh, lichSuCaptor.getValue().getTrangThaiMoi());
    }

    @Test
    @SuppressWarnings("unchecked")
    void uc07_daoTimTheoNguoiMuon_sapXepMoiNhatVaLoc() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DonMuonPhongDAO dao = new DonMuonPhongDAO(jdbc);
        SinhVien sv = createActiveSinhVien();

        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        dao.timTheoNguoiMuon(sv, "DM-01", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                TrangThaiDon.ChoDuyetCap1);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).query(sqlCaptor.capture(), any(RowMapper.class), paramsCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("d.sinhVienId = ?"));
        assertTrue(sql.contains("LOWER(d.maDon) LIKE ?"));
        assertTrue(sql.contains("d.ngaySuDung >= ?"));
        assertTrue(sql.contains("d.ngaySuDung <= ?"));
        assertTrue(sql.contains("d.trangThai = ?"));
        assertTrue(sql.contains("ORDER BY d.ngayTao DESC, d.id DESC"));
        assertEquals(5, paramsCaptor.getValue().length);
    }

    @Test
    void uc07_getTheoDoiDon_hienThiDanhSachVaChiTietVaNutTaoDonLai() throws Exception {
        SinhVien sv = createActiveSinhVien();
        DonMuonPhong don = new DonMuonPhong();
        don.setId(10);
        don.setMaDon("DM-010");
        don.setNgayTao(LocalDate.of(2026, 10, 1));
        don.setNgaySuDung(LocalDate.of(2026, 10, 20));
        don.setGioBatDau(LocalTime.of(8, 0));
        don.setGioKetThuc(LocalTime.of(10, 0));
        don.setLoaiPhongMongMuon("Hội trường");
        don.setTenSuKien("Ngày hội IT");
        don.setMucDich("Giao lưu");
        don.setNoiDung("Chia sẻ định hướng");
        don.setSoNguoiThamDu(120);
        don.setTrangThai(TrangThaiDon.TuChoiCap2);
        don.setDonVi(sv.getDonVi());
        don.setSinhVien(sv);

        Phong phongBoTri = new Phong();
        phongBoTri.setId(2);
        phongBoTri.setMaPhong("HT-01");
        phongBoTri.setTenPhong("Hội trường lớn");

        CanBoCSVC canBo = new CanBoCSVC();
        canBo.setId(3);
        canBo.setHoTen("Trần Thị Bình");

        LuotDuyet ld2 = new LuotDuyet();
        ld2.setCapDuyet(2);
        ld2.setKetQua("Từ chối");
        ld2.setLyDo("Trùng lịch hội nghị trường");
        ld2.setThoiDiem(LocalDateTime.of(2026, 10, 2, 14, 30));
        ld2.setNguoiDuyet(canBo);

        LichSuDon ls = new LichSuDon();
        ls.setTrangThaiCu(TrangThaiDon.ChoDuyetCap2);
        ls.setTrangThaiMoi(TrangThaiDon.TuChoiCap2);
        ls.setGhiChu("Trùng lịch hội nghị trường");
        ls.setThoiDiem(LocalDateTime.of(2026, 10, 2, 14, 30));
        ls.setNguoiThucHien(canBo);

        when(theoDoiDonService.timDonCuaNguoiMuon(any(NguoiDung.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new DanhSachDonResult(List.of(don), Map.of(10, phongBoTri)));
        when(theoDoiDonService.getChiTietDon(eq(10), any(NguoiDung.class)))
                .thenReturn(new ChiTietDonResult(don, phongBoTri, List.of(ld2), List.of(ls), true));

        mockMvc.perform(get("/theo-doi-don")
                        .with(asSinhVien())
                        .param("donId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DM-010")))
                .andExpect(content().string(containsString("HT-01 - Hội trường lớn")))
                .andExpect(content().string(containsString("Trùng lịch hội nghị trường")))
                .andExpect(content().string(containsString("Tạo đơn mới từ đơn này")))
                .andExpect(content().string(containsString("/nop-don?donCuId=10")));
    }

    @Test
    void uc07_getTheoDoiDon_donKhongThuocSoHuu_hienThiCanhBao() throws Exception {
        when(theoDoiDonService.timDonCuaNguoiMuon(any(NguoiDung.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new DanhSachDonResult(List.of(), Map.of()));
        when(theoDoiDonService.getChiTietDon(eq(99), any(NguoiDung.class))).thenReturn(null);

        mockMvc.perform(get("/theo-doi-don")
                        .with(asSinhVien())
                        .param("donId", "99"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Không tìm thấy đơn mượn phòng được chọn")))
                .andExpect(content().string(not(containsString("Chi tiết đơn"))));
    }

    @Test
    void uc07_taoDonTuDonCu_chiChoPhepDonBiTuChoiVaDienSanSucChua() throws Exception {
        PhongDAO phongDAO = mock(PhongDAO.class);
        DonMuonPhongDAO donDAO = mock(DonMuonPhongDAO.class);
        LichSuDonDAO lichSuDonDAO = mock(LichSuDonDAO.class);
        NopDonService service = new NopDonService(phongDAO, donDAO, lichSuDonDAO);

        SinhVien sv = createActiveSinhVien();
        DonMuonPhong donCho = new DonMuonPhong();
        donCho.setId(11);
        donCho.setTrangThai(TrangThaiDon.ChoDuyetCap1);
        donCho.setSinhVien(sv);

        DonMuonPhong donTuChoi = new DonMuonPhong();
        donTuChoi.setId(12);
        donTuChoi.setTrangThai(TrangThaiDon.TuChoiCap1);
        donTuChoi.setLoaiPhongMongMuon("Hội trường");
        donTuChoi.setSoNguoiThamDu(120);
        donTuChoi.setTenSuKien("Seminar CNTT");
        donTuChoi.setMucDich("Học thuật");
        donTuChoi.setNoiDung("Trao đổi chuyên môn");
        donTuChoi.setSinhVien(sv);

        when(donDAO.getChiTiet(11)).thenReturn(donCho);
        when(donDAO.getChiTiet(12)).thenReturn(donTuChoi);

        assertNull(service.getDonMauDeTaoLai(11, sv));
        assertNotNull(service.getDonMauDeTaoLai(12, sv));

        when(nopDonService.getDonMauDeTaoLai(eq(12), any(NguoiDung.class))).thenReturn(donTuChoi);
        when(nopDonService.getDanhSachLoaiPhong()).thenReturn(List.of("Hội trường"));

        mockMvc.perform(get("/nop-don")
                        .with(asSinhVien())
                        .param("donCuId", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"120\"")))
                .andExpect(content().string(containsString("Seminar CNTT")));
    }
}
