package TestCase;


import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tungpham.restclient.RestClientNative;

public class Other {

    public static void main(String[] args) {
        String proxy = "37:43:ẻuieure:user:pass";
        String[] arrProxy = proxy.split(":");
        int lengthProxy = arrProxy.length;

        System.out.println("length " + proxy.split("\\:").length);
        System.out.println("pass " + arrProxy[lengthProxy - 1]);
        System.out.println("user " + arrProxy[lengthProxy - 2]);
        System.out.println(">> " + proxy.indexOf(":" + arrProxy[lengthProxy - 3]));
        System.out.println("ip " + proxy.substring(0, proxy.indexOf(":" + arrProxy[lengthProxy - 3])));

        String url = "https://httpbin.org/get";
        String usg = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

        var client = new RestClientNative()
                .createHttpClient((builderHttpClient) -> {
                    builderHttpClient.connectTimeout(Duration.ofSeconds(150));
                }).setUserAgent(usg);

        client.GET(url, (buider) -> {
        })
                .sendAsync((httpClient, httpReq) -> {
                    
                   return httpClient.sendAsync(httpReq, HttpResponse.BodyHandlers.ofLines()).join();
                })
                .onSuccess((bodyResponse) -> {
                    var streamRes = (Stream<String>) bodyResponse.getResponse().body();
                    var contenty = streamRes.filter(str -> str.length() > 1).collect(Collectors.joining());
                    System.out.println(contenty);
                });

    }
}
