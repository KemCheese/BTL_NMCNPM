# Hệ thống Mượn phòng Trực tuyến PTIT

> Bài tập lớn môn **Nhập môn Công nghệ Phần mềm**

## Giới thiệu dự án

**Hệ thống mượn phòng trực tuyến PTIT** là ứng dụng web nội bộ nhằm số hóa quy trình đăng ký, xét duyệt bố trí phòng học/hội trường cho các câu lạc bộ, đội nhóm sinh viên tại PTIT, thay thế quy trình nộp đơn giấy thủ công.

### Nhóm người dùng và Chức năng chính
- **Người mượn (Sinh viên đại diện CLB/Đội nhóm):** Tra cứu lịch phòng trống, nộp đơn mượn phòng, chỉnh sửa đơn, hủy đơn chưa kết thúc, theo dõi tiến trình xử lý và tạo đơn mới từ đơn bị từ chối.
- **Cán bộ Đoàn Thanh niên (Duyệt cấp 1):** Xem danh sách đơn chờ duyệt cấp 1, thẩm định nội dung/mục đích sự kiện, chấp thuận hoặc từ chối kèm lý do bắt buộc.
- **Cán bộ Trung tâm quản lý phòng / CSVC (Duyệt cấp 2 và Quản trị):** Kiểm tra phòng trống, duyệt cấp 2 và bố trí/đổi phòng, quản lý danh mục phòng, xem báo cáo thống kê tần suất sử dụng phòng.

### Kiến trúc & Công nghệ
- **Kiến trúc:** Mô hình **MVC (Model - View - Controller)** kết hợp phân tầng (Controller ↔ Service ↔ DAO/Repository ↔ Entity).
- **Backend Framework:** **Java** (JDK 17/21), **Spring Boot** (Spring MVC).
- **Cơ sở dữ liệu:** **MySQL** (InnoDB).
- **Tài liệu đặc tả chi tiết:** Tham khảo trong thư mục [`docs/`](./docs/).

