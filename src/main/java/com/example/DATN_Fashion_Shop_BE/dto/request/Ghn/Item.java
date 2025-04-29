package com.example.DATN_Fashion_Shop_BE.dto.request.Ghn;

import com.example.DATN_Fashion_Shop_BE.model.Category;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @NotNull
    private String name;
    @NotNull
    private Integer quantity;
    private Integer price;
    private Integer weight;
    private Category category;

}
