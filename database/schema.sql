CREATE DATABASE IF NOT EXISTS `he_thong_muon_phong`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `he_thong_muon_phong`;

CREATE TABLE `tblNguoiDung` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `tenDangNhap` VARCHAR(100) NOT NULL,
    `matKhau` VARCHAR(255) NOT NULL,
    `hoTen` VARCHAR(150) NOT NULL,
    `diaChi` VARCHAR(255) NULL,
    `ngaySinh` DATE NULL,
    `email` VARCHAR(150) NULL,
    `soDienThoai` VARCHAR(20) NULL,
    `trangThai` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_nguoidung_tendangnhap` (`tenDangNhap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblDonVi` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `maDonVi` VARCHAR(50) NOT NULL,
    `tenCLB` VARCHAR(150) NOT NULL,
    `moTa` TEXT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_donvi_madonvi` (`maDonVi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblSinhVien` (
    `id` INT NOT NULL,
    `maSinhVien` VARCHAR(50) NOT NULL,
    `khoaHoc` INT NOT NULL,
    `nganhHoc` VARCHAR(150) NOT NULL,
    `donViId` INT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_sinhvien_masinhvien` (`maSinhVien`),
    CONSTRAINT `fk_sinhvien_nguoidung` FOREIGN KEY (`id`) REFERENCES `tblNguoiDung` (`id`),
    CONSTRAINT `fk_sinhvien_donvi` FOREIGN KEY (`donViId`) REFERENCES `tblDonVi` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblCanBoDoan` (
    `id` INT NOT NULL,
    `viTriCongViec` VARCHAR(150) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_canbodoan_nguoidung` FOREIGN KEY (`id`) REFERENCES `tblNguoiDung` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblCanBoCSVC` (
    `id` INT NOT NULL,
    `viTriCongViec` VARCHAR(150) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_canbocsvc_nguoidung` FOREIGN KEY (`id`) REFERENCES `tblNguoiDung` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblPhong` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `maPhong` VARCHAR(50) NOT NULL,
    `tenPhong` VARCHAR(150) NOT NULL,
    `toaNha` VARCHAR(50) NOT NULL,
    `tang` INT NOT NULL,
    `sucChuaToiDa` INT NOT NULL,
    `loaiPhong` VARCHAR(100) NOT NULL,
    `trangThietBi` TEXT NULL,
    `tinhTrang` VARCHAR(50) NOT NULL,
    `moTa` TEXT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_phong_maphong` (`maPhong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblDonMuonPhong` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `maDon` VARCHAR(50) NOT NULL,
    `ngayTao` DATE NOT NULL,
    `ngaySuDung` DATE NOT NULL,
    `gioBatDau` TIME NOT NULL,
    `gioKetThuc` TIME NOT NULL,
    `loaiPhongMongMuon` VARCHAR(100) NOT NULL,
    `thietBiYeuCau` TEXT NULL,
    `trangThai` VARCHAR(50) NOT NULL,
    `tenSuKien` VARCHAR(255) NOT NULL,
    `mucDich` TEXT NOT NULL,
    `noiDung` TEXT NOT NULL,
    `soNguoiThamDu` INT NOT NULL,
    `sinhVienId` INT NOT NULL,
    `donViId` INT NOT NULL,
    `phongMongMuonId` INT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_donmuonphong_madon` (`maDon`),
    CONSTRAINT `chk_donmuonphong_trangthai` CHECK (`trangThai` IN (
        'ChoDuyetCap1',
        'ChoDuyetCap2',
        'DaPheDuyet',
        'TuChoiCap1',
        'TuChoiCap2',
        'DaHuy',
        'HoanThanh'
    )),
    CONSTRAINT `fk_donmuonphong_sinhvien` FOREIGN KEY (`sinhVienId`) REFERENCES `tblSinhVien` (`id`),
    CONSTRAINT `fk_donmuonphong_donvi` FOREIGN KEY (`donViId`) REFERENCES `tblDonVi` (`id`),
    CONSTRAINT `fk_donmuonphong_phongmongmuon` FOREIGN KEY (`phongMongMuonId`) REFERENCES `tblPhong` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblLuotDuyet` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `capDuyet` INT NOT NULL,
    `ketQua` VARCHAR(50) NOT NULL,
    `lyDo` TEXT NULL,
    `thoiDiem` DATETIME NOT NULL,
    `donMuonPhongId` INT NOT NULL,
    `nguoiDuyetId` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_luotduyet_donmuonphong` FOREIGN KEY (`donMuonPhongId`) REFERENCES `tblDonMuonPhong` (`id`),
    CONSTRAINT `fk_luotduyet_nguoiduyet` FOREIGN KEY (`nguoiDuyetId`) REFERENCES `tblNguoiDung` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblLichSuDungPhong` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `phongId` INT NOT NULL,
    `donMuonPhongId` INT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_lichsudungphong_donmuonphong` (`donMuonPhongId`),
    CONSTRAINT `fk_lichsudungphong_phong` FOREIGN KEY (`phongId`) REFERENCES `tblPhong` (`id`),
    CONSTRAINT `fk_lichsudungphong_donmuonphong` FOREIGN KEY (`donMuonPhongId`) REFERENCES `tblDonMuonPhong` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tblLichSuDon` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `trangThaiCu` VARCHAR(50) NULL,
    `trangThaiMoi` VARCHAR(50) NOT NULL,
    `ghiChu` TEXT NULL,
    `thoiDiem` DATETIME NOT NULL,
    `donMuonPhongId` INT NOT NULL,
    `nguoiThucHienId` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `chk_lichsudon_trangthaicu` CHECK (`trangThaiCu` IS NULL OR `trangThaiCu` IN (
        'ChoDuyetCap1',
        'ChoDuyetCap2',
        'DaPheDuyet',
        'TuChoiCap1',
        'TuChoiCap2',
        'DaHuy',
        'HoanThanh'
    )),
    CONSTRAINT `chk_lichsudon_trangthaimoi` CHECK (`trangThaiMoi` IN (
        'ChoDuyetCap1',
        'ChoDuyetCap2',
        'DaPheDuyet',
        'TuChoiCap1',
        'TuChoiCap2',
        'DaHuy',
        'HoanThanh'
    )),
    CONSTRAINT `fk_lichsudon_donmuonphong` FOREIGN KEY (`donMuonPhongId`) REFERENCES `tblDonMuonPhong` (`id`),
    CONSTRAINT `fk_lichsudon_nguoithuchien` FOREIGN KEY (`nguoiThucHienId`) REFERENCES `tblNguoiDung` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
