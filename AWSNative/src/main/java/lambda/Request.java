package lambda;

public class Request {
    String objectName;
    int count;
    public Request(){

    }
    public Request(String objectName){
        this.objectName = objectName;
    }
    public Request(int count){
        this.count = count;
    }
    public Request(String objectName, int count){
        this.objectName = objectName;
        this.count = count;
    }
    public void setObjectName(String objectName){
        this.objectName = objectName;
    }
    public void setCount(int count){
        this.count=count;
    }
    public int getCount() {
        return this.count;
    }

    public String getObjectName() {
        return this.objectName;
    }
}
