<div align="center">
  <img src="app/src/main/res/mipmap-hdpi/ic_launcher.webp" alt="AutoSms Logo" width="96" height="96">
  <h1>AutoSms</h1>
  <p><strong>Giải pháp Android tự động hóa gửi tin nhắn SMS cá nhân hóa từ Excel qua SIM thiết bị và gửi tin nhắn tự động qua Zalo.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=flat&logo=android&logoColor=white" alt="Android Version">
    <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin Version">
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Architecture-Clean_Architecture-brightgreen?style=flat" alt="Clean Architecture">
  </p>
</div>

---

## Overview

**AutoSms** là ứng dụng di động Android hỗ trợ doanh nghiệp nhỏ, nhân viên bán hàng và người vận hành tự động hóa gửi tin nhắn hàng loạt được cá nhân hóa cao. Ứng dụng tích hợp trực tiếp bảng tính Excel (`.xlsx`, `.xls`), tự động điền các trường dữ liệu tùy biến vào mẫu tin nhắn, phân bổ SIM gửi phù hợp (SIM 1 / SIM 2), kiểm soát hạn mức (quota) cùng độ trễ ngẫu nhiên chống spam, và duy trì tiến trình gửi ổn định trong nền qua Foreground Service. Ngoài ra, AutoSms hỗ trợ gửi tin nhắn tự động qua ứng dụng Zalo thông qua Android Accessibility Service.

> [!NOTE]
> Toàn bộ dữ liệu danh sách liên hệ, mẫu tin nhắn và nhật ký gửi được lưu trữ hoàn toàn cục bộ trên thiết bị qua SQLite (Room Database), bảo đảm tuyệt đối tính riêng tư và an toàn thông tin khách hàng.

---

## Key Features

- **Nhập dữ liệu thông minh từ Excel**:
  - Tự động đọc file bảng tính Excel định dạng `.xlsx` và `.xls`.
  - Tự động nhận diện các cột số điện thoại phổ biến (`SDT`, `Phone`, `Mobile`, `Điện thoại`,...).
  - Trích xuất linh hoạt mọi cột dữ liệu tùy chỉnh phục vụ ghép nội dung tin nhắn.

- **Động hóa nội dung & Công nghệ Spintax**:
  - Ghép biến dữ liệu từ file Excel vào nội dung theo cú pháp `{Tên_Cột}` hoặc các tham số thông dụng như `{Ten_KH}`, `{So_Tien}`, `{Ma_HD}`, `{Ngay_Han}`.
  - Hỗ trợ cú pháp **Spintax** dạng `{Chào anh|Kính chào|Xin chào}` giúp hoán đổi ngẫu nhiên cấu trúc câu chữ, giảm thiểu nguy cơ bị chặn bởi bộ lọc chống spam của nhà mạng.
  - Tự động định dạng tiền tệ và số liệu theo chuẩn định dạng Việt Nam (ví dụ: `1.000.000`).

- **Nhận diện nhà mạng Việt Nam**:
  - Tự động phân tích đầu số và nhận diện nhà mạng di động Việt Nam (Viettel, VinaPhone, MobiFone, Vietnamobile, Gmobile, Wintel, iTel).
  - Hỗ trợ quy tắc định tuyến gửi ưu tiên đúng nhà mạng của SIM tương ứng để tiết kiệm tối đa cước phí.

- **Quản lý & Cấu hình 2 khe SIM chuyên sâu**:
  - Thiết lập định mức (quota) gửi SMS tối đa theo ngày riêng biệt cho từng khe SIM.
  - Cấu hình khoảng trễ ngẫu nhiên giữa các lần gửi (ví dụ: `15` - `45` giây) nhằm mô phỏng hành vi gửi tự nhiên.
  - Thiết lập khung giờ gửi cho phép trong ngày để tránh làm phiền khách hàng ngoài giờ làm việc.
  - Tự động reset bộ đếm hạn mức mỗi ngày thông qua WorkManager (`DailySmsWorker`).

- **Gửi tin nhắn tự động qua Zalo**:
  - Mở trực tiếp cuộc trò chuyện Zalo theo số điện thoại qua deep link (`https://zalo.me/{phone}`).
  - Sử dụng **Android Accessibility Service** (`ZaloAccessibilityService`) để tự động điền nội dung tin nhắn và thực hiện thao tác gửi.
  - Quản lý danh sách người nhận Zalo chuyên biệt kèm cơ chế giám sát timeout thông minh.

- **Tiến trình gửi bền bỉ trong nền (Foreground Service)**:
  - Khởi chạy tiến trình gửi SMS qua **Foreground Service** loại `dataSync`, duy trì hoạt động liên tục ngay cả khi ứng dụng bị thu nhỏ hoặc thiết bị khóa màn hình.
  - Cung cấp thanh thông báo hệ thống trực quan cập nhật tiến độ theo thời gian thực (Đã gửi, Thất bại, Còn lại).
  - Hỗ trợ các hành động tức thời: Tạm dừng (Pause), Tiếp tục (Resume) hoặc Hủy bỏ (Stop).

