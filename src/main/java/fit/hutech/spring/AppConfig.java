package fit.hutech.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mảng các đường dẫn tiềm năng
        java.util.List<String> locations = new java.util.ArrayList<>();

        // 1. Thử resolve đường dẫn tuyệt đối dynamic
        try {
            // Ưu tiên tìm trong spring/uploads (nếu chạy từ root)
            java.nio.file.Path path = java.nio.file.Paths.get("spring/uploads");
            if (!java.nio.file.Files.exists(path)) {
                // Nếu không thấy, thử tìm uploads (nếu chạy từ spring/)
                path = java.nio.file.Paths.get("uploads");
            }

            // Nếu tìm thấy thư mục tồn tại
            if (java.nio.file.Files.exists(path)) {
                String absolutePath = path.toAbsolutePath().toUri().toString();
                // ResourceLocation cần kết thúc bằng dấu /
                if (!absolutePath.endsWith("/")) {
                    absolutePath += "/";
                }
                System.out.println("=============== LOADED IMAGE PATH: " + absolutePath + " ===============");
                locations.add(absolutePath);
            }
        } catch (Exception e) {
            System.err.println("Error resolving absolute path: " + e.getMessage());
        }

        // 2. Thêm các đường dẫn fallback tương đối
        locations.add("file:spring/uploads/");
        locations.add("file:uploads/");

        registry.addResourceHandler("/images/books/**")
                .addResourceLocations(locations.toArray(new String[0]));
    }
}