package xin.jiangqiang.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Data
@Accessors(chain = true)
public class Page extends Crawler {
    private Integer responseCode;
    private Protocol protocol;
    private String message;
    private Headers headers;
    private ResponseBody body;
    private Request request;
    private byte[] content;
    private String html;
    private Document document;

    public Page(Integer responseCode, Protocol protocol, String message, Headers headers, ResponseBody body, Request request, byte[] content, String html) {
        this.responseCode = responseCode;
        this.protocol = protocol;
        this.message = message;
        this.headers = headers;
        this.body = body;
        this.request = request;
        this.content = content;
        this.html = html;
        this.document = Jsoup.parse(html);
        this.setUrl(request.url().url().toString());
    }

}