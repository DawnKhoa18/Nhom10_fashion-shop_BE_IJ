package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // THÊM IMPORT NÀY
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Path UPLOAD_IMAGE_DIR = Paths.get("uploads", "images")
            .toAbsolutePath()
            .normalize();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = UPLOAD_IMAGE_DIR.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }

        // Đường dẫn ảo để React gọi: http://localhost:8080/Images/...
        registry.addResourceHandler("/Images/**")
                // Đường dẫn vật lý: phải khớp 100% với folder resources/static/images/
                .addResourceLocations(uploadLocation, "classpath:/static/images/");
    }

    // CHỈ CHỈNH SỬA THÊM ĐÚNG HÀM NÀY ĐỂ MỞ CỔNG CORS TOÀN CỤC CHO REACT
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cấu hình cho toàn bộ các API (/api/san-pham,...)
                .allowedOrigins("http://localhost:3000") // Cho phép React truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
