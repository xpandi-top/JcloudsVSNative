package lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import saaf.Inspector;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

import static basic.GetInformation.*;
import static basic.JcloudBasics.readBlobContent;
import static basic.FileTransform.*;
import static basic.JcloudBasics.writeBlob;

public class Transform implements RequestHandler<Request, HashMap<String, Object>> {
    static BlobStoreContext blobContext;
    static BlobStore blobStore;
    private static Long initializeConnectionTime;

    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        //*******************collect initial data
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        if (request!=null&&request.getObjectName()!=null) objectName = request.getObjectName();
        int count = 2;
        boolean connect = true;
        if (request!=null && request.getCount()>0) count = request.getCount();
        int actual_count = count;
        // Initialize Jclouds
        if (blobContext==null){
            blobContext = ContextBuilder.newBuilder(provider)
                    .credentials(identity, credential)
                    .buildApi(BlobStoreContext.class);
            blobStore = blobContext.getBlobStore();
            initializeConnectionTime = new Date().getTime();
            System.out.println("start initialzing");
        }
        // start transform
        try {
            InputStream inputStream = readBlobContent(blobStore,objectName);
            writeBlob(blobStore,"Transform_"+objectName,transform(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            connect = false;
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
        }

        //****************END FUNCTION IMPLEMENTATION***************************
        //Collect final information such as total runtime and cpu deltas.
        getMetrics(isMac,inspector,count,actual_count,connect,initializeConnectionTime);
        return inspector.finish();
    }
    public static void main(String[] args) {
        Transform awsPutToS3 = new Transform();
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
