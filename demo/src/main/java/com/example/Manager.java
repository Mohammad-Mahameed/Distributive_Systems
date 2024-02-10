package com.example;

import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

public class Manager {

    final static AWS aws = AWS.getInstance();
    final static int maxNumberOfWorkers = 18;
    static int numberOfActiveWorkers = 0;       //Overall created-running workers
    static int N = 0;                           //Workers' files ratio (reviews per worker)
    static int sqsCounter = 0;                  //Number of assigned jobs - in order to track the number of the required workers - resets after creating every new worker.
    static Boolean terminate = false;

    public static void main(String []args){

        String AppToManagerSqs = "AppToManager";
        String ManagerToWorkers = "ManagerToWorkers";
        String AppToManagerSqsURL = aws.getQueueURL(AppToManagerSqs);
        String ManagerToWorkersSqsURL = aws.getQueueURL(AppToManagerSqsURL);

        //Initiate at least one worker, in order to avoid missing jobs which contain less than N files
        String workerScript = ""; // TODO
        aws.createEC2(workerScript, "Worker", 1);

        while(!terminate){  //Until a "terminate" message is received
            Message msg = null;

            while(msg == null){ //Wait for a message to be sent in-case there wasn't any
                if((msg = aws.getMessageFromSqs(AppToManagerSqs)) != null){
                    aws.deleteMessageFromSqs(AppToManagerSqsURL, msg);
                }
            }

            String msgBody = msg.body();

            if(msgBody.equals("termiante")){
                terminate();
                /*
                 * TODO: Implement the required implementation when serving more than a single Local Application.
                 */
                continue;   //exit
            }
            else{   //A new job-handlement was requested - a file Path was received
                String inputFilePath = msgBody;
                aws.sendMessageToSqs(ManagerToWorkersSqsURL, inputFilePath);
                sqsCounter++;
                
                if(sqsCounter == N){
                    if(numberOfActiveWorkers == maxNumberOfWorkers) continue;
                    aws.createEC2(workerScript, "Worker", 1);
                    sqsCounter = 0;
                }
            }
        }
    }


    private static void terminate(){
        terminate = true;
    }
}
