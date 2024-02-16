package com.example;
import java.util.concurrent.atomic.*;

import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.IamInstanceProfileSpecification;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import software.amazon.awssdk.services.sqs.model.*;
public class LocalApp {

    final static AWS aws = AWS.getInstance();

    public static void main(String[] args) {// args = [inFilePath, outFilePath, tasksPerWorker, -t (terminate, optional)]
        //Prase args
        LinkedList<String> inputFilesPaths = new LinkedList<String>() , outputFilesPaths = new LinkedList<String>();
        AtomicInteger n = new AtomicInteger(0);
        AtomicBoolean terminate = new AtomicBoolean(false);
        parseArgs(args, inputFilesPaths, outputFilesPaths, n, terminate);

        //Create a JAR bucket
        String jarBucketName = "jar-bucket-assignment1-2024-test-2";
        aws.createBucketIfNotExists(jarBucketName);
        //Create and upload the JAR package to it
        String jarName = "target/demo-1.0-SNAPSHOT.jar";
        aws.uploadJarPackageToS3(jarBucketName, jarName);

        //Init Manager
        String ec2Script = "#!/bin/bash\n" +
                            "sudo yum update -y\n" +
                            "sudo yum install -y java-1.8.0-openjdk\n" +
                            "aws s3 cp s3://jar-bucket-assignment1-2024-test-2/target/demo-1.0-SNAPSHOT.jar /home/ec2-user/target/demo-1.0-SNAPSHOT.jar\n" +
                            "java -jar /home/ec2-user/target/demo-1.0-SNAPSHOT.jar com.example.Manager\n";
        System.out.println("Manager's script:\n" + ec2Script);
        aws.createManagerIfNotExists(ec2Script);

        //Create a S3 bucket and upload the input files to it
        String bucketName = "amj450-new-bucket-test-2";
        aws.createBucketIfNotExists(bucketName);
        aws.uploadInputFilesToS3(inputFilesPaths, bucketName);

        //Create an SQS and pass the input files to the manager
        aws.createSqsQueue("AppToManager-test-2");
        String queueURL = aws.getQueueURL("AppToManager-test-2");
        aws.sendMessagesToManager(inputFilesPaths, queueURL, bucketName);
        if(terminate.get() == true)
            aws.sendMessageToSqs(queueURL, "terminate");
        
        //TODO: points 4-6
        int numOfFiles = outputFilesPaths.size();
        int index = 0;
        while (numOfFiles > index ) {
            try{
                Message message = aws.getMessageFromSqs("ManagerToApp-test-2");
                System.out.println("Received a msg!");
                if(message != null){
                    String objectKey = message.body();
                    System.out.println("message from Mangager to App" + objectKey);
                    String localFilePath = outputFilesPaths.get(index); 
                    String fromBucketName = "worker-s3-new-test";
                    aws.downloadFileFromS3(fromBucketName, objectKey, localFilePath);
                    index ++;
                }
            }catch(Exception e){System.out.println("No msgs yet!");}   
        }
    }

    
    public static void parseArgs(String[] args, LinkedList<String> inputFilesPaths, LinkedList<String> outputFilesPaths, AtomicInteger n, AtomicBoolean terminate){
        int argsSize = args.length, inputOutputFilesCount;

        if(argsSize % 2 == 0){   //terminate is defined
            terminate.set(true);
            inputOutputFilesCount = (argsSize-2)/2;
            n.set(Integer.valueOf(args[argsSize-2]));
        }
        else{
            inputOutputFilesCount = (argsSize-1)/2;
            n.set(Integer.valueOf(args[argsSize-1]));
        }

        for(int i=0; i<inputOutputFilesCount; i++){
            inputFilesPaths.add(args[i]);
            outputFilesPaths.add(args[inputOutputFilesCount + i]);
        }
    }
}