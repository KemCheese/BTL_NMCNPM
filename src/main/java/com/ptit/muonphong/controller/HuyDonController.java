package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.HuyDonService;
import com.ptit.muonphong.service.HuyDonService.KetQuaHuyDon;
import com.ptit.muonphong.service.HuyDonService.ThongTinHuyDonResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/theo-doi-don")
public class HuyDonController {

    private final AuthService authService;
    private final HuyDonService huyDonService;

    public HuyDonController(AuthService authService, HuyDonService huyDonService) {
        this.authService = authService;
        this.huyDonService = huyDonService;
    }

    @GetMapping("/{id}/huy")
    public String hienThiXacNhanHuy(@PathVariable("id") int idDon,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        ThongTinHuyDonResult ketQua = huyDonService.getChiTietDonDeHuy(idDon, nguoiDung);
        if (!ketQua.isChoPhep()) {
            redirectAttributes.addFlashAttribute("errorMessage", ketQua.getErrorMessage());
            return "redirect:/theo-doi-don";
        }
        model.addAttribute("nguoiDung", nguoiDung);
        model.addAttribute("don", ketQua.getDon());
        model.addAttribute("phongBoTri", ketQua.getPhongBoTri());
        return "huy-don";
    }

    @PostMapping("/{id}/huy")
    public String xacNhanHuy(@PathVariable("id") int idDon,
                             RedirectAttributes redirectAttributes) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        KetQuaHuyDon ketQua = huyDonService.huyDon(idDon, nguoiDung);
        if (!ketQua.isThanhCong()) {
            redirectAttributes.addFlashAttribute("errorMessage", ketQua.getErrorMessage());
            return "redirect:/theo-doi-don";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn thành công.");
        return "redirect:/theo-doi-don";
    }
}
