package revaligner.test;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import revaligner.service.FileAligner;
import revaligner.service.SessionCollector;

public class RevAlignerTest
  implements Serializable
{
  static final Logger logger = Logger.getLogger(RevAlignerTest.class);
  
  public static void main(String[] args)
  {
	  int[] A = new int[]{-1,-4,1,3};
	  final Integer[] sorted = ArrayUtils.toObject(A);
	  Arrays.sort(sorted, new Comparator<Integer>() {
		    public int compare(Integer a, Integer b) {
		        return Math.abs(a)-Math.abs(b);
		    }
		});
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
        //fa.exportHtmlLogFileForTranslation(fa.populateSourceTxlf(), fa.getRepsAndFuzzyReps());
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
}
