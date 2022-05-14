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
import static basic.FileTransform.*;

public class WriteObject implements RequestHandler<Request, HashMap<String,Object>> {
    static AmazonS3 s3Client;
    static BasicAWSCredentials awsCreds = new BasicAWSCredentials(identity, credential);
    private static Long initializeConnectionTime;

    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //****************START FUNCTION IMPLEMENTATION*************************
        int count = 256;
        if (request!=null && request.getCount()>0) count = request.getCount();
        objectName = "upload_native_" + count + ".csv";
        if (request!=null&&request.getObjectName()!=null) objectName = request.getObjectName();
        boolean connect = true;
        int actual_count = count;
        StringWriter sw = new StringWriter();
        fileGenerate(sw,count);
        // create file and upload to s3
        byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(bytes.length);
        meta.setContentType("text/csv");
        // Initialize s3Client
        if (s3Client==null){
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            initializeConnectionTime = new Date().getTime();
        }
        // put object to s3
        try {
            s3Client.putObject(containerName, objectName, inputStream, meta);
        }catch (Exception e){
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
            e.printStackTrace();
            connect = false;
        }
        //*********************Function end
        getMetrics(isMac,inspector,count,actual_count,connect,initializeConnectionTime);
        return inspector.finish();
    }

    public static void main(String[] args){
        WriteObject writeObject = new WriteObject();
        System.out.println(writeObject.handleRequest(new Request(), new Context() {
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
