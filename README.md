# IAM Service 2 - Spring Boot Application

## Mô tả
IAM Service 2 là một hệ thống quản lý người dùng (Identity and Access Management) phát triển bằng **Spring Boot**, cung cấp các tính năng quản lý người dùng và phân quyền, tích hợp với **Keycloak** như một Identity Server và hỗ trợ **RBAC** (Role-Based Access Control). Hệ thống này hỗ trợ cả chế độ **self-idp** (quản lý người dùng qua cơ sở dữ liệu nội bộ) và **Keycloak** làm Identity Provider (IdP).

## Các tính năng chính:

### 1. Tích hợp Keycloak như Identity Server
- **Xác thực Request**: Kiểm tra người dùng qua access token của Keycloak. Nếu hợp lệ, cho phép truy cập hệ thống.
- **Đăng ký người dùng**: Tạo tài khoản trên Keycloak và đồng bộ với cơ sở dữ liệu nội bộ.
- **Đăng xuất**: Tích hợp API logout của Keycloak.
- **Refresh Token**: Làm mới token từ Keycloak khi hết hạn.
- **Self-IDP**: Nếu tắt Keycloak, hệ thống sẽ sử dụng thông tin người dùng từ cơ sở dữ liệu nội bộ.

### 2. API Quản lý Người Dùng
- **Tạo User**, **Xóa mềm User**, **Khóa/Mở khóa User**.
- **Reset mật khẩu**, **Xem danh sách User** (có tìm kiếm và phân trang).
- **Thông tin chi tiết của User**, **Gán vai trò cho User**.
- Hỗ trợ cả chế độ **Keycloak** và **Self-IDP**.

### 3. Phân Quyền RBAC
- **CRUD quyền**, **CRUD vai trò**, **Gán quyền cho vai trò**, **Gán vai trò cho user**.
- Sử dụng `@PreAuthorize` và `hasPermission()` để xác định quyền truy cập cho từng API.

### 4. Chức năng Xóa Mềm
- Tất cả hành động xóa trong hệ thống đều là **xóa mềm**, sử dụng trường `deleted`.

### 5. AuditorAware
- Ghi nhận thông tin người thực hiện các thay đổi trong hệ thống (auditor).

### 6. Pagination
- Hỗ trợ phân trang cho các API lấy danh sách người dùng, vai trò và quyền.

### 7. Tích hợp Swagger
- Tự động sinh tài liệu API với Swagger, dễ dàng kiểm tra các endpoint.

### 8. Logging
- **Log theo ngày** (log rolling).
- **Log request, response và exception** nhưng **không log thông tin nhạy cảm** như mật khẩu.

### 9. Vai trò Hệ Thống
- **Quản lý người dùng**: Có quyền truy cập các API quản lý người dùng.
- **Quản trị hệ thống**: Có quyền truy cập các API quản lý quyền và vai trò.

### 10. Tính năng nâng cao (Không bắt buộc)
- Thêm chức năng cho phép người dùng **đổi mật khẩu** khi sử dụng Keycloak làm Identity Server.
- **Tích hợp Google SSO**.

## Các công nghệ sử dụng:
- **Spring Boot**: Framework chính để xây dựng ứng dụng.
- **Spring Security**: Quản lý bảo mật và xác thực.
- **Keycloak**: Dùng làm Identity Server.
- **Spring Data JPA**: Quản lý cơ sở dữ liệu.
- **Swagger**: Tự động sinh tài liệu API.
- **H2 Database**: Cơ sở dữ liệu tạm thời (có thể thay bằng cơ sở dữ liệu khác như PostgreSQL).

---

## Cách cài đặt và sử dụng:

1. **Clone repository**:
   ```bash
   git clone https://github.com/your-repository.git
   cd your-repository
