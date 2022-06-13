package com.changgou.goods.feign;
import com.changgou.goods.pojo.Content;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:sz.itheima
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name="user")
@RequestMapping("/content")
public interface ContentFeign {

    /***
     * Content分页条件搜索实现
     * @param content
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) Content content, @PathVariable(value = "{page}") int page, @PathVariable(value = "{size}") int size);

    /***
     * Content分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable(value = "{page}") int page, @PathVariable("{size}") int size);

    /***
     * 多条件搜索品牌数据
     * @param content
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Content>> findList(@RequestBody(required = false) Content content);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable(value = "{id}") Long id);

    /***
     * 修改Content数据
     * @param content
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody Content content, @PathVariable(value = "{id}") Long id);

    /***
     * 新增Content数据
     * @param content
     * @return
     */
    @PostMapping
    Result add(@RequestBody Content content);

    /***
     * 根据ID查询Content数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Content> findById(@PathVariable(value = "{id}") Long id);

    /***
     * 查询Content全部数据
     * @return
     */
    @GetMapping
    Result<List<Content>> findAll();

    /****
     * 根据分类id查询所有广告列表
     */
    @GetMapping("/list/category/{id}")
    Result<List<Content>> findByCategory(@PathVariable(value = "{id}") Long id);
}