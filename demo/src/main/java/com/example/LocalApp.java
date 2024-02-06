package com.example;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Reservation;
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
        String ec2Script = "#!/bin/bash\n" +
                            "echo Hello World\n";
        aws.createManagerIfNotExists(ec2Script);
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().nextToken(null)
        .build();
        System.out.println("request " + request.toString());
        DescribeInstancesResponse response = aws.ec2.describeInstances(request);
        System.out.println("reservations size " + response.reservations().size());
        System.out.println("response " + response.toString());
    }
}