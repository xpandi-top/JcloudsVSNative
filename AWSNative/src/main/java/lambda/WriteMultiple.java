package lambda;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import saaf.Inspector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

import static basic.GetInformation.*;

public class WriteMultiple implements RequestHandler<Request, HashMap<String,Object>> {
    private static Long initializeConnectionTime;
    static BasicAWSCredentials awsCred = new BasicAWSCredentials(identity, credential);
    static AmazonS3 s3Client;
    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        //*******************collect initial data
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //***********Function start
        if (request!=null&&request.getObjectName()!=null) objectName = request.getObjectName();
        int count = 2;
        boolean connect = true;
        if (request!=null && request.getCount()>0) count = request.getCount();
        int actual_count = count;
        // Initialize s3Client
        if (s3Client==null){
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCred))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            initializeConnectionTime = new Date().getTime();
        }
        // wrtie multiple objects
        for (int i=0; i<count;i++){
            String content = "MyKey"+i;
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(bytes.length);
            meta.setContentType("text/csv");
            try{
                s3Client.putObject(containerName, content, inputStream, meta);

            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace();
                actual_count = i;
                connect = false;
                inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
                break;
            }
        }
        //Collect final information such as total runtime and cpu deltas.
        getMetrics(isMac, inspector, count, actual_count, connect, initializeConnectionTime);
        return inspector.finish();
    }

    public static void main(String[] args) {
        WriteMultiple readMultiple = new WriteMultiple();
        HashMap<String,Object> res = readMultiple.handleRequest(new Request(6), new Context() {
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
        });
        System.out.println(res);

    }
}
