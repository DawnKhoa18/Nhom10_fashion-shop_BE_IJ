package com.example.demo.service;

import com.example.demo.dto.ProductDetailResponse;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Override
    public ProductDetailResponse getProductDetail(Long id) { // CHỈNH SỬA: Đổi tham số nhận vào thành Long id
        // 1. THAY ĐỔI: Tìm theo findById mặc định của JPA thay vì findByName cũ dựa trên chuỗi chữ
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;

        // Lấy ra productId trực tiếp từ object product vừa tìm thấy để chạy tiếp logic bên dưới ổn định
        Long productId = product.getId();

        // 2. Lấy danh sách ảnh phụ của sản phẩm (GIỮ NGUYÊN)
        List<ProductImage> images = productImageRepository.findByProductId(productId);

        // 3. Kiểm tra sản phẩm có size hay không dựa vào MaDanhMuc (categoryId) từ bảng DanhMuc của ông (GIỮ NGUYÊN)
        List<Integer> nhomKhongCoSize = Arrays.asList(15, 16, 17, 19);

        boolean isCoSize = true;
        if (product.getCategoryId() != null) {
            isCoSize = !nhomKhongCoSize.contains(product.getCategoryId());
        }

        // 4. Lấy danh sách màu sắc duy nhất từ các biến thể (GIỮ NGUYÊN)
        List<String> listMauSac = variantRepository.findDistinctColorsByProductId(productId);

        // 5. Lấy danh sách size mặc định theo màu đầu tiên (nếu sản phẩm có hỗ trợ size) (GIỮ NGUYÊN)
        List<String> listSizeTheoMau = new ArrayList<>();
        if (isCoSize && !listMauSac.isEmpty()) {
            String firstColor = listMauSac.get(0);
            listSizeTheoMau = variantRepository.findDistinctSizesByProductIdAndColor(productId, firstColor);
        }

        // 6. Lấy danh sách sản phẩm tương tự (Gọi qua Store Procedure đã cấu hình) (GIỮ NGUYÊN)
        List<Product> listSPTuongTu = productRepository.findRelatedProducts(productId, 10);

        // Đóng gói toàn bộ vào DTO để trả về cho Frontend React (GIỮ NGUYÊN)
        return new ProductDetailResponse(product, images, isCoSize, listMauSac, listSizeTheoMau, listSPTuongTu);
    }

    @Override
    public List<String> getSizesByColor(Long productId, String color) {
        // Hàm này phục vụ việc khi React click đổi màu, gọi API để lấy lại list size tương ứng của màu đó (GIỮ NGUYÊN)
        return variantRepository.findDistinctSizesByProductIdAndColor(productId, color);
    }
}