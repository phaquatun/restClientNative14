package tungpham.restclient;

import java.net.CookieManager;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import tungpham.itf.StreamCookie;
import tungpham.itf.StreamHeader;

@Getter
public class BodyResponse<T> {

    HttpResponse<T> response;
    CookieManager cookieManager;

    public BodyResponse setCookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
        return this;
    }

    public BodyResponse(HttpResponse<T> response) {
        this.response = response;
    }

    public BodyResponse streamCookie(StreamCookie cookie) {
//        System.out.println(">> cookie in cookieManager " + cookieManager.getCookieStore().getCookies().toString());

        response.headers().allValues("set-cookie").forEach((t) -> {
//            System.out.println(">> check value set-cookie " + t);
            var formCookie = new FormSetCookieRes(t);
            cookie.handle(formCookie.getName(), formCookie.getValue());
        });
        return this;
    }

    public BodyResponse streamHeader(StreamHeader header) {

        var mapHeader = response.headers().map();
        for (Map.Entry<String, List<String>> entry : mapHeader.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            header.handle(key, value);
        }
        return this;
    }

}
