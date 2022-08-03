package com.moli.user.center.common.domain.vo;

import com.moli.user.center.common.domain.entity.Role;
import com.moli.user.center.common.domain.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleVo {

    private User user;

    private Long UserId;

    private List<Role> roleList;

}
