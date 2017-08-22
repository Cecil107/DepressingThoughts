/*
 * Copyright 2017 Vladyslav Pohrebniakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vladyslavpohrebniakov.depressingthoughts.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vladyslavpohrebniakov.depressingthoughts.R;
import vladyslavpohrebniakov.depressingthoughts.activities.MainActivity;
import vladyslavpohrebniakov.depressingthoughts.retrofit.ApiKeys;
import vladyslavpohrebniakov.depressingthoughts.retrofit.OAuthToken;
import vladyslavpohrebniakov.depressingthoughts.retrofit.Tweet;
import vladyslavpohrebniakov.depressingthoughts.retrofit.TwitterApiClient;

public class AppWidget extends AppWidgetProvider {

    private TwitterApiClient twitterApi;
    private OAuthToken token;
    private Call<OAuthToken> oAuthTokenCall;
    private Call<List<Tweet>> getTweetsCall;
    private String credentials = Credentials.basic(ApiKeys.CONSUMER_KEY, ApiKeys.CONSUMER_SECRET);

    private List<Tweet> tweets;
    private Random random = new Random();

    private RemoteViews views;

    void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        final CharSequence widgetText = context.getString(R.string.appwidget_text_loading);
        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.appwidgetText, widgetText);

        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.updateButton, updatePendingIntent);

        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidgetText, activityPendingIntent);

        createTwitterApi();

        oAuthTokenCall = twitterApi.postCredentials(TwitterApiClient.CLIENT_CREDENTIALS);
        getTweetsCall = twitterApi.getTweet(TwitterApiClient.USER_ID, TwitterApiClient.COUNT);

        oAuthTokenCall.enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                if (response.isSuccessful()) {
                    token = response.body();
                    views.setTextViewText(R.id.appwidgetText, token.toString());
                    getTweet(context);
                }
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                t.printStackTrace();
            }
        });

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, AppWidget.class);
            int[] appWidgetIds = mgr.getAppWidgetIds(cn);
            onUpdate(context, mgr, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    private void getTweet(final Context context) {
        getTweetsCall.clone().enqueue(new Callback<List<Tweet>>() {
            @Override
            public void onResponse(Call<List<Tweet>> call, Response<List<Tweet>> response) {
                tweets = response.body();
                if (tweets != null) {
                    int index = random.nextInt(tweets.size());
                    views.setTextViewText(R.id.appwidgetText, tweets.get(index).getText());
                    final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                    final ComponentName cn = new ComponentName(context, AppWidget.class);
                    mgr.updateAppWidget(cn, views);
                }
            }

            @Override
            public void onFailure(Call<List<Tweet>> call, Throwable t) {
                Log.e(getClass().getSimpleName(), t.getMessage());
            }
        });
    }

    private void createTwitterApi() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                        token != null ? token.getAuthorization() : credentials);

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterApiClient.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        twitterApi = retrofit.create(TwitterApiClient.class);
    }
}

