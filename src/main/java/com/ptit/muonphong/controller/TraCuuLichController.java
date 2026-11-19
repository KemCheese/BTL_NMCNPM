package com.ptit.muonphong.controller;

import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.TraCuuLichService;
import com.ptit.muonphong.service.TraCuuLichService.PhongLichItem;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/tra-cuu-lich")
public class TraCuuLichController {

    private final AuthService authService;
    private final TraCuuLichService traCuuLichService;

    public TraCuuLichController(AuthService authService, TraCuuLichService traCuuLichService) {
        this.authService = authService;
        this.traCuuLichService = traCuuLichService;
    }

    @GetMapping
    public String traCuuLich(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
                             @RequestParam(required = false) String loaiPhong,
                             @RequestParam(required = false) Integer sucChua,
                             @RequestParam(required = false) String tinhTrang,
                             @RequestParam(required = false) String notFound,
                             Model model) {
        LocalDate ngayTraCuu = ngay != null ? ngay : LocalDate.now();
        String loaiPhongLoc = loaiPhong != null ? loaiPhong.trim() : "";
        String tinhTrangLoc = tinhTrang != null ? tinhTrang.trim() : "";

        Integer sucChuaHopLe = sucChua;
        String errorMessage = null;
        if (notFound != null) {
            errorMessage = "Không tìm thấy thông tin phòng được yêu cầu.";
        } else if (sucChua != null && sucChua <= 0) {
            errorMessage = "Sức chứa tối thiểu phải lớn hơn 0.";
            sucChuaHopLe = null;
        }

        List<PhongLichItem> ketQuaList = traCuuLichService.traCuuLich(
                ngayTraCuu, loaiPhongLoc, sucChuaHopLe, tinhTrangLoc);

        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("ngay", ngayTraCuu);
        model.addAttribute("loaiPhong", loaiPhongLoc);
        model.addAttribute("sucChua", sucChua);
        model.addAttribute("tinhTrang", tinhTrangLoc);
        model.addAttribute("dsLoaiPhong", traCuuLichService.getDanhSachLoaiPhong());
        model.addAttribute("dsTinhTrang", traCuuLichService.getDanhSachTinhTrang());
        model.addAttribute("ketQuaList", ketQuaList);
        model.addAttribute("errorMessage", errorMessage);
        return "tra-cuu-lich";
    }

    @GetMapping("/{idPhong}")
    public String xemChiTietPhong(@PathVariable int idPhong,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
                                  Model model) {
        LocalDate ngayTraCuu = ngay != null ? ngay : LocalDate.now();
        PhongLichItem chiTiet = traCuuLichService.getChiTietPhong(idPhong, ngayTraCuu);
        if (chiTiet == null) {
            return "redirect:/tra-cuu-lich?notFound=true";
        }

        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("ngay", ngayTraCuu);
        model.addAttribute("chiTiet", chiTiet);
        return "chi-tiet-phong";
    }
}
