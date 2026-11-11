<div align="center">
  <img src="app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" alt="AutoSms Logo" width="100">
  <h1>AutoSms</h1>
  <p><strong>Ứng dụng Android tự động hóa gửi tin nhắn SMS cá nhân hóa từ Excel qua SIM thiết bị.</strong></p>
</div>

---

## Overview

**AutoSms** là giải pháp gửi tin nhắn hàng loạt cá nhân hóa trên Android. Ứng dụng đọc dữ liệu khách hàng từ bảng tính Excel (`.xlsx`, `.xls`), tự động điền các thông tin tùy biến vào mẫu tin nhắn, chọn lọc SIM gửi phù hợp (SIM 1 / SIM 2), kiểm soát khoảng trễ ngẫu nhiên nhằm tránh vi phạm quy định chống spam của nhà mạng, và duy trì tiến độ gửi trong nền thông qua Foreground Service.

> [!NOTE]
> Toàn bộ dữ liệu khách hàng, nhật ký tin nhắn và cấu hình SIM được lưu trữ cục bộ trên thiết bị thông qua Room Database, bảo đảm tính riêng tư và an toàn thông tin.

---

## Key Features

- **Nhập dữ liệu linh hoạt từ Excel**: Đọc tự động các cột từ file bảng tính Excel (`.xlsx` / `.xls`), linh hoạt nhận diện cột số điện thoại (`SDT`, `Phone`, `Mobile`, `Điện thoại`,...) và trích xuất các cột thông tin tùy chọn.
- **Động hóa nội dung & Spintax**:
  - Trích xuất dữ liệu Excel vào mẫu tin bằng cú pháp `{Tên_Cột}` hoặc các tham số nhanh như `{Ten_KH}`, `{So_Tien}`, `{Ma_HD}`, `{Ngay_Han}`.
  - Hỗ trợ công nghệ **Spintax** (`{Cháo bạn|Kính chào|Xin chào}`) giúp đảo ngữ ngẫu nhiên nội dung tin nhắn, hạn chế bị nhà mạng đánh dấu spam.
  - Tự động định dạng tiền tệ theo tiêu chuẩn Việt Nam (ví dụ: `1.000.000`).
- **Nhận diện nhà mạng Việt Nam**:
  - Tự động phân tích đầu số và nhận diện nhà mạng (Viettel, VinaPhone, MobiFone, Vietnamobile, Gmobile, Wintel, iTel).
  - Tùy chọn gửi tin ưu tiên theo nhà mạng của SIM tương ứng.
- **Quản lý & Cấu hình 2 SIM**:
  - Thiết lập hạn mức (quota) gửi SMS tối đa theo ngày riêng cho từng khe SIM.
  - Cấu hình khoảng trễ ngẫu nhiên (tránh gửi quá nhanh) và khung giờ gửi cho phép trong ngày.
- **Tiến trình gửi ổn định trong nền**:
  - Chạy tiến trình gửi qua **Foreground Service**, duy trì hoạt động ngay cả khi ứng dụng bị ẩn hoặc màn hình tắt.
  - Hỗ trợ theo dõi tiến độ thời gian thực (Đã gửi, Thất bại, Còn lại), hỗ trợ Tạm dừng (Pause), Tiếp tục (Resume) hoặc Hủy bỏ (Stop).
- **Thống kê & Báo cáo**:
  - Nhật ký chi tiết trạng thái gửi (`SENT`, `DELIVERED`, `FAILED`).
  - Lọc báo cáo theo khoảng thời gian và xuất dữ liệu báo cáo ra file Excel.
- **Cập nhật tự động**: Kiểm tra và tải bản cập nhật APK mới trực tiếp ngay trong ứng dụng.

---

## Technical Stack & Architecture

AutoSms được phát triển chuẩn hóa theo định hướng **Clean Architecture** và mô hình **MVVM / MVI**:

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| **Ngôn ngữ** | Kotlin |
| **Giao diện (UI)** | Jetpack Compose, Material 3, Accompanist / Dynamic System Bars |
| **Kiến trúc** | Clean Architecture (Presentation, Domain, Data), ViewModel, StateFlow |
| **Dependency Injection** | Hilt (Dagger) |
| **Cơ sở dữ liệu (Local Storage)** | Room Database |
| **Tác vụ nền & Lập lịch** | Android Foreground Service, WorkManager |
| **Xử lý Bảng tính** | Apache POI (`poi-ooxml`) |
| **Tải & Cài đặt APK** | OkHttp / Ktor Client, FileProvider |
| **Build System** | Gradle (Kotlin DSL), KSP (Kotlin Symbol Processing) |

---

## Requirements & Permissions

### Yêu cầu hệ thống

