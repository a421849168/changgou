package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "search")
public class SkuController {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SkuFeign skuFeign;


    /**
     * 搜索商品
     * 注意此处的@GetMapping()要添加list的url请求，不然会跟SkuFeign中的请求url冲突
     */
    @GetMapping("list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){
        //替换特殊字符
        handlerSearchMap(searchMap);
        //查询数据
        Map result = skuFeign.search(searchMap);
        model.addAttribute("result",result);
        //返回查询条件
        model.addAttribute("searchMap",searchMap);
        //获取url
        String url = this.getUrl(searchMap);
        model.addAttribute("url", url);

        //返回分页参数
        Page page = new Page(
                new Long(result.get("total").toString()),
                new Integer(result.get("pageNum").toString()),
                new Integer(result.get("pageSize").toString())
        );
        model.addAttribute("page", page);

        //响应视图
        return "search";
    }

    /**
     * 把Map转换成url
     * @param searchMap
     * @return
     */
    private String getUrl(Map<String,String> searchMap){
        // /search/list?category=笔记本&brand=华为&spec_网络=移动4G&price=0-500
        String url = "/search/list";
        //有参数
        if(searchMap != null){
            url += "?";
            for (String key : searchMap.keySet()) {
                //如果是排序的参数，不拼接到url上，便于下次换种方式排序
                if(key.indexOf("sort") > -1 || "pageNum".equals(key)){
                    continue;
                }
                url += key + "=" + searchMap.get(key) + "&";
            }
            //循环完毕后删除最后一个&
            url = url.substring(0, url.length() - 1);
        }
        return url;

    }

    /****
     * 替换特殊字符
     * @param searchMap
     */
    public void handlerSearchMap(Map<String,String> searchMap){
        if(searchMap!=null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if(entry.getKey().startsWith("spec_")){
                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
