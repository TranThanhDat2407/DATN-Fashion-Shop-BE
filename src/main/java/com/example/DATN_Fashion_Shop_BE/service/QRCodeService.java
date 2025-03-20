package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.TotalWishlistResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.WishlistItemResponse;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.model.WishList;
import com.example.DATN_Fashion_Shop_BE.model.WishListItem;
import com.example.DATN_Fashion_Shop_BE.repository.ProductVariantRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import com.example.DATN_Fashion_Shop_BE.repository.WishlistItemRepository;
import com.example.DATN_Fashion_Shop_BE.repository.WishlistRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    public BufferedImage generateQRCode(String orderId) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(orderId, BarcodeFormat.QR_CODE, 200, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
