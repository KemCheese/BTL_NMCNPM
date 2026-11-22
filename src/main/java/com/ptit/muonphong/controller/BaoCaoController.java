package com.ptit.muonphong.controller;

import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.BaoCaoService;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/bao-cao")
public class BaoCaoController {

    private final BaoCaoService baoCaoService;
    private final AuthService authService;

    public BaoCaoController(BaoCaoService baoCaoService, AuthService authService) {
        this.baoCaoService = baoCaoService;
        this.authService = authService;
    }

    @GetMapping
    public String hienThi(
            @RequestParam(value = "tieuChi", required = false, defaultValue = BaoCaoService.TIEU_CHI_THEO_PHONG) String tieuChi,
            @RequestParam(value = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(value = "chiTietPhongId", required = false) Integer chiTietPhongId,
            Model model) {

        LocalDate today = LocalDate.now();
        LocalDate effectiveTuNgay = tuNgay;
        LocalDate effectiveDenNgay = denNgay;
        if (tuNgay == null && denNgay == null) {
            effectiveTuNgay = today.withDayOfMonth(1);
            effectiveDenNgay = today.withDayOfMonth(today.lengthOfMonth());
        }

        List<BaoCaoService.ThongKeDon> dsThongKe = Collections.emptyList();
        boolean coLoiChung = false;
        try {
            dsThongKe = baoCaoService.xemBaoCaoTheoPhong(tieuChi, effectiveTuNgay, effectiveDenNgay);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            coLoiChung = true;
        } catch (DataAccessException ex) {
            model.addAttribute("errorMessage",
                    "Lỗi kết nối cơ sở dữ liệu khi tải báo cáo thống kê. Vui lòng thử lại sau.");
            coLoiChung = true;
        }

        BaoCaoService.ChiTietPhongBaoCao chiTietBaoCao = null;
        if (!coLoiChung && chiTietPhongId != null) {
            try {
                chiTietBaoCao = baoCaoService.xemChiTietDong(chiTietPhongId, effectiveTuNgay, effectiveDenNgay);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("chiTietErrorMessage", ex.getMessage());
            } catch (DataAccessException ex) {
                model.addAttribute("chiTietErrorMessage",
                        "Lỗi kết nối cơ sở dữ liệu khi tải danh sách đơn chi tiết của phòng.");
            }
        }

        double maxTongGio = 0.0;
        int tongLuotTatCa = 0;
        double tongGioTatCa = 0.0;
        for (BaoCaoService.ThongKeDon row : dsThongKe) {
            if (row.getTongGio() > maxTongGio) {
                maxTongGio = row.getTongGio();
            }
            tongLuotTatCa += row.getSoLuot();
            tongGioTatCa += row.getTongGio();
        }

        long soNgayTrongKy = 0;
        if (effectiveTuNgay != null && effectiveDenNgay != null && !effectiveDenNgay.isBefore(effectiveTuNgay)) {
            soNgayTrongKy = ChronoUnit.DAYS.between(effectiveTuNgay, effectiveDenNgay) + 1;
        }

        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("tieuChi", BaoCaoService.laTieuChiTheoPhongHopLe(tieuChi)
                ? BaoCaoService.TIEU_CHI_THEO_PHONG
                : tieuChi);
        model.addAttribute("tuNgay", effectiveTuNgay);
        model.addAttribute("denNgay", effectiveDenNgay);
        model.addAttribute("chiTietPhongId", chiTietPhongId);
        model.addAttribute("chiTietBaoCao", chiTietBaoCao);
        model.addAttribute("dsThongKe", dsThongKe);
        model.addAttribute("maxTongGio", maxTongGio);
        model.addAttribute("tongLuotTatCa", tongLuotTatCa);
        model.addAttribute("tongGioTatCa", BaoCaoService.lamTronHaiChuSo(tongGioTatCa));
        model.addAttribute("soNgayTrongKy", soNgayTrongKy);
        model.addAttribute("soGioMoiNgay", BaoCaoService.SO_GIO_HOAT_DONG_MOI_NGAY);
        return "bao-cao";
    }
}
