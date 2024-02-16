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

public class Worker {

    final static AWS aws = AWS.getInstance();

    static sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();
    static String sqsName = "WorkerToManager-test-2";

    public static void main(String[] args) {
        AtomicBoolean termminated = new AtomicBoolean(false);
        while(termminated.get() == false){
            Message message = aws.getMessageFromSqs("ManagerToWorkers-test-2");
            if(message != null){
                if(message.body() == "terminate"){
                    termminated.set(true);
                }
                else{
                    String objectKey = message.body();
                    System.out.println(objectKey);
                    String localFilePath = "testout"; // Specify the local file path to save the downloaded file
                    String fromBucketName = "amj450-new-bucket-test-2";
                    aws.downloadFileFromS3(fromBucketName, objectKey, localFilePath);
                    List<Book> books = parseFile(localFilePath);
                    allReviewsHandle(books);
                    String htmlFileName = objectKey.substring(0,objectKey.length() - 3) + ".html";
                    makeHtmlSite(books, localFilePath, htmlFileName);
                    String toBucketName = "worker-s3-new-test-2";
                    aws.createBucketIfNotExists(toBucketName);
                    aws.uploadOutputFileToS3(htmlFileName, toBucketName);
                    aws.deleteMessageFromSqs(aws.getQueueURL("ManagerToWorkers-test-2"), message);
                    aws.sendMessageToSqs(aws.getQueueURL(sqsName), htmlFileName);
                } 
            }
        }
    }

    public static List<Book> parseFile(String localFilePath){
        System.out.println("hi");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(localFilePath))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                // Parse each line as JSON
                Book book = objectMapper.readValue(line, Book.class);
                books.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    public static void allReviewsHandle(List<Book> books){
        for(Book book : books){
            for(Review review : book.getReviews()){
                sentimentAnalysisHandler.analyse(review);
                List<String> entityList = namedEntityRecognitionHandler.getEntities(review.getReviewText());
                review.setEntity(entityList);
            }
        }
    }

    public static void makeHtmlSite(List<Book> books, String localFilePath, String htmlFileName){
        HtmlSiteMaker siteMaker = new HtmlSiteMaker(localFilePath);
        for(Book book : books){
            for(Review review : book.getReviews()){
                siteMaker.addLine(review);
            }
        }
        siteMaker.finishSite();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFileName))) {
            writer.write(siteMaker.getHtmlContent());
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
