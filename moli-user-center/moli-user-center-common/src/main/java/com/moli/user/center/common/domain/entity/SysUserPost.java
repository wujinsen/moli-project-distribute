package com.moli.user.center.common.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserPost implements Serializable {

    private Long id;

    private Long userId;

    private Long postId;
}
