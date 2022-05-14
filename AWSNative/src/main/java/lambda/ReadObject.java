package lambda;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import saaf.Inspector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static basic.GetInformation.*;

public class ReadObject implements RequestHandler<Request, HashMap<String,Object>> {
    static AmazonS3 s3Client;
    static BasicAWSCredentials awsCred = new BasicAWSCredentials(identity, credential);
    private static Long initializeConnectionTime;
    @Override
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        Inspector inspector = new Inspector();
        if (!isMac){
            inspector.inspectAll();
        }
        //*************FunctionStart**************
        if (request!=null&&request.getObjectName()!=null) objectName = request.getObjectName();
        int count = 2;
        boolean connect = true;
        if (request!=null && request.getCount()>0) count = request.getCount();
        int actual_count = count;
        // initial s3Client
        if (s3Client==null){
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCred))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            initializeConnectionTime = new Date().getTime();
        }
        //get object file using source bucket and srcKey name
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(containerName, objectName));
        //get content of the file
        InputStream inputStream = s3Object.getObjectContent();
        AtomicInteger rows = new AtomicInteger();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(line-> rows.incrementAndGet());
        }catch  (Exception e) {
            inspector.addAttribute("duration", new Date().getTime()-initializeConnectionTime);
            e.printStackTrace();
            connect = false;
        }
        //*********************Function end
        getMetrics(isMac,inspector,count,actual_count,connect,initializeConnectionTime);
        return inspector.finish();
    }

    public static void main(String[] args) {
        ReadObject readObject = new ReadObject();
        System.out.println(readObject.handleRequest(new Request("upload_jclouds_1650426036153.csv"), new Context() {
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
