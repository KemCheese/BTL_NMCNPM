package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String empty,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String systemError,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (authService.isAuthenticated()) {
            return "redirect:/trang-chu";
        }
        if (empty != null) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ tên đăng nhập vaf mật khẩu");
        } else if (systemError != null) {
            model.addAttribute("errorMessage", "Không thể đăng nhập do lỗi hệ thống. Vui lòng thử lại sau.");
        } else if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống.");
        }
        return "login";
    }

    @GetMapping("/xac-nhan-dang-xuat")
    public String xacNhanDangXuat(Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        model.addAttribute("nguoiDung", nguoiDung);
        return "xac-nhan-dang-xuat";
    }

    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("nguoiDung", authService.getCurrentUser());
        return "403";
    }
}
