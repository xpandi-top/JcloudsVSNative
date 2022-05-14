package lambda;

import com.amazonaws.services.lambda.runtime.*;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import saaf.Inspector;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;

import static basic.FileTransform.fileGenerate;
import static basic.GetInformation.*;
import static basic.JcloudBasics.writeBlob;

public class WriteObject implements RequestHandler<Request, HashMap<String, Object>> {
    static BlobStoreContext blobContext;
    static BlobStore blobStore;
    static Long initializeConnectionTime;
    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        //*******************collect initial data
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        // Get values from request
        int count = 2;
        if (request!=null && request.getCount()>0) count = request.getCount();
        boolean connect = true;
        objectName = "upload_jcloud_" + count + ".csv";
        if (request!=null&&request.getObjectName()!=null) objectName = request.getObjectName();
        int actual_count = count;
        // Initialize Jclouds
        if (blobContext==null){
            blobContext = ContextBuilder.newBuilder(provider)
                    .credentials(identity, credential)
                    .buildApi(BlobStoreContext.class);
            blobStore = blobContext.getBlobStore();
            initializeConnectionTime = new Date().getTime();
        }
        try {
            StringWriter sw=new StringWriter();
            fileGenerate(sw,count);
            writeBlob(blobStore,objectName,sw.toString());
        }catch (Exception e){
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        getMetrics(isMac, inspector, count, actual_count, connect, initializeConnectionTime);
        return inspector.finish();
    }
    public static void main(String[] args) {
        WriteObject awsPutToS3 = new WriteObject();
        Request request = new Request();
        System.out.println(awsPutToS3.handleRequest(request, new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        }));

    }

}
