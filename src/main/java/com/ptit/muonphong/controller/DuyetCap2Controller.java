package com.ptit.muonphong.controller;

import com.ptit.muonphong.entity.DonMuonPhong;
import com.ptit.muonphong.entity.NguoiDung;
import com.ptit.muonphong.service.AuthService;
import com.ptit.muonphong.service.DuyetCap2Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/duyet-cap-2")
public class DuyetCap2Controller {

    private static final String TAB_CHO_DUYET = "cho-duyet";
    private static final String TAB_DA_PHE_DUYET = "da-phe-duyet";

    private final AuthService authService;
    private final DuyetCap2Service duyetCap2Service;

    public DuyetCap2Controller(AuthService authService, DuyetCap2Service duyetCap2Service) {
        this.authService = authService;
        this.duyetCap2Service = duyetCap2Service;
    }

    @GetMapping
    public String xemDanhSach(@RequestParam(value = "tab", defaultValue = TAB_CHO_DUYET) String tab,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId,
                              @RequestParam(value = "checkRooms", defaultValue = "false") boolean checkRooms,
                              Model model) {
        populateModel(tab, selectedId, checkRooms, model);
        return "duyet-cap-2";
    }

    @GetMapping("/{id}")
    public String xemChiTiet(@PathVariable int id,
                             @RequestParam(value = "tab", defaultValue = TAB_CHO_DUYET) String tab,
                             @RequestParam(value = "checkRooms", defaultValue = "false") boolean checkRooms,
                             Model model) {
        populateModel(tab, id, checkRooms, model);
        return "duyet-cap-2";
    }

    @PostMapping("/{id}/phe-duyet")
    public String pheDuyet(@PathVariable int id,
                           @RequestParam(value = "idPhong", required = false) Integer idPhong,
                           RedirectAttributes redirectAttributes) {
        try {
            NguoiDung canBoCSVC = authService.getCurrentUser();
            duyetCap2Service.pheDuyetVaBoTriPhong(id, idPhong, canBoCSVC);
            redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt đơn và bố trí phòng thành công.");
            redirectAttributes.addAttribute("tab", TAB_CHO_DUYET);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addAttribute("tab", TAB_CHO_DUYET);
            redirectAttributes.addAttribute("selectedId", id);
            redirectAttributes.addAttribute("checkRooms", true);
        }
        return "redirect:/duyet-cap-2";
    }

    @PostMapping("/{id}/tu-choi")
    public String tuChoi(@PathVariable int id,
                         @RequestParam(value = "lyDo", required = false) String lyDo,
                         @RequestParam(value = "checkRooms", defaultValue = "false") boolean checkRooms,
                         RedirectAttributes redirectAttributes) {
        try {
            NguoiDung canBoCSVC = authService.getCurrentUser();
            duyetCap2Service.tuChoiDon(id, lyDo, canBoCSVC);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn mượn phòng.");
            redirectAttributes.addAttribute("tab", TAB_CHO_DUYET);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("lyDoDraft", lyDo);
            redirectAttributes.addAttribute("tab", TAB_CHO_DUYET);
            redirectAttributes.addAttribute("selectedId", id);
            if (checkRooms) {
                redirectAttributes.addAttribute("checkRooms", true);
            }
        }
        return "redirect:/duyet-cap-2";
    }

    @PostMapping("/{id}/doi-phong")
    public String doiPhong(@PathVariable int id,
                           @RequestParam(value = "idPhongMoi", required = false) Integer idPhongMoi,
                           RedirectAttributes redirectAttributes) {
        try {
            NguoiDung canBoCSVC = authService.getCurrentUser();
            duyetCap2Service.doiPhong(id, idPhongMoi, canBoCSVC);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi phòng cho đơn thành công.");
            redirectAttributes.addAttribute("tab", TAB_DA_PHE_DUYET);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addAttribute("tab", TAB_DA_PHE_DUYET);
            redirectAttributes.addAttribute("selectedId", id);
            redirectAttributes.addAttribute("checkRooms", true);
        }
        return "redirect:/duyet-cap-2";
    }

    private void populateModel(String tab, Integer selectedId, boolean checkRooms, Model model) {
        String activeTab = TAB_DA_PHE_DUYET.equals(tab) ? TAB_DA_PHE_DUYET : TAB_CHO_DUYET;
        List<DonMuonPhong> danhSachDon = TAB_DA_PHE_DUYET.equals(activeTab)
                ? duyetCap2Service.layDanhSachDaPheDuyetChuaSuDung()
                : duyetCap2Service.layDanhSachChoDuyetCap2();

        model.addAttribute("nguoiDung", authService.getCurrentUser());
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("danhSachDon", danhSachDon);
        model.addAttribute("checkedRooms", false);

        if (selectedId != null) {
            try {
                DonMuonPhong donChiTiet = duyetCap2Service.xemChiTietDon(selectedId);
                model.addAttribute("donChiTiet", donChiTiet);
                if (checkRooms) {
                    model.addAttribute("danhSachPhongTrong", duyetCap2Service.kiemTraPhongTrong(selectedId));
                    model.addAttribute("checkedRooms", true);
                }
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
    }
}
