package com.moli.user.center.server.api;

import com.moli.user.center.common.domain.entity.SysPost;
import com.moli.user.center.common.domain.vo.PostVo;
import com.moli.user.center.server.controller.PostController;
import com.moli.user.center.server.mapper.PostMapper;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostControllerApiTest extends AbstractApiTest {

    @InjectMocks
    private PostController controller;

    @Mock
    private PostMapper postMapper;

    @Test
    public void GET_post_list() {
        ControllerTestSupport.stubEmptyPage(postMapper);
        PostVo vo = new PostVo();
        vo.setPageNum(1);
        vo.setPageSize(10);
        ControllerTestSupport.assertSuccess(controller.list(vo));
    }

    @Test
    public void POST_post_insert() {
        ControllerTestSupport.stubInsert(postMapper);
        ControllerTestSupport.assertSuccess(controller.insert(new SysPost()));
    }

    @Test
    public void PUT_post_update() {
        ControllerTestSupport.stubUpdate(postMapper);
        ControllerTestSupport.assertSuccess(controller.update(new SysPost()));
    }

    @Test
    public void GET_post_id() {
        ControllerTestSupport.stubSelectById(postMapper, new SysPost());
        ControllerTestSupport.assertSuccess(controller.selectOne(1L));
    }

    @Test
    public void DELETE_post_ids() {
        ControllerTestSupport.assertSuccess(controller.remove(new Long[]{1L}));
    }

    @Test
    public void GET_post_allPost() {
        ControllerTestSupport.stubSelectListEmpty(postMapper);
        ControllerTestSupport.assertSuccess(controller.allPost());
    }
}
