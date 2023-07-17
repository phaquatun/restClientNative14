

package tungpham.itf;

import java.net.http.HttpResponse;
import tungpham.restclient.BodyResponse;



public interface OnFailed  {
    void onFailed(String process,int status ,BodyResponse bodyResponse );
}
