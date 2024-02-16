package com.example;

import java.util.concurrent.atomic.AtomicInteger;

import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

public class Manager {

    final static AWS aws = AWS.getInstance();
    final static int maxNumberOfWorkers = 8;
    static int numberOfActiveWorkers = 0;       //Overall created-running workers
    static int N = 1;                           //Workers' files ratio (reviews per worker)
    static int sqsCounter = 0;                  //Number of assigned jobs - in order to track the number of the required workers - resets after creating every new worker.
    static Boolean terminate = false;
    static AtomicInteger sentMessages = new AtomicInteger(0);

    public static void main(String []args){
        Runnable thread1 = new ManagerThread("Deal with messages from Workers");
        Thread t1 = new Thread(thread1);
        t1.start();
        //SQS queue for distributing the messages over the workers
        String ManagerToWorkers = "ManagerToWorkers-test-2";
        aws.createSqsQueue(ManagerToWorkers);
        String ManagerToWorkersSqsURL = aws.getQueueURL(ManagerToWorkers);

        String AppToManagerSqs = "AppToManager-test-2";
        String AppToManagerSqsURL = aws.getQueueURL(AppToManagerSqs);

        //Initiate at least one worker, in order to avoid missing jobs which contain less than N files
        String workerScript = "#!/bin/bash\n" +
                                "sudo yum update -y\n" +
                                "sudo yum install -y java-11-openjdk-devel\n" +
                                "aws s3 cp s3://jar-bucket-assignment1-2024-test-2/target/demo-1.0-SNAPSHOT.jar /home/ec2-user/target/demo-1.0-SNAPSHOT.jar\n" +
                                "java -jar /home/ec2-user/target/demo-1.0-SNAPSHOT.jar com.example.Worker\n";
        if(numberOfActiveWorkers < maxNumberOfWorkers)
            aws.createEC2(workerScript, "Worker" + ++numberOfActiveWorkers, 1);

        while(!terminate){  //Until a "terminate" message is received
            Message msg = null;

            while(msg == null){ //Wait for a message to be sent in-case there wasn't any
                if((msg = aws.getMessageFromSqs(AppToManagerSqs)) != null){
                    aws.deleteMessageFromSqs(AppToManagerSqsURL, msg);
                }
            }

            String msgBody = msg.body();

            if(msgBody.equals("terminate")){
                synchronized(ManagerThread.class){
                    terminate();
                }
                /*
                 * TODO: Implement the required implementation when serving more than a single Local Application.
                 */
                continue;   //exit
            }
            else{   //A new job-handlement was requested - a file Path was received
                String inputFilePath = msgBody;
                aws.sendMessageToSqs(ManagerToWorkersSqsURL, inputFilePath);
                sentMessages.incrementAndGet();
                sqsCounter++;
                
                if(sqsCounter == N){
                    if(numberOfActiveWorkers == maxNumberOfWorkers) continue;
                    aws.createEC2(workerScript, "Worker" + ++numberOfActiveWorkers, 1);
                    sqsCounter = 0;
                }
            }
        }

        try{
            t1.join();
        }catch(Exception e){}
    }


    private static void terminate(){
        terminate = true;
    }
}
