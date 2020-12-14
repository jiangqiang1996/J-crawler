package xin.jiangqiang.entities;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 存取从page中提取的下一代爬虫的信息
 */
@Data
@Accessors(chain = true)
public class Next extends Crawler {
}
