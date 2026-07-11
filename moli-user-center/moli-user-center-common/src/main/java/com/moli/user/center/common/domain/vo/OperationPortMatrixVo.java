package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class OperationPortMatrixVo {

    private Long id;
    private String matrixKey;
    private String displayName;
    private String expectedPort;
    private List<String> aliases = new ArrayList<>();
    private Integer sortOrder;
    private Boolean enabled;
    private String source;
    private String remark;
    private Date createTime;
    private Date updateTime;

    @ApiModelProperty("是否使用内置默认矩阵（DB 空表时）")
    private Boolean usingDefaults;
}