- **Hệ điều hành**: Android 8.0 (API Level 26) trở lên.
- **Phần cứng**: Thiết bị di động có hỗ trợ SIM (1 SIM hoặc Dual SIM) và đang hoạt động dịch vụ viễn thông.
- **Môi trường phát triển**: Android Studio (Ladybug trở lên), JDK 11+, Android SDK 35.

### Quyền ứng dụng (Permissions)

> [!IMPORTANT]
> Ứng dụng yêu cầu cấp một số quyền hệ thống để thực thi nhiệm vụ:

- `SEND_SMS`: Quyền trực tiếp gửi tin nhắn qua sóng di động.
- `READ_PHONE_STATE` & `READ_PHONE_NUMBERS`: Đọc thông tin SIM slot và nhận diện nhà mạng.
- `POST_NOTIFICATIONS`: Hiển thị thanh thông báo tiến trình gửi khi chạy ngầm.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: Giữ tiến trình gửi SMS không bị hệ điều hành Android giải phóng bộ nhớ.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Bỏ qua tối ưu pin nhằm đảm bảo độ trễ gửi tin ổn định.
- `REQUEST_INSTALL_PACKAGES`: Cho phép cập nhật phiên bản ứng dụng mới từ APK.

---

## Usage Guide

### 1. Nhập danh sách khách hàng từ Excel
1. Tại màn hình **Trang chủ**, nhấn **Nhập danh sách Excel**.
2. Chọn file bảng tính (`.xlsx` hoặc `.xls`). Ứng dụng sẽ tự động phân tích các tiêu đề cột.
3. Kiểm tra danh sách khách hàng đã trích xuất thành công.

### 2. Thiết lập Mẫu tin nhắn & Spintax
1. Chuyển sang mục **Mẫu tin nhắn**.
2. Tạo mới mẫu tin nhắn, kết hợp các biến tùy chỉnh từ file Excel:
   ```text
   {Xin chào|Kính chào} {Ten_KH}, hợp đồng số {Ma_HD} của anh/chị có số tiền cần thanh toán là {So_Tien} đ. Hạn thanh toán: {Ngay_Han}. Cảm ơn!
   ```
3. Xem trước (Preview) nội dung tin nhắn đã thế biến để kiểm tra trước khi gửi.

### 3. Cấu hình SIM & Quy tắc gửi
1. Chuyển sang tab **Cấu hình**.
2. Cấu hình cho **SIM 1** và **SIM 2**:
   - Số tin gửi tối đa / ngày.
   - Khoảng trễ ngẫu nhiên giữa các lần gửi (ví dụ: `15` - `45` giây).
   - Khung giờ cho phép gửi tin trong ngày.

### 4. Bắt đầu gửi & Theo dõi tiến độ
1. Chọn SIM thực hiện gửi và danh sách khách hàng cần gửi.
2. Bấm **Bắt đầu gửi**. Tiến trình gửi sẽ hiển thị trực quan ở màn hình **Tiến độ gửi**.
3. Bạn có thể thu nhỏ ứng dụng; thông báo trạng thái sẽ duy trì ở thanh thông báo hệ thống.

### 5. Xuất báo cáo
1. Truy cập mục **Báo cáo**.
2. Chọn khoảng thời gian cần thống kê.
3. Nhấn **Xuất báo cáo Excel** để lưu file nhật ký kết quả gửi.

---

## Building & Development

### Biên dịch dự án qua Gradle Wrapper

Biên dịch bản Debug APK:
```powershell
.\gradlew.bat assembleDebug
```
*File APK đầu ra:* `app/build/outputs/apk/debug/app-debug.apk`

Biên dịch bản Release APK:
```powershell
.\gradlew.bat assembleRelease
```

Chạy Unit Tests:
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
│   │   │   │   ├── data/             # Database (Room), Repositories, Excel & SMS Logic
│   │   │   │   ├── di/               # Hilt Dependency Injection Modules
│   │   │   │   ├── domain/           # Models, UseCases (Spintax, Carrier Detection, Rules)
│   │   │   │   ├── presentation/     # UI Layer (Jetpack Compose Screens, ViewModels, Theme)
│   │   │   │   ├── service/          # Foreground Service & BroadcastReceivers
│   │   │   │   └── util/             # Utility classes (Permissions, Dates, Installers)
│   │   │   └── res/                  # Android Resources (Icons, Themes, Values)
│   │   └── test/                     # Unit Tests for UseCases and Parsers
│   └── build.gradle.kts
└── build.gradle.kts
```

> [!WARNING]
> Tin nhắn SMS được gửi qua hạ tầng mạng di động của SIM trên thiết bị và có thể phát sinh cước phí từ nhà mạng. Vui lòng đảm bảo tuân thủ các quy định hiện hành về tần suất gửi và sự đồng ý của người nhận.
