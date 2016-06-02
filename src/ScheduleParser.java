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

public class ScheduleParser {

	ScheduleParser() {
	}
	
	/**
	 * Parses a pdf file and returns an integer with the result code for the parsing.
	 * 0 = success
	 * 1 = not a pdf file / error while reading pdf file
	 * 2 = Could not parse schedule events from the pdf
	 * 3 = failed to write a new file. Check writing rights for the folder.
	 * @param filePath
	 * @return
	 */
	public int parseSchedule(String filePath) {
		
		String pdfText = readPdf(filePath);
		
		if (pdfText.equals("")) {
			return 1;
		}
		
		// Extract the data from the string and create a list of schedule
		// classes.
		ArrayList<Schedule> schedule = parsePdfText(pdfText);
		
		if (schedule.size() < 1) {
			return 2;
		}
		
		System.out.println("Collected " + schedule.size() + " basic events.");
		
		String newFilePath = generateNewFilePath(filePath);
		
		int writingFileSuccess = writeScheduleFile(schedule, newFilePath);
		
		
		return writingFileSuccess;
	}
	
	private int writeScheduleFile(ArrayList<Schedule> schedule, String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.append("Subject,");
			writer.append("Start Date,");
			writer.append("Start Time,");
			writer.append("End Date,");
			writer.append("End Time");
			writer.append("\n");
			/*
			 * Exempel på kalender-innehåll:
			 * Subject
				Namnet på händelsen, krävs.
				Exempel: Slutprov
				Start Date
				Den första dagen för händelsen, krävs.
				Exempel: 05/30/2020
				Start Time
				Tiden då händelsen börjar.
				Exempel: 10:00 AM
				End Date
				Den sista dagen för händelsen.
				Exempel: 05/30/2020
				End Time
				Tiden då händelsen slutar.
				Exempel: 1:00 PM
			 */
			
			for (Schedule sc : schedule) {
				writer.append("KMD" + ",");
				writer.append(sc.date.toGoogleCalendarString() + ",");
				writer.append(sc.getGoogleCalendarStartTime() + ",");
				writer.append(sc.date.toGoogleCalendarString() + ",");
				writer.append(sc.getGoogleCalendarEndTime());
				writer.append("\n");
				
			}

			// generate whatever data you want

			writer.flush();
			writer.close();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 3;
		}
	}
	
	private String generateNewFilePath(String oldFilePath) {
		String newFilePath;
		String[] parts = oldFilePath.split("/");
		
		String path = "";
		for (int i = 0; i < parts.length-1; i++) {
			path += parts[i] + "/";
		}
		
		// The name of the file excluding the path
		String oldFileName = parts[parts.length-1];
		
		// Validate pdf file extension
		String pdf = oldFileName.substring(oldFileName.length()-4, oldFileName.length());
		if (pdf.equals(".pdf")) {
			String newFileName = oldFileName.substring(0, oldFileName.length()-4);
			newFileName += "_Google-Calendar.csv";
			newFilePath = path + newFileName;
		} else {
			// Something odd with filename, give some default name.
			String newFileName = "schema.csv";
			newFilePath = path + newFileName;
		}

		return newFilePath;
	}
	
	private ArrayList<Schedule> parsePdfText(String pdfText) {
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
				System.out.println(event.date + " " + event.start_time + " "
						+ event.end_time + " " + event.sum_time);
				schedule.add(event);
			}
		}
		return schedule;
	}
	
	private String readPdf(String filePath) {
		// Validate pdf
		String newFilePath;
		String[] parts = filePath.split("/");
		
		// The name of the file excluding the path
		String oldFileName = parts[parts.length-1];
		
		// Validate pdf file extension
		String pdf = oldFileName.substring(oldFileName.length()-4, oldFileName.length());
		if (!pdf.equals(".pdf")) {
			System.err.println("This is not a pdf file.");
			return "";
		}
		
		
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = new File(filePath);
		try {
			PDFParser parser = new PDFParser(new FileInputStream(file));
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(5);
			String parsedText = pdfStripper.getText(pdDoc);
			// System.out.println(parsedText);
			return parsedText;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private boolean isTime(String part) {
		Pattern p = Pattern.compile("(([01]?[0-9])|(2[0-4])):[0-9]{2}");
		Matcher m = p.matcher(part);
		boolean b = m.matches();
		return b;
	}

	private boolean isDate(String part) {
		Pattern p = Pattern
				.compile("2[0-9]{3}-((0[1-9])|(1[012]))-((3[01])|([012][0-9]))");
		Matcher m = p.matcher(part);
		boolean b = m.matches();
		return b;
	}

	private class Schedule {

		String subject, day, start_time, end_time, description, sum_time;
		Date date;

		Schedule() {

		}
		
		public String getGoogleCalendarStartTime() {
			return getTimeFormat(start_time);
		} 
		
		public String getGoogleCalendarEndTime() {
			return getTimeFormat(end_time);
		}
		
		private String getTimeFormat(String time) {
			boolean pm = false;
			String[] parts = time.split(":");
			int hour = Integer.parseInt(parts[0]);
			if (hour > 12) {	
				hour = hour - 12;
				pm = true;
			} else if (hour == 12) {
				pm = true;
			} else if (hour == 0) {
				hour = 12;
			}
			if (pm) {
				return hour + ":" + parts[1] + " PM";
			} else {
				return time + " AM";
			} 
		}
	}

	private class Date {
		String year, month, day;

		/**
		 * Accepts date in the format "yyyy-mm-dd"
		 * 
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
				System.err
						.println("Nej, dåligt datum att skapa datum-klass med.");
			}
		}

		public String toGoogleCalendarString() {
			return month + "/" + day + "/" + year;
		}
		
		public String toString() {
			return year + "-" + month + "-" + day;
		}
	}

}
