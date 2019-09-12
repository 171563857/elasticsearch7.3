package cn.gol.es.service;

import cn.gol.es.entity.R;
import cn.gol.es.entity.EsSearch;

import java.util.List;

public interface IBaseSearchService<T> {
	R findById(String id);

	R findByIds(List<String> ids);

	R create(T t);

	R batchCreate(List<T> list);

	R update(T t);

	R batchUpdate(List<T> list);

	R delete(String id);

	R batchDelete(List<String> ids);

	R select(EsSearch search);
}
