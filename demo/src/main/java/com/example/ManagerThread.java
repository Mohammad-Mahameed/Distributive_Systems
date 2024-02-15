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
    
        public ManagerThread(String name) {
            this.threadName = name;
        }
    
        @Override
        public void run() {

            while(terminate == false){
                Message message = aws.getMessageFromSqs("WorkerToManager-test");
                if(message != null){
                    String objectKey = message.body();
                    String sqsName = "ManagerToApp-test";
                    aws.createSqsQueue(sqsName);
                    aws.sendMessageToSqs(aws.getQueueURL(sqsName), objectKey);
                }
                synchronized(ManagerThread.class){
                    if(Manager.terminate == true)
                        terminate = true;
                }
            }
        }
}
