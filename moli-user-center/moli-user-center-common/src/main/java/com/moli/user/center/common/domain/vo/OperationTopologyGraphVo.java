package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationTopologyGraphVo {

    private List<OperationTopologyServerNodeVo> servers = new ArrayList<>();
    private List<OperationTopologyProjectNodeVo> projects = new ArrayList<>();
    private List<OperationTopologyComponentNodeVo> components = new ArrayList<>();
    private List<OperationTopologyLinkVo> links = new ArrayList<>();
}
