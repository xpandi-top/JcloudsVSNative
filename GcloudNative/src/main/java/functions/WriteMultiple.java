package functions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonObject;
import saaf.Inspector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static basic.GetInformation.*;

public class WriteMultiple implements HttpFunction {
    private static  long initializeConnectionTime;
    private static Storage storage;

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //***********Function start
        // read file from cloud storage
        boolean connect = true;
        int count = 2;
        JsonObject body = gson.fromJson(httpRequest.getReader(),JsonObject.class);
        // The ID of your GCS object
        if (body.has("objectName")) objectName = body.get("objectName").getAsString(); // if objectName is defined
        if (body.has("count")) count = body.get("count").getAsInt(); // if count is defined
        int actual_count = count;
        // initialize storage
        if (storage==null){
            storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credential)))
                    .build().getService();
            initializeConnectionTime = new Date().getTime();
            System.out.println("start initializing");
        }
        for (int i=0; i<count;i++){
            String content = "MyKey"+i;
            try {
                uploadObjectFromMemory(storage,containerName,content,content);
            } catch (IOException e) {
                e.printStackTrace();
                actual_count = i;
                connect = false;
                inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
                break;
            }
        }
        // finish
        getResponse(httpResponse, isMac, inspector, count, actual_count, connect, initializeConnectionTime);

    }
}
