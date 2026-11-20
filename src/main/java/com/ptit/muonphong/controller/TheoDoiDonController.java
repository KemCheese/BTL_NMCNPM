package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.entity.TrangThaiDon;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.SuaDonService;
import com.ptit.muonphong.service.SuaDonService.KetQuaSuaDon;
import com.ptit.muonphong.service.SuaDonService.MoFormSuaResult;
import com.ptit.muonphong.service.SuaDonService.SuaDonCommand;
import com.ptit.muonphong.service.TheoDoiDonService;
import com.ptit.muonphong.service.TheoDoiDonService.ChiTietDonResult;
import com.ptit.muonphong.service.TheoDoiDonService.DanhSachDonResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/theo-doi-don")
public class TheoDoiDonController {

    private final AuthService authService;
    private final TheoDoiDonService theoDoiDonService;
    private final SuaDonService suaDonService;

    public TheoDoiDonController(AuthService authService,
                                TheoDoiDonService theoDoiDonService,
                                SuaDonService suaDonService) {
        this.authService = authService;
        this.theoDoiDonService = theoDoiDonService;
        this.suaDonService = suaDonService;
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

    @GetMapping("/{id}/sua")
    public String hienThiSuaDon(@PathVariable("id") int idDon,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        MoFormSuaResult ketQua = suaDonService.moFormSua(idDon, nguoiDung);
        if (!ketQua.isChoPhep()) {
            redirectAttributes.addFlashAttribute("errorMessage", ketQua.getErrorMessage());
            return "redirect:/theo-doi-don";
        }
        model.addAttribute("nguoiDung", nguoiDung);
        model.addAttribute("don", ketQua.getDon());
        model.addAttribute("suaDonCommand", ketQua.getCommand());
        model.addAttribute("dsLoaiPhong", suaDonService.getDanhSachLoaiPhong());
        return "sua-don";
    }

    @PostMapping("/{id}/sua")
    public String luuSuaDon(@PathVariable("id") int idDon,
                            @ModelAttribute("suaDonCommand") SuaDonCommand command,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        KetQuaSuaDon ketQua = suaDonService.capNhatDon(idDon, command, nguoiDung);
        if (!ketQua.isThanhCong()) {
            MoFormSuaResult formInfo = suaDonService.moFormSua(idDon, nguoiDung);
            if (!formInfo.isChoPhep()) {
                redirectAttributes.addFlashAttribute("errorMessage", ketQua.getErrorMessage());
                return "redirect:/theo-doi-don";
            }
            command.setMaDon(formInfo.getDon().getMaDon());
            model.addAttribute("nguoiDung", nguoiDung);
            model.addAttribute("don", formInfo.getDon());
            model.addAttribute("suaDonCommand", command);
            model.addAttribute("dsLoaiPhong", suaDonService.getDanhSachLoaiPhong());
            model.addAttribute("errorMessage", ketQua.getErrorMessage());
            return "sua-don";
        }

        String thongBao = ketQua.isDaChuyenTuBoTri()
                ? "Cập nhật đơn thành công (" + ketQua.getDon().getMaDon()
                  + " - chuyển sang Trung tâm tự bố trí do phòng mong muốn cũ không còn phù hợp)."
                : "Cập nhật đơn thành công.";
        redirectAttributes.addFlashAttribute("successMessage", thongBao);
        return "redirect:/theo-doi-don";
    }
}
