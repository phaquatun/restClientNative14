

package tungpham.itf;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



public interface SendReqAsync<T> {

    HttpResponse<T>  send(HttpClient httpClient, HttpRequest httpRequest);
}
