package com.example;

import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

public class Manager {

    final static AWS aws = AWS.getInstance();
    final static int maxNumberOfWorkers = 18;
    static Boolean terminate = false;

    public static void main(String []args){

        String AppToManagerSqs = "AppToManager";
        String AppToManagerSqsURL = aws.getQueueURL(AppToManagerSqs);

        while(!terminate){  //Until a "terminate" message is received
            Message msg = null;

            while(msg == null){ //Wait for a message to be sent in-case there wasn't any
                msg = aws.getMessageFromSqs(AppToManagerSqs);
            }

            String msgBody = msg.body();

            if(msgBody.equals("termiante")){
                terminate();
                /*
                 * TODO: Implement the required implementation when serving more than a single Local Application.
                 */
                continue;   //exit
            }
        }
    }

    private static void terminate(){
        terminate = true;
    }
}
