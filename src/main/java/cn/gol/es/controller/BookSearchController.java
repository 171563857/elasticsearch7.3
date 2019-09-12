package cn.gol.es.controller;


import cn.gol.es.entity.EsSearch;
import cn.gol.es.service.IBookSearchService;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/")
public class BookSearchController {
    @Autowired
    private IBookSearchService bookSearchService;

    public static void main(String[] args) {
        EsSearch search = EsSearch.builder().must().key("bookName").value("你好")
                .and().not().key("bookAuhtor").value("小公举")
                .and().or().key("bookId").value("1").value("2").value("3")
                .and().range().key("date").from("20190820").to("20190830").format("yyyyMMdd")
                .and().should().key("uploadName").value("小公举风花雪月")
                .and().sort().key("bookId").desc(true)
                .and().page().from(1111).size(111)
                .and().scroll("123")
                .build();
        System.out.println(JSONObject.toJSONString(search));
    }

    @GetMapping("test")
    public Object test() {
        return bookSearchService.batchCreate(null);
    }
}
