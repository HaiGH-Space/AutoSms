<div align="center">
  <img src="docs/assets/logo.svg" alt="AutoSms Logo" width="108" height="108">
  <h1>AutoSms</h1>
  <p><strong>Giải pháp Android tự động hóa gửi tin nhắn SMS cá nhân hóa từ Excel qua SIM thiết bị, tự động gọi thoại (Auto Call) và gửi tin nhắn tự động qua Zalo.</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=flat&logo=android&logoColor=white" alt="Android Version">
    <img src="https://img.shields.io/badge/Version-0.2.1_(Code_12)-blue?style=flat" alt="App Version">
    <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Kotlin Version">
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Architecture-Clean_Architecture-brightgreen?style=flat" alt="Clean Architecture">
    <img src="https://img.shields.io/badge/CI-GitHub_Actions-2088FF?style=flat&logo=githubactions&logoColor=white" alt="GitHub Actions">
  </p>
</div>

---

## Overview

**AutoSms** là ứng dụng di động Android chuyên dụng hỗ trợ doanh nghiệp, tổ chức và cá nhân kinh doanh tự động hóa quy trình chăm sóc khách hàng qua SMS, cuộc gọi thoại (Auto Call) và Zalo với mức độ cá nhân hóa cao.

Ứng dụng kết nối trực tiếp với file bảng tính Excel (`.xlsx`, `.xls`), tự động nhận diện dữ liệu liên hệ, hỗ trợ biến động cùng cú pháp Spintax chống spam, tự động tối ưu chi phí cước thông qua nhận diện nhà mạng Việt Nam và phân bổ linh hoạt giữa 2 khe SIM. Quy trình gửi SMS và Auto Call chạy độc lập, ổn định trong nền qua Android Foreground Service (tuân thủ nghiêm ngặt Android 14+), kết hợp tính năng quản lý Danh sách đen (Blacklist) chặn số tự động, cùng khả năng gửi tự động qua ứng dụng Zalo thông qua Android Accessibility Service.

> [!NOTE]
> Toàn bộ danh bạ, nhật ký cuộc gọi, lịch sử SMS, mẫu tin nhắn và danh sách chặn được lưu trữ hoàn toàn cục bộ trên thiết bị qua SQLite (Room Database Schema v15), bảo đảm tuyệt đối an toàn và bảo mật dữ liệu.

---

## Key Features

- **Nhập dữ liệu thông minh từ Excel**:
  - Hỗ trợ trực tiếp định dạng `.xlsx` và `.xls` qua Apache POI với cơ chế streaming SAX tối ưu RAM, chống tràn bộ nhớ (OOM).
  - Tự động nhận diện cột số điện thoại phổ biến (`SDT`, `SĐT`, `Phone`, `Mobile`, `Điện thoại`,...).
  - Đa dạng chế độ nạp: **Thay thế toàn bộ**, **Nối tiếp thêm mới** và **Bỏ qua số trùng lặp**.
  - Trích xuất linh hoạt mọi biến tùy chỉnh (`{Ten_KH}`, `{So_Tien}`, `{Ma_HD}`, `{Ngay_Han}`,...).

- **Động hóa nội dung & Công nghệ Spintax**:
  - Ghép biến dữ liệu từ file Excel vào nội dung theo cú pháp `{Tên_Cột}`.
  - Cú pháp **Spintax** dạng `{Chào anh|Kính chào|Xin chào}` hoán đổi linh hoạt câu từ ngẫu nhiên, hạn chế tối đa nguy cơ bị nhà mạng lọc spam.
  - Tự động chuẩn hóa định dạng số và tiền tệ theo chuẩn Việt Nam (`1.000.000 đ`), bảo toàn độ chính xác với số lớn.

- **Nhận diện nhà mạng Việt Nam & Định tuyến SIM**:
  - Nhận diện tức thì tất cả nhà mạng di động tại Việt Nam: Viettel, VinaPhone, MobiFone, Vietnamobile, Gmobile, Wintel, iTel.
  - So khớp tự động đầu số với nhà mạng của từng khe SIM (SIM 1 / SIM 2) để gửi nội mạng, tối ưu chi phí viễn thông.

