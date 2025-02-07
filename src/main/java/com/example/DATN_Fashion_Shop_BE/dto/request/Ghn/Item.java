package com.example.DATN_Fashion_Shop_BE.dto.request.Ghn;

import com.example.DATN_Fashion_Shop_BE.model.Category;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private String name;
    private String code;
    private int quantity;
    private int price;
    private int length;
    private int width;
    private int height;
    private int weight;
    private Category category;

}
