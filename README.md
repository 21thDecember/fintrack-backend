# Personal Finance Management Backend

Backend cho ứng dụng Quản lý Tài chính Cá nhân (Personal Finance Management) xây dựng bằng **Java 17**, **Spring Boot**, và kiến trúc **Modular Monolith**.

---

## 🏗️ Kiến Trúc Hệ Thống (Modular Monolith)

```
com.personalfinance
├── common                  # Các lớp dùng chung (Exception, Response, Config, Util)
│   ├── config              # Cấu hình DataSource linh hoạt (hỗ trợ Neon, Railway, Docker)
│   ├── entity              # Cấu hình app_settings
│   ├── exception           # GlobalExceptionHandler, BusinessException, ResourceNotFoundException
│   ├── response            # ApiResponse<T>, PageResponse<T>
│   └── util                # DateTimeUtil (xử lý ngày giờ, múi giờ Việt Nam và UTC)
├── security                # Bảo mật Spring Security, JWT HMAC-SHA256, Single-user Admin
├── account                 # Quản lý tài khoản (Bank, Cash, Other), tính toán số dư động
├── category                # Quản lý danh mục thu/chi (Income, Expense)
├── person                  # Quản lý người liên quan (bạn bè, người vay nợ)
├── transaction             # Quản lý giao dịch thu chi, chuyển khoản, hủy giao dịch (VOID)
├── debt                    # Quản lý sổ nợ (Phải thu / Phải trả), chia tiền ăn uống, trả nợ
├── budget                  # Ngân sách chi tiêu theo danh mục và tháng
├── report                  # Báo cáo tuần, tháng, phân rã danh mục, tài sản ròng (Net Worth)
├── dashboard               # Thống kê tổng quan, so sánh kỳ, nợ đến hạn, cảnh báo
├── integration.sepay       # Tích hợp SePay Webhook tự động nhận diện giao dịch ngân hàng
├── backup                  # Sao lưu và phục hồi dữ liệu dạng JSON
└── notification            # Cảnh báo nợ quá hạn, chạm ngưỡng ngân sách, giao dịch chưa phân loại
```

---

## ⚙️ Biến Môi Trường (Environment Variables)

| Tên biến | Mặc định | Mô tả |
| :--- | :--- | :--- |
| `PORT` | `8080` | Cổng HTTP của server |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile hoạt động (`dev` hoặc `prod`) |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/personal_finance` | Chuỗi kết nối PostgreSQL (hỗ trợ cả chuẩn JDBC lẫn Neon/Supabase URL `postgresql://...`) |
| `ADMIN_USERNAME` | `admin` | Tên đăng nhập Admin |
| `ADMIN_PASSWORD` | `admin123` | Mật khẩu Admin |
| `JWT_SECRET` | *(chuỗi 256-bit)* | Khóa bí mật ký JWT HMAC-SHA256 |
| `CORS_ALLOWED_ORIGINS` | `*` | Danh sách domain được phép truy cập CORS |
| `SEPAY_WEBHOOK_API_KEY` | *(trống)* | API Key xác thực Webhook gửi từ SePay |

---

## 🗄️ Cơ Sở Dữ Liệu & Migrations (Flyway)

Toàn bộ database schema được quản lý tự động bởi Flyway Migration:
- `V1__init.sql`:
  - `accounts`: Quản lý tài khoản và số dư ban đầu.
  - `categories`: Danh mục thu chi phân cấp (cây danh mục).
  - `persons`: Danh sách người liên quan (bạn bè, đối tác).
  - `transactions`: Giao dịch thu, chi, chuyển khoản, vay nợ.
  - `debts`: Sổ nợ phải thu (RECEIVABLE) và phải trả (PAYABLE).
  - `debt_allocations`: Phân bổ khoản nợ theo từng giao dịch chi tiêu (chia tiền nhóm).
  - `debt_payments`: Lịch sử các lần thanh toán nợ.
  - `budgets`: Thiết lập hạn mức ngân sách theo danh mục hàng tháng.
  - `app_settings`: Cấu hình hệ thống.
  - Đầy đủ các chỉ mục (Indexes) phục vụ truy vấn tốc độ cao.
