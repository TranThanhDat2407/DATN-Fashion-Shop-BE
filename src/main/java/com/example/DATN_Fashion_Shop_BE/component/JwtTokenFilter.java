package com.example.DATN_Fashion_Shop_BE.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);


    // kiểm tra
    // mỗi khi một yêu cầu HTTP đi qua filter này.
    // Nó có nhiệm vụ kiểm tra JWT trong tiêu đề của yêu cầu và xác thực người dùng
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws  IOException {
        try {
            //Nếu yêu cầu nằm trong danh sách bypassTokens, bỏ qua bước xác thực JWT và tiếp tục xử lý các filter khác.
            if(isBypassToken(request)) {
            filterChain.doFilter(request, response); //enable bypass
            return;
        }
            //Kiểm tra xem tiêu đề Authorization có tồn tại và bắt đầu bằng chuỗi Bearer hay không.
            //Nếu không, trả về lỗi 401 Unauthorized.
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }
        // cắt chuỗi bearer để lấy token
        final String token = authHeader.substring(7);
        //Sử dụng tiện ích JwtTokenUtil để lấy thông tin email hoặc số điện thoại từ token.
        final String email = jwtTokenUtil.extractEmail(token);
        System.out.println(email);
        //Kiểm tra xem người dùng có tồn tại và chưa được xác thực trong SecurityContextHolder. và có email
        if (!email.isEmpty()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails =  userDetailsService.loadUserByUsername(email);
            System.out.println(userDetails.getUsername());
            //Nếu token hợp lệ:
            //Tạo đối tượng UsernamePasswordAuthenticationToken.
            //Đặt thông tin xác thực vào SecurityContextHolder,
            //giúp các phần tiếp theo trong ứng dụng biết rằng yêu cầu đã được xác thực.
            if(jwtTokenUtil.validateToken(token, userDetails)) {
                // token đã xác thc
                logger.info(email);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,// Principal: Thông tin người dùng
                                null,// Credentials: Không cần mật khẩu do đã xác thực qua validateToken
                                userDetails.getAuthorities()// Authorities: Quyền của người dùng
                        );
                //Lớp này được sử dụng để thêm thông tin bổ sung vào đối tượng xác thực,
                // chẳng hạn như địa chỉ IP hoặc thông tin user agent từ yêu cầu HTTP hiện tại (request).
                // Thông tin này sẽ được lưu trữ trong authenticationToken.
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                /*
                Đây là một công cụ trung tâm của Spring Security để lưu trữ thông tin bảo mật trong ngữ cảnh hiện tại.
                Mục đích: Giữ thông tin xác thực (authentication) trong suốt quá trình xử lý yêu cầu HTTP.
                Nó sử dụng ThreadLocal để lưu dữ liệu, giúp mỗi luồng xử lý một yêu cầu có dữ liệu bảo mật riêng.
                */
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        //Sau khi xử lý, yêu cầu sẽ được truyền đến các filter tiếp theo
            // (hoặc tới endpoint nếu không còn filter nào).
            filterChain.doFilter(request, response); //enable bypass
        }catch (Exception e) {
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }

    }

    //Các endpoint trong danh sách này không yêu cầu xác thực JWT.
    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/roles**", apiPrefix), "GET"),
                Pair.of(String.format("%s/payment**", apiPrefix), "GET"),
                Pair.of(String.format("%s/reviews**", apiPrefix), "GET"),
                Pair.of(String.format("%s/cart**", apiPrefix), "GET"),
                Pair.of(String.format("%s/cart**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/cart**", apiPrefix), "POST"),
                Pair.of(String.format("%s/cart**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/reviews**", apiPrefix), "GET"),
                Pair.of(String.format("%s/products**", apiPrefix), "GET"),
                Pair.of(String.format("%s/products**", apiPrefix), "POST"),
                Pair.of(String.format("%s/products**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/products/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/products/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/products**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/products/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/products/set-categories", apiPrefix), "POST"),
                Pair.of(String.format("%s/products/remove-category", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/categories**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/register/verify**", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/check-email", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/check-phone", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/forgot-password", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/verify-otp", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/reset-password", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/reset-password/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/reset-password-email/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/refreshToken", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/reset-password-email/**", apiPrefix), "POST"),


                Pair.of(String.format("%s/holidays/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/holidays/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/holidays/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/holidays/**", apiPrefix), "PUT"),

                Pair.of(String.format("%s/address/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/address/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/address/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/address/**", apiPrefix), "PUT"),

                Pair.of(String.format("%s/coupons/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/coupons/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/coupons/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/coupons/**", apiPrefix), "PUT"),

                Pair.of(String.format("%s/inventory-transfers/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/inventory-transfers/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/inventory-transfers/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/inventory-transfers/**", apiPrefix), "PUT"),

                Pair.of(String.format("%s/inventory/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/inventory/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/inventory/**", apiPrefix), "PUT"),

                Pair.of(String.format("%s/languages**", apiPrefix), "GET"),
                Pair.of(String.format("%s/healthcheck/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/actuator/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/currencies**", apiPrefix), "GET"),
                Pair.of(String.format("%s/attribute_values/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/attribute_values/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/orders/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/orders/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/orders/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/orders/**", apiPrefix), "PATCH"),
                Pair.of(String.format("%s/attribute_values/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/attribute_values/**", apiPrefix), "DELETE"),

                Pair.of("/uploads/**", "GET"),

                Pair.of(String.format("%s/categories/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/audit/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/audit/**", apiPrefix), "PUT"),
