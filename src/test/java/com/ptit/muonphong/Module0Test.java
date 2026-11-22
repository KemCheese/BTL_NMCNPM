package com.ptit.muonphong;

import com.ptit.muonphong.config.*;
import com.ptit.muonphong.controller.*;
import com.ptit.muonphong.dao.NguoiDungDAO;
import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ AuthController.class, HomeController.class })
@Import({ SecurityConfig.class, CustomUserDetailsService.class, AuthService.class })
class Module0Test {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NguoiDungDAO nguoiDungDAO;

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

        private CanBoCSVC createActiveCanBoCSVC() {
                CanBoCSVC cbcs = new CanBoCSVC();
                cbcs.setId(3);
                cbcs.setTenDangNhap("cb_cuong");
                cbcs.setMatKhau("123456");
                cbcs.setHoTen("Lê Văn Cường");
                cbcs.setTrangThai("Hoạt động");
                cbcs.setViTriCongViec("Chuyên viên CSVC");
                return cbcs;
        }

        @Test
        void unauthenticatedRedirectsToLogin() throws Exception {
                mockMvc.perform(get("/trang-chu"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        void emptyCredentialsDoNotCallDaoAndShowMessage() throws Exception {
                mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login?empty=true"));

                verify(nguoiDungDAO, never()).findByUsername(anyString());

                mockMvc.perform(get("/login").param("empty", "true"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(
                                                containsString("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!")));
        }

        @Test
        void inactiveUserCannotLogin() throws Exception {
                SinhVien inactive = createActiveSinhVien();
                inactive.setTenDangNhap("sv_khoa");
                inactive.setTrangThai("Ngưng");
                when(nguoiDungDAO.findByUsername("sv_khoa")).thenReturn(inactive);

                mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "sv_khoa")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login?error=true"));

                mockMvc.perform(get("/login").param("error", "true"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(
                                                containsString("Tên đăng nhập hoặc mật khẩu không chính xác!")));
        }

        @Test
        void databaseErrorShowsSystemErrorMessage() throws Exception {
                when(nguoiDungDAO.findByUsername("sv_an"))
                                .thenThrow(new DataAccessResourceFailureException("DB down"));

                mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "sv_an")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login?systemError=true"));

                mockMvc.perform(get("/login").param("systemError", "true"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                "Không thể đăng nhập do lỗi hệ thống. Vui lòng thử lại sau.")));
        }

        @Test
        void sinhVienLoginAndRoleMenuAndLogout() throws Exception {
                when(nguoiDungDAO.findByUsername("sv_an")).thenReturn(createActiveSinhVien());

                MvcResult loginRes = mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "sv_an")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/trang-chu"))
                                .andReturn();

                MockHttpSession session = (MockHttpSession) loginRes.getRequest().getSession(false);

                mockMvc.perform(get("/trang-chu").session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Hệ thống mượn phòng trực tuyến PTIT")))
                                .andExpect(content().string(containsString("href=\"/xac-nhan-dang-xuat\"")))
                                .andExpect(content().string(not(containsString("id=\"logoutModal\""))))
                                .andExpect(content().string(not(containsString("openLogoutModal"))))
                                .andExpect(content().string(containsString("NGƯỜI MƯỢN")))
                                .andExpect(content().string(containsString("Tra cứu lịch phòng")))
                                .andExpect(content().string(containsString("Nộp đơn")))
                                .andExpect(content().string(containsString("Theo dõi đơn")))
                                .andExpect(content().string(not(containsString("Duyệt đơn"))))
                                .andExpect(content().string(not(containsString("Duyệt đơn và bố trí phòng"))));

                mockMvc.perform(get("/xac-nhan-dang-xuat").session(session))
                                .andExpect(status().isOk())
                                .andExpect(view().name("xac-nhan-dang-xuat"))
                                .andExpect(content().string(containsString("Xác nhận đăng xuất")))
                                .andExpect(content().string(containsString("href=\"/trang-chu\"")))
                                .andExpect(content().string(containsString("action=\"/logout\"")));

                mockMvc.perform(get("/duyet-cap-1").session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(forwardedUrl("/403"));

                mockMvc.perform(post("/logout").with(csrf()).session(session))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login?logout=true"));
        }

        @Test
        void canBoDoanSeesOnlyCap1Menu() throws Exception {
                when(nguoiDungDAO.findByUsername("cb_binh")).thenReturn(createActiveCanBoDoan());

                MvcResult loginRes = mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "cb_binh")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/trang-chu"))
                                .andReturn();

                MockHttpSession session = (MockHttpSession) loginRes.getRequest().getSession(false);

                mockMvc.perform(get("/trang-chu").session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("CÁN BỘ ĐOÀN")))
                                .andExpect(content().string(containsString("Duyệt đơn cấp 1")))
                                .andExpect(content().string(not(containsString("Tra cứu lịch phòng"))))
                                .andExpect(content().string(not(containsString("Duyệt đơn cấp 2 và bố trí phòng"))));

                mockMvc.perform(get("/duyet-cap-2").session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(forwardedUrl("/403"));
        }

        @Test
        void canBoCsvSeesOnlyTrungTamMenu() throws Exception {
                when(nguoiDungDAO.findByUsername("cb_cuong")).thenReturn(createActiveCanBoCSVC());

                MvcResult loginRes = mockMvc.perform(post("/login")
                                .with(csrf())
                                .param("username", "cb_cuong")
                                .param("password", "123456"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/trang-chu"))
                                .andReturn();

                MockHttpSession session = (MockHttpSession) loginRes.getRequest().getSession(false);

                mockMvc.perform(get("/trang-chu").session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("CÁN BỘ TRUNG TÂM")))
                                .andExpect(content().string(containsString("Duyệt đơn cấp 2 và bố trí phòng")))
                                .andExpect(content().string(containsString("Quản lý danh mục phòng")))
                                .andExpect(content().string(containsString("Báo cáo, thống kê")))
                                .andExpect(content().string(not(containsString("Duyệt đơn cấp 1"))));

                mockMvc.perform(get("/nop-don").session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(forwardedUrl("/403"));
        }
}
