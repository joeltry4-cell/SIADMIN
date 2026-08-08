package com.siadmin.controller;

import com.siadmin.security.UserPrincipal;
import com.siadmin.service.CutiService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SidebarAttributeAdvice {

    private final CutiService cutiService;

    public SidebarAttributeAdvice(CutiService cutiService) {
        this.cutiService = cutiService;
    }

    @ModelAttribute("cutiBelumDibaca")
    public long cutiBelumDibaca(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getUser().getKaryawan() == null) {
            return 0;
        }
        return cutiService.hitungBelumDibaca(principal.getUser().getKaryawan());
    }
}
