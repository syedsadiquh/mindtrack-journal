package com.syedsadiquh.coreservice.user.service;

import com.syedsadiquh.coreservice.shared.dto.BaseResponse;
import com.syedsadiquh.coreservice.user.dto.request.AdminRegisterRequestDto;

public interface AdminAuthService {
    BaseResponse<String> register(AdminRegisterRequestDto requestDto);
}