- **Quản lý & Cấu hình 2 khe SIM chuyên sâu**:
  - Giám sát trạng thái lắp SIM, tên nhà mạng và số điện thoại từng khe SIM.
  - Thiết lập định mức (quota) gửi SMS tối đa theo ngày cho từng khe SIM độc lập.
  - Cấu hình khoảng trễ ngẫu nhiên an toàn giữa các lần gửi (ví dụ: `40` - `50` giây).
  - Cài đặt khung giờ cho phép gửi tin trong ngày, tự động dừng ngoài giờ làm việc.
  - Cơ chế tự động ngắt gửi sau **3 lần thất bại liên tiếp** để bảo vệ tài khoản và thiết bị.
  - Tự động làm mới (reset) hạn mức mỗi ngày qua AndroidX WorkManager (`DailySmsWorker`).

- **Chiến dịch Tự động gọi thoại (Auto Call)**:
  - Khởi chạy chiến dịch tự động gọi điện theo danh sách nạp từ Excel hoặc danh bạ khách hàng.
  - Duy trì tiến trình gọi nền bền bỉ qua Foreground Service loại `phoneCall`.
  - Giám sát trạng thái cuộc gọi trực tiếp qua `PhoneCallStateObserver` kết hợp TelephonyManager và System Call Log.
  - Tự động đo lường thời gian đàm thoại thực tế (giây), hỗ trợ cấu hình giới hạn thời lượng cuộc gọi tối đa và thời gian chờ chuông (timeout).
  - Cấu hình khoảng trễ ngẫu nhiên giữa các cuộc gọi để mô phỏng hành vi tự nhiên.

- **Quản lý Danh sách đen (Blacklist)**:
  - Quản lý danh sách các số điện thoại từ chối hoặc cần chặn liên lạc.
  - Tự động chặn và bỏ qua liên hệ nằm trong Blacklist trên tất cả các kênh: SMS, Auto Call và Zalo.
  - Thêm số nhanh, tìm kiếm, nhập hàng loạt hoặc xóa số chặn dễ dàng.

- **Gửi tin nhắn tự động qua Zalo**:
  - Mở trực tiếp cuộc trò chuyện Zalo theo số điện thoại qua deep link (`https://zalo.me/{phone}`).
  - Kiểm tra và hướng dẫn cấp quyền Trợ năng (Accessibility) chủ động trước khi khởi chạy chiến dịch.
  - Sử dụng **Android Accessibility Service** (`ZaloAccessibilityService`) để tự động điền tin nhắn và thực hiện thao tác gửi.
  - Tự động xử lý màn hình lỗi WebView, số chưa đăng ký Zalo hoặc tài khoản không tồn tại, tự động điều hướng quay lại AutoSms an toàn (`returnToAutoSms`).
  - Hỗ trợ dán văn bản bằng clipboard dự phòng và cơ chế retry linh hoạt cho thiết bị cấu hình thấp.

- **Tiến trình gửi nền bền bỉ (Foreground Service)**:
  - Dịch vụ nền loại `dataSync` (cho SMS) và `phoneCall` (cho Auto Call) tương thích Android 14+ (API 34/35).
  - Thanh thông báo hệ thống trực quan cập nhật tiến độ theo thời gian thực (Đã gửi/gọi, Thất bại, Còn lại).
  - Hỗ trợ điều khiển trực tiếp: Tạm dừng (Pause), Tiếp tục (Resume) hoặc Hủy bỏ (Stop).

- **Giao diện hiện đại & Báo cáo trực quan**:
  - Thiết kế theo chuẩn Material 3 và hệ thống Design System (`DESIGN.md`) với khung giao diện dùng chung (`FeatureCampaignScaffold`, `CampaignBottomActionBar`).
  - Lưu trữ chi tiết nhật ký trạng thái từng lượt gửi/gọi (`SENT`, `DELIVERED`, `FAILED`, `COMPLETED`).
  - Lọc báo cáo linh hoạt theo ngày/tháng hoặc khoảng thời gian tùy chọn.
  - Xuất báo cáo lịch sử ra file Excel (`.xlsx`) dạng streaming không tràn bộ nhớ.

