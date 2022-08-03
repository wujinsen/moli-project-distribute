package com.moli.user.center.client;

import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface UserCenterServer {

    /**
     * 根据用户名获取用户
     * @return
     */
    MoliResult<User> getInfoByUserName(String userName);

}
