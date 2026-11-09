<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="AutoSms app icon" width="112">
  <h1>AutoSms</h1>
  <p>Quản lý danh sách khách hàng và gửi SMS cá nhân hóa bằng SIM trên thiết bị Android.</p>
</div>

## Giới thiệu

AutoSms là ứng dụng Android giúp nhập danh sách khách hàng từ Excel, tạo mẫu tin nhắn có nội dung cá nhân hóa, gửi qua SIM 1 hoặc SIM 2 và theo dõi kết quả. Dữ liệu khách hàng, mẫu tin, cấu hình SIM và nhật ký gửi được lưu cục bộ trên thiết bị.

## Tính năng

- Nhập danh sách và thông tin khách hàng từ bảng tính Excel.
- Tìm kiếm, chọn khách hàng để gửi riêng cho từng SIM.
- Tạo và quản lý nhiều mẫu tin nhắn; xem trước nội dung sau khi thay biến.
- Chèn dữ liệu từ cột Excel vào mẫu và dùng Spintax để chọn ngẫu nhiên cách diễn đạt.
- Cấu hình hạn mức gửi theo ngày cho từng SIM, khoảng trễ ngẫu nhiên và khung giờ gửi.
- Theo dõi tiến độ, tạm dừng, tiếp tục hoặc dừng quá trình gửi.
- Xem thống kê và nhật ký; xuất báo cáo kết quả ra tệp Excel.
- Kiểm tra phiên bản mới và tải APK cập nhật ngay trong ứng dụng.

## Yêu cầu

- Android 8.0 (API 26) trở lên.
- Điện thoại có dịch vụ SMS và SIM đang hoạt động. Muốn sử dụng cả hai SIM, thiết bị cần hỗ trợ hai SIM.
- Để build: Android Studio, Android SDK 35 và JDK 17 trở lên.

## Build và chạy

Mở dự án trong Android Studio, chờ Gradle đồng bộ, kết nối thiết bị Android rồi chạy cấu hình `app`.

Hoặc dùng Gradle Wrapper trong PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.

Chạy unit test:

```powershell
.\gradlew.bat test
```

## Cách sử dụng

1. Cấp các quyền ứng dụng yêu cầu để truy cập thông tin SIM, gửi SMS và hiển thị thông báo.
2. Từ **Trang chủ**, chọn **Nhập danh sách Excel** và chọn tệp bảng tính. Ứng dụng đọc sheet đầu tiên; hàng tiêu đề cần có cột số điện thoại. Mặc định tên cột là `sdt`; có thể đổi tên cột trong cấu hình gửi. Các tiêu đề như `sđt`, `phone` hoặc có nội dung liên quan đến số điện thoại cũng được nhận diện.
3. Chọn SIM 1 hoặc SIM 2, sau đó chọn những khách hàng cần gửi.
4. Trong **Quản lý → Mẫu tin nhắn**, tạo mẫu và chọn mẫu muốn sử dụng. Dùng `{Tên cột}` để chèn dữ liệu tương ứng từ Excel, ví dụ:

   ```text
   Xin chào {Tên khách hàng}, mã hồ sơ của bạn là {Mã hồ sơ}.
   ```

   Dùng `{Cách viết thứ nhất|Cách viết thứ hai}` để chọn ngẫu nhiên một phương án khi gửi, ví dụ `{Xin chào|Kính chào}`.
5. Trong phần cấu hình, kiểm tra SIM và đặt hạn mức mỗi ngày, khoảng trễ ngẫu nhiên cùng khung giờ cho phép gửi.
6. Mở **Gửi tin**, bắt đầu gửi bằng SIM mong muốn và theo dõi tiến độ. Xem kết quả, lịch sử hoặc xuất báo cáo trong mục **Báo cáo**.

## Quyền và lưu ý

Ứng dụng cần quyền gửi SMS và quyền đọc thông tin/trạng thái SIM để gửi tin bằng SIM đã chọn. Ứng dụng cũng có thể cần quyền thông báo, chạy dịch vụ nền và quyền cài đặt ứng dụng không rõ nguồn gốc khi tải/cài bản cập nhật. Tên và cách cấp quyền có thể khác nhau tùy phiên bản Android và thiết bị.

Tin nhắn được gửi qua mạng di động của nhà mạng và có thể phát sinh cước phí. Hãy kiểm tra hạn mức, nội dung, sự đồng ý của người nhận và các quy định áp dụng trước khi gửi; chỉ liên hệ những người mà bạn có quyền gửi tin.

## Công nghệ

- Kotlin và Jetpack Compose cho ứng dụng Android.
- Room để lưu dữ liệu cục bộ.
- Hilt để tiêm phụ thuộc; WorkManager và foreground service cho tác vụ nền.
- Apache POI để đọc và xuất bảng tính Excel.

## Cấu trúc dự án

- `app/src/main/java/com/gmail/nthai/presentation` — giao diện Compose, điều hướng và ViewModel.
- `app/src/main/java/com/gmail/nthai/domain` — mô hình và các use case nghiệp vụ.
- `app/src/main/java/com/gmail/nthai/data` — cơ sở dữ liệu, repository, xử lý Excel và gửi SMS.
- `app/src/main/java/com/gmail/nthai/service` — dịch vụ gửi SMS và tác vụ nền.
- `app/src/test` — unit test cho các xử lý Excel và nghiệp vụ.

## Thông tin ứng dụng

- Application ID: `com.gmail.nthai`
- Phiên bản: `0.1.6` (version code `7`)
- [Các bản phát hành trên GitHub](https://github.com/HaiGH-Space/AutoSms/releases)
