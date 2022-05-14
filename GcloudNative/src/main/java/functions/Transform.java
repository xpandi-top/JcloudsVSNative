
package functions;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import com.google.cloud.storage.*;
//import com.google.gson.Gson;
import com.google.gson.JsonObject;
import saaf.Inspector;

import static basic.GetInformation.*;
import static basic.FileTransform.*;

public class Transform implements HttpFunction {
    static Storage storage;
    private static Long initializeConnectionTime;
    private static final Logger logger = Logger.getLogger(Transform.class.getName());
    @Override
    public void service(HttpRequest request, HttpResponse response)
            throws IOException {
        //collect initial data
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }

        //**************Implementation start*************
        JsonObject body = gson.fromJson(request.getReader(),JsonObject.class);
        boolean connect = true;
        int count = 2;
        if (body.has("objectName")) objectName = body.get("objectName").getAsString(); // if objectName is defined
        if (body.has("count")) count = body.get("count").getAsInt(); // if objectName is defined
        int actual_count = count;
        // build storage
        if (storage==null){
            storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credential)))
                    .build().getService();
            initializeConnectionTime = new Date().getTime();
            System.out.println("start initializing");
        }
        // function
        try(ReadChannel reader = storage.reader(containerName,objectName)) {
            String content = transform(reader);
            uploadObjectFromMemory(storage,containerName,"Transform_"+objectName,content);
        }catch (Exception e){
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
        }
        // collects
        getResponse(response, isMac, inspector, count, actual_count, connect, initializeConnectionTime);
    }

}