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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static basic.GcloudBasics.deleteObject;
import static basic.GetInformation.*;

public class DeleteMultiple implements HttpFunction {
    private static long initializeConnectionTime;
    private static Storage storage;

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //***********Function start
        // get parameters
        boolean connect = true;
        int count = 2;
        JsonObject body = gson.fromJson(httpRequest.getReader(),JsonObject.class);
        if (body.has("count")) count = body.get("count").getAsInt(); // if count is defined
        if (body.has("objectName")) objectName = body.get("objectName").getAsString(); // if objectName is defined
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
                deleteObject(storage,containerName,content);
            } catch (Exception e) {
                e.printStackTrace();
                actual_count = i;
                connect = false;
                inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
                break;
            }
        }
        // collect metrics
        getResponse(httpResponse, isMac, inspector, count, actual_count, connect, initializeConnectionTime);

    }
}
