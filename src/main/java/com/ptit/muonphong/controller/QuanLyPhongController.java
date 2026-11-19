package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.Phong;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.PhongService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/quan-ly-phong")
public class QuanLyPhongController {

    private final PhongService phongService;
    private final AuthService authService;

    public QuanLyPhongController(PhongService phongService, AuthService authService) {
        this.phongService = phongService;
        this.authService = authService;
    }

    @GetMapping
    public String hienThi(@RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "suaId", required = false) Integer suaId,
            @RequestParam(value = "ngungId", required = false) Integer ngungId,
            Model model) {
        Phong formModel = null;
        boolean editMode = false;
        if (suaId != null) {
            try {
                Phong phongCanSua = phongService.getChiTiet(suaId);
                if (phongCanSua != null) {
                    formModel = phongCanSua;
                    editMode = true;
                } else {
                    model.addAttribute("errorMessage", "Không tìm thấy phòng cần sửa.");
                }
            } catch (DataAccessException ex) {
                model.addAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi tải thông tin phòng.");
            }
        }
        if (ngungId != null) {
            try {
                Phong phongCanNgung = phongService.getChiTiet(ngungId);
                if (phongCanNgung == null) {
                    model.addAttribute("errorMessage", "Không tìm thấy phòng cần ngưng sử dụng.");
                } else if (!phongCanNgung.isDangHoatDong()) {
                    model.addAttribute("errorMessage",
                            "Phòng \"" + phongCanNgung.getMaPhong() + "\" đã ở trạng thái Ngưng sử dụng.");
                } else {
                    List<DonMuonPhong> donLienQuan = phongService.layDonDaDuyetTuongLaiTheoPhong(ngungId);
                    model.addAttribute("ngungTarget", phongCanNgung);
                    model.addAttribute("donLienQuanNgung", donLienQuan);
                }
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            } catch (DataAccessException ex) {
                model.addAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi kiểm tra đơn liên quan.");
            }
        }
        prepareViewModel(model, tuKhoa, formModel, editMode);
        return "quan-ly-phong";
    }

    @PostMapping("/them")
    public String themPhong(@ModelAttribute("phongForm") Phong phongForm,
            BindingResult bindingResult,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Tầng và sức chứa tối đa phải là số nguyên hợp lệ.");
            prepareViewModel(model, tuKhoa, phongForm, false);
            return "quan-ly-phong";
        }

        try {
            phongService.themPhong(phongForm);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepareViewModel(model, tuKhoa, phongForm, false);
            return "quan-ly-phong";
        } catch (DataAccessException ex) {
            model.addAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi thêm phòng. Vui lòng thử lại sau.");
            prepareViewModel(model, tuKhoa, phongForm, false);
            return "quan-ly-phong";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thêm phòng thành công.");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            redirectAttributes.addAttribute("tuKhoa", tuKhoa.trim());
        }
        return "redirect:/quan-ly-phong";
    }

    @PostMapping("/sua")
    public String suaPhong(@ModelAttribute("phongForm") Phong phongForm,
            BindingResult bindingResult,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            khoiPhucThongTinCoDinhKhiLoi(phongForm);
            model.addAttribute("errorMessage", "Tầng và sức chứa tối đa phải là số nguyên hợp lệ.");
            prepareViewModel(model, tuKhoa, phongForm, true);
            return "quan-ly-phong";
        }

        try {
            phongService.capNhatPhong(phongForm);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            khoiPhucThongTinCoDinhKhiLoi(phongForm);
            model.addAttribute("errorMessage", ex.getMessage());
            prepareViewModel(model, tuKhoa, phongForm, true);
            return "quan-ly-phong";
        } catch (DataAccessException ex) {
            model.addAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi cập nhật phòng. Vui lòng thử lại sau.");
            prepareViewModel(model, tuKhoa, phongForm, true);
            return "quan-ly-phong";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng thành công.");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            redirectAttributes.addAttribute("tuKhoa", tuKhoa.trim());
        }
        return "redirect:/quan-ly-phong";
    }

    @PostMapping("/ngung-su-dung")
    public String ngungSuDung(@RequestParam("idPhong") int idPhong,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            phongService.ngungSuDung(idPhong);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            prepareViewModel(model, tuKhoa, null, false);
            return "quan-ly-phong";
        } catch (DataAccessException ex) {
            model.addAttribute("errorMessage", "Lỗi kết nối cơ sở dữ liệu khi ngưng sử dụng phòng. Vui lòng thử lại sau.");
            prepareViewModel(model, tuKhoa, null, false);
            return "quan-ly-phong";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Ngưng sử dụng phòng thành công.");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            redirectAttributes.addAttribute("tuKhoa", tuKhoa.trim());
        }
        return "redirect:/quan-ly-phong";
    }

    private void khoiPhucThongTinCoDinhKhiLoi(Phong phongForm) {
        if (phongForm == null || phongForm.getId() <= 0) {
            return;
        }
        try {
            Phong hienTai = phongService.getChiTiet(phongForm.getId());
            if (hienTai != null) {
                phongForm.setMaPhong(hienTai.getMaPhong());
                phongForm.setTinhTrang(hienTai.getTinhTrang());
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void prepareViewModel(Model model, String tuKhoa, Phong formModel, boolean editMode) {
        String normalizedKeyword = tuKhoa == null ? "" : tuKhoa.trim();
        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("tuKhoa", normalizedKeyword);
        model.addAttribute("loaiPhongOptions", Phong.LOAI_PHONG_HOP_LE);
        model.addAttribute("editMode", editMode);

        if (formModel != null) {
            model.addAttribute("phongForm", formModel);
        } else if (!model.containsAttribute("phongForm")) {
            model.addAttribute("phongForm", new Phong());
        }

        try {
            List<Phong> phongs = phongService.timPhong(normalizedKeyword);
            model.addAttribute("phongs", phongs);
        } catch (DataAccessException ex) {
            model.addAttribute("phongs", Collections.emptyList());
            if (!model.containsAttribute("errorMessage")) {
                model.addAttribute("errorMessage", "Không thể tải danh sách phòng từ cơ sở dữ liệu.");
            }
        }
    }
}
