/**
 * Will hopefully read a pdf schedule of the type that IKEA uses and creates a file that
 * can be imported to google calendar.
 * 
 * @author Nisse, 2016-05-10
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
//import org.apache.pdfbox.searchengine.lucene.LucenePDFDocument;

public class ScheduleParser {

	ScheduleParser() {
		String pdfText = "";
		
        PDFTextStripper pdfStripper = null;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        File file = new File("/home/nisse/Downloads/mypdf.pdf");
        try {
            PDFParser parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(5);
            String parsedText = pdfStripper.getText(pdDoc);
            //System.out.println(parsedText);
            pdfText = parsedText;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } 
	    
        // Extract the data from the string and create a list of schedule classes.
        ArrayList<Schedule> schedule = new ArrayList<Schedule>();
        
        // Separate the different lines of the text
        String[] lines = pdfText.split("\n");
        int roof = 10;
        for (String line : lines) {
        	Schedule event = new Schedule();
        	boolean basic_event = false;
        	String[] parts = line.split(" ");
        	for (String part : parts) {
            	if (isDate(part)) {
            		event.date = new Date(part);
            	} else if (isTime(part)) {
            		if (event.start_time == null) {
            			event.start_time = part;
            		} else if (event.end_time == null) {
            			event.end_time = part;
            		} else if (event.sum_time == null) {
            			event.sum_time = part;
            			basic_event = true;
            		}
            	}
        	}
        	if (basic_event) {
        		System.out.println("Event:");
        		System.out.println(event.date + " " + event.start_time + " " + event.end_time + " " + event.sum_time);
        		schedule.add(event);
        	}
        }
        System.out.println("Collected " + schedule.size() + " basic events.");
        
        try
    	{
    	    FileWriter writer = new FileWriter("schema.csv");
    		 
    	    writer.append("DisplayName");
    	    writer.append(',');
    	    writer.append("Age");
    	    writer.append('\n');

    	    writer.append("MKYONG");
    	    writer.append(',');
    	    writer.append("26");
                writer.append('\n');
    			
    	    writer.append("YOUR NAME");
    	    writer.append(',');
    	    writer.append("29");
    	    writer.append('\n');
    			
    	    //generate whatever data you want
    			
    	    writer.flush();
    	    writer.close();
    	}
    	catch(IOException e)
    	{
    	     e.printStackTrace();
    	}
	}
	
	private class Schedule {
		
		String day, start_time, end_time, description, sum_time;
		Date date;
		
		Schedule() {
			
		}
	}
	
	private class Date {
		String year, month, day;
		/**
		 * Accepts date in the format "yyyy-mm-dd"
		 * @param date
		 */
		Date(String date) {
			// Should i validate?
			if (isDate(date)) {
				String[] parts = date.split("-");
				year = parts[0];
				month = parts[1];
				day = parts[2];
			} else {
				System.err.println("Nej, dåligt datum att skapa datum-klass med.");
			}
		}
		
		public String toString() {
			return year+"-"+month+"-"+day;
		}
	}
	
	
	public static void main(String args[]) {
		new ScheduleParser();
    }
	
	private boolean isTime(String part) {
		Pattern p = Pattern.compile("(([01]?[0-9])|(2[0-4])):[0-9]{2}");
		Matcher m = p.matcher(part);
		boolean b = m.matches();
		return b;
	}
	
	private boolean isDate(String part) {
		Pattern p = Pattern.compile("2[0-9]{3}-((0[1-9])|(1[012]))-((3[01])|([012][0-9]))");
		Matcher m = p.matcher(part);
		boolean b = m.matches();
		return b;
	}

}
