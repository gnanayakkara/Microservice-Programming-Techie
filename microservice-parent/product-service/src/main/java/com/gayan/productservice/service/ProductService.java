package com.gayan.productservice.service;

import com.gayan.productservice.dto.ProductRequest;
import com.gayan.productservice.dto.ProductResponse;
import com.gayan.productservice.model.Product;
import com.gayan.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest){

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product {} is saved",product.getId());
    }

    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductReponse).toList();
    }

    private ProductResponse mapToProductReponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getName())
                .price(product.getPrice())
                .build();
    }
}
