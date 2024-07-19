package com.ecommerce.SneakerStore.responses;

import jakarta.persistence.MappedSuperclass;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseResponse {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
