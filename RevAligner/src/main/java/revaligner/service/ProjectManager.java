package revaligner.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import revaligner.domain.RevProject;

public class ProjectManager
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private long session_last_access_time = 0L;
  private String aligntype = "";
  private FileAligner fileAligner = new FileAligner();
  private RevProject revProject = new RevProject();
  
  public RevProject getRevProject()
  {
    return this.revProject;
  }
  
  public void setAlignType(String aligntype)
  {
    this.aligntype = aligntype;
  }
  
  public String getAlignType()
  {
    return this.aligntype;
  }
  
  public void setPrjInfoFile(String prjinfo)
  {
    this.fileAligner.setPrjInfoFile(prjinfo);
  }
  
  public void setPrjFolder(String prjfolder)
  {
    this.fileAligner.setPrjFolder(prjfolder);
  }
  
  public int getAlignProgress(String prjid)
  {
    return this.fileAligner.getAlignProgress().containsKey(prjid) ? ((Integer)this.fileAligner.getAlignProgress().get(prjid)).intValue() : 0;
  }
  
  public void removeAlignProgressRecord(String prjid)
  {
    this.fileAligner.removeAlignProgress(prjid);
  }
  
  public void setAlignProgress(int alignProgress, String prjid)
  {
    this.fileAligner.setAlignProgress(prjid, alignProgress);
  }
  
  public void setErrorMessage(String errormessage)
  {
    this.fileAligner.setErrorReason(errormessage);
  }
  
  public String getErrorMessage()
  {
    return this.fileAligner.getErrorReason();
  }
  
  public void cancelexcution()
  {
    this.fileAligner.setStopExcution(true);
  }
  
  public boolean isSessionTimesOut()
    throws Exception
  {
    System.out.println("checking timeout: " + getRevProject().getPrjNumber());
    double timeoutinterval = this.revProject.getTimeOutInterval();
    System.out.println("timeoutinterval: " + timeoutinterval);
    long currenttime = System.nanoTime();
    long elapsedTime = currenttime - this.session_last_access_time;
    double totalSeconds = elapsedTime / 1000000000.0D;
    System.out.println("totalSeconds: " + totalSeconds);
    if (totalSeconds > timeoutinterval) {
      return true;
    }
    return false;
  }
  
  public void setSessionLastAccessTime(long currenttime)
    throws Exception
  {
    this.session_last_access_time = currenttime;
  }
  
  public void iniprj(String prjid, String isfilealigned, String isprjoutfortrans)
    throws Exception
  {
    this.revProject.setPrjNumber(prjid);
    
    String prjinfofile = this.revProject.getProjectInfoFile();
    this.fileAligner.setPrjInfoFile(prjinfofile);
    FileInputStream in = new FileInputStream(prjinfofile);
    Properties props = new Properties();
    props.load(in);
    
    this.fileAligner.setCreatorID(this.revProject.getUsername());
    this.fileAligner.setSourceLanguage(props.getProperty("raprojectsourcelanguagecode"));
    this.revProject.setSrcLang(props.getProperty("raprojectsourcelanguagecode"));
    this.fileAligner.setTargetLanguage(props.getProperty("raprojecttargetlanguagecode"));
    this.revProject.setTrgLang(props.getProperty("raprojecttargetlanguagecode"));
    
    String sourcefilename = props.getProperty("rasourcefilename");
    String sourcefile = "";
    if (props.containsKey("rasourcefilename"))
    {
      sourcefile = this.revProject.getPrjWorkingPath() + File.separator + "source" + File.separator + sourcefilename;
      if (new File(sourcefile).exists())
      {
        this.fileAligner.setSourceFile(sourcefile);
      }
      else
      {
        props.setProperty("rasourcefilename", "");
        props.setProperty("rasourcefilesize", "");
      }
    }
    else
    {
      props.setProperty("rasourcefilename", "");
      props.setProperty("rasourcefilesize", "");
    }
    String targetfilename = props.getProperty("ratargetfilename");
    String targetfile = "";
    if (props.containsKey("ratargetfilename"))
    {
      targetfile = this.revProject.getPrjWorkingPath() + File.separator + "target" + File.separator + targetfilename;
      if (new File(targetfile).exists())
      {
        this.fileAligner.setTargetFile(targetfile);
      }
      else
      {
        props.setProperty("ratargetfilename", "");
        props.setProperty("ratargetfilesize", "");
      }
    }
    else
    {
      props.put("ratargetfilename", "");
      props.setProperty("ratargetfilesize", "");
    }
    FileOutputStream out = new FileOutputStream(prjinfofile);
    props.store(out, "RA PROJECT INFOMATION");
    out.close();
    in.close();
    if (isfilealigned.equals("true"))
    {
      if ((!new File(sourcefile).exists()) || (sourcefilename.equals(""))) {
        throw new FileNotFoundException("original source file not found");
      }
      if ((!new File(targetfile).exists()) || (targetfilename.equals(""))) {
        throw new FileNotFoundException("original target file not found");
      }
      String backupsourcefile = this.revProject.getPrjWorkingPath() + File.separator + "source_backup" + File.separator + props.getProperty("rasourcefilename");
      if (new File(backupsourcefile).exists()) {
        this.fileAligner.setBackUpSourceFile(backupsourcefile);
      } else {
        throw new FileNotFoundException("original source file not found");
      }
      String sourcetxlfnonseg = sourcefile + ".nonSeg.txlf";
      if (new File(sourcetxlfnonseg).exists()) {
        this.fileAligner.setSourceTxlfNonSeg(sourcetxlfnonseg);
      } else {
        throw new FileNotFoundException("source txlf non-seg not found");
      }
      String sourcetxlfseg = sourcefile + ".Seg.txlf";
      if (new File(sourcetxlfseg).exists()) {
        this.fileAligner.setSourceTxlfSeg(sourcetxlfseg);
      } else {
        throw new FileNotFoundException("source txlf seg not found");
      }
      String reformattedsourcefile = this.revProject.getPrjWorkingPath() + File.separator + "source_reformatted" + File.separator + props.getProperty("rasourcefilename");
      if (new File(reformattedsourcefile).exists()) {
        this.fileAligner.setReformattedSourceFile(reformattedsourcefile);
      } else {
        throw new FileNotFoundException("reformatted source file not found");
      }
      String reformattedsourcetxlfnonseg = reformattedsourcefile + ".nonSeg.txlf";
      if (new File(reformattedsourcetxlfnonseg).exists()) {
        this.fileAligner.setReformattedSourceTxlf_NonSeg(reformattedsourcetxlfnonseg);
      } else {
        throw new FileNotFoundException("reformatted source txlf non-seg not found");
      }
      String reformattedtargetfile = this.revProject.getPrjWorkingPath() + File.separator + "target_reformatted" + File.separator + props.getProperty("ratargetfilename");
      if (new File(reformattedtargetfile).exists()) {
        this.fileAligner.setReformattedTargetFile(reformattedtargetfile);
      } else {
        throw new FileNotFoundException("reformatted target file not found");
      }
      String reformattedtargettxlfnonseg = reformattedtargetfile + ".nonSeg.txlf";
      if (new File(reformattedtargettxlfnonseg).exists()) {
        this.fileAligner.setReformattedTargetTxlf_NonSeg(reformattedtargettxlfnonseg);
      } else {
        throw new FileNotFoundException("reformatted target txlf non-seg not found");
      }
      String reformattedtargettxlfseg = reformattedtargetfile + ".seg.txlf";
      if (new File(reformattedtargettxlfseg).exists()) {
        this.fileAligner.setReformattedTargetTxlf_Seg(reformattedtargettxlfseg);
      } else {
        throw new FileNotFoundException("reformatted target txlf seg not found");
      }
      String alignedxml = this.revProject.getAlignedXmlPath();
      if (new File(alignedxml).exists()) {
        this.fileAligner.setAlignedFile(alignedxml);
      } else {
        throw new FileNotFoundException("aligned xml not found");
      }
      String auto_saved_alignedxml = this.revProject.getAutoSavedAlignedXmlPath();
      if (new File(auto_saved_alignedxml).exists()) {
        this.fileAligner.setAutoSavedAlignedFile(auto_saved_alignedxml);
      }
    }
    if (isprjoutfortrans.equals("true"))
    {
      String populatedsourcetxlf = sourcefile + ".txlf";
      if (new File(populatedsourcetxlf).exists()) {
        this.fileAligner.setPopulatedSourceTxlf(populatedsourcetxlf);
      } else {
        throw new FileNotFoundException("populated source txlf non-seg not found");
      }
      String htmlreportfortranslation = this.revProject.getPrjWorkingPath() + File.separator + new File(sourcefile).getName().substring(0, new File(sourcefile).getName().lastIndexOf(".")) + "_aligned.html";
      if (new File(htmlreportfortranslation).exists()) {
        this.fileAligner.setHtmlReportForTranslation(htmlreportfortranslation);
      } else {
        throw new FileNotFoundException("html report for translation not found");
      }
      String temphtmlreportfortranslation = this.revProject.getPrjWorkingPath() + File.separator + new File(sourcefile).getName().substring(0, new File(sourcefile).getName().lastIndexOf(".")) + "_aligned.temp";
      if (new File(temphtmlreportfortranslation).exists()) {
        this.fileAligner.setTempHtmlReportForTranslation(temphtmlreportfortranslation);
      } else {
        throw new FileNotFoundException("temp html report for translation not found");
      }
    }
  }
  
  public void alignFiles(String aligntype)
    throws Exception
  {
    String prjid = this.revProject.getPrjNumber();
    setAlignProgress(10, prjid);
    this.fileAligner.createReformattedDocument(aligntype);
    setAlignProgress(20, prjid);
    this.fileAligner.convertSourceToTxlf(true);
    this.fileAligner.convertSourceToTxlf(false);
    this.fileAligner.convertReformattedSourceToTxlf(true);
    setAlignProgress(40, prjid);
    this.fileAligner.convertReformattedTargetToTxlf(true);
    this.fileAligner.convertReformattedTargetToTxlf(false);
    setAlignProgress(60, prjid);
    @SuppressWarnings("unused")
	String[] res = new String[1];
    if (this.fileAligner.verifyParas())
    {
      setAlignProgress(70, prjid);
      if (aligntype.equals("sequential")) {
        this.fileAligner.createAlignedXML();
      } else if (aligntype.equals("auto")) {
        this.fileAligner.createAlignedXML_auto(prjid);
      }
      setAlignProgress(90, prjid);
      if (!this.fileAligner.verifysegments())
      {
        setAlignProgress(0, prjid);
        throw new FileNotFoundException("files not aligned on segment level");
      }
    }
    else
    {
      setAlignProgress(0, prjid);
      throw new FileNotFoundException("files cannot be aligned");
    }
  }
  
  public String[] readnbalignmentreport()
    throws Exception
  {
    String[] res = new String[1];
    res = this.fileAligner.readnbalignmentreport();
    return res;
  }
  
  public void setSourcePath(String path)
    throws Exception
  {
    this.fileAligner.setSourceFile(path);
  }
  
  public void setTargetPath(String path)
    throws Exception
  {
    this.fileAligner.setTargetFile(path);
  }
  
  public void setTranslateTxlfPath(String path)
    throws Exception
  {
    this.fileAligner.setTranslatedTxlf(path);
  }
  
  public void createTargetPackage(String preservefmt)
    throws Exception
  {
    this.fileAligner.createTargetFile();
    this.fileAligner.exportHtmlLogFileForFinalReview();
    this.fileAligner.createTargetCompareFile(preservefmt);
  }
  
  public void readAlignedParagraphs()
    throws Exception
  {
    this.fileAligner.readAlignedFile();
  }
  
  public void readAlignedSegments(String aligntype, boolean useautosaveddata)
    throws Exception
  {
    if (aligntype.equals("sequential")) {
      this.fileAligner.readAlignedFile_seg(useautosaveddata);
    } else if (aligntype.equals("auto")) {
      this.fileAligner.readAlignedFile_seg_auto(useautosaveddata);
    }
  }
  
  public int getNullCnt()
    throws Exception
  {
    return this.fileAligner.getnullcnt();
  }
  
  public HashMap<String, List<String>> getSegmentsFromParagraph(String src_segid, String trg_segid)
    throws Exception
  {
    return this.fileAligner.findandsegmentpara(src_segid, trg_segid);
  }
  
  public LinkedHashMap<String, List<String>> getAlignedSrcParas()
    throws Exception
  {
    return this.fileAligner.getAlignedSrcParas();
  }
  
  public LinkedHashMap<String, List<String>> getAlignedTrgParas()
    throws Exception
  {
    return this.fileAligner.getAlignedTrgParas();
  }
  
  public LinkedHashMap<String, List<String>> getMissingTrgParas()
    throws Exception
  {
    return this.fileAligner.getMissingTrgParas();
  }
  
  public LinkedHashMap<String, List<String>> getMissingTrgSegs()
    throws Exception
  {
    return this.fileAligner.getMissingTrgSegs();
  }
  
  public LinkedHashMap<String, List<String>> getSrcSegments()
    throws Exception
  {
    return this.fileAligner.getSrcSegs();
  }
  
  public LinkedHashMap<String, List<String>> getTrgSegments()
    throws Exception
  {
    return this.fileAligner.getTrgSegs();
  }
  
  public LinkedHashMap<String, String> getLockedParaSeqs()
    throws Exception
  {
    return this.fileAligner.getLockedParaSeqs();
  }
  
  public LinkedHashMap<String, String> getLockedSegSeqs()
    throws Exception
  {
    return this.fileAligner.getLockedSegSeqs();
  }
  
  public LinkedHashMap<String, String> getReviewSegSeqs()
    throws Exception
  {
    return this.fileAligner.getReviewSegSeqs();
  }
  
  public LinkedHashMap<String, String> getIgnoreSegSeqs()
    throws Exception
  {
    return this.fileAligner.getIgnoreSegSeqs();
  }
  
  public LinkedHashMap<String, String> getAlignedParaSeqs()
    throws Exception
  {
    return this.fileAligner.getAligneddParaSeqs();
  }
  
  public void updateAlignedFile(JSONArray arr, JSONArray missings, JSONArray locks, JSONArray segaligned, JSONArray targets, JSONArray missing_targets, int nullcnt)
    throws Exception
  {
    this.fileAligner.update(arr, missings, locks, segaligned, targets, missing_targets, nullcnt);
  }
  
  public void updateAlignedFile_seg(JSONArray targets, JSONArray trg_seqs, JSONArray missing_targets, JSONArray missing_trg_seqs, JSONArray locks, int nullcnt, JSONArray edited, JSONArray review, JSONArray ignore)
    throws Exception
  {
    this.fileAligner.update_seg(targets, trg_seqs, missing_targets, missing_trg_seqs, locks, nullcnt, edited, review, ignore);
  }
  
  public void auto_updateAlignedFile_seg(JSONArray targets, JSONArray trg_seqs, JSONArray missing_targets, JSONArray missing_trg_seqs, JSONArray locks, int nullcnt, JSONArray edited, JSONArray review, JSONArray ignore)
    throws Exception
  {
    this.fileAligner.auto_update_seg(targets, trg_seqs, missing_targets, missing_trg_seqs, locks, nullcnt, edited, review, ignore);
  }
  
  public HashMap<String, List<String>> updateParagraph(String text)
    throws Exception
  {
    return this.fileAligner.updateSingleParagraph(text);
  }
  
  public String zipPorject()
    throws Exception
  {
    this.fileAligner.collectFilesForProjectFile();
    
    ZipFile zf = new ZipFile();
    String trgzipfile = this.revProject.getExportZipFolderPath() + File.separator + this.revProject.getPrjNumber() + ".ra";
    zf.ZipIt(trgzipfile, this.revProject.getExportZipFolderPath());
    return trgzipfile;
  }
  
  public String zipForTargetPackage()
    throws Exception
  {
    this.fileAligner.collectFilesForFinalReview();
    
    ZipFile zf = new ZipFile();
    
    String trgzipfile = this.revProject.getExportZipFolderPath() + File.separator + this.revProject.getPrjNumber() + "_[QC].zip";
    zf.ZipIt(trgzipfile, this.revProject.getExportZipFolderPath());
    return trgzipfile;
  }
  
  public String zipTranslationKit()
    throws Exception
  {
    this.fileAligner.collectFilesForTranslationKit(this.revProject.getPrjNumber());
    ZipFile zf = new ZipFile();
    
    String trgzipfile = this.revProject.getExportZipFolderPath() + File.separator + this.revProject.getPrjNumber() + "_[TK].zip";
    zf.ZipIt(trgzipfile, this.revProject.getExportZipFolderPath());
    return trgzipfile;
  }
  
  public void createTranslationKit()
    throws Exception
  {
    this.fileAligner.buildTargetContentMap();
    this.fileAligner.exportHtmlLogFileForTranslation(this.fileAligner.populateSourceTxlf(), this.fileAligner.getRepsAndFuzzyReps());
  }
  
  public String createTranslationMemory(String prjid, boolean reviewed)
    throws Exception
  {
    return this.fileAligner.createTM(prjid, reviewed);
  }
  
  public static HashMap<String, String> getLangeuageCodeMap()
    throws Exception
  {
    HashMap<String, String> langcodemap = new HashMap<String, String>();
    InputStream langcodeFile = ProjectManager.class.getClassLoader().getResourceAsStream("langcodes.properties");
    
    InputStreamReader inReader = new InputStreamReader(langcodeFile);
    BufferedReader readbuffer = new BufferedReader(inReader);
    String line;
    while ((line = readbuffer.readLine()) != null) {
      if (!line.trim().equals(""))
      {
        String[] arr = line.trim().split("=");
        if ((arr.length == 2) && (!arr[0].equals("")) && (!arr[1].equals(""))) {
          langcodemap.put(arr[0], arr[1]);
        }
      }
    }
    readbuffer.close();
    inReader.close();
    
    return langcodemap;
  }
  
  public static HashMap<String, String> getLangeuageCodeMapReverse()
    throws Exception
  {
    HashMap<String, String> langcodemap = new HashMap<String, String>();
    InputStream langcodeFile = ProjectManager.class.getClassLoader().getResourceAsStream("langcodes.properties");
    
    InputStreamReader inReader = new InputStreamReader(langcodeFile);
    BufferedReader readbuffer = new BufferedReader(inReader);
    String line;
    while ((line = readbuffer.readLine()) != null) {
      if (!line.trim().equals(""))
      {
        String[] arr = line.trim().split("=");
        if ((arr.length == 2) && (!arr[0].equals("")) && (!arr[1].equals(""))) {
          langcodemap.put(arr[1], arr[0]);
        }
      }
    }
    readbuffer.close();
    inReader.close();
    
    return langcodemap;
  }
  
  public boolean searchProjectFolder(String prjid)
    throws Exception
  {
    String trgprjfolder = this.revProject.getBaseWorkingPath() + File.separator + this.revProject.getUsername() + File.separator + prjid;
    if (new File(trgprjfolder).exists()) {
      return true;
    }
    return false;
  }
  
  public boolean searchProjectFolderFromOtherUser(String prjid, String username)
    throws Exception
  {
    String trgprjfolder = this.revProject.getBaseWorkingPath() + File.separator + username + File.separator + prjid;
    if (new File(trgprjfolder).exists()) {
      return true;
    }
    return false;
  }
  
  public String searchProjectFolderAsAdminUser(String prjid)
    throws Exception
  {
    String searchfolder = getRevProject().getBaseWorkingPath();
    for (File file : new File(searchfolder).listFiles()) {
      if (file.isDirectory())
      {
        String user = file.getName();
        for (File sfile : file.listFiles()) {
          if ((sfile.isDirectory()) && (sfile.getName().startsWith("RA#")))
          {
            String name = sfile.getName();
            if (name.equals(prjid)) {
              return user;
            }
          }
        }
      }
    }
    return "";
  }
  
  public boolean searchProjectInfoFile(String prjid)
    throws Exception
  {
    String trgprjinfofile = this.revProject.getBaseWorkingPath() + File.separator + this.revProject.getUsername() + File.separator + prjid + File.separator + "_.info";
    if (new File(trgprjinfofile).exists()) {
      return true;
    }
    return false;
  }
  
  public static boolean checkProjectAccessable(String prjid, SessionCollector sessionCollector, String username)
    throws Exception
  {
    Iterator it = sessionCollector.getSessionMap().entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<HttpSession, String> entry = (Map.Entry)it.next();
      if (((String)entry.getValue()).equals(username + "_" + prjid)) {
        return false;
      }
    }
    return true;
  }
  
  public boolean verifyprojectpackage(String packagePath)
    throws Exception
  {
    String pkgfolder = new File(packagePath).getParent();
    UnzipFile.UnZipIt(packagePath, pkgfolder);
    
    String info = pkgfolder + File.separator + "_.info";
    if (!new File(info).exists()) {
      throw new FileNotFoundException("project info file not found in the project package.");
    }
    String alignedxml = pkgfolder + File.separator + "rev_aligned.xml";
    if (!new File(alignedxml).exists()) {
      throw new FileNotFoundException("aligned xml file not found in the project package.");
    }
    FileInputStream in = new FileInputStream(info);
    Properties props = new Properties();
    props.load(in);
    
    String aligntype = props.getProperty("alignmenttype");
    if (aligntype == null) {
      throw new NullPointerException("missing alignment type in the project info file.");
    }
    String srclangs = props.getProperty("raprojectsourcelanguagecode");
    if (srclangs == null) {
      throw new NullPointerException("missing source language in the project info file.");
    }
    String trglangs = props.getProperty("raprojecttargetlanguagecode");
    if (trglangs == null) {
      throw new NullPointerException("missing target language in the project info file.");
    }
    String sourcename = props.getProperty("rasourcefilename");
    if (sourcename == null) {
      throw new NullPointerException("missing source file name in the project info file.");
    }
    String sourcefile = pkgfolder + File.separator + sourcename;
    if (!new File(sourcefile).exists()) {
      throw new FileNotFoundException("source file not found in the project package.");
    }
    String targetname = props.getProperty("ratargetfilename");
    if (targetname == null) {
      throw new NullPointerException("missing target file name in the project info file.");
    }
    String targetfile = pkgfolder + File.separator + targetname;
    if (!new File(targetfile).exists()) {
      throw new FileNotFoundException("target file not found in the project package.");
    }
    in.close();
    
    String realprjinfofile = this.fileAligner.getPrjInfoFile();
    FileInputStream inreal = new FileInputStream(realprjinfofile);
    Properties props_real = new Properties();
    props_real.load(inreal);
    this.fileAligner.setSourceLanguage(props.getProperty("raprojectsourcelanguagecode"));
    props_real.setProperty("raprojectsourcelanguagecode", srclangs);
    props_real.setProperty("raprojecttargetlanguagecode", trglangs);
    props_real.setProperty("rasourcefilename", sourcename);
    props_real.setProperty("ratargetfilename", targetname);
    props_real.setProperty("rasourcefilesize", Long.toString(new File(sourcefile).length() / 1024L) + " K");
    props_real.setProperty("ratargetfilesize", Long.toString(new File(targetfile).length() / 1024L) + " K");
    
    this.fileAligner.setCreatorID(this.revProject.getUsername());
    this.fileAligner.setSourceLanguage(srclangs);
    this.fileAligner.setTargetLanguage(trglangs);
    
    String srcfolder = this.revProject.getPrjWorkingPath() + File.separator + "source";
    if (!new File(srcfolder).exists()) {
      new File(srcfolder).mkdir();
    }
    FileUtils.copyFileToDirectory(new File(sourcefile), new File(srcfolder));
    this.fileAligner.setSourceFile(srcfolder + File.separator + sourcename);
    
    String trgfolder = this.revProject.getPrjWorkingPath() + File.separator + "target";
    if (!new File(trgfolder).exists()) {
      new File(trgfolder).mkdir();
    }
    FileUtils.copyFileToDirectory(new File(targetfile), new File(trgfolder));
    this.fileAligner.setTargetFile(trgfolder + File.separator + targetname);
    
    this.fileAligner.createReformattedDocument(aligntype);
    this.fileAligner.convertSourceToTxlf(true);
    this.fileAligner.convertSourceToTxlf(false);
    this.fileAligner.convertReformattedSourceToTxlf(true);
    
    this.fileAligner.convertReformattedTargetToTxlf(true);
    if (!this.fileAligner.verifyParas()) {
      return false;
    }
    this.fileAligner.createAlignedXML();
    if (!this.fileAligner.compareAlignedXmls(alignedxml)) {
      return false;
    }
    this.fileAligner.replaceAlignedFile(alignedxml);
    
    props_real.setProperty("isaligned", "true");
    FileOutputStream fileOut = new FileOutputStream(realprjinfofile);
    props_real.store(fileOut, "RA PROJECT INFOMATION");
    fileOut.close();
    inreal.close();
    
    return true;
  }
  
  public void cloneProject(String trgprjnum, String trguser, String timestamp, String currentusername)
    throws Exception
  {
    String baseworkingdir = getRevProject().getBaseWorkingPath();
    String trguserfolder = baseworkingdir + File.separator + trguser;
    String trgprjfolder = trguserfolder + File.separator + trgprjnum;
    if (new File(trgprjfolder).exists())
    {
      String clonedprjfolder = baseworkingdir + File.separator + currentusername + File.separator + trgprjnum + "(C)";
      if (!new File(clonedprjfolder).exists())
      {
        FileUtils.copyDirectory(new File(trgprjfolder), new File(clonedprjfolder));
        
        Properties props = new Properties();
        String infofile = clonedprjfolder + File.separator + "_.info";
        FileInputStream in = null;
        if (new File(infofile).exists())
        {
          in = new FileInputStream(infofile);
          props.load(in);
        }
        props.setProperty("raprojectcreationdate", timestamp);
        props.setProperty("raprojectnumber", trgprjnum + "(C)");
        FileOutputStream out = new FileOutputStream(infofile);
        props.store(out, "RA PROJECT INFOMATION");
        out.close();
        in.close();
      }
      else
      {
        throw new Exception("project has already been cloned");
      }
    }
    else
    {
      throw new Exception("target project folder not found");
    }
  }
  
  public String readAlignedFileForRatio(String alignedfile)
    throws Exception
  {
    String auto_saved_alignedfile = alignedfile + ".temp";
    if (new File(auto_saved_alignedfile).exists()) {
      return this.fileAligner.readAlignedFileForRatio(auto_saved_alignedfile);
    }
    return this.fileAligner.readAlignedFileForRatio(alignedfile);
  }
  
  public String generateAccessToken(String prjid, String username, String timestamp)
    throws Exception
  {
    String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    SecureRandom rnd = new SecureRandom();
    
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      sb.append(AB.charAt(rnd.nextInt(AB.length())));
    }
    String raw = timestamp + "|" + username + "|" + prjid + "|" + sb.toString();
    return new String(Base64.getEncoder().encode(raw.getBytes()));
  }
  
  public String[] parseAccessToken(String token)
    throws Exception
  {
    String decode = new String(Base64.getDecoder().decode(token.getBytes()));
    System.out.println("decode: " + decode);
    String[] ss = decode.split("\\|");
    return ss;
  }
}
