package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.*;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.NopDonService;
import com.ptit.muonphong.service.NopDonService.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/nop-don")
public class NopDonController {

    private final AuthService authService;
    private final NopDonService nopDonService;

    public NopDonController(AuthService authService, NopDonService nopDonService) {
        this.authService = authService;
        this.nopDonService = nopDonService;
    }

    @GetMapping
    public String hienThiForm(@RequestParam(required = false) Integer phongId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngaySuDung,
                              @RequestParam(required = false) Integer donCuId,
                              @RequestParam(required = false) Boolean thanhCong,
                              @RequestParam(required = false) String maDon,
                              Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        NopDonCommand form = new NopDonCommand();

        if (donCuId != null) {
            DonMuonPhong donCu = nopDonService.getDonMauDeTaoLai(donCuId, nguoiDung);
            if (donCu != null) {
                form.setLoaiPhongMongMuon(donCu.getLoaiPhongMongMuon());
                form.setThietBiYeuCau(donCu.getThietBiYeuCau());
                form.setTenSuKien(donCu.getTenSuKien());
                form.setSoNguoiThamDu(donCu.getSoNguoiThamDu());
                form.setSucChuaToiThieu(donCu.getSoNguoiThamDu());
                form.setMucDich(donCu.getMucDich());
                form.setNoiDung(donCu.getNoiDung());
                if (donCu.getPhongMongMuon() != null && donCu.getPhongMongMuon().isDangHoatDong()) {
                    form.setPhongId(donCu.getPhongMongMuon().getId());
                }
            }
        }

        if (ngaySuDung != null) {
            form.setNgaySuDung(ngaySuDung);
        }

        Phong phongDaChon = null;
        if (phongId != null && phongId > 0) {
            phongDaChon = nopDonService.getPhongById(phongId);
            if (phongDaChon != null && phongDaChon.isDangHoatDong()) {
                form.setPhongId(phongDaChon.getId());
                if (form.getLoaiPhongMongMuon() == null || form.getLoaiPhongMongMuon().isBlank()) {
                    form.setLoaiPhongMongMuon(phongDaChon.getLoaiPhong());
                }
            } else {
                phongDaChon = null;
            }
        } else if (form.getPhongId() != null && form.getPhongId() > 0) {
            phongDaChon = nopDonService.getPhongById(form.getPhongId());
        }

        prepareCommonModel(model, nguoiDung, form, 1, false, List.of(), phongDaChon, null);
        if (Boolean.TRUE.equals(thanhCong) && maDon != null && !maDon.isBlank()) {
            model.addAttribute("thanhCong", true);
            model.addAttribute("maDonThanhCong", maDon);
        }
        return "nop-don";
    }

    @PostMapping("/tim-phong")
    public String timPhongTrong(@ModelAttribute("form") NopDonCommand form, Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        TimPhongResult result = nopDonService.timPhongTrong(
                form.getNgaySuDung(),
                form.getGioBatDau(),
                form.getGioKetThuc(),
                form.getLoaiPhongMongMuon(),
                form.getSucChuaToiThieu());

        List<Phong> dsPhongTrong = result.getDsPhongTrong();
        Phong phongDaChon = resolveSelectedPhongInList(form, dsPhongTrong);

        prepareCommonModel(model, nguoiDung, form, 1, result.isHopLe(), dsPhongTrong, phongDaChon,
                result.isHopLe() ? null : result.getErrorMessage());
        return "nop-don";
    }

