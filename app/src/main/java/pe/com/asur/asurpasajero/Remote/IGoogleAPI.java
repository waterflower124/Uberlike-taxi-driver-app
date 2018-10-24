package pe.com.asur.asurpasajero.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by agus on 14/03/2018.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}