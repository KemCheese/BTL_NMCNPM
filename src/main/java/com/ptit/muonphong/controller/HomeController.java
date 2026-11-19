package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping({"/", "/trang-chu"})
    public String trangChu(Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        model.addAttribute("nguoiDung", nguoiDung);
        return "trang-chu";
    }
}
