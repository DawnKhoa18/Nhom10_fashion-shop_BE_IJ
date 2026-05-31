package com.example.demo.controller;

import com.example.demo.dto.ProductDetailResponse; // THÊM ĐỂ ĐÓNG GÓI CHI TIẾT
import com.example.demo.dto.ProductListResponse;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService; // THÊM IMPORT SERVICE MỚI
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
// CHỈ CHỈNH SỬA TẠI ĐÂY: Hỗ trợ cả /api/SanPham và /api/san-pham để khớp chuẩn URL từ React
@RequestMapping({"/api/SanPham", "/api/san-pham"})
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/GetHangMoi")
    public List<Product> getHangMoi() {
        return productRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @GetMapping("/GetHangBanChay")
    public List<Product> getHangBanChay() {
        return productRepository.findBestSellers(PageRequest.of(0, 5));
    }

    private List<Integer> getCategoryFamilyIds(Integer parentId) {
        List<Integer> ids = categoryRepository.findByParentId(parentId)
                .stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        ids.add(parentId);
        return ids;
    }

    @GetMapping("/GetDanhSachAo")
    public List<Product> getAo() {
        return productRepository.findTop5ByCategoryIdInOrderByCreatedAtDesc(getCategoryFamilyIds(1));
    }

    @GetMapping("/GetDanhSachQuan")
    public List<Product> getQuan() {
        return productRepository.findTop5ByCategoryIdInOrderByCreatedAtDesc(getCategoryFamilyIds(2));
    }

    @GetMapping("/GetDanhSachPhuKien")
    public List<Product> getPhuKien() {
        return productRepository.findTop5ByCategoryIdInOrderByCreatedAtDesc(getCategoryFamilyIds(3));
    }

    @GetMapping("/danh-sach")
    public ResponseEntity<ProductListResponse> getDanhSachSanPham(
            @RequestParam(value = "slug", defaultValue = "tat-ca") String slug,
            @RequestParam(value = "sort", defaultValue = "default") String sort,
            @RequestParam(value = "take", defaultValue = "30") int take) {

        List<Product> listAllMatched = new ArrayList<>();
        String titlePage = "Sản phẩm";
        // Banner mặc định cho toàn trang
        String banner = "/Images/Banners/banner-tatca.jpg";

        if ("tat-ca".equals(slug)) {
            listAllMatched = productRepository.findAll();
            titlePage = "Tất cả sản phẩm";
            banner = "/Images/Banners/tat-ca-san-pham-banner.jpg";
        }
        else if ("hang-moi".equals(slug)) {
            listAllMatched = productRepository.findAll();
            titlePage = "Hàng mới";
            banner = "/Images/Banners/hang-moi-banner.jpg";
        }
        else if ("hang-ban-chay".equals(slug)) {
            listAllMatched = productRepository.findAllBestSellers();
            titlePage = "Hàng bán chạy";
            banner = "/Images/Banners/hang-ban-chay-banner.jpg";
        }
        else {
            Category dm = categoryRepository.findBySlug(slug).orElse(null);

            if (dm != null) {
                titlePage = dm.getName();

                // CHỈNH SỬA LOGIC BANNER TẠI ĐÂY
                String dbBanner = dm.getBanner();
                if (dbBanner != null && !dbBanner.isEmpty()) {
                    // Nếu dữ liệu trong DB đã có đường dẫn /Images/... thì lấy luôn
                    // Nếu chỉ có tên file (vd: hoodie.jpg) thì tự nối path vào cho khớp thư mục tĩnh
                    if (dbBanner.startsWith("/Images/")) {
                        banner = dbBanner;
                    } else {
                        banner = "/Images/Banners/" + dbBanner;
                    }
                }

                List<Integer> targetIds = getCategoryFamilyIds(dm.getId());
                listAllMatched = productRepository.findByCategoryIdIn(targetIds);
            }
        }

        // Logic Sắp xếp
        if (listAllMatched != null && !listAllMatched.isEmpty()) {
            java.text.Collator collator = java.text.Collator.getInstance(new java.util.Locale("vi", "VN"));
            switch (sort) {
                case "price_asc":
                    listAllMatched.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price_desc":
                    listAllMatched.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "name_asc":
                    listAllMatched.sort((p1, p2) -> collator.compare(p1.getName(), p2.getName()));
                    break;
                case "name_desc":
                    listAllMatched.sort((p1, p2) -> collator.compare(p2.getName(), p1.getName()));
                    break;
                case "newest":
                    listAllMatched.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                    break;
                case "oldest":
                    listAllMatched.sort(Comparator.comparing(Product::getCreatedAt));
                    break;
                case "best_selling":
                    listAllMatched = productRepository.findAllBestSellers();
                    break;
                default:
                    listAllMatched.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                    break;
            }
        }

        int totalProducts = listAllMatched != null ? listAllMatched.size() : 0;
        boolean hienXemThem = take < totalProducts;

        List<Product> finalPagedList = listAllMatched != null ? listAllMatched.stream()
                                                                .limit(take)
                                                                .collect(Collectors.toList()) : new ArrayList<>();

        ProductListResponse response = new ProductListResponse(finalPagedList, titlePage, banner, hienXemThem);
        return ResponseEntity.ok(response);
    }

    // ================= CHỈ CHỈNH SỬA ĐÚNG ĐOẠN API DƯỚI ĐÂY ĐỂ ĐÓN NHẬN ID TỪ FRONTEND =================

    // API 1: CHỈNH SỬA: Đổi từ /detail/{slug} thành /detail/{id} để nhận productId kiểu số
    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable("id") Long id) {
        ProductDetailResponse detail = productService.getProductDetail(id);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // API 2: Lấy danh sách size thay đổi động khi chọn màu bên Frontend (GIỮ NGUYÊN)
    @GetMapping("/{id}/sizes")
    public ResponseEntity<List<String>> getSizesByColor(
            @PathVariable("id") Long productId,
            @RequestParam("color") String color) {
        List<String> sizes = productService.getSizesByColor(productId, color);
        return ResponseEntity.ok(sizes);
    }
}