- **Thống kê, Báo cáo & Xuất Excel**:
  - Lưu trữ chi tiết nhật ký trạng thái từng tin nhắn gửi (`SENT`, `DELIVERED`, `FAILED`).
  - Lọc báo cáo linh hoạt theo ngày/tháng/khoảng thời gian tùy chọn.
  - Hỗ trợ xuất toàn bộ nhật ký báo cáo kết quả gửi ra file Excel (.xlsx).

- **Tự động kiểm tra bản cập nhật APK**:
  - Tích hợp kiểm tra phiên bản mới từ repository từ xa.
  - Hỗ trợ tải và kích hoạt cài đặt file APK mới an toàn qua Android FileProvider.

---

## Technical Stack & Architecture

AutoSms được thiết kế chuẩn mực theo mô hình **Clean Architecture** kết hợp mẫu kiến trúc **MVVM / MVI** cho tầng giao diện:

| Phân tầng / Thành phần | Công nghệ / Thư viện | Vai trò |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Kotlin 2.0.21 | Ngôn ngữ lập trình chính |
| **Giao diện (UI)** | Jetpack Compose BOM 2024.12.01, Material 3 | Xây dựng giao diện khai báo hiện đại, mượt mà |
| **Điều hướng** | Navigation Compose 2.8.5 | Quản lý luồng chuyển màn hình trong kiến trúc Single-Activity |
| **Kiến trúc** | Clean Architecture (Presentation, Domain, Data) | Phân tách rõ ràng UI, quy tắc nghiệp vụ và truy xuất dữ liệu |
| **Dependency Injection** | Hilt (Dagger) 2.60.1 | Quản lý vòng đời phụ thuộc, tối ưu kiểm thử |
| **Cơ sở dữ liệu** | Room Database 2.8.5 | Lưu trữ dữ liệu quan hệ cục bộ (Entity, DAO, TypeConverters) |
| **Lập lịch nền** | WorkManager 2.10.0 | Lên lịch tự động reset hạn mức SIM hàng ngày |
| **Xử lý Bảng tính** | Apache POI (`poi-ooxml` 5.2.5) | Đọc, phân tích và xuất file bảng tính Excel (`.xlsx`, `.xls`) |
| **Bất đồng bộ** | Kotlin Coroutines 1.9.0 & StateFlow | Xử lý luồng dữ liệu phản ứng bất đồng bộ (Reactive Flow) |
| **Tự động hóa Zalo** | Android Accessibility Service | Tự động hóa thao tác nhập liệu và gửi tin nhắn trên giao diện Zalo |
| **Build & Compiler** | Gradle Kotlin DSL & KSP 2.0.21-1.0.28 | Tối ưu hóa thời gian biên dịch và xử lý annotation |

---

## Requirements & Permissions

### Yêu cầu hệ thống

- **Hệ điều hành**: Android 8.0 (API Level 26) trở lên.
- **Phần cứng**: Thiết bị Android hỗ trợ SIM (1 SIM hoặc Dual SIM) đang kích hoạt gói cước viễn thông.
- **Môi trường phát triển**: Android Studio (Ladybug 2024.2.1 trở lên), JDK 11+, Android SDK 35.

### Quyền ứng dụng (Permissions)

> [!IMPORTANT]
> Ứng dụng yêu cầu cấp một số quyền hệ thống để thực hiện gửi tin nhắn và duy trì tiến trình nền:

- `SEND_SMS`: Quyền gửi trực tiếp tin nhắn SMS qua SIM của thiết bị.
- `READ_PHONE_STATE` & `READ_PHONE_NUMBERS`: Nhận diện khe SIM, trạng thái SIM và phân tích nhà mạng.
- `POST_NOTIFICATIONS`: Hiển thị thông báo tiến độ gửi tin thời gian thực trên Android 13+.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: Duy trì tiến trình gửi SMS trong nền không bị Android giải phóng tài nguyên.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Bỏ qua chế độ tối ưu pin (Doze mode) để đảm bảo độ trễ gửi tin chuẩn xác.
- `BIND_ACCESSIBILITY_SERVICE`: Quyền trợ năng để hỗ trợ tính năng gửi tin nhắn tự động qua Zalo.
- `REQUEST_INSTALL_PACKAGES`: Cho phép cập nhật phiên bản mới trực tiếp từ file APK nội bộ.

---

## Usage Guide

### 1. Nhập danh sách khách hàng từ Excel
1. Tại màn hình **Trang chủ**, nhấn **Nhập danh sách Excel**.
2. Chọn tệp bảng tính (`.xlsx` hoặc `.xls`). Ứng dụng sẽ tự động phân tích và gắn các trường dữ liệu.
3. Kiểm tra danh sách khách hàng và các biến dữ liệu đã được nạp thành công.

