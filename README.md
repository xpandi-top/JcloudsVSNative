## Serverless functions with or without abstraction library on AWS and Google Clouds
- [AWSJcloud](https://github.com/xpandi-top/JcloudsVSNative/tree/main/AWSJcloud) folder: Use Jclouds API to implement lambda/function on AWS
- [AWSNative](https://github.com/xpandi-top/JcloudsVSNative/tree/main/AWSNative) folder: Use AWS SDK to implement lambda/function on AWS
- [GcloudJcloud](https://github.com/xpandi-top/JcloudsVSNative/tree/main/Jcloud) folder: Use Jclouds API to implement lambda/function on Google Cloud
- [GcloudNative](https://github.com/xpandi-top/JcloudsVSNative/tree/main/GcloudNative) folder: Use Google Cloud SDK to implement lambda/function on Google Cloud
### Test cases
Each folder contains following lambda/functions
- CreateContainers.java: Create multiple containers in storage
- DeleteContainers.java: Delete multiple containers in storage
- DeleteMultiple.java: Delete multiple key-value objects in storage
- ReadMultiple.java: Read multiple key-value content in storage
- ReadObject.java: Read content from a single file in storage
- Transform.java: Transform a sale record file from storage
- WriteMultiple.java: Write multiple key-value objects into storage
- WriteObject.java: Write a file to storage.