package cn.gol.es.service.abstracts;

import cn.gol.es.config.SpringContextHolder;
import cn.gol.es.entity.EsSearch;
import cn.gol.es.entity.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static cn.gol.es.entity.BaseSearch.*;

@Slf4j
public abstract class AbstractElasticSearch<T> {
    private static final String CREATED = "CREATED";
    private static final String UPDATED = "UPDATED";
    private static final String DELETED = "DELETED";

    protected abstract String getIndex();

    protected abstract Class<T> getClazz();

    protected abstract String getId(T t);

    public R findById(String id) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        GetRequest request = new GetRequest(getIndex(), id);
        GetResponse response;
        try {
            response = client.get(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        if (response.isSourceEmpty()) {
            return R.ok();
        }
        T t = parseObject(response.getSourceAsString());
        return R.ok(t);
    }

    public R findByIds(List<String> ids) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        MultiGetRequest request = new MultiGetRequest();
        String index = getIndex();
        ids.forEach(id -> request.add(index, id));
        MultiGetResponse responses;
        try {
            responses = client.mget(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        List<T> list = new ArrayList<>();
        for (MultiGetItemResponse response : responses.getResponses()) {
            list.add(parseObject(response.getResponse().getSourceAsString()));
        }
        return R.ok(list);
    }

    public R create(T t) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        IndexRequest request = new IndexRequest(getIndex()).id(getId(t)).source(JSON.toJSONString(t), XContentType.JSON);
        request.setParentTask("", 123);
        IndexResponse response;
        try {
            response = client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return CREATED.equals(response.getResult().name()) ? R.ok() : R.failed();
    }

    public R batchCreate(List<T> list) {
        BulkRequest request = new BulkRequest(getIndex());
        list.forEach(t -> request.add(new IndexRequest(getIndex()).id(getId(t)).source(JSON.toJSONString(t), XContentType.JSON)));
        return R.ok(bulkRequest(request));
    }

    public R update(T t) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        UpdateRequest request = new UpdateRequest(getIndex(), getId(t)).doc(JSON.toJSONString(t), XContentType.JSON);
        UpdateResponse response;
        try {
            response = client.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return UPDATED.equals(response.getResult().name()) ? R.ok() : R.failed();
    }

    public R batchUpdate(List<T> list) {
        BulkRequest request = new BulkRequest(getIndex());
        list.forEach(t -> request.add(new UpdateRequest(getIndex(), getId(t)).doc(JSON.toJSONString(t), XContentType.JSON)));
        return R.ok(bulkRequest(request));
    }

    public R delete(String id) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        DeleteRequest request = new DeleteRequest(getIndex(), id);
        DeleteResponse response;
        try {
            response = client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return DELETED.equals(response.getResult().name()) ? R.ok() : R.failed();
    }

    public R batchDelete(List<String> ids) {
        String index = getIndex();
        BulkRequest request = new BulkRequest(getIndex());
        ids.forEach(id -> request.add(new DeleteRequest(index, id)));
        return R.ok(bulkRequest(request));
    }

    public R select(EsSearch search) {
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        SearchRequest request = new SearchRequest();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        packageMust(search.getMusts(), query);
        packageKeywordFilterOld(search.getKeyword(), search.getFieldNames(), query, search.isKeywordPost());
        packageNickname(search, query);
        packageShould(search.getShoulds(), query);
        packageMustNots(search.getNots(), query);
        packageRanges(search.getRanges(), query);
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        ssb.query(query);
        packageSort(search.getSorts(), ssb);
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        packageKeyword4PostFilter(search.getKeyword(), search.getFieldNames(), filter, search.isKeywordPost());
        packageOr(search.getOrs(), filter);
        packageScoreRandom(search.isScoreRandom(), ssb, query);
        if (filter.hasClauses()) {
            ssb.postFilter(filter);
        }
        packagePaging(search, request, ssb);
        request.searchType(SearchType.DEFAULT).source(ssb);
        SearchResponse response;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        List<T> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            T t = parseObject(hit.getSourceAsString());
            list.add(t);
        }
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isNotBlank(search.getScroll())) {
            result.put("scroll", response.getScrollId());
        } else {
            result.put("pageNo", search.getPage().getPageNo());
            result.put("pageSize", search.getPage().getPageSize());
        }
        result.put("total", response.getHits().getTotalHits().value);
        result.put("size", list.size());
        result.put("data", list);
        return R.ok(result);
    }

    /**
     * Must条件封装
     */
    private void packageMust(List<EsMust> musts, BoolQueryBuilder builder) {
        if (CollectionUtils.isEmpty(musts)) {
            return;
        }
        musts.forEach(must -> {
            if (isBlank(must.getValue())) {
                return;
            }
            builder.filter(QueryBuilders.termQuery(must.getKey(), must.getValue()));
        });
    }

    /**
     * 关键词搜索封装
     */
    private void packageKeyword(String keyword, String[] fieldNames, BoolQueryBuilder builder) {
        if (StringUtils.isBlank(keyword) || null == fieldNames || fieldNames.length == 0) {
            return;
        }
        for (String fieldName : fieldNames) {
            builder.should(QueryBuilders.termQuery(fieldName, keyword));
            builder.should(QueryBuilders.matchPhraseQuery(fieldName, keyword));
        }
        builder.minimumShouldMatch(1);
    }

    private void packageKeywordFilterOld(String keyword, String[] fieldNames, BoolQueryBuilder builder, boolean keywordPost) {
        if (keywordPost) {
            return;
        }
        packageKeyword(keyword, fieldNames, builder);
    }

