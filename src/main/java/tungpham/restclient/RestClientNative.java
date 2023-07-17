package tungpham.restclient;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import tungpham.itf.CreateHttpClient;
import tungpham.itf.OnFailed;
import tungpham.itf.OnSuccess;
import tungpham.itf.RequestGet;
import tungpham.itf.RequestPost;
import tungpham.itf.SendReqAsync;

public class RestClientNative<T> {

    HttpClient httpClient;
    HttpClient.Builder builderHttp;
    String proxy, userAgent, boundary = new BigInteger(256, new Random()).toString();

    HttpRequest httpRequest;
    HttpRequest.Builder builderRequest;

    CookieManager cookieManager;

    BodyResponse bodyResponse;
    HttpResponse<T> httpResponse;

    public RestClientNative createHttpClient(CreateHttpClient createClient) {

        System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        builderHttp = HttpClient.newBuilder();

        createClient.createHttp(builderHttp);

        handleProxy();
        if (cookieManager != null) {
            builderHttp.cookieHandler(cookieManager);
        }
        httpClient = builderHttp.build();

        return this;
    }

    public RestClientNative cookieManager() {
        cookieManager = new CookieManager();
        return this;
    }

    public RestClientNative addProxy(String proxy) {
        this.proxy = proxy;
        return this;
    }

    public RestClientNative setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public RestClientNative setBoundary(String boundary) {
        this.boundary = boundary;
        return this;
    }

    RestClientNative handleProxy() {
        if (proxy == null) {
            return this;
        }

        System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

        var handleProxy = new HandlerlProxy(proxy);

        builderHttp.proxy(ProxySelector.of(new InetSocketAddress(handleProxy.getIp(), handleProxy.port)));
        if (handleProxy.isProxyAuthen()) {
            String user = handleProxy.getUser();
            String pass = handleProxy.getPass();

            builderHttp.authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass.toCharArray());
                }

            });
        }
        return this;
    }

    /*
    *** set Get request
     */
    public RestClientNative GET(String url, RequestGet request) {

        builderRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("User-Agent", userAgent);

        request.handleGet(builderRequest);

        httpRequest = builderRequest.build();

        return this;
    }

    /*
    *** set Post request
     */
    public RestClientNative POST(String url, RequestPost request) {
        builderRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("User-Agent", userAgent);

        request.handlePost(builderRequest);

        httpRequest = builderRequest.build();
        return this;
    }

    public RestClientNative POST(String url, String json, RequestPost request) {
        builderRequest = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .uri(URI.create(url))
                .setHeader("User-Agent", userAgent);

        request.handlePost(builderRequest);

        httpRequest = builderRequest.build();
        return this;
    }

    public RestClientNative POST(String url, Map<String, String> data, RequestPost request) {
        builderRequest = HttpRequest.newBuilder().POST(ofFormDataText(data))
                .uri(URI.create(url))
                .setHeader("User-Agent", userAgent);

        request.handlePost(builderRequest);

        httpRequest = builderRequest.build();
        return this;
    }

    public RestClientNative POSTFile(String url, Map<Object, Object> data, RequestPost request) {
        try {
            builderRequest = HttpRequest.newBuilder().POST(ofFormDataFile(data, boundary))
                    .uri(URI.create(url))
                    .setHeader("User-Agent", userAgent);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        request.handlePost(builderRequest);

        httpRequest = builderRequest.build();
        return this;
    }

    HttpRequest.BodyPublisher ofFormDataFile(Map<Object, Object> data, String boundary) throws IOException {
        // Result request body
        List<byte[]> byteArrays = new ArrayList<>();

        // Separator with boundary
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

        // Iterating over data parts
        for (Map.Entry<Object, Object> entry : data.entrySet()) {

            // Opening boundary
            byteArrays.add(separator);

            // If value is type of Path (file) append content type with file name and file binaries, otherwise simply append key=value
            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }

        // Closing boundary
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

        // Serializing as byte array
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    HttpRequest.BodyPublisher ofFormDataText(Map<String, String> data) {
        String value = data.keySet().stream()
                .map((key) -> encodeKey(key).concat("=") + encodeValue(data.get(key)))
                .collect(Collectors.joining("&"));

        return HttpRequest.BodyPublishers.ofString(value);

    }

    String encodeKey(String key) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public RestClientNative sendAsync(SendReqAsync reqAsyn) {
        this.httpResponse = reqAsyn.send(httpClient, httpRequest);
        return this;
    }

    public RestClientNative onSuccess(OnSuccess success) {
        int statusCode = httpResponse.statusCode();
        bodyResponse = new BodyResponse(httpResponse).setCookieManager(cookieManager);
        if (statusCode == 200) {
            success.onSuccess(bodyResponse);
        }

        return this;
    }

    public RestClientNative onFailed(OnFailed failed) {
        failed.onFailed("", httpResponse.statusCode(), bodyResponse);
        return this;
    }

    public RestClientNative onFailed(OnFailed failed, String process) {
        int statusCode = httpResponse.statusCode();
        if (statusCode != 200) {
            failed.onFailed(process, httpResponse.statusCode(), bodyResponse);
        }
        return this;
    }
}