### 2. Soạn thảo Mẫu tin nhắn & Spintax
1. Chọn tab **Mẫu tin nhắn** hoặc mục chỉnh sửa mẫu.
2. Thêm mới mẫu tin, kết hợp các biến trích xuất từ Excel:
   ```text
   {Xin chào|Kính chào} {Ten_KH}, hợp đồng số {Ma_HD} của bạn cần thanh toán số tiền {So_Tien} đ trước ngày {Ngay_Han}. Cảm ơn quý khách!
   ```
3. Nhấn **Xem trước (Preview)** để kiểm tra nội dung thực tế sau khi thế biến và xử lý Spintax ngẫu nhiên.

### 3. Cấu hình SIM & Quy tắc an toàn
1. Chuyển sang màn hình **Cấu hình**.
2. Thiết lập cho từng khe **SIM 1** và **SIM 2**:
   - Hạn mức tin nhắn tối đa gửi mỗi ngày.
   - Khoảng trễ ngẫu nhiên giữa 2 tin liên tiếp (ví dụ: `20` - `45` giây).
   - Khung giờ hợp lệ cho phép gửi tin trong ngày.
3. Bật tùy chọn ưu tiên gửi theo nhà mạng phù hợp nếu muốn tối ưu hóa chi phí.

### 4. Khởi chạy tiến trình gửi & Giám sát
1. Chọn mẫu tin nhắn, khe SIM điều phối và danh sách liên hệ cần gửi.
2. Bấm **Bắt đầu gửi**. Màn hình tiến độ sẽ hiển thị trạng thái từng lượt gửi.
3. Bạn có thể ẩn ứng dụng; tiến trình gửi sẽ tiếp tục chạy ngầm ổn định kèm thanh thông báo hệ thống. Có thể bấm **Tạm dừng**, **Tiếp tục** hoặc **Hủy** bất kỳ lúc nào.

### 5. Gửi tin nhắn qua Zalo
1. Điều hướng đến màn hình **Zalo**.
2. Nhập danh sách số điện thoại cần gửi hoặc nạp từ tập khách hàng có sẵn.
3. Kích hoạt dịch vụ **AutoSms Accessibility Service** trong phần Cài đặt Trợ năng của Android.
4. Bấm **Bắt đầu gửi Zalo** và để ứng dụng tự động mở cuộc trò chuyện và gửi tin.

### 6. Báo cáo & Xuất nhật ký
1. Mở màn hình **Báo cáo**.
2. Lọc theo mốc thời gian hoặc trạng thái (`Thành công`, `Thất bại`).
3. Bấm **Xuất báo cáo Excel** để lưu file thống kê chi tiết vào bộ nhớ thiết bị.

---

## Building & Development

### Biên dịch dự án qua Gradle Wrapper

Biên dịch bản Debug APK:
```powershell
.\gradlew.bat assembleDebug
```
*File APK đầu ra:* `app/build/outputs/apk/debug/app-debug.apk`

Biên dịch bản Release APK có tối ưu hóa ProGuard/R8:
```powershell
.\gradlew.bat assembleRelease
```

Chạy toàn bộ Unit Tests (Domain UseCases, Parsers, Models):
```powershell
.\gradlew.bat test
```

---

## Project Structure

```text
AutoSms/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gmail/nthai/
│   │   │   │   ├── data/             # Triển khai Room Database, Repositories, Excel Parser & SmsSender
│   │   │   │   ├── di/               # Hilt Dependency Injection Modules (DatabaseModule, etc.)
│   │   │   │   ├── domain/           # Models, Repository Contracts, UseCases (Spintax, Carrier, Rules)
│   │   │   │   ├── presentation/     # Tầng UI Jetpack Compose (Screens, Components, ViewModels, Theme)
│   │   │   │   ├── service/          # Foreground Service, Zalo Accessibility Service, WorkManager Worker
│   │   │   │   └── util/             # Tiện ích hệ thống (Permissions, DateTime, ApkInstaller)
│   │   │   └── res/                  # Android Resources (Layouts, Drawables, Mipmaps, Values)
│   │   └── test/                     # Unit Tests kiểm thử nghiệp vụ UseCase và Parser
│   └── build.gradle.kts
├── docs/                             # Tài liệu kỹ thuật dự án (Architecture, Conventions, Stack,...)
├── gradle/                           # Gradle Wrapper & Version Catalog (libs.versions.toml)
└── build.gradle.kts
```

---

> [!WARNING]
> Tin nhắn SMS được gửi thông qua sóng mạng di động của thiết bị và có thể tính cước phí viễn thông theo gói cước của SIM. Người sử dụng cần đảm bảo tuân thủ các quy định hiện hành về quyền riêng tư và sự chấp thuận của người nhận tin.
