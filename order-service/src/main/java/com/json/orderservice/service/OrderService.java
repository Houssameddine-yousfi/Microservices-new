package com.json.orderservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.json.orderservice.dto.InventoryResponce;
import com.json.orderservice.dto.OrderLineItemsDto;
import com.json.orderservice.dto.OrderRequest;
import com.json.orderservice.model.Order;
import com.json.orderservice.model.OrderLineItems;
import com.json.orderservice.repository.OrderRepository;

import brave.Span;
import brave.Tracer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                    .stream()
                    .map(this::mapFromDto)
                    .toList();
        
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(
            orderLineItem -> orderLineItem.getSkuCode()
        ).toList();


        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpanInScope(inventoryServiceLookup.start())) {
            // check availability in inventory service
            InventoryResponce[] inventoryResponces = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponce[].class)
                    .block();

            boolean allProductInStock = Arrays.stream(inventoryResponces).allMatch(InventoryResponce::isInStock);

            if (allProductInStock) {
                orderRepository.save(order);
                return "Order placed succesfully";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try later");
            }
        } finally {
            inventoryServiceLookup.finish();
        }
        
    }

    private OrderLineItems mapFromDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