//                Pair.of(String.format("%s/categories/**", apiPrefix), "PUT"),
//                Pair.of(String.format("%s/categories/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/categories/**", apiPrefix), "DELETE"),
                Pair.of(String.format("%s/promotions/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/promotions/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/promotions/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/wishlist/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/wishlist/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/wishlist/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/wishlist/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/banners/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/banners/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/banners/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/banners/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/store/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/store/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/store/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/store/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/store/momo/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/store/momo/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/store/momo/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/store/momo/**", apiPrefix), "DELETE"),


                Pair.of(String.format("%s/momo/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/momo/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/momo/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/momo/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/paypal/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/paypal/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/paypal/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/paypal/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/staff/login", apiPrefix), "POST"),

//                Pair.of(String.format("%s/staff/**", apiPrefix), "GET"),


                Pair.of(String.format("%s/order-details/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/order-details/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/order-details/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/order-details/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/payment/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/payment/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/payment/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/payment/**", apiPrefix), "DELETE"),


                Pair.of(String.format("%s/ghn/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/ghn/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/ghn/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/ghn/**", apiPrefix), "DELETE"),

                Pair.of(String.format("%s/revenue/**", apiPrefix), "GET"),
                Pair.of(String.format("%s/revenue/**", apiPrefix), "POST"),
                Pair.of(String.format("%s/revenue/**", apiPrefix), "PUT"),
                Pair.of(String.format("%s/revenue/**", apiPrefix), "DELETE"),

                // Swagger
                Pair.of("/api-docs","GET"),
                Pair.of("/api-docs/**","GET"),
                Pair.of("/swagger-resources","GET"),
                Pair.of("/swagger-resources/**","GET"),
                Pair.of("/configuration/ui","GET"),
                Pair.of("/configuration/security","GET"),
                Pair.of("/swagger-ui/**","GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET")
        );
        String requestPath = request.getServletPath();
        String requestMethod = request.getMethod();
        logger.debug("Checking request: Path = " + requestPath + ", Method = " + requestMethod);

        if (requestPath.matches("^/api/.+/categories/.+/admin$")
                && requestMethod.equalsIgnoreCase("GET")) {
            logger.debug("Not bypassing: Path matches /api/{version}/categories/{languageCode}/admin");
            return false;  // Không bypass
        }
        for (Pair<String, String> token : bypassTokens) {
            String path = token.getFirst();
            String method = token.getSecond();
            if (requestPath.matches(path.replace("**", ".*"))
                    && requestMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
}
