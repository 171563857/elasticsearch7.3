package cn.gol.es.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.*;

@Data
public class BaseSearch {
    private List<EsMust> musts;
    private Map<String, EsMust> key2must;

    private List<EsNot> nots;
    private Map<String, EsNot> key2not;

    private List<EsOr> ors;
    private Map<String, EsOr> key2or;

    private List<EsRange> ranges;
    private Map<String, EsRange> key2range;

    private List<EsShould> shoulds;
    private Map<String, EsShould> key2should;

    private LinkedHashMap<String, EsSort> sorts;

    private EsPage page;
    private EsScroll scroll;

    protected static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    public EsMust.MustBuilder must() {
        this.musts = checkList(this.musts);
        this.key2must = checkMap(this.key2must);
        EsMust.MustBuilder must = new EsMust.MustBuilder(this, musts, key2must);
        return must;
    }

    public EsNot.NotBuilder not() {
        this.nots = checkList(this.nots);
        this.key2not = checkMap(this.key2not);
        EsNot.NotBuilder not = new EsNot.NotBuilder(this, nots, key2not);
        return not;
    }

    public EsOr.OrBuilder or() {
        this.ors = checkList(this.ors);
        this.key2or = checkMap(this.key2or);
        EsOr.OrBuilder or = new EsOr.OrBuilder(this, ors, key2or);
        return or;
    }

    public EsRange.RangeBuilder range() {
        this.ranges = checkList(this.ranges);
        this.key2range = checkMap(this.key2range);
        EsRange.RangeBuilder range = new EsRange.RangeBuilder(this, ranges, key2range);
        return range;
    }

    public EsShould.ShouldBuilder should() {
        this.shoulds = checkList(this.shoulds);
        this.key2should = checkMap(this.key2should);
        EsShould.ShouldBuilder should = new EsShould.ShouldBuilder(this, shoulds, key2should);
        return should;
    }

    public EsSort.SortBuilder sort() {
        this.sorts = checkMap(this.sorts);
        EsSort.SortBuilder sort = new EsSort.SortBuilder(this, sorts);
        return sort;
    }

    public EsPage.PageBuilder page() {
        EsPage.PageBuilder page = new EsPage.PageBuilder(this);
        return page;
    }

    public EsScroll scroll(String scroll) {
        if (null != this.scroll) {
            this.scroll.setScroll(scroll);
            return this.scroll;
        } else {
            this.scroll = new EsScroll(this, scroll);
            return this.scroll;
        }
    }

    private List checkList(List list) {
        return null == list ? new ArrayList() : list;
    }

    private Map checkMap(Map map) {
        return null == map ? new HashMap() : map;
    }

    private LinkedHashMap checkMap(LinkedHashMap map) {
        return null == map ? new LinkedHashMap() : map;
    }

    public static class BaseParent {
        protected BaseSearch parent;

        public EsSearch build() {
            EsSearch search = new EsSearch();
            BeanUtils.copyProperties(parent, search);
            EsScroll scroll = parent.getScroll();
            if (null != scroll) {
                search.setScroll(scroll.getScroll());
            } else {
                EsPage page = parent.getPage();
                int pageNo = null != page && null != page.getPageNo() ? page.getPageNo() : 0;
                int pageSize = null != page && null != page.getPageSize() ? page.getPageSize() : 1;
                search.setPage(new EsPage(pageNo, pageSize));
            }
            return search;
        }