    @PostMapping("/buoc-2")
    public String sangBuoc2(@ModelAttribute("form") NopDonCommand form, Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        String timeError = nopDonService.kiemTraThoiGianTimKiem(
                form.getNgaySuDung(), form.getGioBatDau(), form.getGioKetThuc(), form.getSucChuaToiThieu());
        if (timeError != null) {
            Phong phongHienTai = form.getPhongId() != null && form.getPhongId() > 0
                    ? nopDonService.getPhongById(form.getPhongId()) : null;
            prepareCommonModel(model, nguoiDung, form, 1, false, List.of(), phongHienTai, timeError);
            return "nop-don";
        }

        Phong phongDaChon = null;
        if (form.getPhongId() != null && form.getPhongId() > 0) {
            phongDaChon = nopDonService.getPhongById(form.getPhongId());
            if (phongDaChon == null || !phongDaChon.isDangHoatDong()) {
                form.setPhongId(null);
                TimPhongResult result = nopDonService.timPhongTrong(
                        form.getNgaySuDung(), form.getGioBatDau(), form.getGioKetThuc(),
                        form.getLoaiPhongMongMuon(), form.getSucChuaToiThieu());
                prepareCommonModel(model, nguoiDung, form, 1, result.isHopLe(), result.getDsPhongTrong(), null,
                        "Phòng vừa chọn không còn khả dụng. Vui lòng chọn phòng khác hoặc bỏ qua chọn phòng.");
                return "nop-don";
            }
            if (form.getLoaiPhongMongMuon() == null || form.getLoaiPhongMongMuon().isBlank()) {
                form.setLoaiPhongMongMuon(phongDaChon.getLoaiPhong());
            }
        }

        if (form.getSoNguoiThamDu() == null && form.getSucChuaToiThieu() != null) {
            form.setSoNguoiThamDu(form.getSucChuaToiThieu());
        }

        prepareCommonModel(model, nguoiDung, form, 2, true, List.of(), phongDaChon, null);
        return "nop-don";
    }

    @PostMapping("/quay-lai")
    public String quayLaiBuoc1(@ModelAttribute("form") NopDonCommand form, Model model) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        TimPhongResult result = nopDonService.timPhongTrong(
                form.getNgaySuDung(),
                form.getGioBatDau(),
                form.getGioKetThuc(),
                form.getLoaiPhongMongMuon(),
                form.getSucChuaToiThieu());
        List<Phong> dsPhongTrong = result.getDsPhongTrong();
        Phong phongDaChon = resolveSelectedPhongInList(form, dsPhongTrong);

        prepareCommonModel(model, nguoiDung, form, 1, result.isHopLe(), dsPhongTrong, phongDaChon,
                result.isHopLe() ? null : result.getErrorMessage());
        return "nop-don";
    }

    @PostMapping
    public String guiDon(@ModelAttribute("form") NopDonCommand form,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        NguoiDung nguoiDung = authService.getCurrentUser();
        KetQuaNopDon ketQua = nopDonService.guiDon(form, nguoiDung);
        if (ketQua.isThanhCong()) {
            redirectAttributes.addAttribute("thanhCong", true);
            redirectAttributes.addAttribute("maDon", ketQua.getDonDaLuu().getMaDon());
            return "redirect:/nop-don";
        }

        Phong phongDaChon = null;
        if (form.getPhongId() != null && form.getPhongId() > 0) {
            phongDaChon = nopDonService.getPhongById(form.getPhongId());
        }
        prepareCommonModel(model, nguoiDung, form, 2, true, List.of(), phongDaChon, ketQua.getErrorMessage());
        return "nop-don";
    }

    private Phong resolveSelectedPhongInList(NopDonCommand form, List<Phong> dsPhongTrong) {
        if (form.getPhongId() == null || form.getPhongId() <= 0) {
            return null;
        }
        for (Phong p : dsPhongTrong) {
            if (p.getId() == form.getPhongId()) {
                return p;
            }
        }
        form.setPhongId(null);
        return null;
    }

    private void prepareCommonModel(Model model,
                                    NguoiDung nguoiDung,
                                    NopDonCommand form,
                                    int buoc,
                                    boolean daTimKiem,
                                    List<Phong> dsPhongTrong,
                                    Phong phongDaChon,
                                    String errorMessage) {
        model.addAttribute("nguoiDung", nguoiDung);
        model.addAttribute("form", form);
        model.addAttribute("buoc", buoc);
        model.addAttribute("daTimKiem", daTimKiem);
        model.addAttribute("thanhCong", false);
        model.addAttribute("dsPhongTrong", dsPhongTrong);
        model.addAttribute("phongDaChon", phongDaChon);
        model.addAttribute("dsLoaiPhong", nopDonService.getDanhSachLoaiPhong());
        model.addAttribute("errorMessage", errorMessage);
    }
}
