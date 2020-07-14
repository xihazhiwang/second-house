package bo.han.house.spider;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @auth: hanbo
 * @date: 2020-07-13 12:39
 * @desc：获取房天下西安二手房信息
 */
public class FangtianxiaSpider {

    private static final String base_url = "https://xian.esf.fang.com/housing/__0_39_0_0_%d_0_0_0";

    public static void main(String[] args) throws Exception {

        for (int i = 39; i <= 100; i++) {
            String url = String.format(base_url, i);
            try {
                saveEntity(url);
            } catch (Exception e) {
                saveEntity(url);
            }
            Thread.sleep(20000);
        }
    }

    private static void saveEntity(String url) throws Exception {
        Document document = getDocument(url);
        Elements houseList = document.getElementsByClass("houseList");
        if (houseList != null && !houseList.isEmpty()) {
            Element element = houseList.get(0);
            houseList = element.getElementsByAttributeValueStarting("id", "houselist_");
            for (Element house : houseList) {
                if (house.html().contains("pageNow")) {
                    continue;
                }
                String dataBgcomare = house.attr("data-bgcomare");
                String id = (String) JSONObject.parseObject(dataBgcomare).get("newcode");

                // 详情页面和小区名称
                Elements detailUrlAndName = house.select("dl > dd > p:nth-child(1) > a.plotTit");
                // 地址
                Elements address = house.select("dl > dd > p:nth-child(2)");
                // 详细地址
                String detailAddress = address.html();
                SpiderHouseProperty build = SpiderHouseProperty.builder()
                        .id(id)
                        .updateDate(new Date())
                        .source("fangtianxia")
                        .name(StringUtils.trim(detailUrlAndName.html()))
                        .detailUrl("http:" + detailUrlAndName.attr("href"))
                        .address(String.format("[%s-%s] %s", address.select("a:first-child").html().trim(), address.select("a:last-child").html().trim(), detailAddress.substring(detailAddress.lastIndexOf("</a>") + 4).trim()))
                        .price(new BigDecimal(Optional.of(house.select("div > p.priceAverage > span:nth-child(1)").html()).map(a -> a.replace("暂无均价", "")).filter(StringUtils::isNotBlank).orElse("0")))
                        .year(Integer.parseInt(Optional.of(house.select("dl > dd > ul > li:nth-child(3)").html().replace("年建成", "").replace("暂无资料", "")).map(StringUtils::trimToEmpty).filter(StringUtils::isNotBlank).orElse("0")))
                        .build();

                List<Entity> entities = Db.use().find(Entity.create()
                        .setTableName("residence")
                        .set("id", build.getId()));
                if (entities.isEmpty()) {
                    Db.use().insert(Entity.create()
                            .setTableName("residence")
                            .parseBean(build, true, true));
                }
            }
        }

    }

    private static Document getDocument(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .timeout(6000)
                .method(Connection.Method.GET)
                .followRedirects(true)
                .get();
        String href = document.getElementsByClass("btn-redir").attr("href");
        System.out.println("=========================================最终访问路径：" + href);
        return Jsoup.connect(href)
                .timeout(60000)
                .method(Connection.Method.GET)
                .followRedirects(true)
                .get();
    }

}
