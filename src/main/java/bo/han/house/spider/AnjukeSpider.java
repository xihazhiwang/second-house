package bo.han.house.spider;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @auth: hanbo
 * @date: 2020-07-12 22:40
 * @desc：获取安居客西安二手房小区信息
 */
public class AnjukeSpider {

    public static String base_url = "https://xa.anjuke.com/community/";

    public static String header_user_agent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";

    public static void main(String[] args) throws Exception {
        realSpider();
    }

    public static List<SpiderHouseProperty> realSpider() throws Exception {
        List<SpiderHouseProperty> list = new ArrayList<>();
        int page = 1;
        while (page <= 50) {
            String spiderUrl = base_url + "p" + page;
            System.out.println("开始爬取URL: " + spiderUrl);
            Connection connect = Jsoup.connect(spiderUrl);
            connect.header("User-Agent", header_user_agent);
            Connection.Response execute = connect.execute();
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 11086));
            connect.proxy(proxy);

            Document document = execute.parse();

            for (Element soj : document.getElementsByAttributeValue("_soj", "xqlb")) {
                System.out.println("成功爬取" + spiderUrl + "内容");
                String attrA = soj.selectFirst("a").attr("href");
                String id = attrA.substring(attrA.lastIndexOf("/") + 1);
                List<Entity> entities = Db.use().find(Entity.create("residence").set("id", id));
                if (entities.isEmpty()) {
                    SpiderHouseProperty build = SpiderHouseProperty
                            .builder()
                            .id(id)
                            .detailUrl(attrA)
                            .name(soj.selectFirst("a").attr("title"))
                            .address(soj.selectFirst(".li-info").selectFirst("address").html().trim())
                            .year(Integer.parseInt(Optional.of(soj.selectFirst("p").html().substring(0, 9).trim().replace("竣工日期：", "").replace("暂无数据", "")).filter(StringUtils::isNotBlank).orElse("0")))
                            .price(new BigDecimal(soj.selectFirst("strong").html()))
                            .updateDate(new Date())
                            .build();
                    System.out.println(build);
                    Db.use().insert(Entity.create()
                            .parseBean(build, true, true)
                            .setTableName("residence")
                    );
                }
            }
            Thread.sleep(30000);
            page++;
        }
        return list;
    }
}
