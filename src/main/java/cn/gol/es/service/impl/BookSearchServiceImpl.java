package cn.gol.es.service.impl;

import cn.gol.es.entity.Book;
import cn.gol.es.entity.EsSearch;
import cn.gol.es.entity.R;
import cn.gol.es.service.IBookSearchService;
import cn.gol.es.service.abstracts.AbstractElasticSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class BookSearchServiceImpl extends AbstractElasticSearch<Book> implements IBookSearchService {
    private static final String INDEX = "book";
    private static final Class CLAZZ = Book.class;

    @Override
    protected String getIndex() {
        return INDEX;
    }

    @Override
    protected Class getClazz() {
        return CLAZZ;
    }

    @Override
    protected String getId(Book book) {
        return null == book || null == book.getBookNo() ? null : book.getBookNo().toString();
    }

    @Override
    public R findById(String id) {
        return super.findById(id);
    }

    @Override
    public R findByIds(List<String> ids) {
        return super.findByIds(ids);
    }

    @Override
    public R create(Book book) {
        return super.create(book);
    }

    @Override
    public R batchCreate(List<Book> list) {
        for (int i = 0; i < 3000; i++) {
            list = new ArrayList<>();
            for (int j = 0; j < 10000; j++) {
                list.add(Book.builder().bookNo(ThreadLocalRandom.current().nextInt()).bookName(i + "").bookCover(UUID.randomUUID().toString() + ".png").build());
            }
            super.batchCreate(list);
        }
        return R.ok();
    }

    @Override
    public R update(Book book) {
        return super.update(book);
    }

    @Override
    public R batchUpdate(List<Book> list) {
        return super.batchUpdate(list);
    }

    @Override
    public R delete(String id) {
        return super.delete(id);
    }

    @Override
    public R batchDelete(List<String> ids) {
        return super.batchDelete(ids);
    }

    @Override
    public R select(EsSearch search) {
        return super.select(search);
    }
}
