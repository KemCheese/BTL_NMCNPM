package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.DuyetCap1Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/duyet-cap-1")
public class DuyetCap1Controller {

    private final AuthService authService;
    private final DuyetCap1Service duyetCap1Service;

    public DuyetCap1Controller(AuthService authService, DuyetCap1Service duyetCap1Service) {
        this.authService = authService;
        this.duyetCap1Service = duyetCap1Service;
    }

    @GetMapping
    public String xemDanhSach(@RequestParam(value = "tenCLB", required = false) String tenCLB,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId,
                              Model model) {
        populateModel(tenCLB, selectedId, model);
        return "duyet-cap-1";
    }

    @GetMapping("/{id}")
    public String xemChiTiet(@PathVariable int id,
                             @RequestParam(value = "tenCLB", required = false) String tenCLB,
                             Model model) {
        populateModel(tenCLB, id, model);
        return "duyet-cap-1";
    }

    @PostMapping("/{id}/chap-thuan")
    public String chapThuan(@PathVariable int id,
                            @RequestParam(value = "tenCLB", required = false) String tenCLB,
                            RedirectAttributes redirectAttributes) {
        try {
            NguoiDung canBoDoan = authService.getCurrentUser();
            duyetCap1Service.chapThuanDon(id, canBoDoan);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp thuận đơn và chuyển lên duyệt cấp 2.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addAttribute("selectedId", id);
        }
        if (tenCLB != null && !tenCLB.isBlank()) {
            redirectAttributes.addAttribute("tenCLB", tenCLB.trim());
        }
        return "redirect:/duyet-cap-1";
    }

    @PostMapping("/{id}/tu-choi")
    public String tuChoi(@PathVariable int id,
                         @RequestParam(value = "lyDo", required = false) String lyDo,
                         @RequestParam(value = "tenCLB", required = false) String tenCLB,
                         RedirectAttributes redirectAttributes) {
        try {
            NguoiDung canBoDoan = authService.getCurrentUser();
            duyetCap1Service.tuChoiDon(id, lyDo, canBoDoan);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn mượn phòng.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("lyDoDraft", lyDo);
            redirectAttributes.addAttribute("selectedId", id);
        }
        if (tenCLB != null && !tenCLB.isBlank()) {
            redirectAttributes.addAttribute("tenCLB", tenCLB.trim());
        }
        return "redirect:/duyet-cap-1";
    }

    private void populateModel(String tenCLB, Integer selectedId, Model model) {
        List<DonMuonPhong> tatCaChoDuyet = duyetCap1Service.layDanhSachChoDuyetCap1();
        List<DonMuonPhong> danhSachDon = duyetCap1Service.locTheoTenClb(tatCaChoDuyet, tenCLB);
        List<String> danhSachClb = duyetCap1Service.layDanhSachTenClb(tatCaChoDuyet);

        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("danhSachDon", danhSachDon);
        model.addAttribute("danhSachClb", danhSachClb);
        model.addAttribute("tenCLB", tenCLB != null ? tenCLB.trim() : "");

        if (selectedId != null) {
            try {
                model.addAttribute("donChiTiet", duyetCap1Service.xemChiTietDon(selectedId));
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
    }
}
