package basic;

import com.google.cloud.storage.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GcloudBasics {
    public static void uploadObjectFromMemory(
            Storage storage, String bucketName, String objectName, String contents) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        byte[] content = contents.getBytes(StandardCharsets.UTF_8);
        storage.createFrom(blobInfo, new ByteArrayInputStream(content));
    }

    // https://cloud.google.com/storage/docs/creating-buckets#storage-create-bucket-code_samples
    public static void createContainer(Storage storage, String bucketName, String location){
       boolean exists = storage.get(bucketName)!=null;
        if (!exists) {
            storage.create(
                    BucketInfo.newBuilder(bucketName)
                            .build()
            );
            System.out.println("Created");
        }else {
            System.out.println(bucketName + " is existed");
        }
    }

    public static void deleteContainer(Storage storage,String bucketName){
        boolean exists = storage.get(bucketName)!=null;
        Bucket bucket = storage.get(bucketName);
        if (exists){
            bucket.delete();
            System.out.println("Deleted");
        }else {
            System.out.println("No bucket name "+bucketName);
        }

    }
    public static void deleteObject(Storage storage, String bucketName,String objectName){
        boolean exists = storage.get(bucketName,objectName)!=null;
        if (exists){
            storage.delete(bucketName, objectName);
            System.out.println(objectName+" was deleted");
        }else {
            System.out.println("No object Name " + objectName);
        }
    }
}