        public BaseSearch and() {
            return this.parent;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsMust extends BaseParent {
        private String key;
        private Object value;

        public EsMust(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsMust value(Object value) {
            this.value = value;
            return this;
        }

        public static class MustBuilder extends BaseParent {
            private List<EsMust> musts;
            private Map<String, EsMust> key2must;

            public MustBuilder(BaseSearch parent, List<EsMust> musts, Map<String, EsMust> key2must) {
                this.parent = parent;
                this.musts = musts;
                this.key2must = key2must;
            }

            public EsMust key(String key) {
                EsMust must = this.key2must.get(key);
                if (null != must) {
                    return must;
                }
                must = new EsMust(this.parent, key);
                this.musts.add(must);
                this.key2must.put(key, must);
                return must;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsNot extends BaseParent {
        private String key;
        private HashSet<String> value;

        public EsNot(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsNot value(String value) {
            if (isBlank(value)) {
                return this;
            }
            if (null == this.value) {
                this.value = new HashSet<>();
            }
            this.value.add(value);
            return this;
        }

        public static class NotBuilder extends BaseParent {
            private List<EsNot> nots;
            private Map<String, EsNot> key2not;

            public NotBuilder(BaseSearch parent, List<EsNot> nots, Map<String, EsNot> key2not) {
                this.parent = parent;
                this.nots = nots;
                this.key2not = key2not;
            }

            public EsNot key(String key) {
                EsNot not = key2not.get(key);
                if (null != not) {
                    return not;
                }
                not = new EsNot(this.parent, key);
                this.nots.add(not);
                this.key2not.put(key, not);
                return not;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsOr extends BaseParent {
        private String key;
        private HashSet<String> value;

        public EsOr(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsOr value(String value) {
            if (isBlank(value)) {
                return this;
            }
            if (null == this.value) {
                this.value = new HashSet<>();
            }
            this.value.add(value);
            return this;
        }

        public static class OrBuilder extends BaseParent {
            private List<EsOr> ors;
            private Map<String, EsOr> key2or;

            public OrBuilder(BaseSearch parent, List<EsOr> ors, Map<String, EsOr> key2or) {
                this.parent = parent;
                this.ors = ors;
                this.key2or = key2or;
            }

            public EsOr key(String key) {
                EsOr or = key2or.get(key);
                if (null != or) {
                    return or;
                }
                or = new EsOr(this.parent, key);
                this.ors.add(or);
                this.key2or.put(key, or);
                return or;
            }
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class EsPage extends BaseParent {
        private Integer pageNo;
        private Integer pageSize;

        public EsPage(Integer pageNo, Integer pageSize) {
            this.pageNo = pageNo;
            this.pageSize = pageSize;
        }

        public EsPage(BaseSearch parent, Integer pageNo) {
            this.parent = parent;
            this.pageNo = pageNo;
        }

        public EsPage size(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public static class PageBuilder extends BaseParent {

            public PageBuilder(BaseSearch parent) {
                this.parent = parent;
            }

            public EsPage from(Integer pageNo) {
                EsPage page = parent.getPage();
                if (null != page) {
                    page.setPageNo(pageNo);
                    return page;
                }
                page = new EsPage(parent, pageNo);
                parent.setPage(page);
                return page;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsRange extends BaseParent {
        private String key;
        private String gte;
        private String gt;
        private String lte;
        private String lt;
        private String from;
        private String to;
        private String format;

        public EsRange(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsRange gte(String gte) {
            this.gte = gte;
            return this;
        }

        public EsRange gt(String gt) {
            this.gt = gt;
            return this;
        }

        public EsRange lte(String lte) {
            this.lte = lte;
            return this;
        }

        public EsRange lt(String lt) {
            this.lt = lt;
            return this;
        }

        public EsRange from(String from) {
            this.from = from;
            return this;
        }

        public EsRange to(String to) {
            this.to = to;
            return this;
        }

        public EsRange format(String format) {
            this.format = format;
            return this;
        }

        public boolean isEmpty() {
            if (isBlank(key)) {
                return true;
            }
            if (isBlank(gte) && isBlank(gt)
                    && isBlank(lte) && isBlank(lt)
                    && isBlank(from) && isBlank(to)) {
                return true;
            }
            return false;
        }

        public static class RangeBuilder extends BaseParent {
            private List<EsRange> ranges;
            private Map<String, EsRange> key2range;

            public RangeBuilder(BaseSearch parent, List<EsRange> ranges, Map<String, EsRange> key2range) {
                this.parent = parent;
                this.ranges = ranges;
                this.key2range = key2range;
            }

            public EsRange key(String key) {
                EsRange range = this.key2range.get(key);
                if (null != range) {
                    return range;
                }
                range = new EsRange(this.parent, key);
                this.ranges.add(range);
                this.key2range.put(key, range);
                return range;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsScroll extends BaseParent {
        private String scroll;

        public EsScroll(BaseSearch parent, String scroll) {
            this.parent = parent;
            this.scroll = scroll;
        }

        public EsScroll scroll(String scroll) {
            this.scroll = scroll;
            return this;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsShould extends BaseParent {
        private String key;
        private Object value;

        public EsShould(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsShould value(Object value) {
            this.value = value;
            return this;
        }

        public static class ShouldBuilder extends BaseParent {
            private List<EsShould> shoulds;
            private Map<String, EsShould> key2should;

            public ShouldBuilder(BaseSearch parent, List<EsShould> shoulds, Map<String, EsShould> key2should) {
                this.parent = parent;
                this.shoulds = shoulds;
                this.key2should = key2should;
            }

            public EsShould key(String key) {
                EsShould should = this.key2should.get(key);
                if (null != should) {
                    return should;
                }
                should = new EsShould(this.parent, key);
                this.shoulds.add(should);
                this.key2should.put(key, should);
                return should;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EsSort extends BaseParent {
        private String key;
        private boolean desc;

        public EsSort(BaseSearch parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        public EsSort desc(boolean desc) {
            this.desc = desc;
            return this;
        }

        public static class SortBuilder extends BaseParent {
            private LinkedHashMap<String, EsSort> sorts;

            public SortBuilder(BaseSearch parent, LinkedHashMap<String, EsSort> sorts) {
                this.parent = parent;
                this.sorts = sorts;
            }

            public EsSort key(String key) {
                EsSort sort = sorts.get(key);
                if (null != sort) {
                    return sort;
                }
                sort = new EsSort(this.parent, key);
                this.sorts.put(key, sort);
                return sort;
            }
        }
    }
}
