package com.example;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class sentimentAnalysisHandler {
    
        public static int findSentiment(String review) {

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, parse, sentiment");
            StanfordCoreNLP sentimentPipeline = new StanfordCoreNLP(props);

            int mainSentiment = 0;
            if (review!= null && review.length() > 0) {
                int longest = 0;
                Annotation annotation = sentimentPipeline.process(review);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(
                    SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    String partText = sentence.toString();
                    if (partText.length() > longest) {
                        mainSentiment = sentiment;
                        longest = partText.length();
                    }
                }
            }
            return mainSentiment;
        }
        public static void analyse(Review review){ 
            int result = sentimentAnalysisHandler.findSentiment(review.getReviewText());
            if(result == review.getRating())
                review.setSarcasm(false);
            else
                review.setSarcasm(true);

            HtmlColor htmlColor = null;

            switch (result) {
                case 0:{
                    htmlColor = HtmlColor.DarkRed();
                }
                case 1:{
                    htmlColor = HtmlColor.Red();
                }
                case 2:{
                    htmlColor = HtmlColor.Black();
                }
                case 3:{
                    htmlColor = HtmlColor.LightGreen();
                }
                case 4:{
                    htmlColor = HtmlColor.DarkGreen();
                }
            }
            review.setHtmlColor(htmlColor);
        }
}
