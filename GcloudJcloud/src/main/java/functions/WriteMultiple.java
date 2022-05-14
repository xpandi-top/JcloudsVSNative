
package functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.JsonObject;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import saaf.Inspector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import static basic.GetInformation.*;
import static basic.JcloudBasics.writeBlob;

public class WriteMultiple implements HttpFunction {
    static BlobStoreContext blobContext;
    static BlobStore blobStore;
    private static Long initializeConnectionTime;
//    private static final Logger logger = Logger.getLogger(Transform.class.getName());
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
        //initialize Jclouds
        if (blobContext==null){
            blobContext = ContextBuilder.newBuilder(provider)
                    .credentials(identity, credential)
                    .buildApi(BlobStoreContext.class);
            blobStore = blobContext.getBlobStore();
            initializeConnectionTime = new Date().getTime();
            System.out.println("start initializing");
        }
        // start write
        for (int i=0; i<count;i++) {
            try {
                String content = "MyKey"+i;
                writeBlob(blobStore, content,content);
            } catch (Exception e) {
                e.printStackTrace();
                connect = false;
                actual_count = i;
                inspector.addAttribute("duration", new Date().getTime() - initializeConnectionTime);
                break;
            }
        }
        // Get result
        BufferedWriter writer = response.getWriter();
        getMetrics(isMac,inspector,count,actual_count,connect,initializeConnectionTime);
        String res = gson.toJson(inspector.finish());
        res = res.replace("\\n","_");
        res = res.replace("\n"," ");
        writer.write(res);
    }

}