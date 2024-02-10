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

    public static void main(String[] args) {
        AtomicBoolean termminated = new AtomicBoolean(false);
        /*while(termminated.get() == false){
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
                List<Book> books = parseFile(localFilePath);

            }
            
        }
        */

        String localFilePath = "testInput.txt";

        List<Book> books = parseFile(localFilePath);

        allReviewsHandle(books);

        HtmlSiteMaker siteMaker = new HtmlSiteMaker(localFilePath);
        Book test = books.get(0);
        for(Review review : test.getReviews()){
            siteMaker.addLine(review);
        }
        siteMaker.finishSite();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sample.html"))) {
            writer.write(siteMaker.getHtmlContent());
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Book> parseFile(String localFilePath){
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(localFilePath))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                // Parse each line as JSON
                Book book = objectMapper.readValue(line, Book.class);
                books.add(book);
                // Accessing fields
                /* 
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
                }*/
                
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

}
