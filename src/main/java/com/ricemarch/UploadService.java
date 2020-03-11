package com.ricemarch;

import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UploadService {


    int postMoment(String title, String content, String username, ArrayList<String> imgUrls) {

        String BASE_URL = "http://192.168.2.106:8081/moments/add";

        int statusCode = 0;

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MICROSECONDS).build();
        String subimgurl = String.valueOf(imgUrls).substring(1, String.valueOf(imgUrls).length() - 1);
        FormBody formBody = new FormBody.Builder()
                .add("name", username)
                .add("title", title)
                .add("content", content)
                .add("imgs", subimgurl)
                .build();

        Request request = new Request.Builder()
                .post(formBody)
                .url(BASE_URL)
                .build();
        Call task = client.newCall(request);
        task.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Log.d(TAG, "onFailure " + e.toString());
                System.out.println("onFailure " + e.toString());

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println(response.protocol() + " " + response.code() + " " + response.message());
                System.out.println("onResponse: " + response.body().toString());
            }
        });
        return statusCode;
    }
}
