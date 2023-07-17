package tungpham.restclient;

import lombok.Getter;

@Getter
public class HandlerlProxy {

    String proxy, ip, user, pass;
    int port;

    boolean proxyAuthen;

    public HandlerlProxy(String proxy) {
        this.proxy = proxy;
        handle();
    }

    void handle() {
        if (!proxy.contains(":")) {
            return;
        }
        if (proxy.split(":").length == 3) {
            throw new RuntimeException("err proxy length ==3 . can not parse proxy " + proxy);
        }
        proxyAuthen = proxy.split(":").length > 3 ? true : false;

        String[] arrProxy = proxy.split(":");
        int lengthProxy = arrProxy.length;
        if (proxyAuthen) {
            pass = arrProxy[lengthProxy - 1];
            user = arrProxy[lengthProxy - 2];
            port = Integer.valueOf(arrProxy[lengthProxy - 3]);
            ip = proxy.substring(0, proxy.indexOf(":" + arrProxy[lengthProxy - 3]));
        } else {
            port = Integer.valueOf(arrProxy[lengthProxy - 1]);
            ip = arrProxy[lengthProxy - 2];
        }

    }

}
