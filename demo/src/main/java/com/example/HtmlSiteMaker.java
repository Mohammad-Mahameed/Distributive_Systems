package com.example;

public class HtmlSiteMaker {
    private String htmlContent;

    public HtmlSiteMaker(String title){
        this.htmlContent = "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<title>" +title+ "</title>\n" +
                            "</head>\n" +
                            "<body>\n";
    }

    //<p><a href="https://in.bgu.ac.il/Pages/default.aspx" style="color:#FF0000;">review.title</a> hi</p>
    public void addLine(Review review){
        String isSarcasm = "";
        if(review.getSarcasm())
            isSarcasm = "This is Sarcasm";
        else
            isSarcasm = "This is not a Sarcasm";
        String line = "<p><a href=\"" + 
                        review.getLink() + "\" style=\"color:" + 
                        review.getHtmlColor().getHexCode() + ";\">" +
                        review.getReviewTitle() + "</a>" +
                        " - Sarcasm Detector: (Rating=" + review.getRating() + ", Sentiment=" + 
                        review.getSentiment() + ") SO " + 
                        isSarcasm + " " +
                        "- Entity List: " + review.getEntityString() +
                        "</p>\n";
        
        this.htmlContent = htmlContent + line;
    }

    public void finishSite(){
        this.htmlContent = htmlContent +
                            "</body>\n" +
                            "</html>";
    }

    public String getHtmlContent(){
        return this.htmlContent;
    }
}
