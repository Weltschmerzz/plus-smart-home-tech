package ru.yandex.practicum.commerce.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.api.enumtype.ProductCategory;
import ru.yandex.practicum.commerce.api.enumtype.ProductState;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.mapper.ProductMapper;
import ru.yandex.practicum.commerce.store.model.Product;
import ru.yandex.practicum.commerce.store.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProducts(ProductCategory category,
                                        Integer page,
                                        Integer size,
                                        List<String> sort) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        return productRepository
                .findAllByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable)
                .map(productMapper::toDto);
    }

    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);

        product.setProductId(UUID.randomUUID());

        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        UUID productId = productDto.getProductId();

        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        existing.setProductName(productDto.getProductName());
        existing.setDescription(productDto.getDescription());
        existing.setImageSrc(productDto.getImageSrc());
        existing.setQuantityState(productDto.getQuantityState());
        existing.setProductState(productDto.getProductState());
        existing.setProductCategory(productDto.getProductCategory());
        existing.setPrice(productDto.getPrice());

        Product saved = productRepository.save(existing);
        return productMapper.toDto(saved);
    }

    @Transactional
    public Boolean removeProductFromStore(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);

        return true;
    }

    @Transactional
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);

        return true;
    }

    public ProductDto getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return productMapper.toDto(product);
    }

    private Sort parseSort(List<String> sortValues) {
        if (sortValues == null || sortValues.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sortValues.stream()
                .map(this::parseOrder)
                .toList();

        return Sort.by(orders);
    }

    private Sort.Order parseOrder(String sortValue) {
        String[] parts = sortValue.split(",");

        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1].trim())
                : Sort.Direction.ASC;

        return new Sort.Order(direction, property);
    }
}