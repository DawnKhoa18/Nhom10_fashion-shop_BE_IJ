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
    public ProductDetailResponse getProductDetail(Long id) {

        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return null;

        Long productId = product.getId();

        List<ProductImage> images = productImageRepository.findByProductId(productId);

        List<Integer> nhomKhongCoSize = Arrays.asList(15, 16, 17, 19);

        boolean isCoSize = true;
        if (product.getCategoryId() != null) {
            isCoSize = !nhomKhongCoSize.contains(product.getCategoryId());
        }

        List<String> listMauSac = variantRepository.findDistinctColorsByProductId(productId);

        List<String> listSizeTheoMau = new ArrayList<>();
        if (isCoSize && !listMauSac.isEmpty()) {
            String firstColor = listMauSac.get(0);
            listSizeTheoMau = variantRepository.findDistinctSizesByProductIdAndColor(productId, firstColor);
        }

        List<Product> listSPTuongTu = productRepository.findRelatedProducts(productId, 10);

        return new ProductDetailResponse(product, images, isCoSize, listMauSac, listSizeTheoMau, listSPTuongTu);
    }

    @Override
    public List<String> getSizesByColor(Long productId, String color) {

        return variantRepository.findDistinctSizesByProductIdAndColor(productId, color);
    }
}