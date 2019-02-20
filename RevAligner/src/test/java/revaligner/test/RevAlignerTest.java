package revaligner.test;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aspose.cells.Cells;
import com.aspose.cells.Range;
import com.aspose.cells.Style;
import com.aspose.cells.StyleFlag;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import revaligner.service.FileAligner;
import revaligner.service.SessionCollector;

public class RevAlignerTest
  implements Serializable
{
  static final Logger logger = Logger.getLogger(RevAlignerTest.class);
  
  public static void main(String[] args) throws Exception
  {
	  String s1 = "this is one hundred and thirty";
	  System.out.println(normalizeEnglishToNumber(s1));
	  if(true) return;
    try
    {
      FileAligner fa = new FileAligner();
      fa.setSourceFile("C:\\Program Files (x86)\\pa\\paprjs\\testprj23\\source\\EN.doc");
      fa.setTargetFile("C:\\Program Files (x86)\\pa\\paprjs\\testprj23\\target\\ES.doc");
      fa.setPrjFolder("C:\\Program Files (x86)\\pa\\paprjs\\testprj23");
      fa.setSourceLanguage("en");
      fa.setTargetLanguage("es");
      
      fa.createReformattedDocument("auto");
      
      System.out.println("convert source to txlf (para)....");fa.convertSourceToTxlf(true);
      System.out.println("convert source to txlf (seg)....");fa.convertSourceToTxlf(false);
      System.out.println("convert reformatted source to txlf (para)....");fa.convertReformattedSourceToTxlf(true);
      
      System.out.println("convert reformatted target to txlf (para)....");fa.convertReformattedTargetToTxlf(true);
      
      System.out.println("convert reformatted target to txlf (seg)....");fa.convertReformattedTargetToTxlf(false);
      if (fa.verifyParas())
      {
        fa.createAlignedXML_auto("testprj23", new SessionCollector());
        fa.verifysegments();
        fa.buildTargetContentMap();
        fa.exportHtmlLogFileForTranslation(fa.populateSourceTxlf(), fa.getRepsAndFuzzyReps());
      }
      System.out.println("done!");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      
      RollingFileAppender appender = new RollingFileAppender();
      appender.setAppend(true);
      appender.setMaxFileSize("5MB");
      appender.setMaxBackupIndex(10);
      appender.setName("main");
      appender.setFile("C:\\Program Files (x86)\\pa\\paprjs\\log4j-application.log");
      
      PatternLayout layOut = new PatternLayout();
      layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
      appender.setLayout(layOut);
      
      appender.activateOptions();
      logger.addAppender(appender);
      logger.error("Sorry, something wrong!", ex);
    }
    System.exit(0);
  }
  
  private static String decodehtmlstring(String encoded_s)
  {
    return Jsoup.parse(encoded_s.replaceAll("<br[^>]*?>", "<br>").replaceAll("\\s", "&nbsp;").replace("&lt;", "&amp;lt;").replace("&gt;", "&amp;gt;").replace("<", "&lt;").replace(">", "&gt;")).text().replace(" ", " ").replace("&#8232;", " ");
  }
  
  private static void applyAsposeLicense()
  {
    System.err.println("applying Aspose license..");
    
    com.aspose.words.License license_wordlicense = new com.aspose.words.License();
    InputStream license_word = FileAligner.class.getClassLoader().getResourceAsStream("aspose.license");
    try
    {
      license_wordlicense.setLicense(license_word);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    com.aspose.cells.License license_excellicense = new com.aspose.cells.License();
    InputStream license_excel = FileAligner.class.getClassLoader().getResourceAsStream("aspose.license");
    try
    {
      license_excellicense.setLicense(license_excel);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private static String normalizeEnglishToNumber(String sentence){
		
		List<String> allowedStrings = Arrays.asList
			    (
			    "zero","one","two","three","four","five","six","seven",
			    "eight","nine","ten","eleven","twelve","thirteen","fourteen",
			    "fifteen","sixteen","seventeen","eighteen","nineteen","twenty",
			    "thirty","forty","fifty","sixty","seventy","eighty","ninety",
			    "hundred","thousand","million","billion","trillion"
			    );
		
		StringBuffer sb = new StringBuffer();
		StringBuffer sub_sb = new StringBuffer();
		int i = 0;
		while(i < sentence.length()){
			char c = sentence.charAt(i);
			if(c == ' '){
				sb.append(c);
				i++;
				if(sub_sb.length() != 0){
					String num = convertEnglishToNumber(sub_sb.toString().trim(), allowedStrings);
					sb.append(num);
					sub_sb = new StringBuffer();
				}
			}else{
				int j = i;
				String word = "";
				while(j != sentence.length() && sentence.charAt(j) != ' '){
					word += sentence.charAt(j);
					j++;
				}
				if(!allowedStrings.contains(word)){
					i++;
					continue;
				}else{
					sub_sb.append(word + " ");
					i = j;
				}
			}
		}
		
		return sb.toString();
	}
	
	private static String convertEnglishToNumber(String input, List<String> allowedStrings){
		System.out.println("input: " + input);
	    long result = 0;
	    long finalResult = 0;

      String[] splittedParts = input.trim().split("\\s+");


      for(String str : splittedParts)
      {
          if(str.equalsIgnoreCase("zero")) {
              result += 0;
          }
          else if(str.equalsIgnoreCase("one")) {
              result += 1;
          }
          else if(str.equalsIgnoreCase("two")) {
              result += 2;
          }
          else if(str.equalsIgnoreCase("three")) {
              result += 3;
          }
          else if(str.equalsIgnoreCase("four")) {
              result += 4;
          }
          else if(str.equalsIgnoreCase("five")) {
              result += 5;
          }
          else if(str.equalsIgnoreCase("six")) {
              result += 6;
          }
          else if(str.equalsIgnoreCase("seven")) {
              result += 7;
          }
          else if(str.equalsIgnoreCase("eight")) {
              result += 8;
          }
          else if(str.equalsIgnoreCase("nine")) {
              result += 9;
          }
          else if(str.equalsIgnoreCase("ten")) {
              result += 10;
          }
          else if(str.equalsIgnoreCase("eleven")) {
              result += 11;
          }
          else if(str.equalsIgnoreCase("twelve")) {
              result += 12;
          }
          else if(str.equalsIgnoreCase("thirteen")) {
              result += 13;
          }
          else if(str.equalsIgnoreCase("fourteen")) {
              result += 14;
          }
          else if(str.equalsIgnoreCase("fifteen")) {
              result += 15;
          }
          else if(str.equalsIgnoreCase("sixteen")) {
              result += 16;
          }
          else if(str.equalsIgnoreCase("seventeen")) {
              result += 17;
          }
          else if(str.equalsIgnoreCase("eighteen")) {
              result += 18;
          }
          else if(str.equalsIgnoreCase("nineteen")) {
              result += 19;
          }
          else if(str.equalsIgnoreCase("twenty")) {
              result += 20;
          }
          else if(str.equalsIgnoreCase("thirty")) {
              result += 30;
          }
          else if(str.equalsIgnoreCase("forty")) {
              result += 40;
          }
          else if(str.equalsIgnoreCase("fifty")) {
              result += 50;
          }
          else if(str.equalsIgnoreCase("sixty")) {
              result += 60;
          }
          else if(str.equalsIgnoreCase("seventy")) {
              result += 70;
          }
          else if(str.equalsIgnoreCase("eighty")) {
              result += 80;
          }
          else if(str.equalsIgnoreCase("ninety")) {
              result += 90;
          }
          else if(str.equalsIgnoreCase("hundred")) {
              result *= 100;
          }
          else if(str.equalsIgnoreCase("thousand")) {
              result *= 1000;
              finalResult += result;
              result=0;
          }
          else if(str.equalsIgnoreCase("million")) {
              result *= 1000000;
              finalResult += result;
              result=0;
          }
          else if(str.equalsIgnoreCase("billion")) {
              result *= 1000000000;
              finalResult += result;
              result=0;
          }
          else if(str.equalsIgnoreCase("trillion")) {
              result *= 1000000000000L;
              finalResult += result;
              result=0;
          }
      }

      finalResult += result;
	    
	    return Long.toString(finalResult);
	}
}
