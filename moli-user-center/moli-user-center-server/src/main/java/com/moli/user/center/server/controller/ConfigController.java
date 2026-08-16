package com.moli.user.center.server.controller;

import com.moli.common.constant.PermissionConstants;
import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.ConfigItemVo;
import com.moli.user.center.common.domain.vo.ConfigUpdateRequest;
import com.moli.user.center.server.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数设置。设计见 {@code docs/design/sys-config-notice.md}。
 *
 * <p>接口形态与其它系统管理模块**故意不同**，原因见设计 §3.5：
 * <ul>
 *   <li>无 {@code POST}：参数由代码 {@code ConfigKey} 注册表声明，不能由 UI 创建；</li>
 *   <li>不用 id 寻址：{@code configKey} 就是业务主键，用 id 会迫使前端多存一个无意义的值；</li>
 *   <li>无分页：参数是数十条量级，{@code PageRes} 在这里是纯负担。</li>
 * </ul>
 */
@RestController
@RequestMapping("config")
@Api(tags = "参数设置")
@Slf4j
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 参数列表。数据源是**注册表 ∪ 覆盖值**，因此从未被改过的参数也会返回，
     * 运维能看到系统全部可调项而不是一张空表。
     *
     * @param group 分组过滤（SECURITY / PORTAL / OPS），为空返回全部
     */
    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_CONFIG_LIST)
    @ApiOperation(value = "参数列表", notes = "注册表声明 + 当前生效值与来源；无分页")
    public MoliResult<List<ConfigItemVo>> list(@RequestParam(required = false) String group) {
        return MoliResult.success(configService.listItems(group));
    }

    @PutMapping
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_CONFIG_EDIT, PermissionConstants.SYSTEM_CONFIG_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "设置参数覆盖值", notes = "校验 key 已声明且值可按声明类型解析；写库后清除缓存")
    public MoliResult<Boolean> update(@RequestBody ConfigUpdateRequest request) {
        configService.setOverride(request.getConfigKey(), request.getConfigValue());
        return MoliResult.success(Boolean.TRUE);
    }

    /**
     * 重置为默认值：删除覆盖行。
     *
     * <p>注意语义不是「删除参数」—— 参数由代码声明，始终存在。
     */
    @DeleteMapping("/{configKey}")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_CONFIG_REMOVE, PermissionConstants.SYSTEM_CONFIG_LIST},
            logical = Logical.AND)
    @ApiOperation(value = "重置参数为默认值", notes = "删除运行期覆盖行，回落 yaml 或声明默认值")
    public MoliResult<Boolean> reset(@PathVariable String configKey) {
        configService.resetToDefault(configKey);
        return MoliResult.success(Boolean.TRUE);
    }

}
