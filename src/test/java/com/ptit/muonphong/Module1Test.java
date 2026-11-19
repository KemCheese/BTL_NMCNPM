package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.TraCuuLichController;
import com.ptit.muonphong.dao.*;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
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
import java.time.LocalTime;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraCuuLichController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class, AuthService.class})
class Module1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NguoiDungDAO nguoiDungDAO;

    @MockBean
    private TraCuuLichService traCuuLichService;

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
}
