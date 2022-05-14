package functions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonObject;
import saaf.Inspector;

import java.io.FileInputStream;
import java.util.Date;

import static basic.GcloudBasics.createContainer;
import static basic.GcloudBasics.deleteContainer;
import static basic.GetInformation.*;

public class DeleteContainers implements HttpFunction {
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
        for (int i=0; i<count;i++) {
            try {
                String bucketName = "native_dimo_test_bucket_"+i;
                deleteContainer(storage,bucketName);
            } catch (Exception e) {
                e.printStackTrace();
                actual_count=i;
                connect = false;
                inspector.addAttribute("duration", new Date().getTime() - initializeConnectionTime);
                break;
            }
        }
        //*********************Function end
        getResponse(httpResponse,isMac,inspector,count,actual_count,connect,initializeConnectionTime);
    }
}
