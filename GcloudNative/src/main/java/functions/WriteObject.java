package functions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import saaf.Inspector;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static basic.FileTransform.fileGenerate;
import static basic.GcloudBasics.uploadObjectFromMemory;
import static basic.GetInformation.*;

public class WriteObject implements HttpFunction {
    private static long initializeConnectionTime;
    private static Storage storage;
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //**************Implementation start*************
        // get parameters
        JsonObject body = gson.fromJson(httpRequest.getReader(),JsonObject.class);
        boolean connect = true;
        int count = 2;
        if (body.has("count")) count = body.get("count").getAsInt(); // if objectName is defined
        objectName = "upload_jcloud_" + count + ".csv";
        if (body.has("objectName")) objectName = body.get("objectName").getAsString(); // if objectName is defined
        int actual_count = count;
        // Build storage
        if (storage==null){
            storage = StorageOptions
                    .newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credential)))
                    .build().getService();
            initializeConnectionTime = new Date().getTime();
            System.out.println("start initializing");
        }
        // function write a file
        try {
            StringWriter sw = new StringWriter();
            fileGenerate(sw,count);
            uploadObjectFromMemory(storage,containerName, objectName,sw.toString());
        }catch (Exception e){
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
        }
        //*********************Function end
        getResponse(httpResponse,isMac,inspector,count,actual_count,connect,initializeConnectionTime);
    }
}
