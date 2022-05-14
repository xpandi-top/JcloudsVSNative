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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;

import static basic.GetInformation.*;

public class ReadMultiple implements RequestHandler<Request, HashMap<String,Object>> {
    private static Long initializeConnectionTime ;
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
        if (s3Client==null){
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCred))
                    .withRegion(Regions.DEFAULT_REGION)
                    .build();
            initializeConnectionTime = new Date().getTime();
        }

        for (int i=0; i<count;i++){
            String content = "MyKey"+i;

            try {
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(containerName, content));
                InputStream inputStream = s3Object.getObjectContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                reader.lines().forEach(System.out::println);
            } catch (Exception e) {
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
        ReadMultiple readMultiple = new ReadMultiple();
        HashMap<String,Object> res = readMultiple.handleRequest(new Request(3), new Context() {
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
