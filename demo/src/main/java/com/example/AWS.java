package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import software.amazon.awssdk.core.sync.RequestBody;
import java.nio.file.Paths;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.nio.file.Paths;
import java.util.Base64;

public class AWS {
    /*
     * TODO:
     * Switch to private
     */
    public final S3Client s3;
    public final SqsClient sqs;
    public final Ec2Client ec2;

    public static String ami = "ami-00e95a9222311e8ed";

    public static Region region1 = Region.US_WEST_2;
    public static Region region2 = Region.US_EAST_1;

    private static final AWS instance = new AWS();

    private AWS() {
        s3 = S3Client.builder().region(region1).build();
        sqs = SqsClient.builder().region(region1).build();
        ec2 = Ec2Client.builder().region(region2).build();
    }

    public static AWS getInstance() {
        return instance;
    }


    // S3
    public void createBucketIfNotExists(String bucketName) {
        try {
            s3.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(BucketLocationConstraint.US_WEST_2)
                                    .build())
                    .build());
            s3.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (S3Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void uploadInputFilesToS3(LinkedList<String> inputFilesPaths, String bucketName){
        for(String inputFilePath: inputFilesPaths){
            HashMap<String, String> metaData = new HashMap();
            metaData.put("file-path", inputFilePath);
            s3.putObject(PutObjectRequest.builder()
                        .key(inputFilePath)
                        .bucket(bucketName)
                        .metadata(metaData)
                        .build(), 
                        RequestBody.fromFile(Paths.get(inputFilePath)));
        }
    }

    private static void addFilesToJar(File directory, JarOutputStream jos) throws IOException {
        // Get a list of files and directories in the current directory
        File[] files = directory.listFiles();

        // Iterate over the files
        for (File file : files) {
            String entryName = file.getPath().replace("\\", "/"); // Use forward slash for entry name
            JarEntry jarEntry = new JarEntry(entryName);
            jos.putNextEntry(jarEntry);

            // Write the file contents to the JAR
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
            fis.close();
            jos.closeEntry();
        }
    }

    public void uploadJarPackageToS3(String jarBucketName, String jarName){
        String cwd = System.getProperty("user.dir");    //Current working directory
        Path jarPath = Paths.get(cwd + "/" + jarName);
        
        //Create the JAR file
        try{
            FileOutputStream fos = new FileOutputStream(jarName);
            JarOutputStream jos = new JarOutputStream(fos, new Manifest());

            //Add all the files in the CWD to the JAR package
            addFilesToJar(new File(cwd), jos);

            // Close the JAR output stream
            jos.close();
            fos.close();
        }catch (IOException e){}

        //Upload the JAR file to S3
        HashMap<String, String> metaData = new HashMap();
        metaData.put("JAR", cwd + "/" + jarName);
        s3.putObject(PutObjectRequest.builder()
                    .key(cwd + "/" + jarName)
                    .bucket(jarBucketName)
                    .metadata(metaData)
                    .build(), 
                    RequestBody.fromFile(jarPath));
    }

    // EC2
    public String createManagerIfNotExists(String script) {
        String tagName = "amj450_Manager";
        //Check if a Manager node already exists
        String nextToken = null;

        do {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().nextToken(nextToken)
                    .build();
            DescribeInstancesResponse response = ec2.describeInstances(request);
            //if no reservations are ever made, then no EC2 instances ever existed...
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if (instance.tags().size() != 0 && 
                        (instance.state().name().toString().equalsIgnoreCase("running") || instance.state().name().toString().equalsIgnoreCase("pending")) &&
                        instance.tags().get(0).value().equals(tagName)) {
                        System.out.println("Manager already running!");
                        return null;
                    }
                }
            }
            nextToken = response.nextToken();
        } while (nextToken != null);

        //Create a Manager node

