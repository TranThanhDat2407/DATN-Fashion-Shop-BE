package com.example.DATN_Fashion_Shop_BE.dto.request.banner;

import com.example.DATN_Fashion_Shop_BE.dto.BannerTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BannerCreateRequestDTO {
    private String mediaURL;
    private String redirectURL;
    private String logoURL;
    private Boolean isActive;

    @NotEmpty(message = MessageKeys.INSERT_CATEGORY_EMPTY_TRANS)
    @Valid
    private List<BannerTranslationDTO> translations; // Danh sách bản dịch
}
