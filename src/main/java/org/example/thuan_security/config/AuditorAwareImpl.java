//package org.example.thuan_security.config;
//
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Component
//public class AuditorAwareImpl implements AuditorAware<String> {
//
//    @Override
//    public Optional<String> getCurrentAuditor() {
//        // Trả về tên người dùng hiện tại, có thể lấy từ security context (Spring Security)
//        return Optional.of("current-user"); // hoặc get từ context
//    }
//}