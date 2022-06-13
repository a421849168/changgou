package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate esTemplate;




    /**
     * 导入sku数据到es
     */
    @Override
    public void importSku(){
        //调用Feign查询sku列表
        List<Sku> skuList = skuFeign.findByStatus("1").getData();
        if(skuList != null && skuList.size() > 0){
            List<SkuInfo> skuInfoList = new ArrayList<>();
            SkuInfo skuInfo = null;
            for (Sku sku : skuList) {
                //数据转换
                String json = JSON.toJSONString(sku);
                skuInfo = JSON.parseObject(json, SkuInfo.class);

                //设置嵌套域数据
                //SpringDataEs会把map的key当做一个域名，value当做值来存储
                Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
                skuInfo.setSpecMap(specMap);
                //记录要保存的skuinfo
                skuInfoList.add(skuInfo);
            }
            if(skuInfoList.size() > 0){
                skuEsMapper.saveAll(skuInfoList);
            }
        }
    }

    @Override
    public Map search(Map<String, String> searchMap) {
        Map map = new HashMap();
        //1、构建基本查询条件
        NativeSearchQueryBuilder builder = builderBasicQuery(searchMap);
        //2、根据查询条件-搜索商品列表
        searchList(map, builder);

//        //3、跟据查询条件-分组查询商品分类列表
//        searchCategoryList(map,builder);
//
//        searchBrandList(map,builder);
//        //跟据查询条件-分组查询规格列表
//        searchSpec(map,builder );
//
        //一次分组查询分类、品牌与规格
        searchGroup(map,builder );
        return map;
    }

    private void searchList(Map map, NativeSearchQueryBuilder builder) {
        //h1.配置高亮查询信息-hField = new HighlightBuilder.Field()
        //h1.1:设置高亮域名-在构造函数中设置
        HighlightBuilder.Field hField = new HighlightBuilder.Field("name");
        //h1.2：设置高亮前缀-hField.preTags
        hField.preTags("<em style='color:red;'>");
        //h1.3：设置高亮后缀-hField.postTags
        hField.postTags("</em>");
        //h1.4：设置碎片大小-hField.fragmentSize
        hField.fragmentSize(100);
        //h1.5：追加高亮查询信息-builder.withHighlightFields()
        builder.withHighlightFields(hField);

        //3、获取NativeSearchQuery搜索条件对象-builder.build()
        NativeSearchQuery query = builder.build();
        //h2.高亮数据读取-AggregatedPage<SkuInfo> page = esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapper(){})
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapper() {
            @Override
            //h2.1实现mapResults(查询到的结果,数据列表的类型,分页选项)方法
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //h2.2 先定义一组查询结果列表-List<T> list = new ArrayList<T>()
                List<T> list = new ArrayList<>();
                //h2.3 遍历查询到的所有高亮数据-response.getHits().for
                for (SearchHit hit : response.getHits()) {
                    //h2.3.1 先获取当次结果的原始数据(无高亮)-hit.getSourceAsString()
                    String json = hit.getSourceAsString();
                    //h2.3.2 把json串转换为SkuInfo对象-skuInfo = JSON.parseObject()
                    SkuInfo skuInfo = JSON.parseObject(json,SkuInfo.class);
                    //h2.3.3 获取name域的高亮数据-nameHighlight = hit.getHighlightFields().get("name")
                    HighlightField name = hit.getHighlightFields().get("name");
                    //h2.3.4 如果高亮数据不为空-读取高亮数据
                    if (name != null){
                        //h2.3.4.1 定义一个StringBuffer用于存储高亮碎片-buffer = new StringBuffer()
                        StringBuffer buffer = new StringBuffer();
                        //h2.3.4.2 循环组装高亮碎片数据- nameHighlight.getFragments().for(追加数据)
                        for (Text fragment : name.getFragments()) {
                            buffer.append(fragment);
                        }
                        //h2.3.4.3 将非高亮数据替换成高亮数据-skuInfo.setName()
                        skuInfo.setName(buffer.toString());
                    }
                    //h2.3.5 将替换了高亮数据的对象封装到List中-list.add((T) esItem)
                    list.add((T) skuInfo);
                }
                //h2.4 返回当前方法所需要参数-new AggregatedPageImpl<T>(数据列表，分页选项,总记录数)
                //h2.4 参考new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits())

                return new AggregatedPageImpl<T>(list, pageable, response.getHits().getTotalHits());
            }
        });
        //5、包装结果并返回
        map.put("rows", page.getContent());
        map.put("total", page.getTotalElements());
        map.put("totalPages", page.getTotalPages());

        int pageNum = query.getPageable().getPageNumber();  //当前页
        map.put("pageNum", pageNum);
        int pageSize = query.getPageable().getPageSize();//每页查询的条数
        map.put("pageSize", pageSize);

    }


    /**
     * 构建基本查询条件
     * @param searchMap 用户传入的查询参数
     * @return 查询条件构建器
     */
    private NativeSearchQueryBuilder builderBasicQuery(Map<String, String> searchMap) {
        //1、创建查询条件构建器-builder = new NativeSearchQueryBuilder()
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //2、组装查询条件
        if(searchMap != null){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //2.1关键字搜索-builder.withQuery(QueryBuilders.matchQuery(域名，内容))
            String keywords = searchMap.get("keywords") == null ? "" : searchMap.get("keywords");
            //如果用户传入了关键字
            if(StringUtils.isNotEmpty(keywords)){
                //查询name域
                //builder.withQuery(QueryBuilders.matchQuery("name", keywords));
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords));
            }
            //2.2 分类查询
            String category = searchMap.get("category") == null ? "" : searchMap.get("category");
            //如果用户传入了分类
            if(StringUtils.isNotEmpty(category)){
                //查询category域
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", category));
            }
            //2.3 品牌查询
            String brand = searchMap.get("brand") == null ? "" : searchMap.get("brand");
            //如果用户传入了品牌
            if(StringUtils.isNotEmpty(brand)){
                //查询brand域
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", brand));
            }
            //2.4 规格查询
            //读取用户传入的所有参数的key
            for (String key : searchMap.keySet()) {
                //识别规格:用户传入，spec_网络制式：电信4G
                if(key.startsWith("spec_")){
                    String value = searchMap.get(key).replace("\\","" );
                    boolQueryBuilder.filter(QueryBuilders.matchQuery("specMap"+key.substring(5)+".keyword",value));

//                    //specMap.规格名字.keyword
//                    String specField = "specMap." + key.substring(5) + ".keyword";
//                    //规格域
//                    boolQueryBuilder.must(QueryBuilders.termQuery(specField, searchMap.get(key)));
                }
            }
            //2.5 价格区间查询
            String price = searchMap.get("price") == null ? "" : searchMap.get("price");
            //如果用户传入了价格
            if(StringUtils.isNotEmpty(price)){
                //范围匹配搜索
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
                //解析前端传入的价格：0-500 ,500-1000,3000
                String[] split = price.split("-");
                //处理前面的价格:price >= 0
                boolQueryBuilder.must(rangeQueryBuilder.gte(split[0]));
                //如果解析结果大小1，说明传入的不是3000
                if(split.length > 1){
                    //price <= 500
                    boolQueryBuilder.must(rangeQueryBuilder.lte(split[1]));
                }
            }
            //追加多条件匹配搜索
            builder.withQuery(boolQueryBuilder);

            //当前页
            Integer page = searchMap.get("pageNum") == null ? 1 : new Integer(searchMap.get("pageNum"));
            Integer pageSize = 5;  //每页查询记录数
            //PageRequest.of(当前页【0开始】，每页查询的条数)
            PageRequest pageRequest = PageRequest.of(page, pageSize);
            //设置分页查询
            builder.withPageable(pageRequest);

            //排序
            //排序方式:ASC|DESC
            String sortRule = searchMap.get("sortRule") == null ? "" : searchMap.get("sortRule");
            //排序域名
            String sortField = searchMap.get("sortField") == null ? "" : searchMap.get("sortField");
            if(StringUtils.isNotEmpty(sortField)){
                //fieldSort(域名)，order(排序方式)
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }
        return builder;
    }




    /**
     * 跟据查询条件-分组查询商品分类列表
     * @param map 结果集包装
     * @param builder 查询条件构建器
     */
    private void searchCategoryList(Map map,NativeSearchQueryBuilder builder){
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_category").field("categoryName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_category");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> categoryList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            categoryList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("categoryList", categoryList);
    }


    /**
     * 跟据查询条件-分组查询品牌列表
     * @param map 结果集包装
     * @param builder 查询条件构建器
     */
    private void searchBrandList(Map map,NativeSearchQueryBuilder builder){
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_brand").field("brandName");
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_brand");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> brandList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            brandList.add(bucket.getKeyAsString());
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("brandList", brandList);
    }

    /**
     * 跟据查询条件-分组查询规格列表
     * @param map 结果集包装
     * @param builder 查询条件构建器
     */
    private void searchSpec(Map map,NativeSearchQueryBuilder builder){
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名).size(查询记录数);
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(10000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        builder.addAggregation(termsAggregationBuilder);
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get("group_spec");
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> specList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            specList.add(bucket.getKeyAsString());
        }

        //所有规格列表
        Map<String, Set<String>> specMap = new HashMap<>();
        //包装List<Spec>，组装成Map<String,Set>
        for (String spec : specList) {
            //{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
            //把spec的json串转换成Map<String,String>
            Map<String, String> tempMap = JSON.parseObject(spec, Map.class);
            //循环读取key与value，组装到结果集中
            for (String key : tempMap.keySet()) {
                Set<String> values = specMap.get(key);
                //如果当前key是第一次组装
                if(values == null){
                    values = new HashSet<String>();
                }
                //向结果value中追加一个元素
                values.add(tempMap.get(key));
                //规格结果集追加元素
                specMap.put(key, values);
            }
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("specMap", specMap);
    }

    /**
     * 提取分组聚合结果
     * @param aggregations 聚合结果对象
     * @param group_name 分组域的别名
     * @return 提取的结果集
     */
    private List<String> getGroupResult(Aggregations aggregations, String group_name) {
        //5.提取分组结果数据-stringTerms = aggregations.get(填入刚才查询时的别名)
        StringTerms stringTerms = aggregations.get(group_name);
        //6.定义分类名字列表-categoryList = new ArrayList<String>()
        List<String> specList = new ArrayList<String>();
        //7.遍历读取分组查询结果-stringTerms.getBuckets().for
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //7.1获取分类名字，并将分类名字存入到集合中-bucket.getKeyAsString()
            specList.add(bucket.getKeyAsString());
        }
        return specList;
    }

    /**
     * 跟据查询条件-分组查询商品分类、品牌、规格列表
     * @param map 结果集包装
     * @param builder 查询条件构建器
     */
    private void searchGroup(Map map,NativeSearchQueryBuilder builder){
        //1.设置分组域名-termsAggregationBuilder = AggregationBuilders.terms(别名).field(域名).size(查询记录数);
        //TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_spec").field("spec.keyword").size(10000);
        //2.添加分组查询参数-builder.addAggregation(termsAggregationBuilder)
        //一次设置三个聚合分组条件，一起查询
        builder.addAggregation(AggregationBuilders.terms("group_category").field("categoryName"));
        builder.addAggregation(AggregationBuilders.terms("group_brand").field("brandName"));
        builder.addAggregation(AggregationBuilders.terms("group_spec").field("spec.keyword").size(10000));
        //3.执行搜索-esTemplate.queryForPage(builder.build(), SkuInfo.class)
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //4.获取所有分组查询结果集-page.getAggregations()
        Aggregations aggregations = page.getAggregations();

        //1、提取分类结果
        List<String> categoryList = getGroupResult(aggregations, "group_category");
        map.put("categoryList", categoryList);

        //2、提取品牌结果
        List<String> brandList = getGroupResult(aggregations, "group_brand");
        map.put("brandList", brandList);

        //3、提取规格结果
        List<String> specList = getGroupResult(aggregations, "group_spec");
        //所有规格列表
        Map<String, Set<String>> specMap = new HashMap<>();
        //包装List<Spec>，组装成Map<String,Set>
        for (String spec : specList) {
            //{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
            //把spec的json串转换成Map<String,String>
            Map<String, String> tempMap = JSON.parseObject(spec, Map.class);
            //循环读取key与value，组装到结果集中
            for (String key : tempMap.keySet()) {
                Set<String> values = specMap.get(key);
                //如果当前key是第一次组装
                if(values == null){
                    values = new HashSet<String>();
                }
                //向结果value中追加一个元素
                values.add(tempMap.get(key));
                //规格结果集追加元素
                specMap.put(key, values);
            }
        }
        //8.返回分类数据列表-map.put("categoryList", categoryList)
        map.put("specMap", specMap);
    }

}

