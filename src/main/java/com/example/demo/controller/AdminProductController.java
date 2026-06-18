package com.example.demo.controller;

import com.example.demo.dto.AdminProductResponse;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.ProductVariant;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private static final Path PRODUCT_IMAGE_DIR = Paths.get("uploads", "images", "Products")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @GetMapping
    public List<AdminProductResponse> getAdminProducts() {
        return productRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toAdminProductResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductResponse> getAdminProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(toAdminProductResponse(product)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createProduct(
            @RequestParam("tenSP") String tenSP,
            @RequestParam("maDanhMuc") Integer maDanhMuc,
            @RequestParam("gia") BigDecimal gia,
            @RequestParam("soLuongTon") Integer soLuongTon,
            @RequestParam(value = "chatLieu", required = false) String chatLieu,
            @RequestParam(value = "form", required = false) String form,
            @RequestParam(value = "moTaChiTiet", required = false) String moTaChiTiet,
            @RequestParam("hienThi") Boolean hienThi,
            @RequestParam("hinhDaiDien") MultipartFile hinhDaiDien,
            @RequestParam(value = "hinhHover", required = false) MultipartFile hinhHover,
            @RequestParam(value = "hinhPhu1", required = false) MultipartFile hinhPhu1,
            @RequestParam(value = "hinhPhu2", required = false) MultipartFile hinhPhu2,
            @RequestParam(value = "hinhPhu3", required = false) MultipartFile hinhPhu3
    ) throws IOException {
        if (hinhDaiDien == null || hinhDaiDien.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ảnh đại diện là bắt buộc."));
        }

        Product product = new Product();
        product.setName(tenSP);
        product.setCategoryId(maDanhMuc);
        product.setPrice(gia);
        product.setMaterial(chatLieu);
        product.setForm(form);
        product.setDescription(moTaChiTiet);
        product.setIsVisible(hienThi);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setThumbnail(saveImage(hinhDaiDien));
        Product savedProduct = productRepository.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProductId(savedProduct.getId());
        variant.setSize("Free");
        variant.setColor("Default");
        variant.setPrice(gia);
        variant.setStockQuantity(soLuongTon);
        variant.setCreatedAt(LocalDateTime.now());
        productVariantRepository.save(variant);

        saveProductImage(savedProduct.getId(), hinhHover, 0);
        saveProductImage(savedProduct.getId(), hinhPhu1, 1);
        saveProductImage(savedProduct.getId(), hinhPhu2, 2);
        saveProductImage(savedProduct.getId(), hinhPhu3, 3);

        return ResponseEntity.status(HttpStatus.CREATED).body(toAdminProductResponse(savedProduct));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("tenSP") String tenSP,
            @RequestParam("maDanhMuc") Integer maDanhMuc,
            @RequestParam("gia") BigDecimal gia,
            @RequestParam("soLuongTon") Integer soLuongTon,
            @RequestParam(value = "chatLieu", required = false) String chatLieu,
            @RequestParam(value = "form", required = false) String form,
            @RequestParam(value = "moTaChiTiet", required = false) String moTaChiTiet,
            @RequestParam("hienThi") Boolean hienThi,
            @RequestParam(value = "hinhDaiDien", required = false) MultipartFile hinhDaiDien,
            @RequestParam(value = "hinhHover", required = false) MultipartFile hinhHover,
            @RequestParam(value = "hinhPhu1", required = false) MultipartFile hinhPhu1,
            @RequestParam(value = "hinhPhu2", required = false) MultipartFile hinhPhu2,
            @RequestParam(value = "hinhPhu3", required = false) MultipartFile hinhPhu3
    ) throws IOException {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        product.setName(tenSP);
        product.setCategoryId(maDanhMuc);
        product.setPrice(gia);
        product.setMaterial(chatLieu);
        product.setForm(form);
        product.setDescription(moTaChiTiet);
        product.setIsVisible(hienThi);
        product.setUpdatedAt(LocalDateTime.now());
        if (hinhDaiDien != null && !hinhDaiDien.isEmpty()) {
            product.setThumbnail(saveImage(hinhDaiDien));
        }
        Product savedProduct = productRepository.save(product);

        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        ProductVariant variant = variants.stream().findFirst().orElseGet(ProductVariant::new);
        if (variant.getProductId() == null) {
            variant.setProductId(id);
            variant.setSize("Free");
            variant.setColor("Default");
            variant.setCreatedAt(LocalDateTime.now());
        }
        variant.setPrice(gia);
        variant.setStockQuantity(soLuongTon);
        productVariantRepository.save(variant);

        replaceProductImage(id, hinhHover, 0);
        replaceProductImage(id, hinhPhu1, 1);
        replaceProductImage(id, hinhPhu2, 2);
        replaceProductImage(id, hinhPhu3, 3);

        return ResponseEntity.ok(toAdminProductResponse(savedProduct));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            productImageRepository.deleteByProductId(id);
            productVariantRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Không thể xóa sản phẩm vì đã phát sinh đơn hàng hoặc dữ liệu liên quan."));
        }
    }

    private AdminProductResponse toAdminProductResponse(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        ProductVariant firstVariant = variants.stream().findFirst().orElse(null);

        int totalStock = variants.stream()
                .map(ProductVariant::getStockQuantity)
                .filter(stock -> stock != null)
                .mapToInt(Integer::intValue)
                .sum();

        String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Không có";
        String color = firstVariant != null && firstVariant.getColor() != null ? firstVariant.getColor() : "-";
        String size = firstVariant != null && firstVariant.getSize() != null ? firstVariant.getSize() : "-";

        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                categoryName,
                product.getPrice(),
                Boolean.TRUE.equals(product.getIsVisible()),
                product.getCreatedAt(),
                color,
                size,
                totalStock,
                product.getThumbnail(),
                product.getCategoryId(),
                product.getMaterial(),
                product.getForm(),
                product.getDescription()
        );
    }

    private String saveImage(MultipartFile file) throws IOException {
        Files.createDirectories(PRODUCT_IMAGE_DIR);
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String fileName = UUID.randomUUID() + extension;
        Files.copy(file.getInputStream(), PRODUCT_IMAGE_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void saveProductImage(Long productId, MultipartFile file, int displayOrder) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImageName(saveImage(file));
        image.setDisplayOrder(displayOrder);
        productImageRepository.save(image);
    }

    private void replaceProductImage(Long productId, MultipartFile file, int displayOrder) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        List<ProductImage> images = productImageRepository.findByProductId(productId);
        ProductImage image = images.stream()
                .filter(item -> item.getDisplayOrder() != null && item.getDisplayOrder() == displayOrder)
                .findFirst()
                .orElseGet(ProductImage::new);

        image.setProductId(productId);
        image.setImageName(saveImage(file));
        image.setDisplayOrder(displayOrder);
        productImageRepository.save(image);
    }
}
