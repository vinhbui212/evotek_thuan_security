IAM Service 2 - Spring Boot Application
Mô tả
IAM Service 2 là một hệ thống quản lý người dùng (Identity and Access Management) phát triển bằng Spring Boot, cung cấp các tính năng quản lý người dùng và phân quyền, tích hợp với Keycloak như một Identity Server và hỗ trợ RBAC (Role-Based Access Control). Hệ thống này hỗ trợ cả chế độ self-idp (quản lý người dùng qua cơ sở dữ liệu nội bộ) và Keycloak làm Identity Provider (IdP).

Các tính năng chính:
Tích hợp Keycloak như Identity Server

Cho phép bật/tắt sử dụng Keycloak qua cấu hình.
Xác thực Request: Kiểm tra người dùng qua access token của Keycloak. Nếu hợp lệ, cho phép truy cập hệ thống.
Đăng ký người dùng: Tạo tài khoản trên Keycloak và đồng bộ với cơ sở dữ liệu nội bộ.
Đăng xuất: Tích hợp API logout của Keycloak.
Refresh Token: Làm mới token từ Keycloak khi hết hạn.
Self-IDP: Nếu tắt Keycloak, hệ thống sẽ sử dụng thông tin người dùng từ cơ sở dữ liệu nội bộ.
API Quản lý Người Dùng

Tạo User, Xóa mềm User, Khóa/Mở khóa User.
Reset mật khẩu, Xem danh sách User (có tìm kiếm và phân trang).
Thông tin chi tiết của User, Gán vai trò cho User.
Hỗ trợ cả chế độ Keycloak và Self-IDP.
Phân Quyền RBAC

CRUD quyền, CRUD vai trò, Gán quyền cho vai trò, Gán vai trò cho user.
Sử dụng @PreAuthorize và hasPermission() để xác định quyền truy cập cho từng API.
Chức năng Xóa Mềm

Tất cả hành động xóa trong hệ thống đều là xóa mềm, sử dụng trường deleted.
AuditorAware

Ghi nhận thông tin người thực hiện các thay đổi trong hệ thống (auditor).
Pagination

Hỗ trợ phân trang cho các API lấy danh sách người dùng, vai trò và quyền.
Tích hợp Swagger

Tự động sinh tài liệu API với Swagger, dễ dàng kiểm tra các endpoint.
Logging

Log theo ngày (log rolling).
Log request, response và exception nhưng không log thông tin nhạy cảm như mật khẩu.
Vai trò Hệ Thống

Quản lý người dùng: Có quyền truy cập các API quản lý người dùng.
Quản trị hệ thống: Có quyền truy cập các API quản lý quyền và vai trò.
Tính năng nâng cao (Không bắt buộc)

Thêm chức năng cho phép người dùng đổi mật khẩu khi sử dụng Keycloak làm Identity Server.
Tích hợp Google SSO.
Các công nghệ sử dụng:
Spring Boot: Framework chính để xây dựng ứng dụng.
Spring Security: Quản lý bảo mật và xác thực.
Keycloak: Dùng làm Identity Server.
Spring Data JPA: Quản lý cơ sở dữ liệu.
Swagger: Tự động sinh tài liệu API.
PostgreSQLDatabase: Cơ sở dữ liệu 
