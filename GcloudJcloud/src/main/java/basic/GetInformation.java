package basic;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import saaf.Inspector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.function.Supplier;

import static com.google.common.base.Charsets.UTF_8;

public class GetInformation {
    public static final JsonObject jsonfile = readIni();
    public static final String provider = jsonfile.get("provider").getAsString();
    public static final String identity = jsonfile.get("identity").getAsString();// Access Key ID
    public static final String containerName = jsonfile.get("containerName").getAsString();// bucket namespace
    public static String objectName=jsonfile.get("objectName").getAsString();
    public static final String credential = getCredentialFromJsonKeyFile(jsonfile.get("credential").getAsString());//Secret Access Key.
    public static String osname = System.getProperty("os.name");
    public static boolean isMac = osname.contains("Mac");
    public static Gson gson = new Gson();

    // inspector
    public static void getMetrics(boolean isMac, Inspector inspector, int count, int actual_count, boolean connect, Long initializeConnectionTime) {
        inspector.addAttribute("initializeConnectionTime", initializeConnectionTime);// initialize connection Time for the storage connect
        inspector.addAttribute("connect",connect);// whether there is a connection
        inspector.addAttribute("actual_count",actual_count);
        inspector.addAttribute("count",count);
        if(connect) inspector.addAttribute("duration", new Date().getTime()- initializeConnectionTime);
        if (!isMac){
            inspector.inspectAllDeltas();
        }
    }
    private static String getCredentialFromJsonKeyFile(String filename) {
        try {
            String fileContents = Files.asCharSource(new File(filename), UTF_8).read();
            Supplier<Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);
            return credentialSupplier.get().credential;
        } catch (IOException e) {
            System.err.println("Exception reading private key from '%s': " + filename);
            e.printStackTrace();
            return null;
        }
    }
    public static JsonObject readIni() {
        Gson gson = new Gson();
        String file="{}";
        try {
            file = java.nio.file.Files.readString(Path.of("src/main/java/basic/credential.json.json"));
        }catch (IOException exception){
            exception.printStackTrace();
        }
        return gson.fromJson(file,JsonObject.class);
    }
}
