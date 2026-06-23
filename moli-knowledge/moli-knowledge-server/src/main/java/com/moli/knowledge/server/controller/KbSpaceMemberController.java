package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbSpaceMemberBatchAddRequest;
import com.moli.knowledge.server.dto.KbSpaceMemberBatchRemoveRequest;
import com.moli.knowledge.server.dto.KbSpaceMemberBatchResult;
import com.moli.knowledge.server.entity.KbSpaceMember;
import com.moli.knowledge.server.service.KbSpaceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb/space/member")
@Api(tags = "知识空间成员（ACL）")
public class KbSpaceMemberController {

    @Resource
    private KbSpaceMemberService kbSpaceMemberService;

    @GetMapping("/list")
    @ApiOperation("空间成员列表（需空间管理权限）")
    public MoliResult<List<KbSpaceMember>> list(@RequestParam Long spaceId) {
        return MoliResult.success(kbSpaceMemberService.list(spaceId));
    }

    @PostMapping
    @ApiOperation("添加成员（memberType 0用户/1角色，role viewer/editor/admin）")
    public MoliResult<Long> add(@RequestBody KbSpaceMember member) {
        return MoliResult.success(kbSpaceMemberService.add(member));
    }

    @PostMapping("/batch")
    @ApiOperation("批量添加成员")
    public MoliResult<KbSpaceMemberBatchResult> batchAdd(@RequestBody KbSpaceMemberBatchAddRequest request) {
        return MoliResult.success(kbSpaceMemberService.batchAdd(request));
    }

    @PutMapping
    @ApiOperation("更新成员角色")
    public MoliResult<Boolean> update(@RequestBody KbSpaceMember member) {
        kbSpaceMemberService.update(member);
        return MoliResult.success(Boolean.TRUE);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("移除成员")
    public MoliResult<Boolean> remove(@PathVariable Long id) {
        kbSpaceMemberService.remove(id);
        return MoliResult.success(Boolean.TRUE);
    }

    @PostMapping("/batch/remove")
    @ApiOperation("批量移除成员")
    public MoliResult<KbSpaceMemberBatchResult> batchRemove(@RequestBody KbSpaceMemberBatchRemoveRequest request) {
        return MoliResult.success(kbSpaceMemberService.batchRemove(request));
    }
}
