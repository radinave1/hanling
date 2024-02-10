package com.app.handling;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

@Component
@Order
@Slf4j
public class LogsRequestFilters extends OncePerRequestFilter {

  private static final String MDC_TAG_RESPONSE_TIME = "response_time";
  private static final String MDC_TAG_CLIENT_ID = "client_id";
  private static final String MDC_TAG_MAPPED_URI = "mapped_uri";
  private static final String MDC_TAG_STATUS_CODE = "status_code";
  private static final String MDC_TAG_OWNER_ID = "owner_id";
  private static final String MDC_TAG_REQUEST_PARAMS = "requestParams";
  private static final String MDC_TAG_REQUEST_BODY = "requestBody";


    private void logRequestDetails(ContentCachingRequestWrapper request, HttpServletResponse response, long start) {
        var mappedURI = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        MDC.put(MDC_TAG_MAPPED_URI, request.getMethod() + " " + mappedURI);
        MDC.put(MDC_TAG_RESPONSE_TIME, String.valueOf(System.currentTimeMillis() - start));
        MDC.put(MDC_TAG_STATUS_CODE, String.valueOf(response.getStatus()));

        String requestParams = extractRequestParameters(request);
        String requestBody = extractRequestBody(request);
        MDC.put(MDC_TAG_REQUEST_PARAMS, requestParams);
        MDC.put(MDC_TAG_REQUEST_BODY, requestBody);

        logIncomingRequests(request, "", mappedURI, MDC.get(MDC_TAG_STATUS_CODE), requestParams, requestBody);
    }

    private String extractRequestParameters(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(", ", entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String extractRequestBody(ContentCachingRequestWrapper wrappedRequest) {
        byte[] buf = wrappedRequest.getContentAsByteArray();
        String requestBody = "";
        if (buf.length > 0) {
            int length = Math.min(buf.length, 1024);
            try {
                requestBody = new String(buf, 0, length, wrappedRequest.getCharacterEncoding());
            } catch (UnsupportedEncodingException ex) {
                log.warn("Failed to parse request body", ex);
            }
        }
        return requestBody;
    }

    private void logIncomingRequests(HttpServletRequest request, String clientId, String mappedURI, String responseCode, String requestParams, String requestBody) {
        log.info("API Call: {} {} (client: {}) {}, Params: {}, Body: {}", request.getMethod(), mappedURI, clientId, responseCode, requestParams, requestBody);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        try {
            long start = System.currentTimeMillis();
            filterChain.doFilter(wrappedRequest, response);
            logRequestDetails(wrappedRequest, response, start);
        } finally {
            MDC.clear();
        }
    }

}
