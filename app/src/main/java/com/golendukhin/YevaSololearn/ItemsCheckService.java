package com.golendukhin.YevaSololearn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.golendukhin.YevaSololearn.dataBase.DataBaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import static com.golendukhin.YevaSololearn.FeedActivity.GUARDIAN_REQUEST_URL;

public class ItemsCheckService extends Service {
    private static final int FIRST_ITEM = 1;
    private static final String GUARDIAN_REQUEST = GUARDIAN_REQUEST_URL.concat("&page-size=").concat(String.valueOf(FIRST_ITEM)).concat("&page=").concat(String.valueOf(1));
    private static final int INTERVAL = 10000;
    private static final int READ_TIME_OUT = 1000;
    private static final int CONNECTION_TIME_OUT = 5000;
    private static final int VALID_RESPONSE_CODE = 200;
    private static final String CHANNEL_ID = "channel";
    private static final String REQUEST_METHOD = "GET";
    private final IBinder iBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendNotification(Context context, NotificationManager notificationManager) {
        String notificationChannelId = CHANNEL_ID;
        Intent intent = new Intent(context, FeedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(notificationChannelId);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(notificationChannelId, "", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            builder = new NotificationCompat.Builder(context, notificationChannelId)
                    .setSmallIcon(R.drawable.new_item)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_message))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
        } else {
            builder = new NotificationCompat.Builder(context, notificationChannelId)
                    .setSmallIcon(R.drawable.new_item)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }
        notificationManager.notify(1001, builder.build());
        stopSelf();
    }

    public void startCheck(final Context context, final NotificationManager notificationManager) {
        final DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
        final Handler handler = new Handler();
        Timer timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String feedId = jsonRequest();
                        if (feedId != null) {
                            if (!dataBaseHelper.inWatchedItems(feedId)) {
                                dataBaseHelper.close();
                                sendNotification(context, notificationManager);
                            }
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, INTERVAL, INTERVAL);
    }

    private String jsonRequest() {
        MyAsyncTask task = new MyAsyncTask();
        task.execute();
        String feedId = null;

        try {
            feedId = task.get();
        } catch (Exception ignored) {
        }
        return feedId;
    }

    class LocalBinder extends Binder {
        /**
         * @return ItemsCheckerService so client might start and stop service when app is visible
         */
        ItemsCheckService getService() {
            return ItemsCheckService.this;
        }
    }

    private class MyAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL url = null;
            try {
                url = createUrl();
            } catch (MalformedURLException e) {
            }

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            return extractFeatureFromJson(jsonResponse);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl() throws MalformedURLException {
            URL url;
            try {
                url = new URL(ItemsCheckService.GUARDIAN_REQUEST);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setReadTimeout(READ_TIME_OUT /* milliseconds */);
                urlConnection.setConnectTimeout(CONNECTION_TIME_OUT /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == VALID_RESPONSE_CODE) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
            } catch (IOException ignored) {
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private String extractFeatureFromJson(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject root = jsonObject.getJSONObject("response");
                JSONArray result = root.getJSONArray("results");
                JSONObject item = result.getJSONObject(0);
                return item.getString("id");
            } catch (JSONException ignored) {
            }
            return null;
        }
    }
}