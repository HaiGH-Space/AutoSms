<div align="center">
  <img src="docs/assets/logo.svg" alt="AutoSms Logo" width="108" height="108">
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

**AutoSms** là ứng dụng di động Android mã nguồn mở chuyên dụng hỗ trợ doanh nghiệp, tổ chức và cá nhân kinh doanh tự động hóa quy trình chăm sóc khách hàng qua SMS và Zalo với mức độ cá nhân hóa cao.

Ứng dụng kết nối trực tiếp với file bảng tính Excel (`.xlsx`, `.xls`), tự động nhận diện dữ liệu liên hệ, hỗ trợ biến động cùng cú pháp Spintax chống spam, tự động tối ưu chi phí cước thông qua nhận diện nhà mạng Việt Nam và phân bổ linh hoạt giữa 2 khe SIM. Quy trình gửi SMS chạy ổn định trong nền qua Foreground Service, đồng thời tích hợp tính năng gửi tự động qua ứng dụng Zalo thông qua Android Accessibility Service.

> [!NOTE]
> Toàn bộ danh bạ, dữ liệu khách hàng, mẫu tin nhắn và lịch sử gửi được lưu trữ hoàn toàn cục bộ trên thiết bị qua SQLite (Room Database), bảo đảm an toàn và bảo mật thông tin.

---

## Key Features

- **Nhập dữ liệu thông minh từ Excel**:
  - Hỗ trợ trực tiếp định dạng `.xlsx` và `.xls` qua thư viện Apache POI tối ưu.
  - Tự động quét và nhận diện các cột số điện thoại phổ biến (`SDT`, `Phone`, `Mobile`, `Điện thoại`,...).
  - Đa dạng chế độ nhập: **Thay thế danh sách**, **Nối tiếp** và **Bỏ qua số trùng lặp**.
  - Trích xuất linh hoạt mọi cột dữ liệu tùy chỉnh (`{Ten_KH}`, `{So_Tien}`, `{Ma_HD}`, `{Ngay_Han}`,...).

- **Động hóa nội dung & Công nghệ Spintax**:
  - Ghép biến dữ liệu từ file Excel vào nội dung theo cú pháp `{Tên_Cột}`.
  - Hỗ trợ cú pháp **Spintax** dạng `{Chào anh|Kính chào|Xin chào}` giúp hoán đổi linh hoạt từ ngữ ngẫu nhiên, hạn chế tối đa nguy cơ bị nhà mạng chặn lọc spam.
  - Tự động chuẩn hóa định dạng số và tiền tệ theo chuẩn Việt Nam (`1.000.000 đ`).

- **Nhận diện nhà mạng Việt Nam & Định tuyến SIM**:
  - Nhận diện tức thì tất cả nhà mạng di động tại Việt Nam: Viettel, VinaPhone, MobiFone, Vietnamobile, Gmobile, Wintel, iTel.
  - Tự động so khớp đầu số với nhà mạng của từng khe SIM (SIM 1 / SIM 2) để gửi nội mạng, tối ưu cước phí viễn thông.

- **Quản lý & Cấu hình 2 khe SIM chuyên sâu**:
  - Giám sát trạng thái lắp SIM, tên nhà mạng và số điện thoại từng khe SIM.
  - Thiết lập định mức (quota) gửi SMS tối đa theo ngày cho từng khe SIM độc lập.
  - Cấu hình khoảng trễ tối thiểu và tối đa ngẫu nhiên an toàn giữa các lần gửi (ví dụ: `40` - `50` giây).
  - Cài đặt khung giờ cho phép gửi tin trong ngày, tự động dừng ngoài giờ làm việc.
  - Tự động làm mới (reset) hạn mức mỗi ngày qua AndroidX WorkManager (`DailySmsWorker`).

- **Gửi tin nhắn tự động qua Zalo**:
  - Mở trực tiếp cuộc trò chuyện Zalo theo số điện thoại qua deep link (`https://zalo.me/{phone}`).
  - Sử dụng **Android Accessibility Service** (`ZaloAccessibilityService`) để tự động điền tin nhắn và thực hiện thao tác gửi.
  - Tự động xử lý màn hình lỗi WebView, số chưa đăng ký Zalo hoặc tài khoản không tồn tại, tự động điều hướng quay lại màn hình chính.
  - Cơ chế dán văn bản bằng clipboard dự phòng và hỗ trợ thiết bị cấu hình thấp với cơ chế retry nút gửi linh hoạt.
  - Quản lý danh sách người nhận Zalo, hỗ trợ import từ Excel hoặc chuyển đổi từ danh bạ khách hàng.

- **Tiến trình gửi nền bền bỉ (Foreground Service)**:
  - Khởi chạy tiến trình gửi SMS qua **Foreground Service** loại `dataSync`, duy trì hoạt động ngay cả khi ứng dụng bị thu nhỏ hoặc thiết bị khóa màn hình.
  - Thanh thông báo hệ thống trực quan cập nhật tiến độ theo thời gian thực (Đã gửi, Thất bại, Còn lại).
  - Hỗ trợ điều khiển trực tiếp: Tạm dừng (Pause), Tiếp tục (Resume) hoặc Hủy bỏ (Stop).
  - Quản lý đặt lại trạng thái gửi linh hoạt theo phạm vi (Tất cả, Chỉ người đã gửi, Chỉ người được chọn).

