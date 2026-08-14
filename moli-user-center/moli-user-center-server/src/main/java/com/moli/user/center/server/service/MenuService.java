package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.entity.SysMenu;
import com.moli.user.center.common.domain.vo.MenuVo;

import java.util.List;

public interface MenuService {

    /**
     * 添加菜单
     * @param menu
     * @return
     */
    public Boolean insert(SysMenu menu) ;

    /**
     * 更新菜单
     * @param menu
     * @return
     */
    public Boolean update(SysMenu menu) ;

    /**
     * 根据用户查询菜单树
     */
    List<MenuVo> selectMenuTreeByUserId(Long userId);

    /**
     * 获取菜单列表
     * @param menuVo
     * @return
     */
    List<MenuVo> selectMenuList(MenuVo menuVo);
    /**
     * 根据用户查询菜单列表
     */
    List<MenuVo> selectMenuListByUserId(MenuVo menuVo);

    /**
     * 根据角色获取菜单树
     * @param roleId
     * @return
     */
    List<MenuVo> selectMenuTreeByRoleId(Long roleId);

    /**
     * 所有菜单列表树结构
     * @return
     */
    List<MenuVo> getMenuTreeAll();

    /**
     * 运行时路由：按门户开关与 Session currentSystemId + sys_menu.system_id 过滤（SSO-MENU-1）。
     */
    List<MenuVo> resolveRoutersForCurrentSystem(Long userId);

}
