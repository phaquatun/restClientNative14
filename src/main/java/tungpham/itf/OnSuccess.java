package tungpham.itf;

import java.net.http.HttpResponse;
import tungpham.restclient.BodyResponse;

public interface OnSuccess<T> {

    void onSuccess(BodyResponse bodyResponse);
}
