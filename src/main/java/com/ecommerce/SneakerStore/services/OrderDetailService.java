package com.ecommerce.SneakerStore.services;

import com.ecommerce.SneakerStore.repositories.OrderDetailRepository;
import com.ecommerce.SneakerStore.responses.OrderDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;

    public List<OrderDetailResponse> getOrderDetailsByOrderId(Long id){
        return orderDetailRepository.findByOrderId(id)
                .stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .collect(Collectors.toList());
    }
}
