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

public class LocalApp {

    final static AWS aws = AWS.getInstance();

    public static void main(String[] args) {// args = [inFilePath, outFilePath, tasksPerWorker, -t (terminate, optional)]
        //Prase args
        LinkedList<String> inputFilesPaths = new LinkedList<String>() , outputFilesPaths = new LinkedList<String>();
        AtomicInteger n = new AtomicInteger(0);
        AtomicBoolean terminate = new AtomicBoolean(false);
        parseArgs(args, inputFilesPaths, outputFilesPaths, n, terminate);

        //Init Manager
        String ec2Script = "#!/bin/bash\n" +
                            "echo Hello World\n";
        aws.createManagerIfNotExists(ec2Script);

        //Create a S3 bucket and upload the input files to it
        String bucketName = "amj450-bucket";
        aws.createBucketIfNotExists(bucketName);
        aws.uploadInputFilesToS3(inputFilesPaths, bucketName);

        //Create an SQS and pass the input files to the manager
        aws.createSqsQueue("inputFilesSQS");
        String queueURL = aws.getQueueURL("inputFilesSQS");
        aws.sendMessagesToManager(inputFilesPaths, queueURL, bucketName);
        
        //TODO: points 4-6


        /*DescribeInstancesRequest request = DescribeInstancesRequest.builder().nextToken(null).build();
        System.out.println("request " + request.toString());

        DescribeInstancesResponse response = aws.ec2.describeInstances(request);
        System.out.println("reservations size " + response.reservations().size());
        System.out.println("response " + response.toString());*/
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