package com.example;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ManagerThread implements Runnable {
        private String threadName;
        final static AWS aws = AWS.getInstance();
        boolean terminate;
        static int dealtMessages = 0;
    
        public ManagerThread(String name) {
            this.threadName = name;
        }
    
        @Override
        public void run() {
            String sqsName = "ManagerToApp-test-2";
            aws.createSqsQueue(sqsName);
            aws.createSqsQueue("WorkerToManager-test-2");

            while(terminate == false || dealtMessages < Manager.sentMessages.get()){
                try{
                    Message message = aws.getMessageFromSqs("WorkerToManager-test-2");
                    if(message != null){
                        String senderId = message.messageAttributes().get("SenderId").stringValue();
                        dealtMessages++;
                        aws.deleteMessageFromSqs(aws.getQueueURL("WorkerToManager-test-2"), message);
                        String objectKey = message.body();
                        aws.sendMessageToSqs(aws.getQueueURL(sqsName), objectKey, senderId);
                    }
                }catch(Exception e){}
                
                synchronized(ManagerThread.class){
                    if(Manager.terminate == true)
                        terminate = true;
                }
            }
        }
}
