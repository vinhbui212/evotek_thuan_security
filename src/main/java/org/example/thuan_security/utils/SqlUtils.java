package org.example.thuan_security.utils;

public class SqlUtils {

    // Phương thức encode cho từ khóa tìm kiếm
    public static String encodeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        // Làm sạch từ khóa để tránh các ký tự đặc biệt gây vấn đề trong SQL
        keyword = keyword.replaceAll("([%_])", "\\\\$1"); // Escape các ký tự %, _
        return "%" + keyword.toLowerCase() + "%";  // Thêm dấu % để tìm kiếm kiểu LIKE
    }
}

