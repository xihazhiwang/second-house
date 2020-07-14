package bo.han.house.spider;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @auth: hanbo
 * @date: 2020-07-12 22:58
 * @desc: 小区
 */
@Data
@Builder
public class SpiderHouseProperty implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 小区名称
     */
    private String name;

    /**
     * 页面地址
     */
    private String detailUrl;

    /**
     * 地址
     */
    private String address;

    /**
     * 价格  元/平米
     */
    private BigDecimal price;

    /**
     * 竣工日期
     */
    private Integer year;

    /**
     * 最后更新日期
     */
    private Date updateDate;

    /**
     * 数据来源
     */
    private String source;
}
