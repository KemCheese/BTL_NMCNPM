package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.entity.TrangThaiDon;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.TheoDoiDonService;
import com.ptit.muonphong.service.TheoDoiDonService.ChiTietDonResult;
import com.ptit.muonphong.service.TheoDoiDonService.DanhSachDonResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/theo-doi-don")
public class TheoDoiDonController {

    private final AuthService authService;
    private final TheoDoiDonService theoDoiDonService;

    public TheoDoiDonController(AuthService authService, TheoDoiDonService theoDoiDonService) {
        this.authService = authService;
        this.theoDoiDonService = theoDoiDonService;
    }

    @GetMapping
    public String hienThi(@RequestParam(required = false) String maDon,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
                          @RequestParam(required = false) String trangThai,
                          @RequestParam(required = false) Integer donId,
                          Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        DanhSachDonResult danhSach = theoDoiDonService.timDonCuaNguoiMuon(
                nguoiDung, maDon, tuNgay, denNgay, trangThai);

        ChiTietDonResult chiTietDon = null;
        boolean khongTimThayDon = false;
        if (donId != null) {
            chiTietDon = theoDoiDonService.getChiTietDon(donId, nguoiDung);
            if (chiTietDon == null) {
                khongTimThayDon = true;
            }
        }

        model.addAttribute("nguoiDung", nguoiDung);
        model.addAttribute("maDon", maDon);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("dsTrangThai", TrangThaiDon.values());
        model.addAttribute("dsDon", danhSach.getDsDon());
        model.addAttribute("phongBoTriMap", danhSach.getPhongBoTriMap());
        model.addAttribute("donId", donId);
        model.addAttribute("chiTietDon", chiTietDon);
        model.addAttribute("khongTimThayDon", khongTimThayDon);
        return "theo-doi-don";
    }
}