    /**
     * MustNot条件封装
     */
    private void packageMustNots(List<EsNot> nots, BoolQueryBuilder builder) {
        if (CollectionUtils.isEmpty(nots)) {
            return;
        }
        nots.forEach(not -> {
            if (CollectionUtils.isEmpty(not.getValue())) {
                return;
            }
            not.getValue().forEach(value -> {
                if (isBlank(value)) {
                    return;
                }
                builder.mustNot(QueryBuilders.termQuery(not.getKey(), value));
            });
        });
    }

    /**
     * should条件封装
     */
    private void packageShould(List<EsShould> shoulds, BoolQueryBuilder builder) {
        if (CollectionUtils.isEmpty(shoulds)) {
            return;
        }
        shoulds.forEach(should -> {
            if (isBlank(should.getValue())) {
                return;
            }
            builder.should(QueryBuilders.matchQuery(should.getKey(), should.getValue()));
        });
    }

    /**
     * 排序条件封装
     */
    private void packageSort(LinkedHashMap<String, EsSort> sorts, SearchSourceBuilder ssb) {
        if (CollectionUtils.isEmpty(sorts)) {
            return;
        }
        sorts.forEach((key, sort) -> ssb.sort(key, sort.isDesc() ? SortOrder.DESC : SortOrder.ASC));
    }

    /**
     * 或条件封装
     */
    private void packageOr(List<EsOr> ors, BoolQueryBuilder filter) {
        if (CollectionUtils.isEmpty(ors)) {
            return;
        }
        ors.forEach(or -> {
            if (CollectionUtils.isEmpty(or.getValue())) {
                return;
            }
            BoolQueryBuilder myBuilder = QueryBuilders.boolQuery();
            for (String v : or.getValue()) {
                if (isBlank(v)) {
                    continue;
                }
                myBuilder.should(QueryBuilders.matchQuery(or.getKey(), v));
            }
            myBuilder.minimumShouldMatch(1);
            filter.must(myBuilder);
        });
    }

    /**
     * 随机排序返回
     */
    private void packageScoreRandom(boolean random, SearchSourceBuilder srb, BoolQueryBuilder query) {
        if (!random) {
            srb.query(query);
            return;
        }
        FunctionScoreQueryBuilder randomQuery = QueryBuilders
                .functionScoreQuery(query, ScoreFunctionBuilders.randomFunction())
                .boostMode(CombineFunction.REPLACE);
        srb.query(randomQuery);
    }

    /**
     * 封装分页
     */
    private void packagePaging(EsSearch search, SearchRequest request, SearchSourceBuilder ssb) {
        if (StringUtils.isNotBlank(search.getScroll())) {
            request.scroll(search.getScroll());
        } else {
            ssb.from(search.getPage().getPageNo());
        }
        ssb.size(search.getPage().getPageSize());
    }

    /**
     * 范围条件封装
     */
    private void packageRanges(List<EsRange> ranges, BoolQueryBuilder builder) {
        if (CollectionUtils.isEmpty(ranges)) {
            return;
        }
        ranges.forEach(range -> {
            if (range.isEmpty()) {
                return;
            }
            RangeQueryBuilder query = QueryBuilders.rangeQuery(range.getKey());
            packageRange(range, query);
            builder.filter(query);
        });
    }

    /**
     * 封装范围
     */
    private void packageRange(EsRange range, RangeQueryBuilder query) {
        if (StringUtils.isNotBlank(range.getGte())) {
            query.gte(range.getGte());
        }
        if (StringUtils.isNotBlank(range.getGt())) {
            query.gt(range.getGt());
        }
        if (StringUtils.isNotBlank(range.getLte())) {
            query.lte(range.getLte());
        }
        if (StringUtils.isNotBlank(range.getLt())) {
            query.lt(range.getLt());
        }
        if (StringUtils.isNotBlank(range.getFrom())) {
            query.from(range.getFrom());
        }
        if (StringUtils.isNotBlank(range.getTo())) {
            query.to(range.getTo());
        }
        if (StringUtils.isNotBlank(range.getFormat())) {
            query.format(range.getFormat());
        }
    }

    private void packageKeyword4PostFilter(String keyword, String[] fieldNames, BoolQueryBuilder filter, boolean keywordPost) {
        if (!keywordPost) {
            return;
        }
        packageKeyword(keyword, fieldNames, filter);
    }

    private void packageNickname(EsSearch search, BoolQueryBuilder builder) {
        if (StringUtils.isBlank(search.getNickname())) {
            return;
        }
        builder.should(QueryBuilders.matchPhraseQuery("nickname", search.getNickname()));
        builder.minimumShouldMatch(1);
    }

    private boolean isBlank(Object value) {
        return null == value || StringUtils.isBlank(StringUtils.deleteWhitespace(value.toString()));
    }

    /**
     * String 转 T泛型
     */
    private T parseObject(String result) {
        if (StringUtils.isBlank(result)) {
            return null;
        }
        T t = JSON.parseObject(result, getClazz());
        return t;
    }

    /**
     * 批量操作
     */
    private Map<String, Object> bulkRequest(BulkRequest request) {
        BulkResponse responses;
        RestHighLevelClient client = SpringContextHolder.getBean("RestHighLevelClient");
        try {
            responses = client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        List<String> success = new ArrayList<>();
        Map<String, String> error = new HashMap<>();
        for (BulkItemResponse item : responses.getItems()) {
            item.getResponse().getResult().name();
            if (CREATED.equals(item.getResponse().getResult().name())) {
                success.add(item.getId());
            } else {
                error.put(item.getId(), item.getFailureMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("error", error);
        return result;
    }
}
