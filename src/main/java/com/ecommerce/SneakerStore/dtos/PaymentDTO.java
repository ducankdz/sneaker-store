package com.ecommerce.SneakerStore.dtos;

import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO implements Serializable {
    private String status;
    private String message;
    private String url;
}
