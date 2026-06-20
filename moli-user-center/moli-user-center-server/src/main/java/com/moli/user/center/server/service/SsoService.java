package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.dto.SsoTicketPayload;
import com.moli.user.center.common.domain.entity.SysSystem;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.vo.SsoValidateVo;

public interface SsoService {

    String createTicket(SysUser user, SysSystem system, String hubToken);

    SsoValidateVo validateTicket(String ticket, String systemCode);

    String buildRedirectUrl(SysSystem system, String ticket);

    SsoTicketPayload parseTicket(String ticket);

}