- `V2__seed_data.sql`:
  - Khởi tạo sẵn các danh mục Thu nhập (Lương, Thưởng, Thu nhập khác) và Chi tiêu (Ăn uống, Di chuyển, Mua sắm, Sức khỏe, Giáo dục, v.v.).

---

## 📌 Các Quy Tắc Nghiệp Vụ Cốt Lõi (Core Business Rules)

1. **Tiền tệ chuẩn số nguyên**: Toàn bộ giá trị tiền tệ sử dụng kiểu `BIGINT` (`Long` trong Java), tuyệt đối không dùng float/double để tránh sai số dấu phẩy động.
2. **Số dư tính toán động (Calculated Balance)**: Số dư tài khoản không lưu tĩnh, luôn được tính toán tức thời theo công thức:
   $$\text{Current Balance} = \text{Opening Balance} + \sum \text{IN (POSTED)} - \sum \text{OUT (POSTED)}$$
3. **Chuyển khoản (Transfer)**:
   - Tạo đồng thời 2 giao dịch liên kết mang cùng một mã `transferGroupId`: một giao dịch `OUT` tại tài khoản nguồn và một giao dịch `IN` tại tài khoản đích.
   - Nếu có phí chuyển tiền, tự động tạo giao dịch `EXPENSE` tương ứng.
4. **Bảo toàn dữ liệu Ngân hàng (Bank Immutability)**:
   - Đối với các giao dịch có nguồn gốc từ ngân hàng (`source = BANK`), các trường: `amount`, `externalTransactionId`, `transactionDate`, `accountId`, `originalDescription` là **bất biến**, không được phép chỉnh sửa qua API.
5. **Chia tiền & Phân bổ nợ (Debt Allocation)**:
   - Khi tạo giao dịch chi tiêu (`EXPENSE`), người dùng có thể đính kèm danh sách phân bổ người nợ (`debtAllocations`). Hệ thống tự động tạo các bản ghi `Debt` (`RECEIVABLE`) và `DebtAllocation` tương ứng trong cùng 1 Transaction CSDL.
6. **Thanh toán nợ nguyên khối (Atomic Debt Payment)**:
   - Khi thanh toán nợ (`POST /api/debts/{id}/payments`), hệ thống tạo tự động giao dịch tài chính (`DEBT_COLLECTION` hoặc `DEBT_REPAYMENT`), tạo bản ghi `DebtPayment`, và cập nhật trạng thái nợ (`OPEN` -> `PARTIAL` -> `PAID`) trong một giao dịch DB duy nhất (`@Transactional`).
7. **Không xóa cứng (No Hard Deletes)**:
   - Giao dịch tài chính chỉ được hủy (`POST /api/transactions/{id}/void` sang trạng thái `VOIDED`), tài khoản và danh mục sử dụng soft delete (`is_active = false`).

---

## 🚀 Danh Sách API Endpoints

### 1. Xác thực (Authentication)
- `POST /api/auth/login`: Đăng nhập, nhận JWT Token.
- `GET /api/auth/me`: Thông tin người dùng hiện tại.

### 2. Tài khoản (Accounts)
- `GET /api/accounts`: Danh sách tài khoản kèm số dư tính toán hiện tại.
- `GET /api/accounts/summary`: Tổng số dư toàn bộ hệ thống.
- `GET /api/accounts/{id}`: Chi tiết tài khoản.
- `POST /api/accounts`: Tạo tài khoản mới.
- `PUT /api/accounts/{id}`: Cập nhật thông tin tài khoản.
- `DELETE /api/accounts/{id}`: Vô hiệu hóa tài khoản (Soft delete).

### 3. Danh mục (Categories)
- `GET /api/categories?type=INCOME|EXPENSE`: Lấy danh sách danh mục theo loại.
- `GET /api/categories/{id}`: Chi tiết danh mục.
- `POST /api/categories`: Tạo danh mục.
- `PUT /api/categories/{id}`: Cập nhật danh mục.
- `DELETE /api/categories/{id}`: Vô hiệu hóa danh mục.

### 4. Người liên quan (Persons)
- `GET /api/persons`: Danh sách bạn bè/người vay nợ.
- `GET /api/persons/{id}`: Chi tiết đối tượng.
- `POST /api/persons`: Thêm mới đối tượng.
- `PUT /api/persons/{id}`: Cập nhật thông tin đối tượng.
- `DELETE /api/persons/{id}`: Xóa đối tượng.

