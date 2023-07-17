package tungpham.restclient;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormSetCookieRes {

    String valueHeader, name, value, domain, path, expires, httpOnly, secure, maxAge;
    String[] arr;
    Map<String, String> mapCookieRes;

    public FormSetCookieRes(String valueHeader) {
        this.valueHeader = valueHeader;
        handle();
    }

    public FormSetCookieRes(String... val) {
        this.arr = val;
    }

    public FormSetCookieRes(Map<String, String> mapCookieRes) {
        this.mapCookieRes = mapCookieRes;
    }

    public String mapCookiesResToString() {
        return mapCookieRes.entrySet().stream().map((t) -> {
            return t.getKey().concat("=").concat(t.getValue());
        }).collect(Collectors.joining("; "));
    }

//    public FormSetCookieRes handle(String valueHeader) {
//
//        for (String string : arr) {
//            if (valueHeader.contains(string.concat("="))) {
//                String[] arrV = valueHeader.split(";");
//                for (String string1 : arrV) {
//                    if (string1.contains(string.concat("="))) {
//                        value = string1.replace(string.concat("="), "");
//                    }
//                }
//                break;
//            }
//        }
//
//        return this;
//    }
    void handle() {
        arr = valueHeader.split(";");
        for (String string : arr) {

            String element = string.trim().toLowerCase();
//            System.out.println(">> element " + element);

            if (element.contains("domain=")) {
                domain = element.replace("domain=", "");
            }
            if (element.contains("expires=")) {
                expires = element.replace("expires=", "");
            }
            if (element.contains("httpOnly")) {
                httpOnly = "httpOnly";
            }
            if (element.contains("secure")) {
                secure = "secure";
            }
            if (element.contains("max-age=")) {
                maxAge = element.replace("max-age=", "");
            }
            if (element.contains("path=")) {
//                int local = string.indexOf("path=");
                path = element.replace("path=", "");
            }
            boolean check = !element.contains("domain=") & !element.contains("expires=") & !element.contains("httpOnly")
                    & !element.contains("secure")
                    & !element.contains("max-age=")
                    & !element.contains("path=");

            if (check & element.contains("=")) {
//                System.out.println(">> element need " + element);
                int local = string.indexOf("=");
                name = element.substring(0, local);
                value = element.substring(local + 1);
            }
        }
    }

    @Override
    public String toString() {
        return "FormSetCookieResponse{" + "valueHeader=" + valueHeader + ", name=" + name + ", value=" + value + ", domain=" + domain + ", path=" + path + ", expires=" + expires + ", httpOnly=" + httpOnly + ", secure=" + secure + ", maxAge=" + maxAge + '}';
    }
}