- **Giao diện hiện đại & Thống kê báo cáo**:
  - Hệ thống thành phần giao diện nhất quán (`AutoSmsTopAppBar`, `AutoSmsCardContainer`, `AutoSmsSearchBar`, `StatusBadge`).
  - Lưu trữ chi tiết nhật ký trạng thái từng tin nhắn gửi (`SENT`, `DELIVERED`, `FAILED`).
  - Lọc báo cáo linh hoạt theo ngày/tháng hoặc khoảng thời gian tùy chọn.
  - Hỗ trợ xuất toàn bộ báo cáo lịch sử gửi ra file Excel (`.xlsx`).

- **Tự động kiểm tra cập nhật ứng dụng**:
  - Tích hợp sẵn cơ chế kiểm tra phiên bản mới từ máy chủ từ xa qua metadata `update.json`.
  - Tải về và mở giao diện cài đặt file APK mới an toàn qua Android FileProvider.

---

## Technical Stack & Architecture

AutoSms được thiết kế chuẩn mực theo mô hình **Clean Architecture** kết hợp mẫu kiến trúc **MVVM / MVI** cho tầng giao diện:

| Phân tầng / Thành phần | Công nghệ / Thư viện | Vai trò |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Kotlin 2.0.21 | Ngôn ngữ lập trình chính |
| **Giao diện (UI)** | Jetpack Compose BOM 2024.12.01, Material 3 | Giao diện khai báo hiện đại, mượt mà và trực quan |
| **Điều hướng** | Navigation Compose 2.8.5 | Quản lý luồng chuyển màn hình trong kiến trúc Single-Activity |
| **Kiến trúc** | Clean Architecture (Presentation, Domain, Data) | Phân tách rõ ràng UI, quy tắc nghiệp vụ và truy xuất dữ liệu |
| **Dependency Injection** | Hilt (Dagger) 2.60.1 | Quản lý vòng đời phụ thuộc, tối ưu kiểm thử |
| **Cơ sở dữ liệu** | Room Database 2.8.5 (Schema v12) | Lưu trữ dữ liệu quan hệ cục bộ (Entity, DAO, TypeConverters, Migrations) |
| **Lập lịch nền** | WorkManager 2.10.0 | Lên lịch tự động reset hạn mức SIM hàng ngày |
| **Xử lý Bảng tính** | Apache POI (`poi-ooxml` 5.2.5) | Đọc, phân tích và xuất file bảng tính Excel (`.xlsx`, `.xls`) |
| **Bất đồng bộ** | Kotlin Coroutines 1.9.0 & StateFlow | Xử lý luồng dữ liệu phản ứng bất đồng bộ (Reactive Flow) |
| **Tự động hóa Zalo** | Android Accessibility Service | Tự động hóa thao tác nhập liệu và gửi tin nhắn trên giao diện Zalo |
| **Build & Compiler** | Gradle Kotlin DSL & KSP 2.0.21-1.0.28 | Tối ưu hóa thời gian biên dịch và xử lý annotation |

---

## Requirements & Permissions

### Yêu cầu hệ thống

- **Hệ điều hành**: Android 8.0 (API Level 26) trở lên.
- **Phần cứng**: Thiết bị Android hỗ trợ SIM (1 SIM hoặc Dual SIM) có sóng viễn thông hoạt động.
- **Môi trường phát triển**: Android Studio (Ladybug 2024.2.1 trở lên), JDK 11+, Android SDK 35.

### Quyền ứng dụng (Permissions)

> [!IMPORTANT]
> Ứng dụng yêu cầu cấp một số quyền hệ thống để thực hiện gửi tin nhắn và duy trì tiến trình nền:

- `SEND_SMS`: Quyền gửi trực tiếp tin nhắn SMS qua SIM của thiết bị.
- `READ_PHONE_STATE` & `READ_PHONE_NUMBERS`: Nhận diện khe SIM, trạng thái SIM và phân tích nhà mạng.
- `POST_NOTIFICATIONS`: Hiển thị thông báo tiến độ gửi tin thời gian thực trên Android 13+.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: Duy trì tiến trình gửi SMS trong nền không bị hệ điều hành tắt giải phóng tài nguyên.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Bỏ qua tối ưu pin (Doze mode) để đảm bảo độ trễ gửi tin chuẩn xác.
- `BIND_ACCESSIBILITY_SERVICE`: Quyền trợ năng để hỗ trợ tính năng gửi tin nhắn tự động qua Zalo.
- `REQUEST_INSTALL_PACKAGES`: Cho phép cài đặt bản cập nhật ứng dụng trực tiếp từ file APK nội bộ.

---

## Usage Guide

