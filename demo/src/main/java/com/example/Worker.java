package com.example;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.model.*;

import java.io.File;

public class Worker {

    final static AWS aws = AWS.getInstance();

    static sentimentAnalysisHandler sentimentAnalysisHandler = new sentimentAnalysisHandler();
    static namedEntityRecognitionHandler namedEntityRecognitionHandler = new namedEntityRecognitionHandler();

    public static void main(String[] args) {
        AtomicBoolean termminated = new AtomicBoolean(false);
        while(termminated.get() == false){
            System.out.print("ksmk");
            Message message = aws.getMessageFromManagerToWorker("ManagerToWorkers");
            if(message.body() == "TERMINATE!"){
                //termminated.set(true);
            }
            else{
                System.out.print("hi");
                String s3Url = message.body();
                String localFilePath = "path_to_save_file_locally"; // Specify the local file path to save the downloaded file
                aws.downloadFileFromS3(s3Url, localFilePath);


            }
            
        }
    }

    public static Book[] parseFile(String localFilePath){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Read JSON file into array of Book objects
            Book[] books = objectMapper.readValue(new File("example.json"), Book[].class);

            for (Book book : books) {
                // Accessing fields for each Book object
                String title = book.getTitle();
                System.out.println("Title: " + title);

                // Accessing reviews for each book
                for (Review review : book.getReviews()) {
                    System.out.println("  - Review ID: " + review.getId());
                    System.out.println("    Link: " + review.getLink());
                    System.out.println("    Review Title: " + review.getReviewTitle());
                    System.out.println("    Review Text: " + review.getReviewText());
                    System.out.println("    Rating: " + review.getRating());
                    System.out.println("    Author: " + review.getAuthor());
                    System.out.println("    Date: " + review.getDate());
                }
            }
            return books;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
