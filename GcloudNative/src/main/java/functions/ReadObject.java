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
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static basic.GetInformation.*;

public class ReadObject implements HttpFunction {
    private static long initializeConnectionTime;
    private static Storage storage;
    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //*************FunctionStart**************
        // get parameters
        boolean connect = true;
        int count = 2;
        JsonObject body = gson.fromJson(httpRequest.getReader(),JsonObject.class);
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
        // function
        try(ReadChannel reader = storage.reader(containerName,objectName)) {
            BufferedReader br = new BufferedReader(Channels.newReader(reader, StandardCharsets.UTF_8));
            br.lines().forEach(System.out::println);
        }catch (Exception e){
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
        }
        //*********************Function end
        getResponse(httpResponse,isMac,inspector,count,actual_count,connect,initializeConnectionTime);
    }
}