- **Tự động kiểm tra cập nhật ứng dụng**:
  - Tích hợp cơ chế kiểm tra phiên bản mới từ máy chủ từ xa qua metadata `update.json`.
  - Tải về và mở giao diện cài đặt file APK mới an toàn qua Android FileProvider với HTTPS bảo mật.

---

## Technical Stack & Architecture

AutoSms được thiết kế chuẩn mực theo mô hình **Clean Architecture** kết hợp mẫu kiến trúc **MVVM / MVI** cho tầng giao diện:

| Phân tầng / Thành phần | Công nghệ / Thư viện | Vai trò |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Kotlin 2.0.21 | Ngôn ngữ lập trình chính |
| **Giao diện (UI)** | Jetpack Compose BOM 2024.12.01, Material 3 | Giao diện khai báo hiện đại, tuân thủ Design System `DESIGN.md` |
| **Điều hướng** | Navigation Compose 2.8.5 | Quản lý luồng chuyển màn hình trong kiến trúc Single-Activity |
| **Kiến trúc** | Clean Architecture (Presentation, Domain, Data) | Phân tách rõ ràng UI, quy tắc nghiệp vụ và truy xuất dữ liệu |
| **Dependency Injection** | Hilt (Dagger) 2.60.1 | Quản lý vòng đời phụ thuộc, tối ưu kiểm thử |
| **Cơ sở dữ liệu** | Room Database 2.8.5 (Schema v15) | Lưu trữ dữ liệu quan hệ cục bộ (Entity, DAO, TypeConverters, Migrations v1-v15) |
| **Lập lịch nền** | WorkManager 2.10.0 | Lên lịch tự động reset hạn mức SIM hàng ngày |
| **Xử lý Bảng tính** | Apache POI (`poi-ooxml` 5.2.5) | Đọc SAX streaming và xuất file bảng tính Excel (`.xlsx`, `.xls`) |
| **Bất đồng bộ** | Kotlin Coroutines 1.9.0 & StateFlow | Xử lý luồng dữ liệu phản ứng bất đồng bộ (Reactive Flow) |
| **Tự động hóa Zalo** | Android Accessibility Service | Tự động hóa thao tác nhập liệu và gửi tin nhắn trên giao diện Zalo |
| **Tự động hóa Auto Call** | Android Telecom / TelephonyManager | Thực hiện cuộc gọi thoại và giám sát trạng thái thời lượng cuộc gọi |
| **CI / CD** | GitHub Actions (`.github/workflows/ci.yml`) | Kiểm thử tự động và kiểm tra tính toàn vẹn phiên bản trên môi trường Ubuntu / JDK 21 |
| **Build & Compiler** | Gradle Kotlin DSL & KSP 2.0.21-1.0.28 | Tối ưu hóa thời gian biên dịch và xử lý annotation |

---

## Requirements & Permissions

### Yêu cầu hệ thống

- **Hệ điều hành**: Android 8.0 (API Level 26) trở lên.
- **Phần cứng**: Thiết bị Android hỗ trợ SIM (1 SIM hoặc Dual SIM) có sóng viễn thông hoạt động.
- **Môi trường phát triển**: Android Studio (Ladybug 2024.2.1 trở lên), JDK 11+, Android SDK 35.

### Quyền ứng dụng (Permissions)

> [!IMPORTANT]
> Ứng dụng yêu cầu cấp một số quyền hệ thống để thực hiện gửi tin nhắn, gọi thoại và duy trì tiến trình nền:

