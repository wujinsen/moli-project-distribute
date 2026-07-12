package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationServerInfoVo;
import com.moli.user.center.common.domain.vo.OperationServerSshVo;
import com.moli.user.center.common.domain.vo.OperationServerVo;
import com.moli.user.center.common.domain.vo.OperationSshTestVo;
import com.moli.common.page.PageRes;

import java.util.List;

public interface OperationServerService {

    PageRes<OperationServerVo> list(OperationServerInfoVo query);

    /** 全库已用标签（去重排序），供筛选/输入联想。 */
    List<String> listTagOptions();

    OperationServerVo getById(Long id);

    void create(OperationServerSaveRequest request);

    void update(OperationServerSaveRequest request);

    void deleteByIds(Long[] ids);

    OperationServerVo checkHealth(Long id);

    /** 保存 SSH 凭据（私钥/密码加密存储，只写不读）。 */
    void saveSsh(Long id, OperationServerSshVo form);

    /** 测试 SSH 连接，返回 whoami 与耗时。 */
    OperationSshTestVo testSsh(Long id);

    /** 供部署/上传服务读取带密文凭据的原始行。 */
    OperationServerInfo requireEntity(Long id);
}
