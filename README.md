# Hệ thống Mượn phòng Trực tuyến PTIT

> Bài tập lớn môn **Nhập môn Công nghệ Phần mềm**

## Giới thiệu dự án

**Hệ thống mượn phòng trực tuyến PTIT** là ứng dụng web nội bộ nhằm số hóa quy trình đăng ký, xét duyệt bố trí phòng học/hội trường cho các câu lạc bộ, đội nhóm sinh viên tại PTIT, thay thế quy trình nộp đơn giấy thủ công.

### Nhóm người dùng và Chức năng chính
- **Người mượn (Sinh viên đại diện CLB/Đội nhóm):** Tra cứu lịch phòng trống, nộp đơn mượn phòng, chỉnh sửa đơn, hủy đơn chưa kết thúc, theo dõi tiến trình xử lý và tạo đơn mới từ đơn bị từ chối.
- **Cán bộ Đoàn Thanh niên (Duyệt cấp 1):** Xem danh sách đơn chờ duyệt cấp 1, thẩm định nội dung/mục đích sự kiện, chấp thuận hoặc từ chối kèm lý do bắt buộc.
- **Cán bộ Trung tâm quản lý phòng / CSVC (Duyệt cấp 2 và Quản trị):** Kiểm tra phòng trống, duyệt cấp 2 và bố trí/đổi phòng, quản lý danh mục phòng, xem báo cáo thống kê tần suất sử dụng phòng.


## Hướng dẫn cài đặt

### 1. Khởi tạo cơ sở dữ liệu MySQL

Chạy lần lượt 2 file SQL trong thư mục `database/`:
1. `database/schema.sql` — tạo database `he_thong_muon_phong` và 10 bảng.
2. `database/seed.sql` — nạp dữ liệu mẫu tối thiểu dùng để chạy và kiểm thử.

**Chạy trong PowerShell:**


```powershell
mysql -u root -p --default-character-set=utf8mb4 -e "source D:/BTL_NMCNPM/database/schema.sql"

mysql -u root -p --default-character-set=utf8mb4 -e "source D:/BTL_NMCNPM/database/seed.sql"

```
Để cấu hình mật khẩu/tài khoản MySQL riêng cho máy:
1. Tạo file `src/main/resources/application-local.properties`.
2. Điền thông số:
   ```properties
   server.port=8080
   spring.datasource.url=jdbc:mysql://127.0.0.1:3306/he_thong_muon_phong?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=root
   spring.datasource.password=mat_khau_mysql_cua_ban
   ```

### 3. Chạy ứng dụng

```powershell
mvn spring-boot:run
```
Sau khi khởi động thành công, truy cập trình duyệt tại: **http://localhost:8080**

**Tài khoản mẫu (`database/seed.sql`, mật khẩu chung: `123456`):**

| Tên đăng nhập | Vai trò | Trạng thái |
|---|---|---|
| `sv_an` | Người mượn | Hoạt động |
| `cb_binh` | Cán bộ Đoàn | Hoạt động |
| `cb_cuong` | Cán bộ Trung tâm / CSVC | Hoạt động |
| `sv_khoa` | Người mượn | Ngưng |

---

### 4. Biên dịch và chạy Test

```powershell
mvn compile

mvn test
```


