package cn.gol.es.entity;


import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

import static cn.gol.es.entity.BaseSearch.*;

@Data
public class EsSearch {
    private List<EsMust> musts;
    private List<EsNot> nots;
    private List<EsOr> ors;
    private List<EsRange> ranges;
    private List<EsShould> shoulds;
    private LinkedHashMap<String, EsSort> sorts;
    private List<Integer> ids;
    private String keyword;
    private String nickname;
    private String[] fieldNames;
    private boolean keywordPost;
    /**
     * 根据评分随机返回
     */
    private boolean scoreRandom;
    /**
     * 根据Math().random函数随机返回
     */
    private boolean mathRandom;
    /**
     * scroll游标优先级高于page
     */
    private String scroll;
    private EsPage page;

    public static BaseSearch builder() {
        return new BaseSearch();
    }
}