        Ec2Client ec2 = Ec2Client.builder().region(region2).build();
        RunInstancesRequest runRequest = (RunInstancesRequest) RunInstancesRequest.builder()
            .instanceType(InstanceType.M4_LARGE)
            .imageId(ami)
            .maxCount(1)
            .minCount(1)
            .keyName("vockey")
            .iamInstanceProfile(IamInstanceProfileSpecification.builder().name("LabInstanceProfile").build())
            .userData(Base64.getEncoder().encodeToString((script).getBytes()))
            .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);

        String instanceId = response.instances().get(0).instanceId();
    
        software.amazon.awssdk.services.ec2.model.Tag tag = Tag.builder()
                .key("Name")
                .value(tagName)
                .build();
    
        CreateTagsRequest tagRequest = (CreateTagsRequest) CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();
    
        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "[DEBUG] Successfully started EC2 instance %s based on AMI %s\n",
                    instanceId, ami);
    
        } catch (Ec2Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }

        return instanceId;
    }

    public String createEC2(String script, String tagName, int numberOfInstances) {
        Ec2Client ec2 = Ec2Client.builder().region(region2).build();
        RunInstancesRequest runRequest = (RunInstancesRequest) RunInstancesRequest.builder()
                .instanceType(InstanceType.M4_LARGE)
                .imageId(ami)
                .maxCount(numberOfInstances)
                .minCount(1)
                .keyName("vockey")
                .iamInstanceProfile(IamInstanceProfileSpecification.builder().name("LabInstanceProfile").build())
                .userData(Base64.getEncoder().encodeToString((script).getBytes()))
                .build();


        RunInstancesResponse response = ec2.runInstances(runRequest);

        String instanceId = response.instances().get(0).instanceId();

        software.amazon.awssdk.services.ec2.model.Tag tag = Tag.builder()
                .key("Name")
                .value(tagName)
                .build();

        CreateTagsRequest tagRequest = (CreateTagsRequest) CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "[DEBUG] Successfully started EC2 instance %s based on AMI %s\n",
                    instanceId, ami);

        } catch (Ec2Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }
        return instanceId;
    }


    //SQS

    public void createSqsQueue(String queueName) {
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        sqs.createQueue(createQueueRequest);
    }

    public String getQueueURL(String queueName){
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        GetQueueUrlResponse getQueueUrlResponse = sqs.getQueueUrl(getQueueUrlRequest);

        String queueURL = getQueueUrlResponse.queueUrl();
        return queueURL;
    }

    public LinkedList<String> getInputFilesPathsFromBucket(LinkedList<String> inputFilesKeys, String bucketName){
        LinkedList<String> inputFilesPaths = new LinkedList<String>();

        for(String inputFileKey: inputFilesKeys){
            String filePath;

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(inputFileKey)
            .build();
            
            System.out.println("headRequest = " + headRequest.toString());
            HeadObjectResponse headResponse = s3.headObject(headRequest);
            System.out.println("headResponse = " + headResponse.toString());
            filePath = headResponse.metadata().get("file-path");
            System.err.println("filePath = " + filePath);
            inputFilesPaths.add(filePath);
        }

        return inputFilesPaths;
    }

    public void sendMessagesToManager(LinkedList<String> inputFilesKeys, String queueURL, String bucketName){
        LinkedList<String> inputFilesPaths = getInputFilesPathsFromBucket(inputFilesKeys, bucketName);
        System.err.println("Input Files Paths: " + inputFilesPaths.toString());
        for(String inputFilePath: inputFilesPaths){
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueURL)
                .messageBody(inputFilePath)
                .build();

            sqs.sendMessage(sendMessageRequest);
        }
    }

    public Message getMessageFromSqs(String queueName){
        String queueUrl = getQueueURL(queueName);
 
         // Create a request to receive a single message from the queue
         ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                 .queueUrl(queueUrl)
                 .maxNumberOfMessages(1)  // Receive a single message
                 .build();
 
         // Receive messages from the queue
         ReceiveMessageResponse receiveMessageResponse = sqs.receiveMessage(receiveMessageRequest);
 
         if(receiveMessageResponse.hasMessages())
             return receiveMessageResponse.messages().get(0);
         
         return null;
     }

    public void sendMessageToSqs(String queueURL, String messageBody){
        SendMessageRequest sendRequest = SendMessageRequest.builder()
                .queueUrl(queueURL)
                .messageBody(messageBody)
                .build();
        sqs.sendMessage(sendRequest);
    }

    public void deleteMessageFromSqs(String queueUrl, Message message){
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
        sqs.deleteMessage(deleteRequest);
    }

}
