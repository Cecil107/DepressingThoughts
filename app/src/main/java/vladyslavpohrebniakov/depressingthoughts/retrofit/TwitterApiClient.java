package vladyslavpohrebniakov.depressingthoughts.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TwitterApiClient {

    String BASE_URL = "https://api.twitter.com/";
    String CLIENT_CREDENTIALS = "client_credentials";
    int USER_ID = 198644497;
    int COUNT = 200;

    @FormUrlEncoded
    @POST("oauth2/token")
    Call<OAuthToken> postCredentials(
            @Field("grant_type") String grantType
    );

    @GET("/1.1/statuses/user_timeline.json?exclude_replies=true&include_rts=false")
    Call<List<Tweet>> getTweet(
            @Query("user_id") int userId,
            @Query("count") int count
    );
}