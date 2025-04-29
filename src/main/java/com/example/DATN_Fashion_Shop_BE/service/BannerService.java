package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.banner.BannerCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.BannerRepository;
import com.example.DATN_Fashion_Shop_BE.repository.BannerTranslationRepository;
import com.example.DATN_Fashion_Shop_BE.repository.LanguageRepository;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BannerService {
    private BannerRepository bannerRepository;

    private BannerTranslationRepository bannersTranslationRepository;

    private LanguageRepository languageRepository;

    private FileStorageService fileStorageService;

    private final LocalizationUtils localizationUtils;

    public List<BannerDTO> getBannersWithTranslations(String languageCode, Boolean isActive) {
        // Lấy danh sách banner theo điều kiện isActive
        List<Banner> banners;
        if (isActive != null) {
            banners = bannerRepository.findByIsActive(isActive);
        } else {
            banners = bannerRepository.findAll();
        }

        // Lấy danh sách ID banner
        List<Long> bannerIds = banners.stream()
                .map(Banner::getId)
                .collect(Collectors.toList());

        // Lấy bản dịch theo danh sách ID và languageCode
        List<BannersTranslation> translations = bannersTranslationRepository
                .findByBannerIdInAndLanguageCode(bannerIds, languageCode);

        // Tạo map từ ID banner -> bản dịch
        Map<Long, BannersTranslation> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        translation -> translation.getBanner().getId(),
                        translation -> translation
                ));

        // Sử dụng phương thức fromBanner để ánh xạ
        return banners.stream()
                .map(banner -> BannerDTO.fromBanner(banner, translationMap.get(banner.getId())))
                .collect(Collectors.toList());
    }

    public BannerEditResponseDTO getBannerForEdit(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_FAILED, id))
                );

        List<BannersTranslation> translations = bannersTranslationRepository.findByBannerId(id);

        return BannerEditResponseDTO.fromBanner(banner, translations);
    }

    @Transactional
    public BannerEditResponseDTO createBanner(BannerCreateRequestDTO request,
                                              MultipartFile logoFile,
                                              MultipartFile videoFile) {
        String logoName = null;
        String mediaName = null;

        // Kiểm tra nếu logoFile không null, sau đó upload logo
        if (logoFile != null && !logoFile.isEmpty()) {
            logoName = fileStorageService.uploadFileAndGetName(logoFile, "banners/logo_images");
        }

        // Kiểm tra nếu videoFile không null, sau đó upload video
        if (videoFile != null && !videoFile.isEmpty()) {
            mediaName = fileStorageService.uploadFileAndGetName(videoFile, "banners/media");
        }

        // 2. Tạo Banner
        Banner banner = new Banner();
        banner.setLogoUrl(logoName);  // Nếu logoFile là null, logoName vẫn là null
        banner.setMediaUrl(mediaName); // Nếu videoFile là null, mediaName vẫn là null
        banner.setRedirectUrl(request.getRedirectURL());
        banner.setIsActive(request.getIsActive());
        banner = bannerRepository.save(banner);

        // Tạo biến final cho banner
        final Banner savedBanner = banner;

        // 3. Lưu các bản dịch
        List<BannersTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    // Tạo bản dịch cho banner
                    BannersTranslation translation = new BannersTranslation();
                    translation.setTitle(translationDTO.getTitle());
                    translation.setSubtitle(translationDTO.getSubtitle());
                    translation.setBanner(savedBanner);
                    translation.setLanguage(language);
                    return translation;
                })
                .collect(Collectors.toList());
        bannersTranslationRepository.saveAll(translations);

        // 4. Trả về DTO
        return BannerEditResponseDTO.fromBanner(savedBanner, translations);
    }

    @Transactional
    public BannerEditResponseDTO updateBanner(Long bannerId,
                                              BannerCreateRequestDTO request,
                                              MultipartFile logoFile,
                                              MultipartFile mediaFile) {

        // 1. Kiểm tra banner tồn tại
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.BANNER_RETRIEVED_FAILED, bannerId)));

        // 2. Cập nhật các trường thông tin
        if (logoFile != null) {
            String logoUrl = fileStorageService.uploadFileAndGetName(logoFile, "banners/logo_images");
            banner.setLogoUrl(logoUrl);
        }

        if (mediaFile != null) {
            String mediaUrl = fileStorageService.uploadFileAndGetName(mediaFile, "banners/media");
            banner.setMediaUrl(mediaUrl);
        }

        banner.setRedirectUrl(request.getRedirectURL());
        banner.setIsActive(request.getIsActive());

        bannerRepository.save(banner);

        // 4. Cập nhật các bản dịch
        List<BannersTranslation> existingTranslations = bannersTranslationRepository.findByBannerId(bannerId);
        bannersTranslationRepository.deleteAll(existingTranslations);

        List<BannersTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException(localizationUtils
                                    .getLocalizedMessage(MessageKeys.LANGUAGE_RETRIEVED_FAILED, bannerId)));

                    BannersTranslation translation = new BannersTranslation();
                    translation.setTitle(translationDTO.getTitle());
                    translation.setSubtitle(translationDTO.getSubtitle());
                    translation.setBanner(banner);
                    translation.setLanguage(language);
                    return translation;
                })
                .collect(Collectors.toList());
        bannersTranslationRepository.saveAll(translations);

        // 5. Trả về DTO với thông tin đã cập nhật
        return BannerEditResponseDTO.fromBanner(banner, translations);
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found with ID: " + bannerId));

        // Xóa banner (cascade sẽ tự động xóa các translations liên quan)
        bannerRepository.delete(banner);
    }

    public Page<BannerAdminResponseDTO> getBannerPage(int page, int size, String title, Boolean isActive, String languageCode, String sortBy, String sortDir) {
        // Set default sorting direction to ascending if not provided
        Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Build sorting based on the provided sort criteria
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "id"); // Default sorting by ID

        // Build pagination
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Apply filters and fetch banner page
        Page<Banner> bannersPage = bannerRepository.findBannersByFillers(title, isActive, pageRequest);

        // Map banners to DTO
        Page<BannerAdminResponseDTO> bannerDTOPage = bannersPage.map(banner -> {
            // Fetch translation based on the provided language code
            BannersTranslation bannerTranslation = bannersTranslationRepository
                    .findByBannerIdAndLanguageCode(banner.getId(), languageCode)
                    .orElseThrow(() -> new RuntimeException("Translation not found for banner ID: " + banner.getId() + " and language: " + languageCode));

            // Build the BannerAdminResponseDTO with translated fields
            return BannerAdminResponseDTO.fromBanner(banner, bannerTranslation);
        });

        return bannerDTOPage;
    }

    public BannerAdminResponseDTO scheduleBanner(Long bannerId, LocalDateTime activationDate, LocalDateTime endDate) {
        // Lấy banner từ database
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found with ID: " + bannerId));

        // Cập nhật ngày giờ kích hoạt và kết thúc cho banner
        banner.setActivationDate(activationDate);
        banner.setEndDate(endDate);
        bannerRepository.save(banner); // Lưu lại thay đổi

        // Lấy bản dịch của banner
        BannersTranslation bannerTranslation = bannersTranslationRepository
                .findByBannerIdAndLanguageCode(bannerId, "en") // Giả sử chúng ta chỉ lấy bản dịch tiếng Anh
                .orElseThrow(() -> new RuntimeException("Banner translation not found"));

        // Trả về DTO chứa thông tin banner đã cập nhật
        return BannerAdminResponseDTO.fromBanner(banner, bannerTranslation);
    }

    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 30 2 * * ?")  // Chạy vào lúc 2h30 sáng mỗi ngày
    public void checkAndUpdateBannerStatus() {
        // Lấy danh sách tất cả các banner
        List<Banner> banners = bannerRepository.findAll();

        // Duyệt qua các banner và kiểm tra ngày kết thúc
        for (Banner banner : banners) {
            if (banner.getEndDate() != null && banner.getEndDate().isBefore(LocalDateTime.now())) {
                // Nếu ngày kết thúc đã qua, cập nhật trạng thái thành false
                banner.setIsActive(false);
                bannerRepository.save(banner);  // Lưu cập nhật vào cơ sở dữ liệu
            }
        }
    }
}
