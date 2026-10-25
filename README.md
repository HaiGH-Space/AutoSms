# AutoSms

AutoSms là ứng dụng Android hỗ trợ quản lý danh sách khách hàng và gửi SMS theo mẫu bằng SIM trên thiết bị. Ứng dụng cho phép nhập dữ liệu từ Excel, chọn người nhận cho từng SIM, cấu hình thời gian gửi và theo dõi kết quả gửi.

## Tính năng

- Nhập danh sách khách hàng từ tệp Excel `.xlsx`.
- Quản lý, tìm kiếm và chọn người nhận cho SIM 1 hoặc SIM 2.
- Soạn mẫu tin nhắn có biến lấy từ các cột Excel, ví dụ `{{Tên khách hàng}}`.
- Hỗ trợ Spintax `{Lựa chọn 1|Lựa chọn 2}` để chọn ngẫu nhiên nội dung khi gửi.
- Gửi SMS theo từng SIM; theo dõi tiến độ và có thể tạm dừng, tiếp tục hoặc dừng.
- Cấu hình giới hạn SMS mỗi ngày cho từng SIM, khoảng trễ ngẫu nhiên và khung giờ gửi.
- Xem nhật ký, thống kê kết quả và xuất báo cáo Excel.

## Yêu cầu

- Android 8.0 (API 26) trở lên.
- Thiết bị có hỗ trợ điện thoại/SMS và có SIM để gửi tin.
- Để build ứng dụng: Android Studio với Android SDK 35 và JDK 11 trở lên.

## Build và chạy

Mở dự án bằng Android Studio, chờ Gradle đồng bộ, sau đó chạy cấu hình `app` trên thiết bị Android.

Hoặc build APK debug bằng Gradle Wrapper:

```powershell
.\gradlew.bat assembleDebug
```

Trên macOS/Linux:

```bash
./gradlew assembleDebug
```

APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.

Chạy unit test:

```powershell
.\gradlew.bat test
```

## Cách sử dụng

1. Mở ứng dụng và cấp các quyền Điện thoại/SMS được yêu cầu.
2. Chọn **Nhập danh sách Excel** để chọn tệp `.xlsx`. Ứng dụng đọc sheet đầu tiên; hàng tiêu đề cần có cột số điện thoại. Tên cột mặc định là `sdt`; các tiêu đề như `sđt`, `phone` hoặc có nội dung liên quan đến số điện thoại cũng được nhận diện. Nếu một ô chứa nhiều số, có thể phân tách bằng dấu phẩy, chấm phẩy, xuống dòng, dấu gạch chéo hoặc tab.
3. Trong danh sách khách hàng của SIM 1 hoặc SIM 2, chọn những người cần gửi.
4. Soạn và lưu mẫu tin nhắn. Dùng `{{Tên cột}}` để chèn dữ liệu theo tiêu đề Excel; dùng `{Xin chào|Kính chào}` để tạo lựa chọn Spintax.
5. Trong mục cấu hình, kiểm tra SIM, đặt hạn mức mỗi ngày, khoảng trễ, khung giờ gửi và tên cột số điện thoại nếu cần.
6. Mở mục tiến độ gửi, bắt đầu gửi bằng SIM mong muốn và theo dõi nhật ký. Xem thống kê hoặc xuất tệp `.xlsx` trong mục báo cáo.

## Quyền và lưu ý

Ứng dụng cần quyền gửi SMS và quyền đọc trạng thái/thông tin SIM để gửi tin bằng SIM đã chọn. Tùy phiên bản Android, thiết bị cũng có thể yêu cầu quyền thông báo hoặc thiết lập bổ sung để duy trì tiến trình nền.

SMS được gửi qua dịch vụ di động của SIM trên thiết bị. Hãy kiểm tra cước phí, hạn mức của nhà mạng, nội dung tin nhắn và sự đồng ý của người nhận trước khi gửi. Chỉ gửi tin cho những người nhận mà bạn có quyền liên hệ và tuân thủ quy định hiện hành.

## Công nghệ

- Kotlin, Android Gradle Plugin và Jetpack Compose.
- Room để lưu dữ liệu cục bộ.
- Hilt và WorkManager cho tiêm phụ thuộc và tác vụ nền.
- Apache POI để đọc/ghi tệp Excel.

## Thông tin ứng dụng

- Application ID: `com.gmail.nthai`
- Phiên bản hiện tại: `1.0` (version code `1`)
- Mã nguồn ứng dụng: `app/src/main/java/com/gmail/nthai`
