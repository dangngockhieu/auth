package com.example.auth.util.format_response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.example.auth.util.annotation.ApiMessage;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class FormatResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        if (body instanceof RestResponse) {
            return body;
        }
        // Xử lý trường hợp Controller trả về kiểu String
        if (body instanceof String) {
            return body;
        }

        // Bỏ qua nếu là lỗi
        if (status >= 400) {
            return body;
        }

        // Lấy message từ annotation @ApiMessage (nếu có)
        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        String message = (apiMessage != null) ? apiMessage.value() : "Success";

        // Khởi tạo và map dữ liệu
        RestResponse<Object> res = RestResponse.success(message, body);

        return res;
    }
}