- `SEND_SMS`: Quyền gửi trực tiếp tin nhắn SMS qua SIM của thiết bị.
- `CALL_PHONE` & `MANAGE_OWN_CALLS`: Quyền thực hiện cuộc gọi thoại cho tính năng Auto Call.
- `READ_CALL_LOG`: Quyền đọc nhật ký cuộc gọi để đo lường chính xác thời lượng đàm thoại.
- `READ_PHONE_STATE` & `READ_PHONE_NUMBERS`: Nhận diện khe SIM, trạng thái SIM, trạng thái cuộc gọi và phân tích nhà mạng.
- `POST_NOTIFICATIONS`: Hiển thị thông báo tiến độ gửi tin và cuộc gọi thời gian thực trên Android 13+.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` & `FOREGROUND_SERVICE_PHONE_CALL`: Duy trì tiến trình gửi SMS và Auto Call trong nền không bị hệ điều hành đóng.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Bỏ qua tối ưu pin (Doze mode) để đảm bảo tiến trình gửi/gọi liên tục.
- `BIND_ACCESSIBILITY_SERVICE`: Quyền trợ năng để hỗ trợ tính năng gửi tin nhắn tự động qua Zalo.
- `REQUEST_INSTALL_PACKAGES`: Cho phép cài đặt bản cập nhật ứng dụng trực tiếp từ file APK nội bộ.

---

## Usage Guide

### 1. Nhập danh sách khách hàng từ Excel
1. Tại màn hình **Trang chủ** (hoặc tab Danh sách trong từng chiến dịch), nhấn **Nhập danh sách Excel**.
2. Chọn tệp bảng tính (`.xlsx` hoặc `.xls`). Ứng dụng sẽ tự động phân tích và nhận diện các cột dữ liệu.
3. Chọn chế độ nạp danh sách: **Thay thế toàn bộ**, **Nối tiếp thêm mới** hoặc **Bỏ qua số trùng**.
4. Kiểm tra danh sách liên hệ và các biến dữ liệu đã được nạp thành công.

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

### 4. Khởi chạy chiến dịch Auto Call
1. Truy cập màn hình **Auto Call** từ Trang chủ hoặc thanh điều hướng.
2. Tại tab **Danh sách**, chuẩn bị danh sách số điện thoại cần gọi (nhập từ Excel hoặc đồng bộ từ khách hàng).
3. Tại tab **Cấu hình**, thiết lập:
   - Khoảng trễ giữa 2 cuộc gọi liên tiếp (giây).
   - Giới hạn thời lượng cuộc gọi tối đa (giây).
   - Thời gian chờ nhấc máy (timeout).
4. Cấp quyền gọi thoại (`CALL_PHONE`) và quyền đọc nhật ký cuộc gọi (`READ_CALL_LOG`).
5. Bấm **Bắt đầu gọi** và theo dõi tiến trình trực tiếp tại tab **Tiến độ**.

### 5. Quản lý Danh sách đen (Blacklist)
1. Truy cập tab **Danh sách đen** trong mục Cấu hình / Quản lý.
2. Nhập số điện thoại cần chặn kèm lý do (ví dụ: "Khách yêu cầu không làm phiền").
3. Hệ thống sẽ tự động bỏ qua các số này trong tất cả chiến dịch SMS, Auto Call và Zalo.

### 6. Khởi chạy tiến trình gửi SMS / Zalo & Giám sát
1. Chọn mẫu tin nhắn, khe SIM điều phối (với SMS) hoặc kích hoạt dịch vụ Trợ năng (với Zalo).
2. Bấm **Bắt đầu gửi**.
3. Tiến trình sẽ hiển thị trạng thái từng lượt gửi theo thời gian thực và cập nhật qua thanh thông báo hệ thống.

---

## Build & Development

```bash
# Clone repository
git clone https://github.com/HaiGH-Space/AutoSms.git
cd AutoSms

# Biên dịch bản Debug APK
./gradlew assembleDebug

# Biên dịch bản Release APK (có tối ưu R8/ProGuard)
./gradlew assembleRelease

# Chạy toàn bộ Unit Tests
./gradlew testDebugUnitTest

# Kiểm tra tính toàn vẹn metadata update.json
./gradlew :app:validateUpdateJson
```
