package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class OperationProjectSaveRequest {

    @ApiModelProperty("主键（更新时必填）")
    private Long id;

    @ApiModelProperty("主服务器 ID（推荐为 serverIds 首项；与 serverIp 二选一，同时填以 serverId 为准）")
    private Long serverId;

    @ApiModelProperty("关联服务器 ID 列表（N:N；保存时全量替换 operation_server_project）")
    private List<Long> serverIds;

    @Size(max = 64, message = "serverIp 过长")
    private String serverIp;

    @Size(max = 64, message = "innerIp 过长")
    private String innerIp;

    @Size(max = 512, message = "url 过长")
    private String url;

    @NotBlank(message = "projectName 不能为空")
    @Size(max = 256, message = "projectName 过长")
    private String projectName;

    @Size(max = 512, message = "deployPath 过长")
    private String deployPath;

    @Pattern(regexp = "^$|^\\d{1,5}$", message = "port 必须为数字")
    private String port;

    @OperationEnvironment
    private Integer environment;

    @Size(max = 512, message = "remark 过长")
    private String remark;

    @AssertTrue(message = "serverId、serverIds 与 serverIp 至少填一项")
    public boolean isServerRefPresent() {
        return serverId != null
                || (serverIds != null && !serverIds.isEmpty())
                || StringUtils.isNotBlank(serverIp);
    }
}
