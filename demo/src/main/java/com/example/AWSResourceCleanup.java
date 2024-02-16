package com.example;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.regions.Region;
import java.util.stream.Collectors;
import java.util.List;

public class AWSResourceCleanup {

    public static Region region1 = Region.US_WEST_2;
    public static Region region2 = Region.US_EAST_1;

    public static void main(String[] args) {
        // Initialize clients
        S3Client s3Client = S3Client.builder().region(region1).build();
        SqsClient sqsClient = SqsClient.builder().region(region1).build();
        Ec2Client ec2Client = Ec2Client.builder().region(region2).build();

        // Delete all S3 buckets
        List<Bucket> buckets = s3Client.listBuckets().buckets();
        for (Bucket bucket : buckets) {
            deleteBucketAndObjects(s3Client, bucket.name());
        }

        // Delete all SQS queues
        ListQueuesResponse queueResult = sqsClient.listQueues();
        for (String queueUrl : queueResult.queueUrls()) {
            sqsClient.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build());
        }

        // Terminate all EC2 instances
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
        DescribeInstancesResponse response = ec2Client.describeInstances(request);
        for (Instance instance : response.reservations().stream()
                                      .flatMap(r -> r.instances().stream())
                                      .collect(Collectors.toList())) {
            ec2Client.terminateInstances(TerminateInstancesRequest.builder().instanceIds(instance.instanceId()).build());
        }
    }

    private static void deleteBucketAndObjects(S3Client s3Client, String bucketName) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Response listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
            for (S3Object s3Object : listObjectsResponse.contents()) {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3Object.key()).build());
            }
            listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName)
                                                         .continuationToken(listObjectsResponse.nextContinuationToken())
                                                         .build();
        } while (listObjectsResponse.isTruncated());

        s3Client.deleteBucket(b -> b.bucket(bucketName));
    }
}