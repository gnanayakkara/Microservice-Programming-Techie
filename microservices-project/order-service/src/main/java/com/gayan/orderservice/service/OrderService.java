package com.gayan.orderservice.service;

import com.gayan.orderservice.dto.InventoryResponse;
import com.gayan.orderservice.dto.OrderLineItemsDto;
import com.gayan.orderservice.dto.OrderRequest;
import com.gayan.orderservice.model.Order;
import com.gayan.orderservice.model.OrderLineItems;
import com.gayan.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) throws IllegalAccessException {

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItems(orderLineItems);

        List<String> skuCodes = order.getOrderLineItems().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //call Inventory Service and place order if product is in
        InventoryResponse[] inventoryResponsArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        Boolean isAllProductsInStock = Arrays.stream(inventoryResponsArray).allMatch(InventoryResponse::isInStock);

        if(isAllProductsInStock){
            orderRepository.save(order);
        }else{
            throw new IllegalAccessException("Product is not in stock,please try again later");
        }



    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

        OrderLineItems orderLineItems = OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();

        return orderLineItems;
    }
}
