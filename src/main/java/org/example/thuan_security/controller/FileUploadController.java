package org.example.thuan_security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.thuan_security.model.Users;
import org.example.thuan_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {


    private static String uploadDir ="/uploads/" ;
    private final UserRepository userRepository;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file, @RequestParam("email") String email) {
        try {
            // Kiểm tra và khởi tạo thư mục uploadDir
            if (uploadDir == null || uploadDir.isEmpty()) {
                throw new IllegalArgumentException("Đường dẫn uploadDir không được để trống");
            }

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Không thể tạo thư mục uploadDir");
                }
            }

            // Tìm người dùng theo email
            Users user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng với email: " + email);
            }

            // Xóa file cũ nếu tồn tại
            if (user.getImage_url() != null) {
                String oldFilePath = user.getImage_url().replace("http://localhost:8080/api/files/images/", uploadDir);
                File oldFile = new File(oldFilePath);
                if (oldFile.exists()) {
                    if (!oldFile.delete()) {
                        log.warn("Không thể xóa file cũ: " + oldFilePath);
                    }
                }
            }

            // Lưu file mới
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, file.getBytes());
            log.info("File saved at: " + filePath.toString());

            // Cập nhật URL ảnh
            String imageUrl = "http://localhost:8080/api/files/images/" + fileName;
            user.setImage_url(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok("File uploaded and updated successfully: " + imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể tải ảnh lên");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }


    //    @GetMapping("/image")
//    public String getImageByEmail(@RequestParam String email){
//        Users users= userRepository.findByEmail(email);
//        return users.getImage_url();
//    }
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir + filename);
            byte[] image = Files.readAllBytes(filePath);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}