### 5. Giao dịch (Transactions)
- `GET /api/transactions`: Tìm kiếm, lọc và phân trang giao dịch theo ngày, tài khoản, danh mục, trạng thái, từ khóa.
- `GET /api/transactions/{id}`: Chi tiết giao dịch.
- `POST /api/transactions`: Tạo giao dịch (hỗ trợ phân bổ nợ chia tiền).
- `POST /api/transactions/transfer`: Chuyển tiền giữa 2 tài khoản.
- `PUT /api/transactions/{id}`: Cập nhật giao dịch.
- `POST /api/transactions/{id}/void`: Hủy giao dịch (VOID).

### 6. Sổ nợ (Debts)
- `GET /api/debts?type=...&status=...&personId=...`: Lọc danh sách nợ.
- `GET /api/debts/summary`: Tổng nợ phải thu, tổng nợ phải trả, chênh lệch nợ ròng.
- `GET /api/debts/{id}`: Chi tiết khoản nợ kèm lịch sử các lần thanh toán.
- `POST /api/debts`: Tạo khoản nợ độc lập thủ công.
- `POST /api/debts/from-transaction`: Tạo nợ từ giao dịch chi tiêu có sẵn.
- `POST /api/debts/{id}/payments`: Ghi nhận thanh toán/thu nợ.
- `PUT /api/debts/{id}`: Cập nhật ghi chú/hạn trả nợ.
- `POST /api/debts/{id}/cancel`: Hủy khoản nợ.

### 7. Ngân sách (Budgets)
- `GET /api/budgets?year=...&month=...`: Danh sách ngân sách kèm số tiền đã chi, số tiền còn lại và tỷ lệ phần trăm.
- `GET /api/budgets/{id}`: Chi tiết ngân sách.
- `POST /api/budgets`: Thiết lập hoặc cập nhật ngân sách.
- `PUT /api/budgets/{id}`: Sửa hạn mức ngân sách.
- `DELETE /api/budgets/{id}`: Xóa ngân sách.

### 8. Báo cáo (Reports)
- `GET /api/reports/weekly?date=...`: Báo cáo tuần (Tổng thu, chi gộp, chi hộ, chi tiêu cá nhân, dòng tiền ròng, phân rã danh mục).
- `GET /api/reports/monthly?year=...&month=...`: Báo cáo tháng và so sánh % với tháng trước.
- `GET /api/reports/net-worth`: Báo cáo tài sản ròng ($\text{Tài sản} = \text{Tiền mặt/Ngân hàng} + \text{Phải thu} - \text{Phải trả}$).

### 9. Dashboard
- `GET /api/dashboard?period=THIS_MONTH|THIS_WEEK|THIS_YEAR|LAST_MONTH`: Dữ liệu bảng điều khiển tổng hợp (Tài sản, Thu chi, 10 giao dịch mới nhất, nợ sắp đến hạn, cảnh báo giao dịch chưa phân loại, so sánh kỳ trước).

### 10. SePay Webhook
- `POST /api/webhooks/sepay`: Endpoint nhận webhook từ cổng SePay (tự động xử lý trùng lặp Idempotent, khớp tài khoản, tự động phân loại danh mục thông minh).

### 11. Sao lưu & Phục hồi (Backup)
- `GET /api/backup/export`: Xuất toàn bộ dữ liệu hệ thống ra tệp JSON.
- `POST /api/backup/import`: Nhập và phục hồi dữ liệu từ JSON.

### 12. Cảnh báo & Thông báo (Notifications)
- `GET /api/notifications`: Danh sách các cảnh báo hiện hành (Nợ quá hạn/sắp đến hạn, vượt hạn mức chi tiêu, giao dịch chưa phân loại).

---

## 🛠️ Hướng Dẫn Biên Dịch & Chạy Ứng Dụng

### 1. Biên dịch và chạy kiểm thử:
```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
mvn clean test
```

### 2. Khởi chạy ứng dụng:
```bash
export DATABASE_URL="postgresql://username:password@ep-host.neon.tech/neondb?sslmode=require"
export ADMIN_USERNAME="admin"
export ADMIN_PASSWORD="your-strong-password"
export JWT_SECRET="your-256-bit-hex-secret-key"

mvn spring-boot:run
```
