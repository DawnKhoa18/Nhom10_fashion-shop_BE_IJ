package com.example.demo.controller;

import com.example.demo.dto.ProductDetailResponse;
import com.example.demo.dto.ProductListResponse;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.text.Normalizer;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController

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
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "take", defaultValue = "30") int take) {

        List<Product> listAllMatched = new ArrayList<>();
        String titlePage = "Sản phẩm";

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

                String dbBanner = dm.getBanner();
                if (dbBanner != null && !dbBanner.isEmpty()) {

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

        String searchText = normalizeSearchText(keyword);
        boolean hasSearch = !searchText.isEmpty();
        if (hasSearch) {
            listAllMatched = listAllMatched.stream()
                    .filter(product -> containsKeyword(product.getName(), searchText)
                            || containsKeyword(product.getCategory() != null ? product.getCategory().getName() : null, searchText)
                            || containsKeyword(product.getDescription(), searchText)
                            || containsKeyword(product.getMaterial(), searchText)
                            || containsKeyword(product.getForm(), searchText)
                            || String.valueOf(product.getId()).contains(searchText))
                    .collect(Collectors.toList());
            titlePage = "Kết quả tìm kiếm: " + keyword.trim();
            banner = "";
        }

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
                    if (hasSearch) {
                        listAllMatched.sort(Comparator
                                .comparingInt((Product product) -> searchScore(product, searchText))
                                .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                    } else {
                        listAllMatched.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                    }
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

    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable("id") Long id) {
        ProductDetailResponse detail = productService.getProductDetail(id);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/sizes")
    public ResponseEntity<List<String>> getSizesByColor(
            @PathVariable("id") Long productId,
            @RequestParam("color") String color) {
        List<String> sizes = productService.getSizesByColor(productId, color);
        return ResponseEntity.ok(sizes);
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null && normalizeSearchText(value).contains(keyword);
    }

    private String normalizeSearchText(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd');
    }

    private int searchScore(Product product, String keyword) {
        String name = product.getName();
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        if (containsKeyword(name, keyword)) return 0;
        if (containsKeyword(categoryName, keyword)) return 1;
        if (containsKeyword(product.getMaterial(), keyword)) return 2;
        if (containsKeyword(product.getForm(), keyword)) return 3;
        if (containsKeyword(product.getDescription(), keyword)) return 4;
        if (String.valueOf(product.getId()).contains(keyword)) return 5;
        return 99;
    }
}