### 1. Nhập danh sách khách hàng từ Excel
1. Tại màn hình **Trang chủ**, nhấn **Nhập danh sách Excel**.
2. Chọn tệp bảng tính (`.xlsx` hoặc `.xls`). Ứng dụng sẽ tự động phân tích và nhận diện các cột dữ liệu.
3. Chọn chế độ nạp danh sách: Thay thế toàn bộ, Nối tiếp thêm mới hoặc Bỏ qua số trùng.
4. Kiểm tra danh sách khách hàng và các biến dữ liệu đã được nạp thành công.

### 2. Soạn thảo Mẫu tin nhắn & Spintax
1. Chọn tab **Mẫu tin nhắn** hoặc mục chỉnh sửa mẫu.
2. Thêm mới mẫu tin, kết hợp các biến trích xuất từ Excel:
   ```text
   {Xin chào|Kính chào} {Ten_KH}, hợp đồng số {Ma_HD} của bạn cần thanh toán số tiền {So_Tien} đ trước ngày {Ngay_Han}. Cảm ơn quý khách!
   ```
3. Nhấn **Xem trước (Preview)** để kiểm tra nội dung thực tế sau khi thế biến và xử lý Spintax ngẫu nhiên.

### 3. Cấu hình SIM & Quy tắc an toàn
1. Chuyển sang màn hình **Cấu hình** (hoặc tab SIM trong mục Quản lý).
2. Thiết lập cho từng khe **SIM 1** và **SIM 2**:
   - Hạn mức tin nhắn tối đa gửi mỗi ngày.
   - Khoảng trễ tối thiểu và tối đa giữa 2 tin liên tiếp (ví dụ: `40` - `50` giây).
   - Khung giờ hợp lệ cho phép gửi tin trong ngày.
3. Bật tùy chọn ưu tiên gửi theo nhà mạng tương ứng để tối ưu cước phí viễn thông.

### 4. Khởi chạy tiến trình gửi & Giám sát
1. Chọn mẫu tin nhắn, khe SIM điều phối và danh sách liên hệ cần gửi.
2. Bấm **Bắt đầu gửi**. Màn hình tiến độ sẽ hiển thị trạng thái từng lượt gửi theo thời gian thực.
3. Người dùng có thể thu nhỏ ứng dụng; tiến trình gửi sẽ tiếp tục chạy ngầm ổn định kèm thanh thông báo hệ thống. Có thể bấm **Tạm dừng**, **Tiếp tục** hoặc **Hủy** bất kỳ lúc nào.

### 5. Gửi tin nhắn qua Zalo
1. Điều hướng đến màn hình **Gửi Zalo**.
2. Nhập danh sách số điện thoại cần gửi hoặc nạp từ tập khách hàng có sẵn / import từ file Excel.
3. Kích hoạt dịch vụ **AutoSms Accessibility Service** trong phần Cài đặt Trợ năng (Accessibility) của Android.
4. Bật tùy chọn thiết bị cấu hình thấp nếu cần thêm thời gian trễ và retry.
5. Bấm **Bắt đầu gửi Zalo** và để ứng dụng tự động mở cuộc trò chuyện và gửi tin theo mẫu đã chọn.

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

Chạy kiểm thử giao diện và điều hướng (Instrumented Tests):
```powershell
.\gradlew.bat connectedAndroidTest
```

---

## Project Structure

```text
AutoSms/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gmail/nthai/
│   │   │   │   ├── data/             # Triển khai Room Database, Repositories, Excel Parser/Exporter & SmsSender
│   │   │   │   ├── di/               # Hilt Dependency Injection Modules (DatabaseModule, UseCaseModule,...)
│   │   │   │   ├── domain/           # Models, Repository Contracts, UseCases (Spintax, Carrier, Constraints)
│   │   │   │   ├── presentation/     # Jetpack Compose UI (Screens, Components, ViewModels, Navigation, Theme)
│   │   │   │   ├── service/          # Foreground Service, Zalo Accessibility Service, WorkManager Worker
│   │   │   │   └── util/             # Tiện ích hệ thống (Permissions, DateTime, ApkInstaller)
│   │   │   └── res/                  # Android Resources (Drawables, Mipmaps, Values, Xml)
│   │   └── test/                     # Unit Tests kiểm thử nghiệp vụ UseCase và Parser
│   └── build.gradle.kts
├── docs/                             # Tài liệu kỹ thuật dự án (Architecture, Conventions, Stack,...)
│   ├── assets/                       # Vector brand mark & SVG logo (logo.svg)
│   └── codebase/                     # 7 tài liệu đặc tả codebase chi tiết
├── gradle/                           # Gradle Wrapper & Version Catalog (libs.versions.toml)
└── build.gradle.kts
```

---

> [!WARNING]
> Tin nhắn SMS được gửi thông qua sóng mạng di động của thiết bị và có thể tính cước phí viễn thông theo gói cước của SIM. Người sử dụng cần đảm bảo tuân thủ các quy định hiện hành về quyền riêng tư và sự chấp thuận của người nhận tin.
