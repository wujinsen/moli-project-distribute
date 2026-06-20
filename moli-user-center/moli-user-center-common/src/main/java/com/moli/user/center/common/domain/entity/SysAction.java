package com.moli.user.center.common.domain.entity;

import lombok.Data;

@Data
public class SysAction {

    private Long id;

    private String permCode;

    private String resource;

    private String action;

    private String name;

    private Long menuId;

    private Integer orderNum;

    private Integer status;
}
