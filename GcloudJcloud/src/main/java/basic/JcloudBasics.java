package basic;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.domain.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static basic.GetInformation.containerName;
import static com.google.common.base.Charsets.UTF_8;

public class JcloudBasics {

    // read from an object and print the content
    public static void readBlob(BlobStore blobStore, String objectName) throws IOException {
        Blob downloadBlob = blobStore.getBlob(containerName, objectName);
        if (downloadBlob != null) {
            InputStream inputStream = downloadBlob.getPayload().openStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(System.out::println);
            }
        }
    }

    // read from an object and return the content stream
    public static InputStream readBlobContent(BlobStore blobStore, String objectName) throws IOException {
        Blob downloadBlob = blobStore.getBlob(containerName, objectName);
        if (downloadBlob != null) {
            return downloadBlob.getPayload().openStream();
        }
        return null;
    }

    // create an object with key-value
    public static void writeBlob(BlobStore blobStore, String blobName, String content) throws IOException {
        ByteSource payload = ByteSource.wrap(content.getBytes(UTF_8));
        // Add Blob
        Blob blob = blobStore.blobBuilder(blobName)
                .payload(payload)
                .contentLength(payload.size())
                .build();
        blobStore.putBlob(containerName, blob);
    }

    // delete a object by objectname
    public static void deleteBlob(BlobStore blobStore, String objectname){
        if (blobStore.blobExists(containerName,objectname)){
            blobStore.removeBlob(containerName,objectname);
            System.out.println("deleted");
        }else {
            System.out.println("file not exist");
        }
    }

    // create a container
    public static void createContainer(BlobStore blobStore, String containerName, String locationName){
        Location location = null;
        if (locationName!=null){
            for (Location loc:blobStore.listAssignableLocations()){
                if (locationName.contains(loc.getId())){
                    location = loc;
                    break;
                }
            }
        }
        boolean created = blobStore.createContainerInLocation(location,containerName);
        if (created) System.out.println("Created");
        else System.out.println("already exist");
    }

    //delete a container
    public static void deleteContainer(BlobStore blobStore, String containerName){
        blobStore.deleteContainer(containerName);
    }
}
