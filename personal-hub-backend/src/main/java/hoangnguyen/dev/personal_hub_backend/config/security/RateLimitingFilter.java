//package hoangnguyen.dev.personal_hub_backend.config.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import hoangnguyen.dev.personal_hub_backend.dto.response.ApiErrorResponse;
//import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletOutputStream;
//import jakarta.servlet.WriteListener;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpServletResponseWrapper;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class RateLimitingFilter extends OncePerRequestFilter {
//    private final Map<String, AtomicInteger> attemptsByIp = new ConcurrentHashMap<>();
//    private final Map<String, Long> blockExpiryByIp = new ConcurrentHashMap<>();
//
//    private static final int MAX_ATTEMPTS = 5;
//    private static final long BLOCK_DURATION_MS = 300000; // 5 phút
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // Chỉ áp dụng cho các API đăng nhập
//        if (request.getRequestURI().equals("/api/auth/login") && "POST".equals(request.getMethod())) {
//            String ipAddress = getClientIP(request);
//
//            // Kiểm tra xem IP có bị chặn không
//            if (isBlocked(ipAddress)) {
//                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//                response.setContentType("application/json");
//
//                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                        .code(ErrorCodeEnum.TOO_MANY_ATTEMPTS.getCode())
//                        .message(ErrorCodeEnum.TOO_MANY_ATTEMPTS.getMessage())
//                        .path(request.getRequestURI())
//                        .timestamp(LocalDateTime.now())
//                        .build();
//
//                response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
//                return;
//            }
//
//            // Wrap the response to track authentication failures
//            ResponseWrapper responseWrapper = new ResponseWrapper(response);
//
//            filterChain.doFilter(request, responseWrapper);
//
//            // Check if login failed (unauthorized status)
//            if (responseWrapper.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
//                AtomicInteger attempts = attemptsByIp.computeIfAbsent(ipAddress, k -> new AtomicInteger(0));
//                int currentAttempts = attempts.incrementAndGet();
//
//                if (currentAttempts >= MAX_ATTEMPTS) {
//                    blockExpiryByIp.put(ipAddress, System.currentTimeMillis() + BLOCK_DURATION_MS);
//                }
//            } else if (responseWrapper.getStatus() == HttpStatus.OK.value()) {
//                // Nếu đăng nhập thành công, reset số lần thử
//                attemptsByIp.remove(ipAddress);
//            }
//
//            // Copy content to original response
//            response.getOutputStream().write(responseWrapper.getContentAsByteArray());
//        } else {
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    private boolean isBlocked(String ipAddress) {
//        Long blockExpiry = blockExpiryByIp.get(ipAddress);
//        if (blockExpiry != null) {
//            if (System.currentTimeMillis() < blockExpiry) {
//                return true;
//            } else {
//                // Hết thời gian block
//                blockExpiryByIp.remove(ipAddress);
//                attemptsByIp.remove(ipAddress);
//            }
//        }
//        return false;
//    }
//
//    private String getClientIP(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return xForwardedFor.split(",")[0].trim();
//        }
//        return request.getRemoteAddr();
//    }
//
//    private static class ResponseWrapper extends HttpServletResponseWrapper {
//        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        private int status = HttpStatus.OK.value();
//
//        public ResponseWrapper(HttpServletResponse response) {
//            super(response);
//        }
//
//        @Override
//        public ServletOutputStream getOutputStream() throws IOException {
//            return new ServletOutputStream() {
//                @Override
//                public boolean isReady() {
//                    return true;
//                }
//
//                @Override
//                public void setWriteListener(WriteListener listener) {
//                }
//
//                @Override
//                public void write(int b) throws IOException {
//                    outputStream.write(b);
//                }
//            };
//        }
//
//        @Override
//        public void setStatus(int sc) {
//            super.setStatus(sc);
//            this.status = sc;
//        }
//
//        public int getStatus() {
//            return status;
//        }
//
//        public byte[] getContentAsByteArray() {
//            return outputStream.toByteArray();
//        }
//    }
//}