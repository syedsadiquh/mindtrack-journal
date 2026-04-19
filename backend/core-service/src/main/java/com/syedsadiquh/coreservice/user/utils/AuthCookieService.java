package com.syedsadiquh.coreservice.user.utils;

import com.syedsadiquh.coreservice.infrastructure.config.AuthCookieProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthCookieProperties props;

    public void writeRefreshCookie(HttpHeaders headers, String refreshToken) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(props.getName(), refreshToken)
                .httpOnly(true)
                .secure(props.isSecure())
                .path(props.getPath())
                .maxAge(props.getMaxAgeSeconds())
                .sameSite(props.getSameSite());
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            b.domain(props.getDomain());
        }
        headers.add(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public void clearRefreshCookie(HttpHeaders headers) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(props.getName(), "")
                .httpOnly(true)
                .secure(props.isSecure())
                .path(props.getPath())
                .maxAge(0)
                .sameSite(props.getSameSite());
        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            b.domain(props.getDomain());
        }
        headers.add(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public String cookieName() {
        return props.getName();
    }
}
