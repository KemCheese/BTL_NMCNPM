USE `he_thong_muon_phong`;

INSERT INTO `tblDonVi` (`id`, `maDonVi`, `tenCLB`, `moTa`) VALUES
(1, 'CLB-IT', 'CLB Tin học', 'Câu lạc bộ Tin học PTIT'),
(2, 'CLB-BR', 'CLB Bóng rổ', 'Câu lạc bộ Bóng rổ PTIT');

INSERT INTO `tblNguoiDung` (`id`, `tenDangNhap`, `matKhau`, `hoTen`, `diaChi`, `ngaySinh`, `email`, `soDienThoai`, `trangThai`) VALUES
(1, 'sv_an', '123456', 'Nguyễn Văn An', 'Hà Đông, Hà Nội', '2004-05-15', 'annv@ptit.edu.vn', '0912345678', 'Hoạt động'),
(2, 'cb_binh', '123456', 'Trần Thị Bình', 'Hà Đông, Hà Nội', '1992-08-20', 'binhtt@ptit.edu.vn', '0987654321', 'Hoạt động'),
(3, 'cb_cuong', '123456', 'Lê Văn Cường', 'Thanh Xuân, Hà Nội', '1988-03-10', 'cuonglv@ptit.edu.vn', '0901122334', 'Hoạt động'),
(4, 'sv_khoa', '123456', 'Phạm Minh Khóa', 'Hà Đông, Hà Nội', '2004-11-02', 'khoapm@ptit.edu.vn', '0933445566', 'Ngưng');

INSERT INTO `tblSinhVien` (`id`, `maSinhVien`, `khoaHoc`, `nganhHoc`, `donViId`) VALUES
(1, 'B22DCCN001', '2022', 'Công nghệ thông tin', 1),
(4, 'B22DCCN002', '2022', 'An toàn thông tin', 2);

INSERT INTO `tblCanBoDoan` (`id`, `viTriCongViec`) VALUES
(2, 'Cán bộ Văn phòng Đoàn Thanh niên');

INSERT INTO `tblCanBoCSVC` (`id`, `viTriCongViec`) VALUES
(3, 'Chuyên viên Trung tâm Quản lý phòng');

INSERT INTO `tblPhong` (`id`, `maPhong`, `tenPhong`, `toaNha`, `tang`, `sucChuaToiDa`, `loaiPhong`, `trangThietBi`, `tinhTrang`, `moTa`) VALUES
(1, 'HT-A2', 'Hội trường A2', 'A2', 1, 300, 'Hội trường', 'máy chiếu, âm thanh, micro', 'Đang hoạt động', 'Hội trường lớn tầng 1 nhà A2'),
(2, 'HT-B1', 'Hội trường B1', 'B1', 2, 200, 'Hội trường', 'máy chiếu', 'Đang hoạt động', 'Hội trường tầng 2 nhà B1'),
(3, 'HT-A3', 'Hội trường A3', 'A3', 1, 150, 'Hội trường', 'máy chiếu, âm thanh', 'Ngưng sử dụng', 'Đang bảo trì hệ thống điện'),
(4, 'A2-201', 'Phòng học A2-201', 'A2', 2, 60, 'Phòng học thường', 'máy chiếu, bảng', 'Đang hoạt động', 'Phòng học tiêu chuẩn');

INSERT INTO `tblDonMuonPhong`
(`id`, `maDon`, `ngayTao`, `ngaySuDung`, `gioBatDau`, `gioKetThuc`, `loaiPhongMongMuon`, `thietBiYeuCau`, `trangThai`, `tenSuKien`, `mucDich`, `noiDung`, `soNguoiThamDu`, `sinhVienId`, `donViId`, `phongMongMuonId`) VALUES
(1, 'DM-001', '2026-11-17', '2026-12-25', '18:00:00', '21:00:00', 'Hội trường', 'micro và máy chiếu', 'ChoDuyetCap1', 'Đêm nhạc chào tân sinh viên', 'Giao lưu chào đón tân sinh viên khóa mới', 'Chương trình văn nghệ và giao lưu câu lạc bộ', 120, 1, 1, 1),
(2, 'DM-002', '2026-11-17', '2026-12-28', '14:00:00', '17:00:00', 'Hội trường', 'máy chiếu, âm thanh', 'ChoDuyetCap2', 'Hội thảo AI ứng dụng', 'Chia sẻ kiến thức AI cho sinh viên', 'Tọa đàm chuyên đề và hỏi đáp', 150, 1, 1, 1),
(3, 'DM-003', '2026-11-16', '2026-12-30', '08:00:00', '11:00:00', 'Hội trường', 'máy chiếu', 'DaPheDuyet', 'Sinh hoạt chuyên đề cuối năm', 'Tổng kết hoạt động CLB', 'Báo cáo kết quả học kỳ và định hướng', 100, 1, 1, 1),
(4, 'DM-004', '2026-11-18', '2026-12-26', '08:00:00', '11:00:00', 'Phòng học thường', 'máy chiếu', 'ChoDuyetCap1', 'Giải giao hữu nội bộ', 'Họp kỹ thuật trước giải đấu', 'Phổ biến thể lệ và bốc thăm chia bảng', 40, 4, 2, 4);

INSERT INTO `tblLuotDuyet` (`id`, `capDuyet`, `ketQua`, `lyDo`, `thoiDiem`, `donMuonPhongId`, `nguoiDuyetId`) VALUES
(1, 1, 'Chấp thuận', NULL, '2026-11-18 09:00:00', 2, 2),
(2, 1, 'Chấp thuận', NULL, '2026-11-17 09:30:00', 3, 2),
(3, 2, 'Phê duyệt', NULL, '2026-11-17 14:00:00', 3, 3);

INSERT INTO `tblLichSuDungPhong` (`id`, `phongId`, `donMuonPhongId`) VALUES
(1, 1, 3);

INSERT INTO `tblLichSuDon` (`id`, `trangThaiCu`, `trangThaiMoi`, `ghiChu`, `thoiDiem`, `donMuonPhongId`, `nguoiThucHienId`) VALUES
(1, NULL, 'ChoDuyetCap1', 'Tạo đơn mới', '2026-11-17 08:00:00', 1, 1),
(2, NULL, 'ChoDuyetCap1', 'Tạo đơn mới', '2026-11-17 08:30:00', 2, 1),
(3, 'ChoDuyetCap1', 'ChoDuyetCap2', 'Duyệt cấp 1: Chấp thuận', '2026-11-18 09:00:00', 2, 2),
(4, NULL, 'ChoDuyetCap1', 'Tạo đơn mới', '2026-11-16 10:00:00', 3, 1),
(5, 'ChoDuyetCap1', 'ChoDuyetCap2', 'Duyệt cấp 1: Chấp thuận', '2026-11-17 09:30:00', 3, 2),
(6, 'ChoDuyetCap2', 'DaPheDuyet', 'Duyệt cấp 2 và bố trí phòng HT-A2', '2026-11-17 14:00:00', 3, 3),
(7, NULL, 'ChoDuyetCap1', 'Tạo đơn mới', '2026-11-18 10:00:00', 4, 4);
