package revaligner.service;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.aspose.words.Comment;
import com.aspose.words.CommentRangeEnd;
import com.aspose.words.CommentRangeStart;
import com.aspose.words.CompositeNode;
import com.aspose.words.ControlChar;
import com.aspose.words.FieldStart;
import com.aspose.words.FindReplaceOptions;
import com.aspose.words.Font;
import com.aspose.words.Footnote;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Paragraph;
import com.aspose.words.Range;
import com.aspose.words.Revision;
import com.aspose.words.RevisionCollection;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.Section;
import com.aspose.words.SectionCollection;
import com.aspose.words.Shape;
import com.aspose.words.Table;
import com.aspose.words.Underline;


import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.gs4tr.filters3.extraction.ExtractionSupportImpl;
import org.gs4tr.filters3.msoffice.ConvertDOC;
import org.gs4tr.filters3.msoffice.MergeDOC;
import org.gs4tr.filters3.msoffice.common.AsposeFactory;
import org.gs4tr.filters3.msoffice.common.OfficeConfigurationConverterImpl;
import org.gs4tr.filters3.msoffice.word.WordDocumentAligner;
import org.gs4tr.foundation.locale.Locale;
import org.gs4tr.foundation3.xliff.model.XliffDocument;
import org.gs4tr.foundation3.xml.XmlParser;
import org.gs4tr.tm3.commandline.utils.XliffSegmenter;
import org.gs4tr.tm3.segmenter.Segmenter;
import org.gs4tr.tm3.segmenter.SegmenterFactory;
import org.gs4tr.tm3.wordcounter.strategy.TradosWordCounter;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class FileAligner
  implements Serializable
{
  private String sourcefile;
  private String targetfile;
  private String alignedfile;
  private String auto_saved_alignedfile;
  private String backupsourcefile;
  private String comparedoc;
  private String reformattedsourcefile;
  private String reformattedtargetfile;
  private String sourcetxlf_nonSeg;
  private String sourcetxlf_seg;
  private String reformattedsourcetxlf_nonSeg;
  private String reformattedsourcetxlf_seg;
  private String populatedsourcetxlf;
  private String reformattedtargettxlf_nonSeg;
  private String reformattedtargettxlf_seg;
  private String reformattedtargetmapfile;
  private String targettxlf_nonSeg;
  private String targettxlf_seg;
  private String translatedtxlf;
  private String aligneddoc;
  private String htmlreportfortranslation;
  private String htmlreportfortranslation_temp;
  private String htmlreportforfinalreview;
  private String prjfolder;
  private String sourcelanguage;
  private String targetlanguage;
  private String creatorid;
  private String exportfolder;
  private String prjinfofile;
  private String tempfolder;
  private String nbalignerfolder;
  private boolean isSectionBreakDeletedORInserted = false;
  private boolean stopexcution = false;
  private LinkedHashMap<String, List<String>> src_paras;
  private LinkedHashMap<String, List<String>> trg_paras;
  private LinkedHashMap<String, List<String>> missing_trg_paras;
  private LinkedHashMap<String, List<String>> src_segs;
  private LinkedHashMap<String, List<String>> trg_segs;
  private LinkedHashMap<String, String> locked_para_seqs;
  private LinkedHashMap<String, String> aligned_para_seqs;
  private int nullcnt = 0;
  private String estimateNBAlignerCompTime = "";
  private static HashMap<String, Integer> alignProgress = new HashMap();
  private LinkedHashMap<Integer, List> txlftrgsegmap;
  private LinkedHashMap<Integer, boolean[]> txlftrgsewsmap;
  private LinkedHashMap<String, List<String>> missing_trg_segs;
  private LinkedHashMap<String, String> locked_seg_seqs;
  private LinkedHashMap<String, String> review_seg_seqs;
  private LinkedHashMap<String, String> ignore_seg_seqs;
  private int[] replacestyles = new int[3];
  private static int doublestrikethrough = 99;
  private static int needreviewthreshhold = 75;
  private static double textexpansionthreshold = 2.0D;
  private String errorreason = "";
  private char[] esps = { '.', '?', '!' };
  
  public FileAligner()
  {
    applyAsposeLicense();
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
  
  public HashMap<String, Integer> getAlignProgress()
  {
    return alignProgress;
  }
  
  public void removeAlignProgress(String prjid)
  {
    if (alignProgress.containsKey(prjid)) {
      alignProgress.remove(prjid);
    }
  }
  
  public void setAlignProgress(String prjid, int alignProgress)
  {
    this.alignProgress.put(prjid, Integer.valueOf(alignProgress));
  }
  
  public void setnullcnt(int cnt)
  {
    this.nullcnt = cnt;
  }
  
  public int getnullcnt()
  {
    return this.nullcnt;
  }
  
  public void setStopExcution(boolean stop)
  {
    this.stopexcution = stop;
  }
  
  public boolean getStopExcution()
  {
    return this.stopexcution;
  }
  
  public void setSourceFile(String sourcefile)
  {
    this.sourcefile = sourcefile;
  }
  
  public void setBackUpSourceFile(String backupsourcefile)
  {
    this.backupsourcefile = backupsourcefile;
  }
  
  public void setTargetFile(String targetfile)
  {
    this.targetfile = targetfile;
  }
  
  public void setAlignedFile(String alignedfile)
  {
    this.alignedfile = alignedfile;
  }
  
  public void setAutoSavedAlignedFile(String autosavedalignedfile)
  {
    this.auto_saved_alignedfile = autosavedalignedfile;
  }
  
  public void replaceAlignedFile(String newalignedfile)
    throws Exception
  {
    new File(this.alignedfile).delete();
    FileUtils.copyFileToDirectory(new File(newalignedfile), new File(this.prjfolder));
  }
  
  public void setReformattedSourceFile(String reformattedsourcefile)
  {
    this.reformattedsourcefile = reformattedsourcefile;
  }
  
  public void setReformattedTargetFile(String reformattedtargetfile)
  {
    this.reformattedtargetfile = reformattedtargetfile;
  }
  
  public void setReformattedTargetMapFile(String reformattedtargetmapfile)
  {
    this.reformattedtargetmapfile = reformattedtargetmapfile;
  }
  
  public void setSourceTxlfSeg(String sourcetxlfseg)
  {
    this.sourcetxlf_seg = sourcetxlfseg;
  }
  
  public void setSourceTxlfNonSeg(String sourcetxlfnonseg)
  {
    this.sourcetxlf_nonSeg = sourcetxlfnonseg;
  }
  
  public void setReformattedSourceTxlf_NonSeg(String reformattedsourcetxlf_nonSeg)
  {
    this.reformattedsourcetxlf_nonSeg = reformattedsourcetxlf_nonSeg;
  }
  
  public void setReformattedSourceTxlf_Seg(String reformattedsourcetxlf_seg)
  {
    this.reformattedsourcetxlf_seg = reformattedsourcetxlf_seg;
  }
  
  public void setReformattedTargetTxlf_NonSeg(String reformattedtargetetxlf_nonseg)
  {
    this.reformattedtargettxlf_nonSeg = reformattedtargetetxlf_nonseg;
  }
  
  public void setReformattedTargetTxlf_Seg(String reformattedtargetetxlf_seg)
  {
    this.reformattedtargettxlf_seg = reformattedtargetetxlf_seg;
  }
  
  public void setPopulatedSourceTxlf(String populatedsourcetxlf)
  {
    this.populatedsourcetxlf = populatedsourcetxlf;
  }
  
  public void setHtmlReportForTranslation(String htmlreportfortranslation)
  {
    this.htmlreportfortranslation = htmlreportfortranslation;
  }
  
  public void setTempHtmlReportForTranslation(String temphtmlreportfortranslation)
  {
    this.htmlreportfortranslation_temp = temphtmlreportfortranslation;
  }
  
  public void setTranslatedTxlf(String translatedtxlf)
  {
    this.translatedtxlf = translatedtxlf;
  }
  
  public void setPrjFolder(String prjfolder)
  {
    this.prjfolder = prjfolder;
  }
  
  public void setPrjInfoFile(String prjinfofile)
  {
    this.prjinfofile = prjinfofile;
  }
  
  public String getPrjInfoFile()
  {
    return this.prjinfofile;
  }
  
  public void setSourceLanguage(String sourcelanguage)
  {
    this.sourcelanguage = sourcelanguage;
  }
  
  public void setTargetLanguage(String targetlanguage)
  {
    this.targetlanguage = targetlanguage;
  }
  
  public void setCreatorID(String creatorid)
  {
    this.creatorid = creatorid;
  }
  
  public void setErrorReason(String errorreason)
  {
    this.errorreason = errorreason;
  }
  
  public LinkedHashMap<String, List<String>> getAlignedSrcParas()
  {
    return this.src_paras;
  }
  
  public LinkedHashMap<String, List<String>> getAlignedTrgParas()
  {
    return this.trg_paras;
  }
  
  public LinkedHashMap<String, List<String>> getMissingTrgParas()
  {
    return this.missing_trg_paras;
  }
  
  public LinkedHashMap<String, List<String>> getMissingTrgSegs()
  {
    return this.missing_trg_segs;
  }
  
  public LinkedHashMap<String, List<String>> getSrcSegs()
  {
    return this.src_segs;
  }
  
  public LinkedHashMap<String, List<String>> getTrgSegs()
  {
    return this.trg_segs;
  }
  
  public LinkedHashMap<String, String> getLockedParaSeqs()
  {
    return this.locked_para_seqs;
  }
  
  public LinkedHashMap<String, String> getLockedSegSeqs()
  {
    return this.locked_seg_seqs;
  }
  
  public LinkedHashMap<String, String> getReviewSegSeqs()
  {
    return this.review_seg_seqs;
  }
  
  public LinkedHashMap<String, String> getIgnoreSegSeqs()
  {
    return this.ignore_seg_seqs;
  }
  
  public LinkedHashMap<String, String> getAligneddParaSeqs()
  {
    return this.aligned_para_seqs;
  }
  
  public String getErrorReason()
  {
    return this.errorreason;
  }
  
  public void createReformattedDocument(String aligntype)
    throws Exception
  {
    this.errorreason = "Unknown error";
    com.aspose.words.Document doc_src = new com.aspose.words.Document(this.sourcefile);
    
    File backupsourcefolder = new File(this.prjfolder + "/source_backup");
    if (!backupsourcefolder.exists()) {
      backupsourcefolder.mkdirs();
    }
    this.backupsourcefile = (backupsourcefolder.getAbsolutePath() + File.separator + new File(this.sourcefile).getName());
    doc_src.save(this.backupsourcefile);
    if (!isReplaceStyleAvailable(doc_src))
    {
      this.errorreason = "Cannot reformat source file, replace styles not available";
      throw new Exception("cannot reformat source file, replace styles not available");
    }
    System.out.println("\"underline\" replaced by \"" + Underline.getName(this.replacestyles[0]) + "\" underline");
    System.out.println("\"strikethrough\" replaced by \"" + (this.replacestyles[1] == 99 ? "double strikethrough" : Underline.getName(this.replacestyles[1])) + "\"");
    System.out.println("\"underline + strikethrough\" replaced by \"" + (this.replacestyles[1] == 99 ? Underline.getName(this.replacestyles[0]) + " + double strikethrough" : Underline.getName(this.replacestyles[2])) + "\"");
    for (int i = 0; i < doc_src.getRevisions().getCount(); i++)
    {
      Revision rev = doc_src.getRevisions().get(i);
      if (((rev.getRevisionType() == 1) || (rev.getRevisionType() == 0) || (rev.getRevisionType() == 4)) && (isTrackedHidden(rev)))
      {
        this.errorreason = "There are hidden tracked text in the document";
        throw new Exception("there are hidden tracked text in the document");
      }
      if ((rev.getRevisionType() == 2) && 
        (rev.getParentNode().getNodeType() == 21))
      {
        Run run = (Run)rev.getParentNode();
        if ((run.isDeleteRevision()) || (run.isInsertRevision()))
        {
          Run clone = (Run)run.deepClone(true);
          rev.accept();
          i--;
          run.setInsertRevision(clone.getInsertRevision());
          run.setDeleteRevision(clone.getDeleteRevision());
        }
      }
    }
    if (this.replacestyles[1] == doublestrikethrough) {
      for (int t = 0; t < doc_src.getChildNodes(21, true).getCount(); t++)
      {
        Run run = (Run)doc_src.getChildNodes(21, true).get(t);
        if ((run.getFont().getUnderline() == 1) && (run.getFont().getStrikeThrough() == true))
        {
          run.getFont().setStrikeThrough(false);
          run.getFont().setUnderline(0);
          
          run.getFont().setDoubleStrikeThrough(true);
          run.getFont().setUnderline(this.replacestyles[0]);
        }
        else if (run.getFont().getUnderline() == 1)
        {
          run.getFont().setUnderline(0);
          
          run.getFont().setUnderline(this.replacestyles[0]);
        }
        else if (run.getFont().getStrikeThrough() == true)
        {
          run.getFont().setStrikeThrough(false);
          
          run.getFont().setDoubleStrikeThrough(true);
        }
      }
    } else {
      for (int t = 0; t < doc_src.getChildNodes(21, true).getCount(); t++)
      {
        Run run = (Run)doc_src.getChildNodes(21, true).get(t);
        if ((run.getFont().getUnderline() == 1) && (run.getFont().getStrikeThrough() == true))
        {
          run.getFont().setStrikeThrough(false);
          run.getFont().setUnderline(0);
          
          run.getFont().setUnderline(this.replacestyles[2]);
        }
        else if (run.getFont().getUnderline() == 1)
        {
          run.getFont().setUnderline(0);
          
          run.getFont().setUnderline(this.replacestyles[0]);
        }
        else if (run.getFont().getStrikeThrough() == true)
        {
          run.getFont().setStrikeThrough(false);
          
          run.getFont().setUnderline(this.replacestyles[1]);
        }
      }
    }
    System.out.println("normalizing whitespace after ESMs...");
    normalizeSpaceAfterESM(doc_src, Locale.makeLocale(this.sourcelanguage));
    if (getStopExcution()) {
      throw new Exception("job cancelled.");
    }
    System.out.println("deleting comments and shape alt text in source...");
    DeleteCommentsAndShapeAltText(doc_src);
    if (getStopExcution()) {
      throw new Exception("job cancelled.");
    }
    System.out.println("rmeove hyperlinks in source...");
    if (getStopExcution()) {
      throw new Exception("job cancelled.");
    }
    System.out.println("merging sections in source...");
    mergeSectionsInDocuemnt(doc_src);
    if (getStopExcution()) {
      throw new Exception("job cancelled.");
    }
    System.out.println("converting move tracks to insertion/deletion...");
    convertMoveToIDTracks(doc_src);
    if (getStopExcution()) {
      throw new Exception("job cancelled.");
    }
    doc_src.save(this.sourcefile);
    

    System.out.println("rmeove hyperlinks in source...");
    

    System.out.println("merging paragraphs in source...");
    mergeRevision(doc_src);
    mergeParagraphsInDocuemnt(doc_src);
    
    System.out.println("creating fake tracks...");
    createFakeTrackChanges(doc_src);
    
    File reformattedsourcefolder = new File(this.prjfolder + "/source_reformatted");
    if (!reformattedsourcefolder.exists()) {
      reformattedsourcefolder.mkdirs();
    }
    this.reformattedsourcefile = (reformattedsourcefolder.getAbsolutePath() + File.separator + new File(this.sourcefile).getName());
    if (new File(this.reformattedsourcefile).exists()) {
      new File(this.reformattedsourcefile).delete();
    }
    System.out.println("saving source...");
    doc_src.save(this.reformattedsourcefile);
    

    File reformattedtargetfolder = new File(this.prjfolder + "/target_reformatted");
    if (!reformattedtargetfolder.exists()) {
      reformattedtargetfolder.mkdirs();
    }
    this.reformattedtargetfile = (reformattedtargetfolder.getAbsolutePath() + File.separator + new File(this.targetfile).getName());
    if (new File(this.reformattedtargetfile).exists()) {
      new File(this.reformattedtargetfile).delete();
    }
    com.aspose.words.Document doc_trg = new com.aspose.words.Document(this.targetfile);
    System.out.println("reformatting target file...");
    if (aligntype.equals("sequential")) {
      reformatTargetFile(doc_trg);
    } else if (aligntype.equals("auto")) {
      reformatTargetFile_auto(doc_trg);
    }
    doc_trg.save(this.reformattedtargetfile);
  }
  
  private void reformatTargetFile(com.aspose.words.Document doc_trg)
    throws Exception
  {
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    normalizeSpaceAfterESM(doc_trg, Locale.makeLocale(this.targetlanguage));
    DeleteCommentsAndShapeAltText(doc_trg);
    

    convertMoveToIDTracks(doc_trg);
    while (doc_trg.getRevisions().getCount() != 0)
    {
      Revision rev = doc_trg.getRevisions().get(0);
      rev.reject();
    }
  }
  
  private void reformatTargetFile_auto(com.aspose.words.Document doc_trg)
    throws Exception
  {
    normalizeSpaceAfterESM(doc_trg, Locale.makeLocale(this.targetlanguage));
    DeleteCommentsAndShapeAltText(doc_trg);
    

    convertMoveToIDTracks(doc_trg);
    while (doc_trg.getRevisions().getCount() != 0)
    {
      Revision rev = doc_trg.getRevisions().get(0);
      rev.reject();
    }
  }
  
  private void convertMoveToIDTracks(com.aspose.words.Document doc)
    throws Exception
  {
    com.aspose.words.Document temp = new com.aspose.words.Document();
    com.aspose.words.DocumentBuilder db = new com.aspose.words.DocumentBuilder(temp);
    Paragraph temp_para_del = db.insertParagraph();
    temp.startTrackRevisions("");
    temp_para_del.remove();
    
    db.writeln();
    Paragraph temp_para_ins = (Paragraph)db.getCurrentParagraph().getPreviousSibling();
    
    NodeCollection moveFromStarts = doc.getChildNodes(13, true);
    NodeCollection moveFromEnds = doc.getChildNodes(14, true);
    NodeCollection moveToStarts = doc.getChildNodes(15, true);
    NodeCollection moveToEnds = doc.getChildNodes(16, true);
    for (int i = 0; i < moveFromStarts.getCount(); i++)
    {
      com.aspose.words.Node curNode = moveFromStarts.get(i);
      while (curNode.getNodeType() != 14)
      {
        curNode = curNode.nextPreOrder(doc);
        if (curNode == null) {
          break;
        }
        if ((curNode != null) && (curNode.getNodeType() == 21))
        {
          Run run = (Run)curNode;
          Run runClone = new Run(doc, run.getText());
          runClone.getFont().setName(run.getFont().getName());
          run.getParentNode().insertAfter(runClone, run);
          run.remove();
          doc.startTrackRevisions("");
          runClone.remove();
          doc.stopTrackRevisions();
          curNode = runClone;
        }
        else if ((curNode != null) && (curNode.getNodeType() == 8))
        {
          Paragraph para = (Paragraph)curNode;
          if ((curNode.getPreviousSibling() != null) && (curNode.getPreviousSibling().getNodeType() == 8))
          {
            Paragraph prev_para = (Paragraph)curNode.getPreviousSibling();
            prev_para.setDeleteRevision(temp_para_del.getDeleteRevision());
          }
        }
      }
    }
    for (int i = 0; i < moveToStarts.getCount(); i++)
    {
      com.aspose.words.Node curNode = moveToStarts.get(i);
      while (curNode.getNodeType() != 16)
      {
        curNode = curNode.nextPreOrder(doc);
        if (curNode == null) {
          break;
        }
        if ((curNode != null) && (curNode.getNodeType() == 21))
        {
          Run run = (Run)curNode;
          Run runClone = new Run(doc, run.getText());
          runClone.getFont().setName(run.getFont().getName());
          doc.startTrackRevisions("");
          run.getParentNode().insertAfter(runClone, run);
          doc.stopTrackRevisions();
          run.remove();
          curNode = runClone;
        }
        else if ((curNode != null) && (curNode.getNodeType() == 8))
        {
          Paragraph para = (Paragraph)curNode;
          if ((para.getPreviousSibling() != null) && (para.getPreviousSibling().getNodeType() == 8))
          {
            Paragraph prev_para = (Paragraph)para.getPreviousSibling();
            prev_para.setInsertRevision(temp_para_ins.getInsertRevision());
          }
        }
      }
    }
  }
  
  private void createFakeTrackChanges(com.aspose.words.Document doc)
    throws Exception
  {
    boolean ismovefrom = false;
    boolean ismoveto = false;
    HashSet<Footnote> set = new HashSet();
    for (int i = 0; i < doc.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node node = doc.getChildNodes(0, true).get(i);
      if (node.getNodeType() == 13)
      {
        ismovefrom = true;
      }
      else if (node.getNodeType() == 14)
      {
        ismovefrom = false;
      }
      else if (node.getNodeType() == 15)
      {
        ismoveto = true;
      }
      else if (node.getNodeType() == 16)
      {
        ismoveto = false;
      }
      else if (node.getNodeType() == 20)
      {
        Footnote fn = (Footnote)node;
        if (ismovefrom)
        {
          System.out.println(fn.getText());
          set.add(fn);
        }
      }
    }
    doc.getChildNodes(13, true).clear();
    doc.getChildNodes(14, true).clear();
    doc.getChildNodes(15, true).clear();
    doc.getChildNodes(16, true).clear();
    for (int i = 0; i < doc.getChildNodes(21, true).getCount(); i++)
    {
      Run run = (Run)doc.getChildNodes(21, true).get(i);
      
      Font font = run.getFont();
      font.setStrikeThrough(false);
      if ((run.isInsertRevision()) && (!run.isDeleteRevision()))
      {
        Run runClone = new Run(doc, run.getText());
        runClone.getFont().setName(run.getFont().getName());
        runClone.getFont().setUnderline(1);
        runClone.getFont().setStrikeThrough(false);
        run.getParentNode().insertAfter(runClone, run);
        run.remove();
      }
      else if ((run.isInsertRevision()) && (run.isDeleteRevision()))
      {
        run.remove();
        i--;
      }
      else if (run.isDeleteRevision())
      {
        Run runClone = new Run(doc, run.getText());
        runClone.getFont().setName(run.getFont().getName());
        runClone.getFont().setStrikeThrough(true);
        
        run.getParentNode().insertAfter(runClone, run);
        run.remove();
      }
    }
    for (int i = 0; i < doc.getRevisions().getCount(); i++)
    {
      Revision rev = doc.getRevisions().get(i);
      if ((rev.getRevisionType() == 1) && (rev.getParentNode().getNodeType() == 6))
      {
        Row row = (Row)rev.getParentNode();
        for (int j = 0; j < row.getChildNodes(8, true).getCount(); j++)
        {
          Paragraph p = (Paragraph)row.getChildNodes(8, true).get(j);
          if (p.isDeleteRevision()) {
            p.setDeleteRevision(new Paragraph(doc).getDeleteRevision());
          }
          for (int z = 0; z < p.getChildNodes(21, true).getCount(); z++)
          {
            Run run = (Run)p.getChildNodes(21, true).get(z);
            Font font = run.getFont();
            if (!font.getStrikeThrough()) {
              font.setStrikeThrough(true);
            }
          }
        }
        rev.reject();
        i--;
      }
      else if ((rev.getRevisionType() == 1) && ((rev.getParentNode().getNodeType() == 22) || (rev.getParentNode().getNodeType() == 23) || (rev.getParentNode().getNodeType() == 24)))
      {
        rev.reject();
        i--;
      }
      else if ((rev.getRevisionType() == 1) && (rev.getParentNode().getNodeType() == 8))
      {
        rev.reject();
        i--;
      }
      else if ((rev.getRevisionType() == 0) && (rev.getParentNode().getNodeType() == 8))
      {
        rev.accept();
        i--;
      }
      else if ((rev.getRevisionType() == 1) && (rev.getParentNode().getNodeType() == 18))
      {
        rev.reject();
        i--;
      }
      else if ((rev.getRevisionType() == 1) && (rev.getParentNode().getNodeType() == 20))
      {
        rev.reject();
        i--;
      }
      else if ((rev.getRevisionType() == 4) && (rev.getParentNode().getNodeType() == 20))
      {
        Footnote ft = (Footnote)rev.getParentNode();
        if (set.contains(ft))
        {
          rev.reject();
          i--;
        }
      }
      else if (rev.getRevisionType() == 0)
      {
        rev.accept();
      }
      else if (rev.getRevisionType() == 1)
      {
        rev.reject();
      }
    }
    while (doc.getRevisions().getCount() != 0) {
      if (doc.getRevisions().get(0).getRevisionType() == 0) {
        doc.getRevisions().get(0).accept();
      } else if (doc.getRevisions().get(0).getRevisionType() == 1) {
        doc.getRevisions().get(0).reject();
      } else {
        doc.getRevisions().get(0).accept();
      }
    }
  }
  
  private void mergeSectionsInDocuemnt(com.aspose.words.Document doc)
    throws Exception
  {
    boolean[] ismovefromsection = new boolean[doc.getSections().getCount()];
    int index = -1;
    boolean ismovefrom = false;
    for (int i = 0; i < doc.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node node = doc.getChildNodes(0, true).get(i);
      if (node.getNodeType() == 13) {
        ismovefrom = true;
      }
      if (node.getNodeType() == 14) {
        ismovefrom = false;
      }
      if (node.getNodeType() == 2) {
        index++;
      }
      if ((node.getNodeType() == 8) && 
        (((Paragraph)node).isEndOfSection())) {
        if (ismovefrom)
        {
          ismovefromsection[index] = true;
          this.isSectionBreakDeletedORInserted = true;
        }
        else
        {
          ismovefromsection[index] = false;
        }
      }
    }
    for (int i = doc.getSections().getCount() - 2; i >= 0; i--)
    {
      Section prev_section = doc.getSections().get(i + 1);
      Section curr_Section = doc.getSections().get(i);
      int para_count = curr_Section.getChildNodes(8, true).getCount();
      if (para_count > 0)
      {
        Paragraph lastpara = (Paragraph)curr_Section.getChildNodes(8, true).get(para_count - 1);
        if ((lastpara.isDeleteRevision()) || (ismovefromsection[i]))
        {
          prev_section.prependContent(curr_Section);
          curr_Section.remove();
        }
      }
    }
  }
  
  private void mergeRevision(com.aspose.words.Document doc)
    throws Exception
  {
    Paragraph prev = null;
    for (int i = 0; i < doc.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node node = doc.getChildNodes(0, true).get(i);
      if (node.getNodeType() == 21)
      {
        Run run = (Run)node;
        if ((run.isDeleteRevision()) && (run.isInsertRevision()))
        {
          run.remove();
          i--;
        }
      }
      else if (node.getNodeType() == 8)
      {
        Paragraph para = (Paragraph)node;
        if ((prev != null) && (prev.isInsertRevision()) && (prev.isDeleteRevision()))
        {
          for (int j = 0; j < para.getChildNodes().getCount(); j++)
          {
            com.aspose.words.Node nd = para.getChildNodes().get(j);
            if ((nd.getNodeType() != 21) || (!((Run)nd).isDeleteRevision()) || (!((Run)nd).getText().contains(ControlChar.PAGE_BREAK)))
            {
              prev.appendChild(nd.deepClone(true));
              i++;
            }
          }
          prev.setDeleteRevision(para.getDeleteRevision());
          prev.setInsertRevision(para.getInsertRevision());
          
          para.remove();
          i--;
        }
        else
        {
          prev = para;
        }
      }
    }
  }
  
  private void mergeParagraphsInDocuemnt(com.aspose.words.Document doc)
    throws Exception
  {
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    Paragraph prev_para = null;
    Paragraph sample = new Paragraph(doc);
    
    Shape shape = null;
    boolean isStillSplitting = false;
    boolean ismoveaway = false;
    boolean ismoveto = false;
    int toskip = 0;
    List<Paragraph> toremove = new ArrayList();
    for (int i = 0; i < doc.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node node = doc.getChildNodes(0, true).get(i);
      if (node.getNodeType() == 18)
      {
        shape = (Shape)node;
      }
      else if (node.getNodeType() == 13)
      {
        ismoveaway = true;
      }
      else if (node.getNodeType() == 14)
      {
        ismoveaway = false;
      }
      else if (node.getNodeType() == 15)
      {
        ismoveto = true;
      }
      else if (node.getNodeType() == 16)
      {
        ismoveto = false;
      }
      else if (node.getNodeType() == 8)
      {
        if (toskip > 0)
        {
          toskip--;
        }
        else
        {
          Paragraph para = (Paragraph)node;
          if ((para.isEndOfSection()) && (
            (para.isInsertRevision()) || (para.isDeleteRevision()))) {
            this.isSectionBreakDeletedORInserted = true;
          }
          if ((para.isInCell()) && (para.getPreviousSibling() == null))
          {
            prev_para = null;
            isStillSplitting = false;
          }
          if ((shape != null) && (shape.getChildNodes(8, true).getCount() > 0) && (shape.getChildNodes(8, true).get(0) == para))
          {
            prev_para = null;
            isStillSplitting = false;
          }
          if (prev_para != null)
          {
            if (isStillSplitting)
            {
              if ((prev_para.isInsertRevision()) || (ismoveto))
              {
                if (extractionSupportImpl.isExtractable(getParaText(para))) {
                  para.appendChild(new Run(doc, "&parains;"));
                }
              }
              else {
                isStillSplitting = false;
              }
            }
            else if (((prev_para.isInsertRevision()) || (ismoveto)) && (!isWholeParaInserted(prev_para)))
            {
              if (extractionSupportImpl.isExtractable(getParaText(para))) {
                para.appendChild(new Run(doc, "&parains;"));
              }
              isStillSplitting = true;
            }
            if ((para.isEndOfCell()) || (para.isEndOfHeaderFooter()) || (para.isEndOfSection())) {
              isStillSplitting = false;
            }
            if ((shape != null) && (shape.getChildNodes(8, true).getCount() > 0) && (shape.getChildNodes(8, true).get(shape.getChildNodes(8, true).getCount() - 1) == para)) {
              isStillSplitting = false;
            }
            if (((prev_para.isDeleteRevision()) || (ismoveaway)) && (!isWholeParaDeleted(prev_para)))
            {
              if ((para.getChildNodes(21, true).getCount() != 0) || (para.isDeleteRevision()))
              {
                prev_para.appendChild(new Run(doc, "&paradel;"));
                i++;
                for (int j = 0; j < para.getChildNodes().getCount(); j++)
                {
                  com.aspose.words.Node nd = para.getChildNodes().get(j);
                  if ((nd.getNodeType() != 21) || (!((Run)nd).isDeleteRevision()) || (!((Run)nd).getText().contains(ControlChar.PAGE_BREAK)))
                  {
                    prev_para.appendChild(nd.deepClone(true));
                    i++;
                  }
                }
                if (para.getChildNodes(14, true).getCount() != 0) {
                  ismoveaway = false;
                }
              }
              prev_para.setDeleteRevision(para.getDeleteRevision());
              prev_para.setInsertRevision(para.getInsertRevision());
              if (!para.isInsertRevision())
              {
                for (int z = 0; z < para.getChildNodes().getCount(); z++)
                {
                  com.aspose.words.Node temp_nd = para.getChildNodes().get(z);
                  if (temp_nd.getNodeType() == 15) {
                    ismoveto = true;
                  } else if (temp_nd.getNodeType() == 16) {
                    ismoveto = false;
                  }
                }
                para.remove();
                i--;
              }
              else
              {
                toremove.add(para);
                prev_para = para;
              }
            }
            else if ((prev_para.isDeleteRevision()) && (isWholeParaDeleted(prev_para)))
            {
              prev_para.setDeleteRevision(sample.getDeleteRevision());
              prev_para = para;
            }
            else if ((para.isEndOfCell()) || (para.isEndOfHeaderFooter()))
            {
              para.setDeleteRevision(sample.getDeleteRevision());
              prev_para = null;
            }
            else if ((shape != null) && (shape.getChildNodes(8, true).getCount() > 0) && (shape.getChildNodes(8, true).get(shape.getChildNodes(8, true).getCount() - 1) == para))
            {
              para.setDeleteRevision(sample.getDeleteRevision());
              prev_para = null;
            }
            else
            {
              prev_para = para;
            }
          }
          else
          {
            if ((isStillSplitting) && (extractionSupportImpl.isExtractable(getParaText(para))))
            {
              para.appendChild(new Run(doc, "&parains;"));
              i++;
            }
            if (para.isInsertRevision())
            {
              if ((extractionSupportImpl.isExtractable(getParaText(para))) && (!isWholeParaInserted(para))) {
                isStillSplitting = true;
              }
            }
            else {
              isStillSplitting = false;
            }
            if ((para.isEndOfCell()) || (para.isEndOfHeaderFooter()))
            {
              if ((para.getNextSibling() == null) && (para.getPreviousSibling() == null) && (para.isDeleteRevision()) && (isWholeParaDeleted(para))) {
                para.setDeleteRevision(sample.getDeleteRevision());
              }
              prev_para = null;
            }
            else if ((shape != null) && (shape.getChildNodes(8, true).getCount() > 0) && (shape.getChildNodes(8, true).get(shape.getChildNodes(8, true).getCount() - 1) == para))
            {
              if ((para.getNextSibling() == null) && (para.getPreviousSibling() == null) && (para.isDeleteRevision()) && (isWholeParaDeleted(para))) {
                para.setDeleteRevision(sample.getDeleteRevision());
              }
              prev_para = null;
            }
            else
            {
              prev_para = para;
            }
          }
        }
      }
      else if (node.getNodeType() == 5)
      {
        Table tb = (Table)node;
        if (isWholeTableDeleted(tb, doc)) {
          toskip = tb.getChildNodes(8, true).getCount();
        }
      }
    }
    for (Paragraph p : toremove) {
      if (p != null) {
        p.removeAllChildren();
      }
    }
    doc.save("C:\\Program Files (x86)\\pa\\paprjs\\testprj23\\test.docx");
  }
  
  private boolean isWholeTableDeleted(Table tb, com.aspose.words.Document doc)
  {
    Row sampler = new Row(doc);
    for (int i = 0; i < tb.getChildNodes(6, true).getCount(); i++) {
      if (((Row)tb.getChildNodes(6, true).get(i)).getDeleteRevision() == sampler.getDeleteRevision()) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isWholeParaDeleted(Paragraph para)
  {
    for (int i = 0; i < para.getChildNodes(21, true).getCount(); i++) {
      if (!((Run)para.getChildNodes(21, true).get(i)).isDeleteRevision()) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isWholeParaInserted(Paragraph para)
  {
    for (int i = 0; i < para.getChildNodes(21, true).getCount(); i++)
    {
      Run run = (Run)para.getChildNodes(21, true).get(i);
      if (!run.isInsertRevision()) {
        return false;
      }
    }
    return true;
  }
  
  public String[] createAlignedXML()
    throws Exception
  {
    System.out.println("creating aligned xml....");
    
    String[] res = new String[1];
    
    this.txlftrgsegmap = new LinkedHashMap();
    this.alignedfile = (this.prjfolder + File.separator + "rev_aligned.xml");
    this.reformattedtargetmapfile = (this.prjfolder + File.separator + "target_reformatted" + File.separator + ".mp");
    StringBuffer sbmp = new StringBuffer();
    if (new File(this.alignedfile).exists()) {
      new File(this.alignedfile).delete();
    }
    SegmenterFactory factory = new SegmenterFactory();
    Configuration segconfig = createConfigForSegmenter(false, this.sourcelanguage);
    Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(this.sourcelanguage), segconfig);
    
    org.dom4j.Document document = DocumentHelper.createDocument();
    org.dom4j.Element root = document.addElement("alinger");
    org.dom4j.Element head = root.addElement("head");
    head.addAttribute("src_lang", this.sourcelanguage);
    head.addAttribute("trg_lang", this.targetlanguage);
    head.addAttribute("creator", this.creatorid);
    org.dom4j.Element aligned = root.addElement("aligned");
    org.dom4j.Element orphans = root.addElement("orphans");
    

    org.dom4j.Document document_source_formatted_nonSeg = XmlParser.parseXmlFile(this.reformattedsourcetxlf_nonSeg);
    org.dom4j.Element root_source_formatted_nonSeg = document_source_formatted_nonSeg.getRootElement();
    List list_source_formatted_nonSeg = root_source_formatted_nonSeg.selectNodes("//*[name() = 'trans-unit']");
    




    org.dom4j.Document document_target_nonSeg = XmlParser.parseXmlFile(this.reformattedtargettxlf_nonSeg);
    org.dom4j.Element root_target_nonSeg = document_target_nonSeg.getRootElement();
    
    List list_target_nonSeg = root_target_nonSeg.selectNodes("//*[name() = 'trans-unit']");
    
    org.dom4j.Document document_target_seg = XmlParser.parseXmlFile(this.reformattedtargettxlf_seg);
    org.dom4j.Element root_target_seg = document_target_seg.getRootElement();
    
    List list_target_seg = root_target_seg.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    int trg_para_count = 0;
    
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    

    Workbook wb = new Workbook();
    Cells cells = wb.getWorksheets().get(0).getCells();
    int cnt = 0;
    

    boolean issrcfirsthf = true;
    boolean istrgfirsthf = true;
    int gcount = -1;
    for (int i = 0; i < list_source_formatted_nonSeg.size(); i++)
    {
      org.dom4j.Element src_txlf = ((org.dom4j.Element)list_source_formatted_nonSeg.get(i)).element("source");
      String merged_text = getTxlfElementText_withFakeTC(src_txlf);
      if (extractionSupportImpl.isExtractable(merged_text.replace("&amp;paradel;", "").replace("&amp;parains;", "")))
      {
        gcount++;
        

        org.dom4j.Element group = aligned.addElement("group");
        group.addAttribute("id", Integer.toString(gcount));
        
        merged_text = trimText(merged_text, true)[0];
        
        org.dom4j.Element merged_src_text = group.addElement("text");
        
        merged_src_text.setText(merged_text.replace("&amp;paradel;", "").replace("&amp;parains;", ""));
        
        String[] split_merged_text = merged_text.replaceAll("(&amp;paradel;)+", "&amp;paradel;").replaceAll("^&amp;paradel;", "").replaceAll("&amp;paradel;$", "").split("&amp;paradel;");
        List<String> segmentsGroup = segmentStringWithRevs(merged_text.replaceAll("(&amp;paradel;)+", "&amp;paradel;").replace("&amp;parains;", ""), this.sourcelanguage);
        
        List<List<String>> resegmentedGroup = new ArrayList();
        resegmentedGroup.add(new ArrayList());
        int idx = 0;
        String orgs;
        String[] newsegs;
        for (int s = 0; s < segmentsGroup.size(); s++)
        {
          orgs = (String)segmentsGroup.get(s);
          if (orgs.contains("&amp;paradel;"))
          {
            newsegs = orgs.split("&amp;paradel;");
            for (int ss = 0; ss < newsegs.length; ss++)
            {
              String sss = newsegs[ss];
              if (!sss.trim().equals("")) {
                ((List)resegmentedGroup.get(idx)).add(fixMissingTags(sss));
              }
              if ((((List)resegmentedGroup.get(idx)).size() != 0) && (ss != newsegs.length - 1))
              {
                resegmentedGroup.add(new ArrayList());
                idx++;
              }
            }
            if (orgs.trim().endsWith("&amp;paradel;"))
            {
              resegmentedGroup.add(new ArrayList());
              idx++;
            }
          }
          else
          {
            ((List)resegmentedGroup.get(idx)).add(fixMissingTags(orgs));
          }
        }
        if (split_merged_text.length > resegmentedGroup.size())
        {
          System.out.println(i);
          System.out.println("merged_text: " + merged_text);
          for (String smt : split_merged_text)
          {
            System.out.println("split_merged_text: " + smt);
          }
          for (List<String> smts : resegmentedGroup) {
            System.out.println("resegmentedGroup: " + smts);
          }
          for (String smtss : segmentsGroup) {
            System.out.println("segmentedGroup: " + smtss);
          }
        }
        for (int j = 0; j < split_merged_text.length; j++) {
          if (!split_merged_text[j].replaceAll("<(/)*ins>|<(/)*del>", "").trim().equals(""))
          {
            split_merged_text[j] = fixMissingTags(split_merged_text[j]);
            
            org.dom4j.Element unit = group.addElement("unit");
            unit.addAttribute("id", Integer.toString(j));
            unit.addAttribute("alignsegs", "false");
            
            org.dom4j.Element src = unit.addElement("src_para");
            org.dom4j.Element src_text = src.addElement("text");
            boolean ishf = split_merged_text[j].contains("&amp;hf;");
            if (!ishf) {
              issrcfirsthf = false;
            }
            boolean isAddedPara = split_merged_text[j].contains("&amp;parains;");
            src.addAttribute("added", "" + isAddedPara);
            String[] trim_result = trimText(split_merged_text[j].replace("&amp;parains;", "").replace("&amp;hf;", ""), false);
            src.addAttribute("lefttrim", trim_result[1]);
            src.addAttribute("righttrim", trim_result[2]);
            split_merged_text[j] = trim_result[0];
            
            int src_tctype_para = TrackChangeHelper.getTrackChangeType(split_merged_text[j]);
            src.addAttribute("tctype", TrackChangeType.getName(src_tctype_para));
            String rejected_src = split_merged_text[j].replaceAll("(?s)<ins>.*?</ins>", "").replace("<del>", "").replace("</del>", "");
            if ((!extractionSupportImpl.isExtractable(rejected_src)) || (ishf)) {
              unit.addAttribute("locked", "true");
            } else {
              unit.addAttribute("locked", "false");
            }
            src_text.setText(split_merged_text[j]);
            
            cells.get(cnt, 0).setHtmlString("<html>" + split_merged_text[j].replace("ins>", "u>").replace("del>", "strike>").replace("<br> ", "&#8629;<br>") + "</html>");
            

            org.dom4j.Element src_segs = src.addElement("segments");
            List<String> segments = (List)resegmentedGroup.get(j);
            for (int z = 0; z < segments.size(); z++)
            {
              String segment_text = trimText((String)segments.get(z), false)[0];
              org.dom4j.Element src_seg = src_segs.addElement("src_seg");
              src_seg.addAttribute("id", Integer.toString(z));
              src_seg.addAttribute("needreview", "false");
              src_seg.addAttribute("ignored", "false");
              int tctype_seg = TrackChangeHelper.getTrackChangeType(segment_text);
              src_seg.addAttribute("tctype", TrackChangeType.getName(tctype_seg));
              String accepted_t = segment_text.replaceAll("(?s)<del>.*?</del>", "").replace("<ins>", "").replace("</ins>", "");
              src_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(accepted_t)));
              
              String rejected_s = segment_text.replaceAll("(?s)<ins>.*?</ins>", "").replace("<del>", "").replace("</del>", "");
              if ((!extractionSupportImpl.isExtractable(rejected_s)) || (ishf)) {
                src_seg.addAttribute("locked", "true");
              } else {
                src_seg.addAttribute("locked", "false");
              }
              src_seg.setText(segment_text);
            }
            org.dom4j.Element trg = unit.addElement("trg_para");
            if ((src_tctype_para != 1) && (!isAddedPara) && (!ishf) && (trg_para_count < list_target_nonSeg.size()))
            {
              trg.addAttribute("id", Integer.toString(gcount) + " - " + Integer.toString(j));
              org.dom4j.Element trg_text = trg.addElement("text");
              org.dom4j.Element trg_txlf = ((org.dom4j.Element)list_target_nonSeg.get(trg_para_count)).element("source");
              org.dom4j.Element trg_txlf_seg = (org.dom4j.Element)list_target_seg.get(trg_para_count);
              while (trg_txlf.getText().contains("&hf;"))
              {
                trg_para_count++;
                trg_txlf = ((org.dom4j.Element)list_target_nonSeg.get(trg_para_count)).element("source");
                trg_txlf_seg = (org.dom4j.Element)list_target_seg.get(trg_para_count);
              }
              istrgfirsthf = false;
              

              String trg_formatted_text = getTxlfElementText_normal(trg_txlf);
              trg_text.setText(trg_formatted_text.replace("&amp;hf;", ""));
              
              cells.get(cnt, 1).setHtmlString("<html>" + trg_formatted_text.replace("ins>", "u>").replace("del>", "strike>").replace("&amp;hf;", "").replace("<br> ", "&#8629;<br>") + "</html>");
              cnt++;
              

              org.dom4j.Element trg_segs = trg.addElement("segments");
              List<String> trgsegs = segmentStringWithRevs(trg_formatted_text, this.targetlanguage);
              List<org.dom4j.Element> srcsegs = src_segs.elements("src_seg");
              int trg_tmp_cnt = 0;
              for (int z = 0; trg_tmp_cnt < trgsegs.size(); z++)
              {
                org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
                trg_seg.addAttribute("id", Integer.toString(z));
                trg_seg.addAttribute("edited", "false");
                if ((z < srcsegs.size()) && (((org.dom4j.Element)srcsegs.get(z)).attributeValue("tctype").equals(TrackChangeType.getName(1))))
                {
                  trg_seg.addAttribute("isExtractable", "false");
                  trg_seg.setText("");
                }
                else
                {
                  String trgsegtext = ((String)trgsegs.get(trg_tmp_cnt)).replace("&amp;hf;", "").trim();
                  trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(trgsegtext)));
                  trg_seg.setText(trgsegtext);
                  

                  String mapid = Integer.toString(gcount) + " - " + Integer.toString(j) + " - " + Integer.toString(z);
                  List t = ((org.dom4j.Element)trg_txlf_seg.elements("trans-unit").get(trg_tmp_cnt)).content();
                  
                  sbmp.append(mapid + "\t" + trg_para_count + "\t" + trg_tmp_cnt + "\n");
                  
                  trg_tmp_cnt++;
                }
              }
              trg_para_count++;
            }
            else
            {
              trg.addAttribute("id", Integer.toString(gcount) + " - " + Integer.toString(j));
              org.dom4j.Element trg_text = trg.addElement("text");
              trg_text.setText("");
              trg.addElement("segments");
              cnt++;
            }
            int trgcnt = trg.element("segments").elements("trg_seg").size();
            int srccnt = src.element("segments").elements("src_seg").size();
            if (trgcnt < srccnt) {
              for (int x = 1; x <= srccnt - trgcnt; x++)
              {
                org.dom4j.Element trg_seg = trg.element("segments").addElement("trg_seg");
                trg_seg.addAttribute("id", Integer.toString(trgcnt + x - 1));
                trg_seg.addAttribute("edited", "false");
                trg_seg.addAttribute("isExtractable", "false");
                trg_seg.setText("");
              }
            }
          }
        }
      }
    }
    int unitcnt = list_source_formatted_nonSeg.size();
    for (int i = trg_para_count; i < list_target_nonSeg.size(); i++)
    {
      org.dom4j.Element trg_txlf = ((org.dom4j.Element)list_target_nonSeg.get(trg_para_count)).element("source");
      org.dom4j.Element trg_txlf_seg = (org.dom4j.Element)list_target_seg.get(trg_para_count);
      if (!trg_txlf.getText().contains("&hf;"))
      {
        org.dom4j.Element group = aligned.addElement("group");
        group.addAttribute("id", Integer.toString(unitcnt));
        group.addElement("text").setText("");
        
        org.dom4j.Element unit = group.addElement("unit");
        unit.addAttribute("id", "0");
        unit.addAttribute("alignsegs", "false");
        unit.addAttribute("locked", "false");
        
        org.dom4j.Element trg = unit.addElement("trg_para");
        trg.addAttribute("id", Integer.toString(unitcnt) + " - 0");
        org.dom4j.Element trg_text = trg.addElement("text");
        
        String trg_formatted_text = getTxlfElementText_normal(trg_txlf);
        trg_text.setText(trg_formatted_text.replace("&amp;hf;", ""));
        

        org.dom4j.Element trg_segs = trg.addElement("segments");
        List<String> trgsegs = segmentStringWithRevs(trg_formatted_text.replace("&amp;hf;", ""), this.targetlanguage);
        for (int z = 0; z < trgsegs.size(); z++)
        {
          org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
          trg_seg.addAttribute("id", Integer.toString(z));
          trg_seg.addAttribute("edited", "false");
          trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable((String)trgsegs.get(z))));
          
          trg_seg.setText(((String)trgsegs.get(z)).trim());
          

          String mapid = Integer.toString(unitcnt) + " - 0 - " + Integer.toString(z);
          List t = ((org.dom4j.Element)trg_txlf_seg.elements("trans-unit").get(z)).content();
          
          sbmp.append(mapid + "\t" + trg_para_count + "\t" + z + "\n");
        }
        trg_para_count++;
        unitcnt++;
      }
    }
    wb.save(this.prjfolder + File.separator + "verifySegsPop.xlsx");
    
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.alignedfile)), "UTF8");
    document.write(writer);
    writer.close();
    
    OutputStreamWriter writermp = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.reformattedtargetmapfile)), "UTF8");
    writermp.write(sbmp.toString());
    writermp.close();
    
    return res;
  }
  
  public void createAlignedXML_auto(String prjid)
    throws Exception
  {
    System.out.println("creating aligned xml with nbAligner....");
    
    this.alignedfile = (this.prjfolder + File.separator + "rev_aligned.xml");
    this.reformattedtargetmapfile = (this.prjfolder + File.separator + "target_reformatted" + File.separator + ".mp");
    HashMap<String, String> srcidmap = new HashMap();
    
    this.nbalignerfolder = (this.prjfolder + File.separator + "nbaligner");
    if (!new File(this.nbalignerfolder).exists()) {
      new File(this.nbalignerfolder).mkdir();
    }
    FileUtils.cleanDirectory(new File(this.nbalignerfolder));
    String nbsourcefolder = this.nbalignerfolder + File.separator + this.sourcelanguage;
    new File(nbsourcefolder).mkdir();
    org.dom4j.Document nbsource = DocumentHelper.createDocument();
    org.dom4j.Element root_src = nbsource.addElement("txml");
    root_src.addAttribute("locale", this.sourcelanguage);
    root_src.addAttribute("version", "1.0");
    root_src.addAttribute("segtype", "sentence");
    org.dom4j.Element translatable_src = root_src.addElement("translatable");
    translatable_src.addAttribute("blockId", "1");
    String nbtargetfolder = this.nbalignerfolder + File.separator + this.targetlanguage;
    new File(nbtargetfolder).mkdir();
    org.dom4j.Document nbtarget = DocumentHelper.createDocument();
    org.dom4j.Element root_trg = nbtarget.addElement("txml");
    root_trg.addAttribute("locale", this.targetlanguage);
    root_trg.addAttribute("version", "1.0");
    root_trg.addAttribute("segtype", "sentence");
    org.dom4j.Element translatable_trg = root_trg.addElement("translatable");
    translatable_trg.addAttribute("blockId", "0");
    if (new File(this.alignedfile).exists()) {
      new File(this.alignedfile).delete();
    }
    SegmenterFactory factory = new SegmenterFactory();
    Configuration segconfig = createConfigForSegmenter(false, this.sourcelanguage);
    Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(this.sourcelanguage), segconfig);
    
    org.dom4j.Document document = DocumentHelper.createDocument();
    org.dom4j.Element root = document.addElement("alinger");
    org.dom4j.Element head = root.addElement("head");
    head.addAttribute("src_lang", this.sourcelanguage);
    head.addAttribute("trg_lang", this.targetlanguage);
    head.addAttribute("creator", this.creatorid);
    org.dom4j.Element aligned = root.addElement("aligned");
    org.dom4j.Element orphans = root.addElement("orphans");
    

    org.dom4j.Document document_source_formatted_nonSeg = XmlParser.parseXmlFile(this.reformattedsourcetxlf_nonSeg);
    org.dom4j.Element root_source_formatted_nonSeg = document_source_formatted_nonSeg.getRootElement();
    List list_source_formatted_nonSeg = root_source_formatted_nonSeg.selectNodes("//*[name() = 'trans-unit']");
    




    org.dom4j.Document document_target_nonSeg = XmlParser.parseXmlFile(this.reformattedtargettxlf_nonSeg);
    org.dom4j.Element root_target_nonSeg = document_target_nonSeg.getRootElement();
    
    List list_target_nonSeg = root_target_nonSeg.selectNodes("//*[name() = 'trans-unit']");
    
    org.dom4j.Document document_target_seg = XmlParser.parseXmlFile(this.reformattedtargettxlf_seg);
    org.dom4j.Element root_target_seg = document_target_seg.getRootElement();
    
    List list_target_seg = root_target_seg.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    
    ExtractionSupportImpl extractionSupportImpl_src = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.sourcelanguage));
    Configuration config_src = new BaseConfiguration();
    config_src.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl_src.setConfiguration(config_src);
    
    ExtractionSupportImpl extractionSupportImpl_trg = new ExtractionSupportImpl(Locale.makeLocale(this.targetlanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config_trg = new BaseConfiguration();
    config_trg.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl_trg.setConfiguration(config_trg);
    
    boolean issrcfirsthf = true;
    boolean istrgfirsthf = true;
    int gcount = -1;
    int segmentId = 0;
    for (int i = 0; i < list_source_formatted_nonSeg.size(); i++)
    {
      org.dom4j.Element src_txlf = ((org.dom4j.Element)list_source_formatted_nonSeg.get(i)).element("source");
      String merged_text = getTxlfElementText_withFakeTC(src_txlf);
      if (extractionSupportImpl_src.isExtractable(merged_text.replace("&amp;paradel;", "").replace("&amp;parains;", "")))
      {
        gcount++;
        

        org.dom4j.Element group = aligned.addElement("group");
        group.addAttribute("id", Integer.toString(gcount));
        
        merged_text = trimText(merged_text, true)[0];
        org.dom4j.Element merged_src_text = group.addElement("text");
        
        merged_src_text.setText(merged_text.replace("&amp;paradel;", "").replace("&amp;parains;", ""));
        
        String[] split_merged_text = merged_text.replaceAll("(&amp;paradel;)+", "&amp;paradel;").replaceAll("^&amp;paradel;", "").replaceAll("&amp;paradel;$", "").split("&amp;paradel;");
        List<String> segmentsGroup = segmentStringWithRevs(merged_text.replaceAll("(&amp;paradel;)+", "&amp;paradel;").replace("&amp;parains;", ""), this.sourcelanguage);
        List<List<String>> resegmentedGroup = new ArrayList();
        resegmentedGroup.add(new ArrayList());
        int idx = 0;
        String orgs;
        String[] newsegs;
        for (int s = 0; s < segmentsGroup.size(); s++)
        {
          orgs = (String)segmentsGroup.get(s);
          if (orgs.contains("&amp;paradel;"))
          {
            newsegs = orgs.split("&amp;paradel;");
            for (int ss = 0; ss < newsegs.length; ss++)
            {
              String sss = newsegs[ss];
              if (!sss.trim().equals("")) {
                ((List)resegmentedGroup.get(idx)).add(fixMissingTags(sss));
              }
              if ((((List)resegmentedGroup.get(idx)).size() != 0) && (ss != newsegs.length - 1))
              {
                resegmentedGroup.add(new ArrayList());
                idx++;
              }
            }
            if (orgs.trim().endsWith("&amp;paradel;"))
            {
              resegmentedGroup.add(new ArrayList());
              idx++;
            }
          }
          else
          {
            ((List)resegmentedGroup.get(idx)).add(fixMissingTags(orgs));
          }
        }
        if (split_merged_text.length > resegmentedGroup.size())
        {
          System.out.println(i);
          System.out.println("merged_text: " + merged_text);
          for (String smt : split_merged_text)
          {
            System.out.println("split_merged_text: " + smt);
          }
          for (List<String> smts : resegmentedGroup) {
            System.out.println("resegmentedGroup: " + smts);
          }
          for (String smtss : segmentsGroup) {
            System.out.println("segmentedGroup: " + smtss);
          }
        }
        for (int j = 0; j < split_merged_text.length; j++) {
          if (!split_merged_text[j].replaceAll("<(/)*ins>|<(/)*del>", "").trim().equals(""))
          {
            split_merged_text[j] = fixMissingTags(split_merged_text[j]);
            
            Element unit = group.addElement("unit");
            unit.addAttribute("id", Integer.toString(j));
            unit.addAttribute("alignsegs", "false");
            
            Element src = unit.addElement("src_para");
            org.dom4j.Element src_text = src.addElement("text");
            boolean isAddedPara = split_merged_text[j].contains("&amp;parains;");
            src.addAttribute("added", "" + isAddedPara);
            String[] trim_result = trimText(split_merged_text[j].replace("&amp;parains;", ""), false);
            src.addAttribute("lefttrim", trim_result[1]);
            src.addAttribute("righttrim", trim_result[2]);
            split_merged_text[j] = trim_result[0];
            
            int src_tctype_para = TrackChangeHelper.getTrackChangeType(split_merged_text[j]);
            src.addAttribute("tctype", TrackChangeType.getName(src_tctype_para));
            String rejected_src = split_merged_text[j].replaceAll("(?s)<ins>.*?</ins>", "").replace("<del>", "").replace("</del>", "");
            if (!extractionSupportImpl_src.isExtractable(rejected_src)) {
              unit.addAttribute("locked", "true");
            } else {
              unit.addAttribute("locked", "false");
            }
            src_text.setText(split_merged_text[j]);
            

            org.dom4j.Element src_segs = src.addElement("segments");
            List<String> segments = (List)resegmentedGroup.get(j);
            for (int z = 0; z < segments.size(); z++)
            {
              String segment_text = trimText((String)segments.get(z), false)[0];
              org.dom4j.Element src_seg = src_segs.addElement("src_seg");
              src_seg.addAttribute("id", Integer.toString(z));
              src_seg.addAttribute("needreview", "false");
              src_seg.addAttribute("ignored", "false");
              int tctype_seg = TrackChangeHelper.getTrackChangeType(segment_text);
              src_seg.addAttribute("tctype", TrackChangeType.getName(tctype_seg));
              String accepted_t = segment_text.replaceAll("(?s)<del>.*?</del>", "").replace("<ins>", "").replace("</ins>", "");
              src_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl_src.isExtractable(accepted_t)));
              
              String rejected_s = segment_text.replaceAll("(?s)<ins>.*?</ins>", "").replace("<del>", "").replace("</del>", "");
              if (!extractionSupportImpl_src.isExtractable(rejected_s))
              {
                src_seg.addAttribute("locked", "true");
              }
              else
              {
                src_seg.addAttribute("locked", "false");
                
                org.dom4j.Element segment_src = translatable_src.addElement("segment");
                segment_src.addAttribute("segmentId", Integer.toString(segmentId));
                
                srcidmap.put(i + " - " + j + " - " + z, Integer.toString(segmentId));
                
                segmentId++;
                segment_src.addElement("source").setText(rejected_s);
              }
              src_seg.setText(segment_text);
            }
          }
        }
      }
    }
    segmentId = 0;
    for (int i = 0; i < list_target_nonSeg.size(); i++)
    {
      org.dom4j.Element trg_txlf = ((org.dom4j.Element)list_target_nonSeg.get(i)).element("source");
      
      String trg_formatted_text = getTxlfElementText_normal(trg_txlf);
      List<String> trgsegs = segmentStringWithRevs(trg_formatted_text, this.targetlanguage);
      for (int j = 0; j < trgsegs.size(); j++)
      {
        String trgseg = ((String)trgsegs.get(j)).trim().replaceAll("(\\s)+", " ");
        if (extractionSupportImpl_trg.isExtractable(trgseg))
        {
          org.dom4j.Element segment_trg = translatable_trg.addElement("segment");
          segment_trg.addAttribute("segmentId", Integer.toString(segmentId));
          segmentId++;
          segment_trg.addElement("source").setText(trgseg);
        }
      }
    }
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(nbsourcefolder + File.separator + this.sourcelanguage + ".txml")), "UTF8");
    nbsource.write(writer);
    writer.close();
    
    writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(nbtargetfolder + File.separator + this.targetlanguage + ".txml")), "UTF8");
    nbtarget.write(writer);
    writer.close();
    

    String pahtexe = "\\\\10.2.50.190\\AutoAlignerCLI\\AutoAlignerCLI.exe";
    
















    ProcessBuilder pb = new ProcessBuilder(new String[] { pahtexe, "-i", this.nbalignerfolder, "-o", this.nbalignerfolder, "-lang_pairs", this.sourcelanguage + "_" + this.targetlanguage, "-lang_detect", "normal", "-identicals", "-match_filenames", "-txml_or_xmx_output", "-docnames_output", "-disallow_src_merging" });
    pb.redirectErrorStream(true);
    
    Process p = pb.start();
    InputStreamReader isr = new InputStreamReader(p.getInputStream());
    BufferedReader br = new BufferedReader(isr);
    

    boolean sentESTTime = false;
    boolean alignstart = false;
    String lineRead;
    while ((lineRead = br.readLine()) != null)
    {
      System.out.println(lineRead);
      if (lineRead.contains("Aligning..."))
      {
        alignstart = true;
      }
      else
      {
        if ((lineRead.contains("Estimated Time to Completion:")) && (alignstart)) {
          this.estimateNBAlignerCompTime = lineRead.replace("Estimated Time to Completion: ", "").replace(" Minute(s)", "");
        }
        if ((!this.estimateNBAlignerCompTime.equals("")) && (!sentESTTime))
        {
          sentESTTime = true;
          try
          {
            int minutes = 200 + Integer.parseInt(this.estimateNBAlignerCompTime);
            setAlignProgress(prjid, minutes);
            this.estimateNBAlignerCompTime = "";
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        }
      }
    }
    p.waitFor();
    

    for (File file : new File(this.nbalignerfolder).listFiles())
    {
      if (file.getName().endsWith(".zip")) {
        UnzipFile.UnZipIt(file.getAbsolutePath(), this.nbalignerfolder);
      }
    }
    String alignedtxml = "";
    for (File file : new File(this.nbalignerfolder).listFiles())
    {
      if (file.getName().endsWith(".txml")) {
        alignedtxml = file.getAbsolutePath();
      }
    }
    if (alignedtxml.equals("")) {
      throw new Exception("file didn't aligned by nbaligner");
    }
    HashMap<String, String[]> alignedtrgs = new HashMap();
    List<String[]> missingtrgs = new ArrayList();
    int src_idx = -1;
    
    org.dom4j.Document alignedtxmldoc = XmlParser.parseXmlFile(alignedtxml);
    org.dom4j.Element root_alignedtxmldoc = alignedtxmldoc.getRootElement();
    for (int i = 0; i < root_alignedtxmldoc.elements("translatable").size(); i++)
    {
      org.dom4j.Element translatable = (org.dom4j.Element)root_alignedtxmldoc.elements("translatable").get(i);
      for (int j = 0; j < translatable.elements("segment").size(); j++)
      {
        org.dom4j.Element segment = (org.dom4j.Element)translatable.elements("segment").get(j);
        org.dom4j.Element source = segment.element("source");
        org.dom4j.Element target = segment.element("target");
        if ((source != null) && (!source.getTextTrim().equals("")))
        {
          src_idx++;
          if ((target != null) && (!target.getTextTrim().equals("")))
          {
            String matchscore = target.attributeValue("score");
            int trg_idx = Integer.parseInt(target.attributeValue("sent_no"));
            if (matchscore.equals("0"))
            {
              alignedtrgs.put(Integer.toString(src_idx), new String[] { target.getTextTrim(), "1", Integer.toString(trg_idx) });
            }
            else if (target.attribute("original_segments_count") != null)
            {
              int merged_cnt = Integer.parseInt(target.attributeValue("original_segments_count"));
              String trg_idx_str = Integer.toString(trg_idx) + " - " + Integer.toString(trg_idx + merged_cnt - 1);
              alignedtrgs.put(Integer.toString(src_idx), new String[] { target.getTextTrim(), matchscore, trg_idx_str });
            }
            else
            {
              alignedtrgs.put(Integer.toString(src_idx), new String[] { target.getTextTrim(), matchscore, Integer.toString(trg_idx) });
            }
          }
        }
        else if ((target != null) && (!target.getTextTrim().equals("")))
        {
          String matchscore = target.attributeValue("score");
          int trg_idx = Integer.parseInt(target.attributeValue("sent_no"));
          
          missingtrgs.add(new String[] { target.getTextTrim(), Integer.toString(trg_idx) });
        }
      }
    }
    int null_idx = 0;
    List<org.dom4j.Element> groups = aligned.elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        org.dom4j.Element src_para = unit.element("src_para");
        org.dom4j.Element src_para_segs = src_para.element("segments");
        org.dom4j.Element trg_para = unit.addElement("trg_para");
        org.dom4j.Element trg_para_segs = trg_para.addElement("segments");
        List<org.dom4j.Element> src_segs = src_para_segs.elements("src_seg");
        for (int z = 0; z < src_segs.size(); z++)
        {
          org.dom4j.Element src_seg = (org.dom4j.Element)src_segs.get(z);
          org.dom4j.Element trg_seg = trg_para_segs.addElement("trg_seg");
          
          String mapid = Integer.toString(i) + " - " + Integer.toString(j) + " - " + Integer.toString(z);
          trg_seg.addAttribute("edited", "false");
          String trgsegtext = "";
          if (srcidmap.containsKey(mapid))
          {
            String sourceidintxml = (String)srcidmap.get(mapid);
            if (alignedtrgs.containsKey(sourceidintxml))
            {
              src_seg.addAttribute("locked", "true");
              trgsegtext = ((String[])alignedtrgs.get(sourceidintxml))[0];
              String score = ((String[])alignedtrgs.get(sourceidintxml))[1];
              String targetidintxml = ((String[])alignedtrgs.get(sourceidintxml))[2];
              if (Integer.parseInt(score) < needreviewthreshhold) {
                src_seg.addAttribute("needreview", "true");
              }
              trg_seg.addAttribute("id", targetidintxml);
              trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl_trg.isExtractable(trgsegtext)));
            }
            else
            {
              trg_seg.addAttribute("id", "n - " + null_idx);
              null_idx++;
              trg_seg.addAttribute("isExtractable", "false");
            }
          }
          else
          {
            trg_seg.addAttribute("id", "n - " + null_idx);
            null_idx++;
            trg_seg.addAttribute("isExtractable", "false");
          }
          trg_seg.setText(trgsegtext);
        }
      }
    }
    org.dom4j.Element orp_unit = orphans.addElement("unit");
    orp_unit.addAttribute("id", "0");
    org.dom4j.Element orp_trg_para = orp_unit.addElement("trg_para");
    org.dom4j.Element orp_segments = orp_trg_para.addElement("segments");
    for (int i = 0; i < missingtrgs.size(); i++)
    {
      String orptrgtext = ((String[])missingtrgs.get(i))[0];
      String orptrgid = ((String[])missingtrgs.get(i))[1];
      org.dom4j.Element orp_trg_seg = orp_segments.addElement("trg_seg");
      orp_trg_seg.addAttribute("id", orptrgid);
      orp_trg_seg.addAttribute("edited", "false");
      orp_trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl_trg.isExtractable(orptrgtext)));
      orp_trg_seg.setText(orptrgtext);
    }
    OutputStreamWriter oswriter = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.alignedfile)), "UTF8");
    document.write(oswriter);
    oswriter.close();
  }
  
  public String[] readnbalignmentreport()
    throws Exception
  {
    System.out.println("reading auto-Aligner report....");
    
    String[] res = new String[1];
    String alignmentreport = "";
    for (File file : new File(this.nbalignerfolder).listFiles()) {
      if (file.getName().contains("Alignment_Report")) {
        alignmentreport = file.getAbsolutePath();
      }
    }
    if (alignmentreport.equals("")) {
      throw new Exception("file didn't aligned properly");
    }
    BufferedReader input = new BufferedReader(new FileReader(alignmentreport));
    String line = null;
    while ((line = input.readLine()) != null) {
      if (line.contains("Alignment Score:")) {
        res[0] = line.replace("Alignment Score:", "").trim();
      }
    }
    input.close();
    return res;
  }
  
  public void buildTargetContentMap()
    throws Exception
  {
    System.out.println("rebuilding target content map file....");
    
    ExtractionSupportImpl extractionSupportImpl_trg = new ExtractionSupportImpl(Locale.makeLocale(this.targetlanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config_trg = new BaseConfiguration();
    config_trg.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl_trg.setConfiguration(config_trg);
    
    this.txlftrgsegmap = new LinkedHashMap();
    this.txlftrgsewsmap = new LinkedHashMap();
    org.dom4j.Document document_target_seg = XmlParser.parseXmlFile(this.reformattedtargettxlf_seg);
    org.dom4j.Element root_target_seg = document_target_seg.getRootElement();
    List list_target_para = root_target_seg.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    int segmentId = 1;
    for (int i = 0; i < list_target_para.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)list_target_para.get(i);
      for (int j = 0; j < group.elements("trans-unit").size(); j++)
      {
        org.dom4j.Element trg_txlf_seg = (org.dom4j.Element)group.elements("trans-unit").get(j);
        String trgseg = trg_txlf_seg.element("source").getText().trim().replaceAll("(\\s)+", " ");
        if (extractionSupportImpl_trg.isExtractable(trgseg))
        {
          List tmp_content = new ArrayList();
          for (int z = 0; z < trg_txlf_seg.content().size(); z++) {
            if ((trg_txlf_seg.content().get(z) instanceof org.dom4j.Element)) {
              tmp_content.add(trg_txlf_seg.content().get(z));
            }
          }
          this.txlftrgsegmap.put(Integer.valueOf(segmentId), tmp_content);
          boolean[] seg_attr = { false, false };
          if (j == 0) {
            seg_attr[0] = true;
          }
          if (j == group.elements("trans-unit").size() - 1) {
            seg_attr[1] = true;
          }
          this.txlftrgsewsmap.put(Integer.valueOf(segmentId), seg_attr);
          segmentId++;
        }
      }
    }
  }
  
  public boolean compareAlignedXmls(String trgxml)
    throws Exception
  {
    System.out.println("comparing xmls....");
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
    org.w3c.dom.Document document_trg = builder.parse(new File(trgxml));
    org.w3c.dom.Element root_trg = document_trg.getDocumentElement();
    NodeList list_target_paras_trg = root_trg.getElementsByTagName("trg_para");
    while (list_target_paras_trg.getLength() != 0) {
      list_target_paras_trg.item(0).getParentNode().removeChild(list_target_paras_trg.item(0));
    }
    org.w3c.dom.Document document_src = builder.parse(new File(this.alignedfile));
    org.w3c.dom.Element root_src = document_src.getDocumentElement();
    NodeList list_target_paras_src = root_src.getElementsByTagName("trg_para");
    while (list_target_paras_src.getLength() != 0) {
      list_target_paras_src.item(0).getParentNode().removeChild(list_target_paras_src.item(0));
    }
    Diff diff = DiffBuilder.compare(document_src).withTest(document_trg).checkForIdentical().build();
    if (diff.hasDifferences()) {
      return false;
    }
    return true;
  }
  
  private String fixMissingTags(String s)
  {
    String result = s.replaceAll("(?s)<ins>.*?</ins>", "").replaceAll("(?s)<del>.*?</del>", "");
    if (result.contains("<ins>")) {
      result = s + "</ins>";
    } else if (result.contains("</ins>")) {
      result = "<ins>" + s;
    } else if (result.contains("<del>")) {
      result = s + "</del>";
    } else if (result.contains("</del>")) {
      result = "<del>" + s;
    }
    return s;
  }
  
  private String getTxlfElementText_withFakeTC(org.dom4j.Element src)
  {
    String text = "";
    ArrayList<String> node_ids = new ArrayList();
    
    int start_idx = -1;
    int end_idx = 0;
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text)) {
        if ((((org.dom4j.Text)src.content().get(j)).getText().trim().equals("")) || (start_idx != -1)) {
          end_idx = j;
        } else {
          start_idx = j;
        }
      }
    }
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text))
      {
        text = text + ((org.dom4j.Text)src.content().get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      }
      else if ((src.content().get(j) instanceof org.dom4j.Element))
      {
        org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
        if ((e.getName().equals("bx")) && (e.attribute("ctype").getValue().equals("x-strike-through")))
        {
          text = text + "<del>";
          node_ids.add(e.attribute("rid").getValue());
        }
        else if (e.getName().equals("ex"))
        {
          if (node_ids.contains(e.attribute("rid").getValue()))
          {
            text = text + "</del>";
            node_ids.remove(e.attribute("rid").getValue());
          }
        }
        else if ((e.getName().equals("bpt")) && (e.attribute("ctype").getValue().equals("x-underlined")) && (e.getText().contains("type=\"1\"")))
        {
          text = text + "<ins>";
          node_ids.add(e.attribute("rid").getValue());
        }
        else if (e.getName().equals("ept"))
        {
          if (node_ids.contains(e.attribute("rid").getValue()))
          {
            text = text + "</ins>";
            node_ids.remove(e.attribute("rid").getValue());
          }
        }
        else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab")))
        {
          text = text + " ";
        }
        else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-paragraphBreak")) && (j > start_idx) && (j < end_idx))
        {
          text = text + " ";
        }
        else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb")) && (j > start_idx) && (j < end_idx))
        {
          text = text + "<br> ";
        }
        else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("pb")) && (j > start_idx) && (j < end_idx))
        {
          text = text + " ";
        }
        else if ((e.getName().equals("x")) && (e.attribute("equiv-text") != null) && (j > start_idx) && (j < end_idx))
        {
          text = text + e.attributeValue("equiv-text");
        }
      }
    }
    if (!text.contains("<ins>"))
    {
      if (src.selectNodes("..//*[name() = 'it'][@ctype = 'x-underlined'][@pos = 'open']").size() != 0)
      {
        org.dom4j.Node node = (org.dom4j.Node)src.selectNodes("..//*[name() = 'it'][@ctype = 'x-underlined'][@pos = 'open']").get(0);
        if (node.getText().contains("type=\"1\"")) {
          text = "<ins>" + text + "</ins>";
        }
      }
    }
    else if ((!text.contains("<del>")) && 
      (src.selectNodes("..//*[name() = 'it'][@ctype = 'x-strike-through'][@pos = 'open']").size() != 0)) {
      text = "<del>" + text + "</del>";
    }
    return text;
  }
  
  private String getTxlfElementText_normal(org.dom4j.Element src)
  {
    String text = "";
    
    int start_idx = -1;
    int end_idx = 0;
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text)) {
        if ((((org.dom4j.Text)src.content().get(j)).getText().trim().equals("")) || (start_idx != -1)) {
          end_idx = j;
        } else {
          start_idx = j;
        }
      }
    }
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text))
      {
        text = text + ((org.dom4j.Text)src.content().get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      }
      else if ((src.content().get(j) instanceof org.dom4j.Element))
      {
        org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
        if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab"))) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-paragraphBreak")) && (j > start_idx) && (j < end_idx)) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb")) && (j > start_idx) && (j < end_idx)) {
          text = text + "<br> ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("pb")) && (j > start_idx) && (j < end_idx)) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("equiv-text") != null) && (j > start_idx) && (j < end_idx)) {
          text = text + e.attributeValue("equiv-text");
        }
      }
    }
    return text;
  }
  
  private List replacetextinDomObj(List contents)
  {
    List clone = new ArrayList();
    for (int j = 0; j < contents.size(); j++) {
      if ((!(contents.get(j) instanceof org.dom4j.Text)) || (!((org.dom4j.Text)contents.get(j)).getText().equals("[skipseg]"))) {
        if ((contents.get(j) instanceof org.dom4j.Element)) {
          clone.add(contents.get(j));
        }
      }
    }
    return clone;
  }
  
  private String assembleText(List contents)
  {
    String text = "";
    if (contents == null) {
      return "";
    }
    int start_idx = -1;
    int end_idx = 0;
    for (int j = 0; j < contents.size(); j++) {
      if ((contents.get(j) instanceof org.dom4j.Text)) {
        if ((((org.dom4j.Text)contents.get(j)).getText().trim().equals("")) || (start_idx != -1)) {
          end_idx = j;
        } else {
          start_idx = j;
        }
      }
    }
    for (int j = 0; j < contents.size(); j++) {
      if ((contents.get(j) instanceof org.dom4j.Text))
      {
        text = text + ((org.dom4j.Text)contents.get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      }
      else if ((contents.get(j) instanceof org.dom4j.Element))
      {
        org.dom4j.Element e = (org.dom4j.Element)contents.get(j);
        if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab"))) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb")) && (j > start_idx) && (j < end_idx)) {
          text = text + "<br> ";
        }
      }
    }
    return text;
  }
  
  private String getTxlfElementText_noescape(org.dom4j.Element src)
  {
    String text = "";
    
    int start_idx = -1;
    int end_idx = 0;
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text)) {
        if ((((org.dom4j.Text)src.content().get(j)).getText().trim().equals("")) || (start_idx != -1)) {
          end_idx = j;
        } else {
          start_idx = j;
        }
      }
    }
    for (int j = 0; j < src.content().size(); j++) {
      if ((src.content().get(j) instanceof org.dom4j.Text))
      {
        text = text + ((org.dom4j.Text)src.content().get(j)).getText();
      }
      else if ((src.content().get(j) instanceof org.dom4j.Element))
      {
        org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
        if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab"))) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-paragraphBreak")) && (j > start_idx) && (j < end_idx)) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb")) && (j > start_idx) && (j < end_idx)) {
          text = text + "<br> ";
        } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("pb")) && (j > start_idx) && (j < end_idx)) {
          text = text + " ";
        } else if ((e.getName().equals("x")) && (e.attribute("equiv-text") != null) && (j > start_idx) && (j < end_idx)) {
          text = text + e.attributeValue("equiv-text");
        }
      }
    }
    return text;
  }
  
  public boolean verifysegments()
    throws Exception
  {
    System.out.println("verifying segments mapping....");
    boolean isValid = false;
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    org.dom4j.Document document_source = XmlParser.parseXmlFile(this.sourcetxlf_seg);
    org.dom4j.Element root_source = document_source.getRootElement();
    
    List list_source = root_source.selectNodes("//*[name() = 'trans-unit']");
    int numberOfPara_source = list_source.size();
    
    List<org.dom4j.Element> text_source = new ArrayList();
    Iterator iter_source = list_source.iterator();
    while (iter_source.hasNext())
    {
      org.dom4j.Element source = ((org.dom4j.Element)iter_source.next()).element("source");
      text_source.add(source);
    }
    List<String> mergedsegtext = new ArrayList();
    org.dom4j.Document alignedxml = XmlParser.parseXmlFile(this.alignedfile);
    org.dom4j.Element root = alignedxml.getRootElement();
    List groups = root.selectNodes("//*[name() = 'group']");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      List units = group.elements("unit");
      ArrayList<String> keys = new ArrayList();
      ArrayList<String> key_left = new ArrayList();
      ArrayList<String> key_right = new ArrayList();
      ArrayList<String> org_keys = new ArrayList();
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        org.dom4j.Element src_para = unit.element("src_para");
        if (src_para != null)
        {
          List segs = src_para.element("segments").elements("src_seg");
          for (int z = 0; z < segs.size(); z++)
          {
            org.dom4j.Element seg = (org.dom4j.Element)segs.get(z);
            keys.add(seg.getText().replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "").replace("<br>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").trim());
            org_keys.add(seg.getText());
            if ((z == 0) && (z == segs.size() - 1))
            {
              key_left.add(src_para.attributeValue("lefttrim"));
              key_right.add(src_para.attributeValue("righttrim"));
            }
            else if (z == 0)
            {
              key_left.add(src_para.attributeValue("lefttrim"));
              key_right.add("true");
            }
            else if (z == segs.size() - 1)
            {
              key_left.add("true");
              key_right.add(src_para.attributeValue("righttrim"));
            }
            else
            {
              key_left.add("true");
              key_right.add("true");
            }
          }
        }
      }
      SegmenterFactory factory = new SegmenterFactory();
      Configuration segconfig = createConfigForSegmenter(false, this.sourcelanguage);
      Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(this.sourcelanguage), segconfig);
      List<String> finsegs = segmenter.segment(group.elementText("text").replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "").replace("<br>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"));
      ArrayList<ArrayList<Integer>> indices = new ArrayList();
      int key_start_index = 0;
      ArrayList<Integer> indice;
      for (int k = 0; k < finsegs.size(); k++)
      {
        String finsegtext = (String)finsegs.get(k);
        
        String combined_key = "";
        indice = new ArrayList();
        for (int x = key_start_index; x < keys.size(); x++)
        {
          combined_key = combined_key + (String)keys.get(x);
          



          indice.add(Integer.valueOf(x));
          if (combined_key.replace(" ", " ").trim().replaceAll("(\\s)+", "").equals(finsegtext.replace(" ", " ").trim().replaceAll("(\\s)+", "")))
          {
            indices.add(indice);
            key_start_index = x + 1;
            break;
          }
        }
      }
      for (int m = 0; m < indices.size(); m++)
      {
        ArrayList<Integer> temp_indice = (ArrayList)indices.get(m);
        String temp = "";
        for (int it : temp_indice)
        {
          temp = temp + (String)org_keys.get(it);
        }
        mergedsegtext.add(temp);
      }
    }
    List<String> rejectedtexts = new ArrayList();
    Workbook wb = new Workbook();
    Worksheet ws = wb.getWorksheets().get(0);
    Cells cells = ws.getCells();
    int count = Math.max(text_source.size(), mergedsegtext.size());
    int t_count = 0;
    for (int i = 0; i < count; i++)
    {
      String t_src = "";
      String t_fom = "";
      if (i < text_source.size())
      {
        org.dom4j.Element src = (org.dom4j.Element)text_source.get(i);
        for (int j = 0; j < src.content().size(); j++) {
          if ((src.content().get(j) instanceof org.dom4j.Text))
          {
            t_src = t_src + ((org.dom4j.Text)src.content().get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
          }
          else if ((src.content().get(j) instanceof org.dom4j.Element))
          {
            org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
            if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab"))) {
              t_src = t_src + " ";
            } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb"))) {
              t_src = t_src + "<br> ";
            }
          }
        }
      }
      cells.get(i, 0).setHtmlString(t_src);
      if (i < mergedsegtext.size()) {
        t_fom = ((String)mergedsegtext.get(i)).replace("<ins>", "<u>").replace("</ins>", "</u>").replace("<del>", "<strike>").replace("</del>", "</strike>");
      }
      String accepted_t_fom = t_fom.replaceAll("(?s)<strike>.*?</strike>", "").replace("<u>", "").replace("</u>", "").replace("&amp;paradel;", "").replace("<br>", "");
      String rejected_t_fom = t_fom.replaceAll("(?s)<u>.*?</u>", "").replace("<strike>", "").replace("</strike>", "").replace("&amp;paradel;", "").replace("<br>", "");
      if (extractionSupportImpl.isExtractable(accepted_t_fom))
      {
        String input = "<html>" + t_fom.replace("<br> ", "&#8629;<br>").replace("&amp;paradel;", "<strike>&para;</strike><br>") + "</html>";
        
        cells.get(t_count, 1).setHtmlString(input);
        t_count++;
        if ((i < mergedsegtext.size()) && (TrackChangeHelper.getTrackChangeType((String)mergedsegtext.get(i)) == 3)) {
          rejectedtexts.add(rejected_t_fom);
        } else {
          rejectedtexts.add("");
        }
      }
    }
    wb.save(this.prjfolder + File.separator + "verifySegs.xlsx");
    if (numberOfPara_source == t_count)
    {
      System.out.println("result: TRUE source: " + numberOfPara_source + " formatted: " + t_count);
      isValid = true;
      

      String timestamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(new Date());
      for (int r = 0; r < list_source.size(); r++) {
        if (!((String)rejectedtexts.get(r)).equals(""))
        {
          org.dom4j.Element transunit = (org.dom4j.Element)list_source.get(r);
          org.dom4j.Element originalbase = transunit.addElement("alt-trans");
          org.dom4j.Element source = transunit.element("source");
          org.dom4j.Element target = transunit.element("target");
          source.addAttribute("gs4tr:seginfo", "<root username=\"TC Aligner\" timestamp=\"" + timestamp + "\"/>");
          if (target != null) {
            transunit.elements().add(transunit.elements().indexOf(target) + 1, originalbase.clone());
          } else {
            transunit.elements().add(transunit.elements().indexOf(source) + 1, originalbase.clone());
          }
          transunit.remove(originalbase);
          
          org.dom4j.Element original = transunit.element("alt-trans");
          original.addAttribute("alttranstype", "x-previous-source-version");
          original.addAttribute("gs4tr:seginfo", "<root username=\"Original\" timestamp=\"" + timestamp + "\"/>");
          org.dom4j.Element original_source = original.addElement("source");
          original_source.addText((String)rejectedtexts.get(r));
          original.addElement("target");
        }
      }
      OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.sourcetxlf_seg)), "UTF8");
      document_source.write(writer);
      writer.close();
    }
    else
    {
      System.out.println("result: false source: " + numberOfPara_source + " formatted: " + t_count);
    }
    return isValid;
  }
  
  public void align()
    throws Exception
  {
    org.dom4j.Document document = DocumentHelper.createDocument();
    org.dom4j.Element root = document.addElement("alinger");
    org.dom4j.Element head = root.addElement("head");
    head.addAttribute("src_lang", this.sourcelanguage);
    head.addAttribute("trg_lang", this.targetlanguage);
    head.addAttribute("creator", this.creatorid);
    
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    org.dom4j.Element content = root.addElement("content");
    org.dom4j.Element orphans = root.addElement("orphans");
    
    com.aspose.words.Document doc_src = new com.aspose.words.Document(this.sourcefile);
    com.aspose.words.Document doc_trg = new com.aspose.words.Document(this.targetfile);
    
    doc_src.joinRunsWithSameFormatting();
    trimParaLeadingTrailingSpace(doc_src);
    
    UnlinkFields(doc_src);
    doc_src.save(this.sourcefile + ".docx");
    
    doc_trg.joinRunsWithSameFormatting();
    trimParaLeadingTrailingSpace(doc_trg);
    
    UnlinkFields(doc_trg);
    
    int seqnum = 0;
    int srcparaindex = 0;
    int srcparaindex_accept = 0;
    int trgparaindex = 0;
    int srcparacnt = doc_src.getChildNodes(8, true).getCount();
    int trgparacnt = doc_trg.getChildNodes(8, true).getCount();
    
    boolean ismovefrom = false;
    boolean ismoveto = false;
    boolean isprvdelpara = false;
    
    int prv = 999999;
    int unitid = 0;
    for (int i = 0; i < srcparacnt; i++)
    {
      Paragraph para_src = (Paragraph)doc_src.getChildNodes(8, true).get(i);
      
      String para_text = getParaText(para_src);
      
      boolean isExtractable = extractionSupportImpl.isExtractable(para_text);
      boolean isNumeric = org.gs4tr.foundation3.core.utils.Text.isNumeric(para_text);
      if ((!para_text.equals("")) && (isExtractable) && (!isNumeric))
      {
        String src_para_text = "";
        boolean hasadds = false;
        boolean hasdels = false;
        boolean hasnorm = false;
        for (int j = 0; j < para_src.getChildNodes(0, true).getCount(); j++)
        {
          com.aspose.words.Node node = para_src.getChildNodes(0, true).get(j);
          if (node.getNodeType() == 13)
          {
            ismovefrom = true;
          }
          else if (node.getNodeType() == 14)
          {
            ismovefrom = false;
          }
          else if (node.getNodeType() == 15)
          {
            ismoveto = true;
          }
          else if (node.getNodeType() == 16)
          {
            ismoveto = false;
          }
          else if (node.getNodeType() == 21)
          {
            Run run = (Run)para_src.getChildNodes(0, true).get(j);
            if (!run.getFont().getName().equals("Wingdings")) {
              if ((run.isInsertRevision()) && (!run.isDeleteRevision()))
              {
                hasadds = true;
                src_para_text = src_para_text + "<ins>" + run.getText().replace("<", "&lt;").replace(">", "&gt;") + "</ins>";
              }
              else if (run.isDeleteRevision())
              {
                hasdels = true;
                src_para_text = src_para_text + "<del>" + run.getText().replace("<", "&lt;").replace(">", "&gt;") + "</del>";
              }
              else if (ismoveto)
              {
                hasadds = true;
                src_para_text = src_para_text + "<ins>" + run.getText().replace("<", "&lt;").replace(">", "&gt;") + "</ins>";
              }
              else if (ismovefrom)
              {
                hasdels = true;
                src_para_text = src_para_text + "<del>" + run.getText().replace("<", "&lt;").replace(">", "&gt;") + "</del>";
              }
              else
              {
                hasnorm = true;
                src_para_text = src_para_text + run.getText().replace("<", "&lt;").replace(">", "&gt;");
              }
            }
          }
        }
        org.dom4j.Element unit;
        org.dom4j.Element src_para;
        if ((hasadds) && (!hasdels) && (!hasnorm))
        {
          unit = content.addElement("unit");
          src_para = unit.addElement("src_para");
          unit.addAttribute("id", Integer.toString(unitid++));
          src_para.addAttribute("para_type", "insertion");
        }
        else if ((!hasadds) && (!hasdels) && (hasnorm))
        {
          unit = content.addElement("unit");
          src_para = unit.addElement("src_para");
          unit.addAttribute("id", Integer.toString(unitid++));
          src_para.addAttribute("para_type", "regular");
        }
        else if ((!hasadds) && (hasdels) && (!hasnorm))
        {
          unit = content.addElement("unit");
          src_para = unit.addElement("src_para");
          unit.addAttribute("id", Integer.toString(unitid++));
          src_para.addAttribute("para_type", "deletion");
        }
        else
        {
          unit = content.addElement("unit");
          src_para = unit.addElement("src_para");
          unit.addAttribute("id", Integer.toString(unitid++));
          src_para.addAttribute("para_type", "mix");
        }
        src_para.addAttribute("para_seq", Integer.toString(srcparaindex));
        src_para.addAttribute("para_seq_acpt", Integer.toString(srcparaindex_accept));
        if (prv != srcparaindex_accept) {}
        prv = srcparaindex_accept;
        src_para.addText(wordToHtml(src_para_text));
        if (((hasdels) || (hasnorm)) && (!isNumeric))
        {
          if (trgparaindex < trgparacnt)
          {
            Paragraph para_trg = (Paragraph)doc_trg.getChildNodes(8, true).get(trgparaindex);
            String para_trg_text = getParaText(para_trg);
            boolean isExtractable_trg = extractionSupportImpl.isExtractable(para_trg_text);
            if (trgparaindex == trgparacnt - 1)
            {
              if ((!para_trg_text.equals("")) && (isExtractable_trg))
              {
                org.dom4j.Element trg_para = unit.addElement("trg_para");
                
                String trg_para_text = "";
                for (int j = 0; j < para_trg.getChildNodes(21, true).getCount(); j++)
                {
                  Run run = (Run)para_trg.getChildNodes(21, true).get(j);
                  if (!run.getFont().getName().equals("Wingdings")) {
                    trg_para_text = trg_para_text + run.getText().replace("<", "&lt;").replace(">", "&gt;");
                  }
                }
                trg_para.addAttribute("para_seq", Integer.toString(trgparaindex));
                trg_para.addText(wordToHtml(trg_para_text));
              }
            }
            else
            {
              while ((doc_trg.getChildNodes(8, true).get(trgparaindex).getText().trim().equals("")) || (!extractionSupportImpl.isExtractable(doc_trg.getChildNodes(8, true).get(trgparaindex).getText())) || (org.gs4tr.foundation3.core.utils.Text.isNumeric(doc_trg.getChildNodes(8, true).get(trgparaindex).getText().trim())))
              {
                trgparaindex++;
                if (trgparaindex == trgparacnt - 1) {
                  break;
                }
              }
              para_trg = (Paragraph)doc_trg.getChildNodes(8, true).get(trgparaindex);
              para_trg_text = getParaText(para_trg);
              if ((!para_trg_text.equals("")) && (extractionSupportImpl.isExtractable(para_trg_text)) && (!org.gs4tr.foundation3.core.utils.Text.isNumeric(para_trg_text)))
              {
                org.dom4j.Element trg_para = unit.addElement("trg_para");
                
                String trg_para_text = "";
                for (int j = 0; j < para_trg.getChildNodes(21, true).getCount(); j++)
                {
                  Run run = (Run)para_trg.getChildNodes(21, true).get(j);
                  if (!run.getFont().getName().equals("Wingdings")) {
                    trg_para_text = trg_para_text + run.getText().replace("<", "&lt;").replace(">", "&gt;");
                  }
                }
                trg_para.addAttribute("para_seq", Integer.toString(trgparaindex));
                trg_para.addText(wordToHtml(trg_para_text));
              }
            }
          }
          trgparaindex++;
        }
        if (para_src.isDeleteRevision())
        {
          isprvdelpara = true;
        }
        else
        {
          seqnum++;
          isprvdelpara = false;
        }
        if ((para_src.isEndOfCell()) || (para_src.isEndOfHeaderFooter()) || (para_src.isEndOfSection()))
        {
          seqnum++;
          isprvdelpara = false;
        }
        srcparaindex++;
        if (!isprvdelpara) {
          srcparaindex_accept++;
        }
      }
      else
      {
        for (int j = 0; j < para_src.getChildNodes(0, true).getCount(); j++)
        {
          com.aspose.words.Node node = para_src.getChildNodes(0, true).get(j);
          if (node.getNodeType() == 13) {
            ismovefrom = true;
          } else if (node.getNodeType() == 14) {
            ismovefrom = false;
          } else if (node.getNodeType() == 15) {
            ismoveto = true;
          } else if (node.getNodeType() == 16) {
            ismoveto = false;
          }
        }
        if (!para_src.isDeleteRevision())
        {
          seqnum++;
          isprvdelpara = false;
          


          srcparaindex_accept++;
        }
        srcparaindex++;
      }
    }
    if (trgparaindex < trgparacnt) {
      for (int i = trgparaindex; i < trgparacnt; i++)
      {
        Paragraph para_trg = (Paragraph)doc_trg.getChildNodes(8, true).get(i);
        if ((!para_trg.getText().trim().equals("")) && (extractionSupportImpl.isExtractable(para_trg.getText())))
        {
          org.dom4j.Element unit = content.addElement("unit");
          unit.addAttribute("id", Integer.toString(seqnum));
          org.dom4j.Element trg_para = unit.addElement("trg_para");
          
          String trg_para_text = "";
          for (int j = 0; j < para_trg.getChildNodes(21, true).getCount(); j++)
          {
            Run run = (Run)para_trg.getChildNodes(21, true).get(j);
            if (!run.getFont().getName().equals("Wingdings")) {
              trg_para_text = trg_para_text + run.getText().replace("<", "&lt;").replace(">", "&gt;");
            }
          }
          trg_para.addAttribute("para_seq", Integer.toString(trgparaindex));
          trg_para.addText(wordToHtml(trg_para_text));
          
          seqnum++;
          trgparaindex++;
        }
      }
    }
    this.alignedfile = (new File(new File(this.sourcefile).getParent()).getParent() + "/rev_aligned.xml");
    if (new File(this.alignedfile).exists()) {
      new File(this.alignedfile).delete();
    }
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.alignedfile)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public void readAlignedFile()
    throws Exception
  {
    this.src_paras = new LinkedHashMap();
    this.trg_paras = new LinkedHashMap();
    this.missing_trg_paras = new LinkedHashMap();
    this.locked_para_seqs = new LinkedHashMap();
    this.aligned_para_seqs = new LinkedHashMap();
    this.src_segs = new LinkedHashMap();
    this.trg_segs = new LinkedHashMap();
    
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        String seq = Integer.toString(i) + " - " + Integer.toString(j);
        if (unit.attributeValue("locked").equals("true")) {
          this.locked_para_seqs.put(Integer.toString(this.locked_para_seqs.size()), seq);
        }
        if (unit.attributeValue("alignsegs").equals("true")) {
          this.aligned_para_seqs.put(Integer.toString(this.aligned_para_seqs.size()), seq);
        }
        String src = "";
        String src_tctype = "";
        if (unit.element("src_para") != null)
        {
          src = unit.element("src_para").element("text").getText();
          src_tctype = unit.element("src_para").attributeValue("tctype");
          
          List<String> st = new ArrayList();
          st.add(encodeHtml(src.replaceAll("<ins>(\\s)+</ins>", "<ins>&nbsp;</ins>").replaceAll("<del>(\\s)+</del>", "<del>&nbsp;</del>")));
          st.add(seq);
          st.add(src_tctype);
          this.src_paras.put(Integer.toString(this.src_paras.size()), st);
          
          List<org.dom4j.Element> src_segs = unit.element("src_para").element("segments").elements("src_seg");
          String segstring = "";
          String segstctypestring = "";
          for (int t = 0; t < src_segs.size(); t++)
          {
            org.dom4j.Element seg = (org.dom4j.Element)src_segs.get(t);
            if (t == src_segs.size() - 1)
            {
              segstring = segstring + seg.getText();
              segstctypestring = segstctypestring + seg.attributeValue("tctype");
            }
            else
            {
              segstring = segstring + seg.getText() + "|||";
              segstctypestring = segstctypestring + seg.attributeValue("tctype") + "|||";
            }
          }
          List<String> sts = new ArrayList();
          sts.add(encodeHtml(segstring));
          sts.add(seq);
          sts.add(segstctypestring);
          this.src_segs.put(Integer.toString(this.src_segs.size()), sts);
        }
        org.dom4j.Element trg_para = unit.element("trg_para");
        String trg = trg_para.element("text").getText();
        String trg_seq = trg_para.attributeValue("id");
        
        List<String> st = new ArrayList();
        st.add(encodeHtml(trg));
        st.add(trg_seq);
        this.trg_paras.put(Integer.toString(this.trg_paras.size()), st);
        
        List<org.dom4j.Element> trg_segs = unit.element("trg_para").element("segments").elements("trg_seg");
        String segstring = "";
        for (int t = 0; t < trg_segs.size(); t++)
        {
          org.dom4j.Element seg = (org.dom4j.Element)trg_segs.get(t);
          if (t == trg_segs.size() - 1) {
            segstring = segstring + seg.getText();
          } else {
            segstring = segstring + seg.getText() + "|||";
          }
        }
        List<String> sts = new ArrayList();
        sts.add(encodeHtml(segstring));
        sts.add(trg_seq);
        this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
      }
    }
    org.dom4j.Element orphans = document.getRootElement().element("orphans");
    List<org.dom4j.Element> units = orphans.elements("unit");
    for (int i = 0; i < units.size(); i++)
    {
      org.dom4j.Element trg_para = ((org.dom4j.Element)units.get(i)).element("trg_para");
      
      String trgtext = trg_para.element("text").getText();
      String trg_seq = trg_para.attributeValue("id");
      
      List<String> st = new ArrayList();
      st.add(encodeHtml(trgtext));
      st.add(trg_seq);
      this.missing_trg_paras.put(Integer.toString(this.missing_trg_paras.size()), st);
      
      List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
      String segstring = "";
      for (int t = 0; t < trg_segs.size(); t++)
      {
        org.dom4j.Element seg = (org.dom4j.Element)trg_segs.get(t);
        if (t == trg_segs.size() - 1) {
          segstring = segstring + seg.getText();
        } else {
          segstring = segstring + seg.getText() + "|||";
        }
      }
      List<String> sts = new ArrayList();
      sts.add(encodeHtml(segstring));
      sts.add(trg_seq);
      this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
    }
  }
  
  public String createTM(String prjid, boolean reviewed)
    throws Exception
  {
    String tm_add = this.prjfolder + File.separator + (reviewed ? "reviewed_tm_" : "aligned_tm_") + prjid + "_" + this.sourcelanguage + "_" + this.targetlanguage;
    
    StringBuffer sb = new StringBuffer();
    
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd~HHmmss");
    Date date = new Date();
    String TNow = dateFormat.format(date);
    String dummyTMHeader = "%" + TNow + "\t%User System\t%TU=00000000\t%" + this.sourcelanguage + "\t%Wordfast TM v.546/00\t%" + this.targetlanguage + "\t%-----------\t\t\t\t\t                                                            .\r\n";
    sb.append(dummyTMHeader);
    
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        if (unit.element("src_para") != null)
        {
          List<org.dom4j.Element> src_segs = unit.element("src_para").element("segments").elements("src_seg");
          List<org.dom4j.Element> trg_segs = unit.element("trg_para").element("segments").elements("trg_seg");
          for (int t = 0; t < src_segs.size(); t++)
          {
            org.dom4j.Element seg = (org.dom4j.Element)src_segs.get(t);
            if (seg.attributeValue("locked").equals("true"))
            {
              String srctext = seg.getText();
              String trgtext = ((org.dom4j.Element)trg_segs.get(t)).getText();
              
              String TUline = TNow + "\t" + (reviewed ? "System" : "ALIGN!") + "\t0\t" + this.sourcelanguage + "\t" + srctext + "\t" + this.targetlanguage + "\t" + trgtext;
              sb.append(TUline);
              sb.append("\r\n");
            }
          }
        }
      }
    }
    Writer out = new OutputStreamWriter(new FileOutputStream(tm_add), "UTF-8");
    out.write(sb.toString());
    out.close();
    
    return tm_add;
  }
  
  public void readAlignedFile_seg(boolean useautosaveddata)
    throws Exception
  {
    this.src_segs = new LinkedHashMap();
    this.trg_segs = new LinkedHashMap();
    this.missing_trg_segs = new LinkedHashMap();
    this.locked_seg_seqs = new LinkedHashMap();
    this.review_seg_seqs = new LinkedHashMap();
    this.ignore_seg_seqs = new LinkedHashMap();
    HashSet<String> lock_tmp = new HashSet();
    
    org.dom4j.Document document = null;
    if (useautosaveddata) {
      document = XmlParser.parseXmlFile(this.auto_saved_alignedfile);
    } else {
      document = XmlParser.parseXmlFile(this.alignedfile);
    }
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    int maxnullcnt = 0;
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        if (unit.element("src_para") != null)
        {
          List<org.dom4j.Element> src_segs = unit.element("src_para").element("segments").elements("src_seg");
          for (int t = 0; t < src_segs.size(); t++)
          {
            org.dom4j.Element seg = (org.dom4j.Element)src_segs.get(t);
            
            String seq = Integer.toString(i) + " - " + Integer.toString(j) + " - " + Integer.toString(t);
            List<String> sts = new ArrayList();
            sts.add(seq);
            sts.add(seg.attributeValue("tctype"));
            sts.add(encodeHtml(seg.getText()));
            this.src_segs.put(Integer.toString(this.src_segs.size()), sts);
            if (seg.attributeValue("locked").equals("true"))
            {
              this.locked_seg_seqs.put(Integer.toString(this.locked_seg_seqs.size()), seq);
              lock_tmp.add(seq);
            }
            if (seg.attributeValue("needreview").equals("true")) {
              this.review_seg_seqs.put(Integer.toString(this.review_seg_seqs.size()), seq);
            }
            if (seg.attributeValue("ignored").equals("true")) {
              this.ignore_seg_seqs.put(Integer.toString(this.ignore_seg_seqs.size()), seq);
            }
          }
        }
        if (unit.element("trg_para") != null)
        {
          List<org.dom4j.Element> trg_segs = unit.element("trg_para").element("segments").elements("trg_seg");
          for (int t = 0; t < trg_segs.size(); t++) {
            if (((org.dom4j.Element)trg_segs.get(t)).attributeValue("id").startsWith("n - "))
            {
              int tmpcnt = Integer.parseInt(((org.dom4j.Element)trg_segs.get(t)).attributeValue("id").replace("n - ", ""));
              maxnullcnt = Math.max(maxnullcnt, tmpcnt);
            }
          }
        }
      }
    }
    this.nullcnt = (maxnullcnt + 1);
    
    boolean isstart = true;
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        


        List<org.dom4j.Element> trgsegs = unit.element("trg_para").element("segments").elements("trg_seg");
        for (int t = 0; t < trgsegs.size(); t++)
        {
          org.dom4j.Element seg = (org.dom4j.Element)trgsegs.get(t);
          List<String> sts = new ArrayList();
          if ((seg.getText().equals("")) && (!seg.attributeValue("id").contains("-")))
          {
            if ((this.trg_segs.size() < this.src_segs.size()) && ((lock_tmp.contains(((List)this.src_segs.get(Integer.toString(this.trg_segs.size()))).get(0))) || (isstart)))
            {
              String seq = "n - " + Integer.toString(this.nullcnt);
              sts.add(seq);
              sts.add("");
              this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
              this.nullcnt += 1;
              sts = new ArrayList();
            }
          }
          else
          {
            isstart = false;
            if (seg.attributeValue("id").contains("-"))
            {
              sts.add(seg.attributeValue("id"));
              sts.add(encodeHtml(seg.getText()));
              this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
            }
            else
            {
              while ((this.trg_segs.size() < this.src_segs.size()) && (lock_tmp.contains(((List)this.src_segs.get(Integer.toString(this.trg_segs.size()))).get(0))))
              {
                String seq = "n - " + Integer.toString(this.nullcnt);
                sts.add(seq);
                sts.add("");
                this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
                this.nullcnt += 1;
                sts = new ArrayList();
              }
              String seq = Integer.toString(i) + " - " + Integer.toString(j) + " - " + Integer.toString(t);
              sts.add(seq);
              sts.add(encodeHtml(seg.getText()));
              this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
            }
          }
        }
      }
    }
    if (this.trg_segs.size() < this.src_segs.size())
    {
      List<String> sts = new ArrayList();
      for (int x = this.trg_segs.size(); x < this.src_segs.size(); x++)
      {
        String seq = "n - " + Integer.toString(this.nullcnt);
        sts.add(seq);
        sts.add("");
        this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
        this.nullcnt += 1;
        sts = new ArrayList();
      }
    }
    org.dom4j.Element orphans = document.getRootElement().element("orphans");
    List<org.dom4j.Element> units = orphans.elements("unit");
    for (int i = 0; i < units.size(); i++)
    {
      org.dom4j.Element trg_para = ((org.dom4j.Element)units.get(i)).element("trg_para");
      
      List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
      for (int t = 0; t < trg_segs.size(); t++)
      {
        org.dom4j.Element seg = (org.dom4j.Element)trg_segs.get(t);
        
        List<String> sts = new ArrayList();
        sts.add(seg.attributeValue("id"));
        sts.add(seg.getText());
        this.missing_trg_segs.put(Integer.toString(this.missing_trg_segs.size()), sts);
      }
    }
  }
  
  public String readAlignedFileForRatio(String alignedfile)
    throws Exception
  {
    String ratio = "0";
    
    org.dom4j.Document document = XmlParser.parseXmlFile(alignedfile);
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    
    int lockedtargets = 0;
    int removedtargets = 0;
    int totaltargets = 0;
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        org.dom4j.Element src_para = unit.element("src_para");
        if (src_para != null)
        {
          List<org.dom4j.Element> src_segs = src_para.element("segments").elements("src_seg");
          for (int z = 0; z < src_segs.size(); z++)
          {
            org.dom4j.Element src_seg = (org.dom4j.Element)src_segs.get(z);
            totaltargets++;
            if ((src_seg.attribute("locked") != null) && (src_seg.attributeValue("locked").equals("true"))) {
              lockedtargets++;
            }
          }
        }
      }
    }
    List<org.dom4j.Element> missings = document.getRootElement().element("orphans").elements("unit");
    for (int i = 0; i < missings.size(); i++)
    {
      List<org.dom4j.Element> trgs = ((org.dom4j.Element)missings.get(i)).element("trg_para").element("segments").elements("trg_seg");
      removedtargets += trgs.size();
    }
    ratio = Double.toString(lockedtargets / (removedtargets + totaltargets));
    while (ratio.length() < 4) {
      ratio = ratio + "0";
    }
    ratio = ratio.substring(0, 4);
    
    return ratio;
  }
  
  public void readAlignedFile_seg_auto(boolean useautosaveddata)
    throws Exception
  {
    this.src_segs = new LinkedHashMap();
    this.trg_segs = new LinkedHashMap();
    this.missing_trg_segs = new LinkedHashMap();
    this.locked_seg_seqs = new LinkedHashMap();
    this.review_seg_seqs = new LinkedHashMap();
    this.ignore_seg_seqs = new LinkedHashMap();
    HashSet<String> lock_tmp = new HashSet();
    
    this.auto_saved_alignedfile = (this.alignedfile + ".temp");
    
    org.dom4j.Document document = null;
    if (useautosaveddata) {
      document = XmlParser.parseXmlFile(this.auto_saved_alignedfile);
    } else {
      document = XmlParser.parseXmlFile(this.alignedfile);
    }
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    int maxnullcnt = 0;
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        if (unit.element("src_para") != null)
        {
          List<org.dom4j.Element> src_segs = unit.element("src_para").element("segments").elements("src_seg");
          for (int t = 0; t < src_segs.size(); t++)
          {
            org.dom4j.Element seg = (org.dom4j.Element)src_segs.get(t);
            
            String seq = Integer.toString(i) + " - " + Integer.toString(j) + " - " + Integer.toString(t);
            List<String> sts = new ArrayList();
            sts.add(seq);
            sts.add(seg.attributeValue("tctype"));
            sts.add(encodeHtml(seg.getText()));
            this.src_segs.put(Integer.toString(this.src_segs.size()), sts);
            if (seg.attributeValue("locked").equals("true"))
            {
              this.locked_seg_seqs.put(Integer.toString(this.locked_seg_seqs.size()), seq);
              lock_tmp.add(seq);
            }
            if (seg.attributeValue("needreview").equals("true")) {
              this.review_seg_seqs.put(Integer.toString(this.review_seg_seqs.size()), seq);
            }
            if (seg.attributeValue("ignored").equals("true")) {
              this.ignore_seg_seqs.put(Integer.toString(this.ignore_seg_seqs.size()), seq);
            }
          }
        }
        if (unit.element("trg_para") != null)
        {
          List<org.dom4j.Element> trg_segs = unit.element("trg_para").element("segments").elements("trg_seg");
          for (int t = 0; t < trg_segs.size(); t++) {
            if (((org.dom4j.Element)trg_segs.get(t)).attributeValue("id").startsWith("n - "))
            {
              int tmpcnt = Integer.parseInt(((org.dom4j.Element)trg_segs.get(t)).attributeValue("id").replace("n - ", ""));
              maxnullcnt = Math.max(maxnullcnt, tmpcnt);
            }
          }
        }
      }
    }
    this.nullcnt = (maxnullcnt + 1);
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        


        List<org.dom4j.Element> trgsegs = unit.element("trg_para").element("segments").elements("trg_seg");
        for (int t = 0; t < trgsegs.size(); t++)
        {
          org.dom4j.Element seg = (org.dom4j.Element)trgsegs.get(t);
          List<String> sts = new ArrayList();
          













          String seq = seg.attributeValue("id");
          sts.add(seq);
          sts.add(encodeHtml(seg.getText()));
          this.trg_segs.put(Integer.toString(this.trg_segs.size()), sts);
        }
      }
    }
    org.dom4j.Element orphans = document.getRootElement().element("orphans");
    List<org.dom4j.Element> units = orphans.elements("unit");
    for (int i = 0; i < units.size(); i++)
    {
      org.dom4j.Element trg_para = ((org.dom4j.Element)units.get(i)).element("trg_para");
      
      List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
      for (int t = 0; t < trg_segs.size(); t++)
      {
        org.dom4j.Element seg = (org.dom4j.Element)trg_segs.get(t);
        
        List<String> sts = new ArrayList();
        sts.add(seg.attributeValue("id"));
        sts.add(encodeHtml(seg.getText()));
        this.missing_trg_segs.put(Integer.toString(this.missing_trg_segs.size()), sts);
      }
    }
  }
  
  public void update(JSONArray arr, JSONArray missings, JSONArray locks, JSONArray segaligned, JSONArray targets, JSONArray missing_targets, int cnt)
    throws Exception
  {
    File alignedFile = new File(this.alignedfile);
    if (!alignedFile.exists()) {
      throw new FileNotFoundException("Could not find aligned xml file");
    }
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    

    List<JSONArray> trg_list = new ArrayList();
    for (int i = 0; i < arr.length(); i++) {
      trg_list.add(arr.getJSONArray(i));
    }
    List<JSONArray> missings_list = new ArrayList();
    for (int i = 0; i < missings.length(); i++) {
      missings_list.add(missings.getJSONArray(i));
    }
    List<String> locks_list = new ArrayList();
    for (int i = 0; i < locks.length(); i++) {
      locks_list.add(locks.getString(i));
    }
    List<String> segaligned_list = new ArrayList();
    for (int i = 0; i < segaligned.length(); i++) {
      segaligned_list.add(segaligned.getString(i));
    }
    this.nullcnt = cnt;
    
    int unitcnt = 0;
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    org.dom4j.Element root = document.getRootElement();
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        unitcnt++;
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        String seq = i + " - " + j;
        if (locks_list.contains(seq)) {
          unit.attribute("locked").setValue("true");
        } else {
          unit.attribute("locked").setValue("false");
        }
        if (segaligned_list.contains(seq)) {
          unit.attribute("alignsegs").setValue("true");
        } else {
          unit.attribute("alignsegs").setValue("false");
        }
        if (unitcnt <= trg_list.size())
        {
          JSONArray segs = (JSONArray)trg_list.get(unitcnt - 1);
          String trg_para_text = targets.getString(unitcnt - 1);
          
          org.dom4j.Element trg_para = unit.element("trg_para");
          if (trg_para != null) {
            trg_para.clearContent();
          } else {
            trg_para = unit.addElement("trg_para");
          }
          trg_para.addAttribute("id", segs.getString(0));
          org.dom4j.Element trg_text = trg_para.addElement("text");
          org.dom4j.Element trg_segs = trg_para.addElement("segments");
          for (int s = 1; s < segs.length(); s++)
          {
            org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
            trg_seg.addAttribute("id", Integer.toString(s - 1));
            trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(segs.getString(s))));
            trg_seg.setText(decodehtmlstring(segs.getString(s)));
          }
          trg_text.setText(decodehtmlstring(trg_para_text));
        }
        else
        {
          units.remove(j);
          j--;
        }
      }
      if (group.elements("unit").size() == 0)
      {
        groups.remove(i);
        i--;
      }
    }
    org.dom4j.Element orphans = root.element("orphans");
    orphans.clearContent();
    for (int i = 0; i < missings_list.size(); i++)
    {
      JSONArray segs = (JSONArray)missings_list.get(i);
      String trg_para_text = missing_targets.getString(i);
      
      org.dom4j.Element unit = orphans.addElement("unit");
      unit.addAttribute("id", Integer.toString(i));
      org.dom4j.Element trg_para = unit.addElement("trg_para");
      trg_para.addAttribute("id", segs.getString(0));
      org.dom4j.Element trg_text = trg_para.addElement("text");
      org.dom4j.Element trg_segs = trg_para.addElement("segments");
      for (int s = 1; s < segs.length(); s++)
      {
        org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
        trg_seg.addAttribute("id", Integer.toString(s));
        trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(segs.getString(s))));
        trg_seg.setText(decodehtmlstring(segs.getString(s)));
      }
      trg_text.setText(decodehtmlstring(trg_para_text));
    }
    new File(this.alignedfile).delete();
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.alignedfile)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public void update_seg(JSONArray targets, JSONArray trg_seqs, JSONArray missing_targets, JSONArray missing_trg_seqs, JSONArray locks, int cnt, JSONArray edited, JSONArray review, JSONArray ignore)
    throws Exception
  {
    File alignedFile = new File(this.alignedfile);
    if (!alignedFile.exists()) {
      throw new FileNotFoundException("Could not find aligned xml file");
    }
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    List<String> trg_list = new ArrayList();
    for (int i = 0; i < targets.length(); i++) {
      trg_list.add(targets.getString(i));
    }
    List<String> trg_seq_list = new ArrayList();
    for (int i = 0; i < trg_seqs.length(); i++) {
      trg_seq_list.add(trg_seqs.getString(i));
    }
    List<String> missing_list = new ArrayList();
    for (int i = 0; i < missing_targets.length(); i++) {
      missing_list.add(missing_targets.getString(i));
    }
    List<String> missing_seq_list = new ArrayList();
    for (int i = 0; i < missing_trg_seqs.length(); i++) {
      missing_seq_list.add(missing_trg_seqs.getString(i));
    }
    List<String> locks_list = new ArrayList();
    for (int i = 0; i < locks.length(); i++) {
      locks_list.add(locks.getString(i));
    }
    this.nullcnt = cnt;
    
    List<String> edited_list = new ArrayList();
    for (int i = 0; i < edited.length(); i++) {
      edited_list.add(edited.getString(i));
    }
    List<String> review_list = new ArrayList();
    for (int i = 0; i < review.length(); i++) {
      review_list.add(review.getString(i));
    }
    List<String> ignore_list = new ArrayList();
    for (int i = 0; i < ignore.length(); i++) {
      ignore_list.add(ignore.getString(i));
    }
    int segcnt = 0;
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    org.dom4j.Element root = document.getRootElement();
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        unit.addAttribute("alignsegs", "true");
        org.dom4j.Element src_para = unit.element("src_para");
        if (src_para != null)
        {
          List<org.dom4j.Element> src_segs = src_para.element("segments").elements("src_seg");
          for (int z = 0; z < src_segs.size(); z++)
          {
            org.dom4j.Element src_seg = (org.dom4j.Element)src_segs.get(z);
            String seq = i + " - " + j + " - " + z;
            if (locks_list.contains(seq)) {
              src_seg.attribute("locked").setValue("true");
            } else {
              src_seg.attribute("locked").setValue("false");
            }
            if (review_list.contains(seq)) {
              src_seg.attribute("needreview").setValue("true");
            } else {
              src_seg.attribute("needreview").setValue("false");
            }
            if (ignore_list.contains(seq)) {
              src_seg.attribute("ignored").setValue("true");
            } else {
              src_seg.attribute("ignored").setValue("false");
            }
          }
          org.dom4j.Element trg_para = unit.element("trg_para");
          trg_para.remove(trg_para.element("text"));
          List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
          for (int z = 0; z < trg_segs.size(); z++)
          {
            org.dom4j.Element trg_seg = (org.dom4j.Element)trg_segs.get(z);
            if (z >= src_segs.size())
            {
              trg_para.element("segments").remove(trg_seg);
            }
            else if (segcnt < trg_list.size())
            {
              trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(segcnt)));
              if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(segcnt)))) || (decodehtmlstring((String)trg_seq_list.get(segcnt)).startsWith("n - "))) {
                trg_seg.addAttribute("edited", "true");
              }
              String text = decodehtmlstring((String)trg_list.get(segcnt));
              trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
              trg_seg.setText(text);
              segcnt++;
            }
            else
            {
              trg_para.element("segments").remove(trg_seg);
            }
          }
          if (trg_para.element("segments").elements("trg_seg").size() == 0) {
            group.remove(unit);
          }
        }
        else
        {
          org.dom4j.Element trg_para = unit.element("trg_para");
          trg_para.remove(trg_para.element("text"));
          List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
          for (int z = 0; z < trg_segs.size(); z++)
          {
            org.dom4j.Element trg_seg = (org.dom4j.Element)trg_segs.get(z);
            if (segcnt < trg_list.size())
            {
              trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(segcnt)));
              if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(segcnt)))) || (decodehtmlstring((String)trg_seq_list.get(segcnt)).startsWith("n - "))) {
                trg_seg.addAttribute("edited", "true");
              }
              String text = decodehtmlstring((String)trg_list.get(segcnt));
              trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
              trg_seg.setText(text);
              segcnt++;
            }
            else
            {
              trg_para.element("segments").remove(trg_seg);
            }
          }
          if (trg_para.element("segments").elements("trg_seg").size() == 0) {
            group.remove(unit);
          }
        }
      }
      if (group.elements("unit").size() == 0)
      {
        groups.remove(i);
        i--;
      }
    }
    if (segcnt < trg_list.size())
    {
      org.dom4j.Element group = root.element("aligned").addElement("group");
      group.addAttribute("id", Integer.toString(groups.size()));
      org.dom4j.Element unit = group.addElement("unit");
      unit.addAttribute("id", "0");
      org.dom4j.Element trg_para = unit.addElement("trg_para");
      trg_para.addAttribute("id", Integer.toString(groups.size()) + " - 0");
      org.dom4j.Element trgsegs = trg_para.addElement("segments");
      for (int x = segcnt; x < trg_list.size(); x++)
      {
        String text = decodehtmlstring((String)trg_list.get(x));
        org.dom4j.Element trg_seg = trgsegs.addElement("trg_seg");
        trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(x)));
        if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(x)))) || (decodehtmlstring((String)trg_seq_list.get(x)).startsWith("n - "))) {
          trg_seg.addAttribute("edited", "true");
        }
        trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
        trg_seg.setText(text);
      }
    }
    org.dom4j.Element orphans = root.element("orphans");
    orphans.clearContent();
    org.dom4j.Element unit = orphans.addElement("unit");
    unit.addAttribute("id", "0");
    org.dom4j.Element trg_para = unit.addElement("trg_para");
    trg_para.addAttribute("id", "0 - 0");
    org.dom4j.Element trg_segs = trg_para.addElement("segments");
    for (int i = 0; i < missing_list.size(); i++)
    {
      org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
      String text = decodehtmlstring((String)missing_list.get(i));
      trg_seg.addAttribute("id", decodehtmlstring((String)missing_seq_list.get(i)));
      if ((edited_list.contains(decodehtmlstring((String)missing_seq_list.get(i)))) || (decodehtmlstring((String)trg_seq_list.get(i)).startsWith("n - "))) {
        trg_seg.addAttribute("edited", "true");
      }
      trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
      trg_seg.setText(text);
    }
    new File(this.alignedfile).delete();
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.alignedfile)), "UTF8");
    document.write(writer);
    writer.close();
    if (new File(this.auto_saved_alignedfile).exists()) {
      new File(this.auto_saved_alignedfile).delete();
    }
  }
  
  public void auto_update_seg(JSONArray targets, JSONArray trg_seqs, JSONArray missing_targets, JSONArray missing_trg_seqs, JSONArray locks, int cnt, JSONArray edited, JSONArray review, JSONArray ignore)
    throws Exception
  {
    File alignedFile = new File(this.alignedfile);
    if (!alignedFile.exists()) {
      throw new FileNotFoundException("Could not find aligned xml file");
    }
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    List<String> trg_list = new ArrayList();
    for (int i = 0; i < targets.length(); i++) {
      trg_list.add(targets.getString(i));
    }
    List<String> trg_seq_list = new ArrayList();
    for (int i = 0; i < trg_seqs.length(); i++) {
      trg_seq_list.add(trg_seqs.getString(i));
    }
    List<String> missing_list = new ArrayList();
    for (int i = 0; i < missing_targets.length(); i++) {
      missing_list.add(missing_targets.getString(i));
    }
    List<String> missing_seq_list = new ArrayList();
    for (int i = 0; i < missing_trg_seqs.length(); i++) {
      missing_seq_list.add(missing_trg_seqs.getString(i));
    }
    List<String> locks_list = new ArrayList();
    for (int i = 0; i < locks.length(); i++) {
      locks_list.add(locks.getString(i));
    }
    this.nullcnt = cnt;
    
    List<String> edited_list = new ArrayList();
    for (int i = 0; i < edited.length(); i++) {
      edited_list.add(edited.getString(i));
    }
    List<String> review_list = new ArrayList();
    for (int i = 0; i < review.length(); i++) {
      review_list.add(review.getString(i));
    }
    List<String> ignore_list = new ArrayList();
    for (int i = 0; i < ignore.length(); i++) {
      ignore_list.add(ignore.getString(i));
    }
    int segcnt = 0;
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    org.dom4j.Element root = document.getRootElement();
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      
      List<org.dom4j.Element> units = group.elements("unit");
      for (int j = 0; j < units.size(); j++)
      {
        org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
        unit.addAttribute("alignsegs", "true");
        org.dom4j.Element src_para = unit.element("src_para");
        if (src_para != null)
        {
          List<org.dom4j.Element> src_segs = src_para.element("segments").elements("src_seg");
          for (int z = 0; z < src_segs.size(); z++)
          {
            org.dom4j.Element src_seg = (org.dom4j.Element)src_segs.get(z);
            String seq = i + " - " + j + " - " + z;
            if (locks_list.contains(seq)) {
              src_seg.attribute("locked").setValue("true");
            } else {
              src_seg.attribute("locked").setValue("false");
            }
            if (review_list.contains(seq)) {
              src_seg.attribute("needreview").setValue("true");
            } else {
              src_seg.attribute("needreview").setValue("false");
            }
            if (ignore_list.contains(seq)) {
              src_seg.attribute("ignored").setValue("true");
            } else {
              src_seg.attribute("ignored").setValue("false");
            }
          }
          org.dom4j.Element trg_para = unit.element("trg_para");
          trg_para.remove(trg_para.element("text"));
          List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
          for (int z = 0; z < trg_segs.size(); z++)
          {
            org.dom4j.Element trg_seg = (org.dom4j.Element)trg_segs.get(z);
            if (z >= src_segs.size())
            {
              trg_para.element("segments").remove(trg_seg);
            }
            else if (segcnt < trg_list.size())
            {
              trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(segcnt)));
              if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(segcnt)))) || (decodehtmlstring((String)trg_seq_list.get(segcnt)).startsWith("n - "))) {
                trg_seg.addAttribute("edited", "true");
              }
              String text = decodehtmlstring((String)trg_list.get(segcnt));
              trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
              trg_seg.setText(text);
              segcnt++;
            }
            else
            {
              trg_para.element("segments").remove(trg_seg);
            }
          }
          if (trg_para.element("segments").elements("trg_seg").size() == 0) {
            group.remove(unit);
          }
        }
        else
        {
          org.dom4j.Element trg_para = unit.element("trg_para");
          trg_para.remove(trg_para.element("text"));
          List<org.dom4j.Element> trg_segs = trg_para.element("segments").elements("trg_seg");
          for (int z = 0; z < trg_segs.size(); z++)
          {
            org.dom4j.Element trg_seg = (org.dom4j.Element)trg_segs.get(z);
            if (segcnt < trg_list.size())
            {
              trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(segcnt)));
              if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(segcnt)))) || (decodehtmlstring((String)trg_seq_list.get(segcnt)).startsWith("n - "))) {
                trg_seg.addAttribute("edited", "true");
              }
              String text = decodehtmlstring((String)trg_list.get(segcnt));
              trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
              trg_seg.setText(text);
              segcnt++;
            }
            else
            {
              trg_para.element("segments").remove(trg_seg);
            }
          }
          if (trg_para.element("segments").elements("trg_seg").size() == 0) {
            group.remove(unit);
          }
        }
      }
      if (group.elements("unit").size() == 0)
      {
        groups.remove(i);
        i--;
      }
    }
    if (segcnt < trg_list.size())
    {
      org.dom4j.Element group = root.element("aligned").addElement("group");
      group.addAttribute("id", Integer.toString(groups.size()));
      org.dom4j.Element unit = group.addElement("unit");
      unit.addAttribute("id", "0");
      org.dom4j.Element trg_para = unit.addElement("trg_para");
      org.dom4j.Element trgsegs = trg_para.addElement("segments");
      for (int x = segcnt; x < trg_list.size(); x++)
      {
        String text = decodehtmlstring((String)trg_list.get(x));
        org.dom4j.Element trg_seg = trgsegs.addElement("trg_seg");
        trg_seg.addAttribute("id", decodehtmlstring((String)trg_seq_list.get(x)));
        if ((edited_list.contains(decodehtmlstring((String)trg_seq_list.get(x)))) || (decodehtmlstring((String)trg_seq_list.get(x)).startsWith("n - "))) {
          trg_seg.addAttribute("edited", "true");
        }
        trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
        trg_seg.setText(text);
      }
    }
    org.dom4j.Element orphans = root.element("orphans");
    orphans.clearContent();
    org.dom4j.Element unit = orphans.addElement("unit");
    unit.addAttribute("id", "0");
    org.dom4j.Element trg_para = unit.addElement("trg_para");
    org.dom4j.Element trg_segs = trg_para.addElement("segments");
    for (int i = 0; i < missing_list.size(); i++)
    {
      org.dom4j.Element trg_seg = trg_segs.addElement("trg_seg");
      String text = decodehtmlstring((String)missing_list.get(i));
      trg_seg.addAttribute("id", decodehtmlstring((String)missing_seq_list.get(i)));
      if ((edited_list.contains(decodehtmlstring((String)missing_seq_list.get(i)))) || (decodehtmlstring((String)trg_seq_list.get(i)).startsWith("n - "))) {
        trg_seg.addAttribute("edited", "true");
      }
      trg_seg.addAttribute("isExtractable", Boolean.toString(extractionSupportImpl.isExtractable(text)));
      trg_seg.setText(text);
    }
    this.auto_saved_alignedfile = (this.alignedfile + ".temp");
    if (new File(this.auto_saved_alignedfile).exists()) {
      new File(this.auto_saved_alignedfile).delete();
    }
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.auto_saved_alignedfile)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public String decodehtmlstring(String encoded_s)
  {
    return Jsoup.parse(encoded_s.replaceAll("<br[^>]*?>", "<br>").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replaceAll("\\s", "&nbsp;")).text().replace(" ", " ").replace("&#8232;", " ");
  }
  
  private String encodeHtml(String s)
  {
    return s.replace(" ", "&#8232;");
  }
  
  public HashMap<String, List<String>> updateSingleParagraph(String text)
    throws Exception
  {
    HashMap<String, List<String>> semap = new HashMap();
    List<String> trgsegs = segmentStringWithRevs(decodehtmlstring(text), this.targetlanguage);
    for (int i = 0; i < trgsegs.size(); i++)
    {
      System.out.println((String)trgsegs.get(i));
      trgsegs.set(i, ((String)trgsegs.get(i)).trim());
    }
    semap.put("trgsegs", trgsegs);
    
    return semap;
  }
  
  public HashMap<String, List<String>> findandsegmentpara(String src_segid, String trg_segid)
    throws Exception
  {
    HashMap<String, List<String>> stsegs = new HashMap();
    List<String> trgs = new ArrayList();
    List<String> srcs = new ArrayList();
    File alignedFile = new File(this.alignedfile);
    if (!alignedFile.exists()) {
      throw new FileNotFoundException("Could not find aligned xml file");
    }
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    org.dom4j.Element root = document.getRootElement();
    List<org.dom4j.Element> units = root.element("content").elements("unit");
    for (int i = 0; i < units.size(); i++)
    {
      org.dom4j.Element unit = (org.dom4j.Element)units.get(i);
      
      String trg_seq = "";
      if (unit.element("trg_para") != null)
      {
        org.dom4j.Element trg = (org.dom4j.Element)unit.element("trg_para").clone();
        trg_seq = unit.element("trg_para").attributeValue("para_seq");
        if (trg_seq.equals(trg_segid))
        {
          SegmenterFactory factory = new SegmenterFactory();
          Configuration segconfig = createConfigForSegmenter(false, this.targetlanguage);
          Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(this.targetlanguage), segconfig);
          trgs = segmenter.segment(trg.getText());
        }
      }
      String src_seq = "";
      if (unit.element("src_para") != null)
      {
        org.dom4j.Element src = (org.dom4j.Element)unit.element("src_para").clone();
        src_seq = unit.element("src_para").attributeValue("para_seq");
        if (src_seq.equals(src_segid)) {
          srcs = segmentStringWithRevs(src.getText(), this.sourcelanguage);
        }
      }
    }
    stsegs.put("srcsegs", srcs);
    stsegs.put("trgsegs", trgs);
    
    return stsegs;
  }
  
  public List<String> segmentStringWithRevs(String srctext, String language)
    throws Exception
  {
    List<String> srcs = new ArrayList();
    Map<Integer, String> tagmap = new TreeMap();
    SegmenterFactory factory = new SegmenterFactory();
    Configuration config = createConfigForSegmenter(false, language);
    Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(language), config);
    

    srctext = srctext.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    Pattern p = Pattern.compile("<(/)*ins>|<del>.*?</del>|<br>|&paradel;");
    Matcher m = p.matcher(srctext);
    while (m.find()) {
      tagmap.put(Integer.valueOf(m.start()), m.group());
    }
    String srctext_notag = srctext.replaceAll("<(/)*ins>|<del>.*?</del>|<br>|&paradel;", "");
    srcs = segmenter.segment(srctext_notag);
    


    List<String> srcs_split = new ArrayList();
    int i;
    int entrycount;
    int strlen;
    ArrayList<String> tags;
    ArrayList<String> faketags;
    int offset;
    if (srcs.size() == 0)
    {
      srcs.add(srctext);
    }
    else
    {
      i = 0;
      entrycount = 0;
      strlen = ((String)srcs.get(0)).length();
      tags = new ArrayList();
      faketags = new ArrayList();
      offset = 0;
      for (Map.Entry<Integer, String> entry : tagmap.entrySet())
      {
        entrycount++;
        int position = ((Integer)entry.getKey()).intValue();
        String tag = (String)entry.getValue();
        while (((i < srcs.size() - 1) && (strlen - 1 < position + offset)) || ((i == srcs.size() - 1) && (strlen < position + offset)))
        {
          if (tags.size() != 0) {
            for (int t = 0; t < tags.size(); t++)
            {
              String remt = (String)tags.get(t);
              if (remt.equals("<ins>"))
              {
                srcs.set(i, (String)srcs.get(i) + "</ins>");
                if (i < srcs.size() - 1)
                {
                  srcs.set(i + 1, "<ins>" + (String)srcs.get(i + 1));
                  offset += 5;
                  faketags.add("<ins>");
                }
              }
              else if (remt.equals("</ins>"))
              {
                srcs.set(i, "<ins>" + (String)srcs.get(i));
              }
            }
          }
          tags = new ArrayList();
          tags.addAll(faketags);
          faketags = new ArrayList();
          
          i++;
          strlen += ((String)srcs.get(i)).length();
        }
        if ((tag.equals("</ins>")) && (tags.contains("<ins>"))) {
          tags.remove("<ins>");
        } else {
          tags.add(tag);
        }
        String trgstr = (String)srcs.get(i);
        int insertpnt = position + offset - (strlen - trgstr.length());
        
        trgstr = trgstr.substring(0, insertpnt) + tag + trgstr.substring(insertpnt, trgstr.length());
        srcs.set(i, trgstr);
        strlen += tag.length();
        if ((entrycount == tagmap.size()) && 
          (tags.size() != 0)) {
          for (int t = 0; t < tags.size(); t++)
          {
            String remt = (String)tags.get(t);
            if (remt.equals("<ins>")) {
              srcs.set(i, (String)srcs.get(i) + "</ins>");
            } else if (remt.equals("</ins>")) {
              srcs.set(i, "<ins>" + (String)srcs.get(i));
            }
          }
        }
      }
    }
    for (int f = 0; f < srcs.size(); f++) {
      srcs_split.addAll(segmentStringWithRevs2((String)srcs.get(f), segmenter));
    }
    for (int f = 0; f < srcs_split.size(); f++) {
      srcs_split.set(f, trimText(((String)srcs_split.get(f)).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replaceAll("&lt;([/]*)(ins|del|br)&gt;", "<$1$2>"), false)[0]);
    }
    return srcs_split;
  }
  
  public List<String> segmentStringWithRevs2(String srctext, Segmenter segmenter)
  {
    List<String> srcs = new ArrayList();
    Map<Integer, String> tagmap = new TreeMap();
    
    Pattern p = Pattern.compile("<(/)*ins>|<(/)*del>|<br>|&paradel;");
    Matcher m = p.matcher(srctext);
    while (m.find()) {
      tagmap.put(Integer.valueOf(m.start()), m.group());
    }
    String srctext_notag = srctext.replaceAll("<(/)*ins>|<(/)*del>|<br>|&paradel;", "");
    srcs = segmenter.segment(srctext_notag);
    
    List<String> new_srcs = new ArrayList();
    if (srcs.size() == 0)
    {
      new_srcs.add(srctext);
    }
    else
    {
      int i = 0;
      int entrycount = 0;
      int strlen = ((String)srcs.get(0)).length();
      ArrayList<String> tags = new ArrayList();
      ArrayList<String> faketags = new ArrayList();
      int offset = 0;
      for (Map.Entry<Integer, String> entry : tagmap.entrySet())
      {
        entrycount++;
        int position = ((Integer)entry.getKey()).intValue();
        String tag = (String)entry.getValue();
        while (strlen < position + offset)
        {
          if (tags.size() != 0) {
            for (int t = 0; t < tags.size(); t++)
            {
              String remt = (String)tags.get(t);
              if (remt.equals("<ins>"))
              {
                srcs.set(i, (String)srcs.get(i) + "</ins>");
                if (i < srcs.size() - 1)
                {
                  srcs.set(i + 1, "<ins>" + (String)srcs.get(i + 1));
                  offset += 5;
                  faketags.add("<ins>");
                }
              }
              else if (remt.equals("<del>"))
              {
                srcs.set(i, (String)srcs.get(i) + "</del>");
                if (i < srcs.size() - 1)
                {
                  srcs.set(i + 1, "<del>" + (String)srcs.get(i + 1));
                  offset += 5;
                  faketags.add("<del>");
                }
              }
              else if (remt.equals("</ins>"))
              {
                srcs.set(i, "<ins>" + (String)srcs.get(i));
              }
              else if (remt.equals("</del>"))
              {
                srcs.set(i, "<del>" + (String)srcs.get(i));
              }
            }
          }
          tags = new ArrayList();
          tags.addAll(faketags);
          faketags = new ArrayList();
          
          i++;
          strlen += ((String)srcs.get(i)).length();
        }
        if ((tag.equals("</ins>")) && (tags.contains("<ins>"))) {
          tags.remove("<ins>");
        } else if ((tag.equals("</del>")) && (tags.contains("<del>"))) {
          tags.remove("<del>");
        } else {
          tags.add(tag);
        }
        String trgstr = (String)srcs.get(i);
        int insertpnt = position + offset - (strlen - trgstr.length());
        trgstr = trgstr.substring(0, insertpnt) + tag + trgstr.substring(insertpnt, trgstr.length());
        srcs.set(i, trgstr);
        strlen += tag.length();
        if ((entrycount == tagmap.size()) && 
          (tags.size() != 0)) {
          for (int t = 0; t < tags.size(); t++)
          {
            String remt = (String)tags.get(t);
            if (remt.equals("<ins>")) {
              srcs.set(i, (String)srcs.get(i) + "</ins>");
            } else if (remt.equals("<del>")) {
              srcs.set(i, (String)srcs.get(i) + "</del>");
            } else if (remt.equals("</ins>")) {
              srcs.set(i, "<ins>" + (String)srcs.get(i));
            } else if (remt.equals("</del>")) {
              srcs.set(i, "<del>" + (String)srcs.get(i));
            }
          }
        }
      }
      new_srcs.addAll(srcs);
    }
    return new_srcs;
  }
  
  private static String[] trimText(String text, boolean preservenbsp)
  {
    if (!preservenbsp) {
      text = text.replace(" ", " ");
    }
    String[] t = new String[3];
    String patternLEFT = "^[\\s\\t]+";
    String patternRIGHT = "[\\s\\t]+$";
    String acceptedText = text.replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "");
    
    Pattern left = Pattern.compile(patternLEFT);
    Pattern right = Pattern.compile(patternRIGHT);
    
    Matcher m_left = left.matcher(acceptedText);
    Matcher m_right = right.matcher(acceptedText);
    if (m_left.find()) {
      t[1] = "true";
    } else {
      t[1] = "false";
    }
    if (m_right.find()) {
      t[2] = "true";
    } else {
      t[2] = "false";
    }
    t[0] = text.trim().replaceAll("(?s)<ins>(\\s|<br>)*</ins>$", "").replaceAll("(?s)<del>(\\s|<br>)*</del>$", "").trim().replaceAll("(?s)(\\s|<br>)+</ins>$", "</ins>").replaceAll("(?s)(\\s|<br>)+</del>$", "</del>").replaceAll("(?s)^<ins>(\\s|<br>)*</ins>", "").replaceAll("(?s)^<del>(\\s|<br>)*</del>", "").trim().replaceAll("(?s)^<ins>(\\s|<br>)+", "<ins>").replaceAll("(?s)^<del>(\\s|<br>)+", "<del>");
    return t;
  }
  
  private List trimContents(List contents)
  {
    List newcontents = new ArrayList();
    
    int start_idx = 0;
    boolean start_reach = true;
    int end_idx = contents.size() - 1;
    boolean end_reach = true;
    while ((start_idx < end_idx) && ((start_reach) || (end_reach)))
    {
      Object st = contents.get(start_idx);
      if ((st instanceof org.dom4j.Text))
      {
        if ((((org.dom4j.Text)st).getText().trim().equals("")) && (start_reach)) {
          start_idx++;
        } else {
          start_reach = false;
        }
      }
      else {
        start_reach = false;
      }
      Object ed = contents.get(end_idx);
      if ((ed instanceof org.dom4j.Text))
      {
        if ((((org.dom4j.Text)ed).getText().trim().equals("")) && (end_reach)) {
          end_idx--;
        } else {
          end_reach = false;
        }
      }
      else {
        end_reach = false;
      }
    }
    for (int i = start_idx; i <= end_idx; i++) {
      newcontents.add(contents.get(i));
    }
    return newcontents;
  }
  
  public void exportHtmlLogFileForFinalReview()
    throws Exception
  {
    System.out.println("creating log file for final review....");
    
    List<String> newtrgs = gatherNewTargetsFromTranslatedTxlf();
    List<List<String>> notes = gatherNotesFromTranslatedTxlf();
    org.jsoup.nodes.Document doc = Jsoup.parse(new File(this.htmlreportfortranslation_temp), "UTF-8", "");
    org.jsoup.nodes.Element ttl = doc.select("ttl").first();
    for (org.jsoup.nodes.Element radio : doc.select("input[type=\"radio\"]")) {
      if (radio.hasAttr("disabled")) {
        radio.removeAttr("disabled");
      }
    }
    for (org.jsoup.nodes.Element radiotext : doc.select(".filter")) {
      radiotext.attr("style", "#23407B;");
    }
    ttl.text("RA Report - For Final Reivew");
    
    Elements rows = doc.select("tr");
    
    int newtrgcnt = 0;
    for (int i = 3; i < rows.size(); i++)
    {
      org.jsoup.nodes.Element row = (org.jsoup.nodes.Element)rows.get(i);
      org.jsoup.nodes.Element trg = row.select("td.trg").first();
      org.jsoup.nodes.Element cmt = row.select("th.cmt").first();
      

      String orgtrgtext = trg.text().replace("↵ ", "↵<br>").replace(" ", " ");
      
      String newtrgtext = "";
      //System.out.println(newtrgs.get(newtrgcnt));
      if (!row.attr("id").isEmpty())
      {
        newtrgtext = trimText((String)newtrgs.get(newtrgcnt), false)[0];
        

        List<String> note_list = (List)notes.get(newtrgcnt);
        String html = "";
        for (String note : note_list) {
          if (note_list.indexOf(note) != note_list.size() - 1) {
            html = html + "&#8226;&nbsp;" + note.replace("\n", "<br>") + "<br>";
          } else {
            html = html + "&#8226;&nbsp;" + note.replace("\n", "<br>");
          }
        }
        cmt.html(html);
        
        newtrgcnt++;
      }
      if (!orgtrgtext.equals(newtrgtext))
      {
        row.addClass("TrgTracked");
        





        diff_match_patch dmp = new diff_match_patch();
        dmp.Diff_EditCost = 6;
        LinkedList<diff_match_patch.Diff> Diffs = dmp.diff_main(orgtrgtext, newtrgtext);
        dmp.diff_cleanupSemantic(Diffs);
        String result = dmp.diff_prettyHtml(Diffs);
        result = result.replace("&lt;br&gt;", "<br>").replace("<span>", "").replace("</span>", "").replaceAll("(?s)<ins[^>]*?>", "<ins>").replaceAll("(?s)</ins[^>]*?>", "</ins>").replaceAll("(?s)<del[^>]*?>", "<del>").replaceAll("(?s)</del[^>]*?>", "</del>");
        trg.html(result.replace(" ", "&nbsp;"));
      }
    }
    this.htmlreportforfinalreview = (new File(this.translatedtxlf).getParent() + File.separator + new File(this.sourcefile).getName().substring(0, new File(this.sourcefile).getName().lastIndexOf(".")) + "_final.html");
    if (new File(this.htmlreportforfinalreview).exists()) {
      new File(this.htmlreportforfinalreview).delete();
    }
    Writer logWriter = new OutputStreamWriter(new FileOutputStream(this.htmlreportforfinalreview), "UTF-8");
    logWriter.write(doc.toString().replace("&nbsp;", " "));
    logWriter.close();
  }
  
  private List<String> gatherNewTargetsFromTranslatedTxlf()
  {
    List<String> trgs = new ArrayList();
    org.dom4j.Document document_trans = XmlParser.parseXmlFile(this.translatedtxlf);
    org.dom4j.Element root_trans = document_trans.getRootElement();
    
    List<com.aspose.words.Node> list_trans = root_trans.selectNodes("//*[name() = 'trans-unit']");
    for (int i = 0; i < list_trans.size(); i++)
    {
      org.dom4j.Element unit = (org.dom4j.Element)list_trans.get(i);
      org.dom4j.Element trg = unit.element("target");
      String trgtext = getTxlfElementText_noescape(trg).replace("<br> ", "↵<br>");
      trgs.add(trgtext);
    }
    return trgs;
  }
  
  private List<List<String>> gatherNotesFromTranslatedTxlf()
  {
    List<List<String>> notes = new ArrayList();
    org.dom4j.Document document_trans = XmlParser.parseXmlFile(this.translatedtxlf);
    org.dom4j.Element root_trans = document_trans.getRootElement();
    
    List<com.aspose.words.Node> list_trans = root_trans.selectNodes("//*[name() = 'trans-unit']");
    for (int i = 0; i < list_trans.size(); i++)
    {
      List<String> note_list = new ArrayList();
      org.dom4j.Element unit = (org.dom4j.Element)list_trans.get(i);
      List<org.dom4j.Element> note_element_list = unit.elements("note");
      for (org.dom4j.Element e : note_element_list) {
        note_list.add(e.getText());
      }
      notes.add(note_list);
    }
    return notes;
  }
  
  public void exportHtmlLogFileForTranslation(ArrayList<String[]> reportstats, int[] fr_stats)
    throws Exception
  {
    System.out.println("creating log file for translation....");
    StringBuffer sb = new StringBuffer();
    
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />\n");
    sb.append("<title>Source Changes Report</title>\n");
    sb.append("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js\"></script>\n");
    

    sb.append("<STYLE type=\"text/css\">\n");
    sb.append("body, table {font-family: arial,sans-serif; font-size: 12px;}\n");
    sb.append("table {display: block; width:100%;}\n");
    sb.append("tbody {display: table; width:100%;}\n");
    sb.append("td {background: #FFFFFF; color: #000000;}\n");
    sb.append("th {background: #E0E0E0; text-align:center; font-weight:bold}\n");
    sb.append("ttl {font-size: 24; font-weight: bold; color : #888}\n");
    sb.append(".lbsrc {float: left;text-align:center;border-radius:16px;box-shadow: 3px 3px 3px #888888;border: 1px solid #CCC;font-size: 12.3px;word-wrap: break-word;line-height: 16px;margin:6px 0px;padding: 2px 7px;width: 6%;outline: 0;vertical-align: baseline;background: #FFFFFF no-repeat 19px 19px;}\n");
    sb.append(".lbtrg {float: left;text-align:center;border-radius:16px;box-shadow: 3px 3px 3px #888888;border: 1px solid #CCC;font-size: 12.3px;word-wrap: break-word;line-height: 16px;margin:6px;padding: 2px 7px;width: 6%;outline: 0;vertical-align: baseline;background: #E0EEEE no-repeat 19px 19px;}\n");
    sb.append(".fproperties {font-weight: bold;margin:2px;}\n");
    sb.append(".fpropertiescontent {font-weight: bold; color : #bbb}\n");
    sb.append(".filter {font-weight: bold;margin:2px;color:#23407B}\n");
    sb.append(".sid {width:5%}\n");
    sb.append(".act {width:5%;font-size:12px}\n");
    sb.append(".src {width:34%;white-space:pre-wrap;vertical-align:top;text-align:left;}\n");
    sb.append(".trg {width:34%;background: #E0EEEE;white-space:pre-wrap;vertical-align:top;text-align:left;}\n");
    sb.append(".sco {width:4%}\n");
    sb.append(".swc {width:4%}\n");
    sb.append(".cmt {width:14%;text-align:left}\n");
    sb.append(".tdh {background:#C9C9C9;font-weight:bold;}\n");
    sb.append(".wc {background:#EEEEEE;font-weight:bold;text-align:center}\n");
    sb.append(".nomatch {font-weight:bold;text-align:center;background: #888;}\n");
    sb.append(".nontranslatable {font-weight:bold;text-align:center;background: #888;}\n");
    sb.append(".fuzzy {font-weight:bold;text-align:center;background: yellow;}\n");
    sb.append(".gold {font-weight:bold;text-align:center;background: #5EDA9E;}\n");
    sb.append("ins {color: #1E90FF;text-decoration: underline;}\n");
    sb.append("del {color: #990000;text-decoration: line-through;}\n");
    sb.append(".accept {border-color: #1E90FF}\n");
    sb.append(".reject {border-color: red}\n");
    sb.append(".normal {border-color: black}\n");
    sb.append(".option {display:inline-block;cursor:pointer;border-style:solid;border-width:1px;width:10px;height:10px;margin-left:6%;margin-right:6%;border-radius: 2px;-moz-box-shadow:inset 0 0 10px #bbbbbb;-webkit-box-shadow: inset 0 0 10px #bbbbbb;box-shadow:inset 0 0 10px #bbbbbb;}\n");
    sb.append(".selected {font-size: 14px}\n");
    sb.append(".unselected {font-size: 6px}\n");
    sb.append(".topref{float:right;font-weight:bold;color:blue}\n");
    sb.append("#search {float:right;position:fixed;top:1%;right:7px;opacity:0.5;width:40px;Background-color:#ccc;border-style:none;padding:2px}\n");
    sb.append("</STYLE>\n");
    
    sb.append("</head>\n");
    sb.append("<body>\n");
    sb.append("<ttl>RA Report - For Translation</ttl>\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    sb.append("<div class=\"lbsrc\">source</div>\n");
    sb.append("<div class=\"lbtrg\">translation</div>\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    sb.append("<hr>\n");
    sb.append("<font color=\"1E90FF\">&#11035;&nbsp;accept track changes</font>&nbsp;&nbsp;<font color=\"red\">&#11035;&nbsp;reject track changes</font>&nbsp;&nbsp;&#11035;&nbsp;show all track changes\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    sb.append("<div class=\"fproperties\">File Name : &nbsp;&nbsp;<span class=\"fpropertiescontent\">" + new File(this.populatedsourcetxlf).getName() + "</span></div>\n");
    sb.append("<div class=\"fproperties\">Language Pair : &nbsp;&nbsp;<span class=\"fpropertiescontent\">" + this.sourcelanguage + " > " + this.targetlanguage + "</span></div>\n");
    
    StringBuffer sb3 = new StringBuffer();
    sb3.append("<table cellspacing=\"0\" cellpadding=\"4\" bgcolor=\"#E0E0E0\" border=\"1\" bordercolor=\"#224466\" style=\"table-layout:fixed\">\n");
    sb3.append("<tr>\n");
    







    sb3.append("<th class=\"tdh\">Source</th>\n");
    sb3.append("<th class=\"tdh\">Fully Aligned</th>\n");
    sb3.append("<th class=\"tdh\">Aligned but Need Review</th>\n");
    sb3.append("<th class=\"tdh\">Aligned but Source Tracked</th>\n");
    sb3.append("<th class=\"tdh\">Not Aligned</th>\n");
    sb3.append("<th class=\"tdh\">Total</th>\n");
    sb3.append("</tr>\n");
    
    int generalid = 0;
    int tc_count = 0;
    StringBuffer sb2 = new StringBuffer();
    sb2.append("<table cellspacing=\"0\" cellpadding=\"4\" bgcolor=\"#E0E0E0\" border=\"1\" bordercolor=\"#224466\" style=\"table-layout:fixed\">\n");
    sb2.append("<tr>\n");
    sb2.append("<th class=\"tdh sid\">Segment ID</th>\n");
    sb2.append("<th class=\"tdh act\">View</th>\n");
    sb2.append("<th class=\"tdh src\">Source</th>\n");
    sb2.append("<th class=\"tdh trg\">Target</th>\n");
    sb2.append("<th class=\"tdh sco\">Score</th>\n");
    sb2.append("<th class=\"tdh swc\">Count</th>\n");
    sb2.append("<th class=\"tdh cmt\">Comment</th>\n");
    sb2.append("</tr>\n");
    
    int wc_gold = 0;
    int wc_fuzzy_high = 0;
    int wc_fuzzy_low = 0;
    int wc_nomatch = 0;
    int wc_reps = fr_stats[0];
    int wc_fuzzyreps = fr_stats[1];
    for (int i = 0; i < reportstats.size(); i++)
    {
      String[] rstats = (String[])reportstats.get(i);
      
      String source = rstats[0];
      String target = rstats[1];
      String score = rstats[2];
      String match_type = rstats[3];
      String tc_type = rstats[4];
      String wc = rstats[5];
      String comment = rstats[6];
      if ((tc_type.equals("INSERTION")) || (tc_type.equals("MIX"))) {
        tc_count++;
      }
      if (!match_type.equals("N/A"))
      {
        generalid++;
        sb2.append("<tr id=\"" + generalid + "\" class=\"tp" + tc_type + "\">\n");
        sb2.append("<th class=\"sid\">" + generalid + "</td>\n");
      }
      else
      {
        sb2.append("<tr class=\"tp" + tc_type + "\">\n");
        sb2.append("<th class=\"sid\">N/A</td>\n");
      }
      sb2.append("<th class=\"act\"><div class=\"option accept\"></div><div class=\"option reject\"></div><div class=\"option normal\"></div></td>\n");
      sb2.append("<td class=\"src\">" + source.replace(" ", "&nbsp;") + "</td>\n");
      sb2.append("<td class=\"trg\">" + target.replace(" ", "&nbsp;") + "</td>\n");
      if (match_type.equals("exact-match"))
      {
        sb2.append("<td class=\"sco gold\">" + score + "</td>\n");
        sb2.append("<th class=\"swc\">" + wc + "</td>\n");
        wc_gold += Integer.parseInt(wc);
      }
      else if (match_type.equals("fuzzy-match"))
      {
        sb2.append("<td class=\"sco fuzzy\">" + score + "</td>\n");
        sb2.append("<th class=\"swc\">" + wc + "</td>\n");
        if (score.equals("75")) {
          wc_fuzzy_low += Integer.parseInt(wc);
        } else {
          wc_fuzzy_high += Integer.parseInt(wc);
        }
      }
      else if (match_type.equals("x-no-match"))
      {
        sb2.append("<td class=\"sco nomatch\">" + score + "</td>\n");
        sb2.append("<th class=\"swc\">" + wc + "</td>\n");
        wc_nomatch += Integer.parseInt(wc);
      }
      else
      {
        sb2.append("<td class=\"sco nontranslatable\">" + score + "</td>\n");
        sb2.append("<th class=\"swc\">" + wc + "</td>\n");
      }
      sb2.append("<th class=\"cmt\">" + comment + "</td>\n");
      sb2.append("</tr>\n");
    }
    sb2.append("</table>\n");
    
    sb3.append("<tr>\n");
    if (Locale.makeLocale(this.sourcelanguage).isFarEast()) {
      sb3.append("<th class=\"tdh\">Character Count</th>\n");
    } else {
      sb3.append("<th class=\"tdh\">Word Count</th>\n");
    }
    wc_nomatch = wc_nomatch - wc_reps - wc_fuzzyreps;
    sb3.append("<td class=\"wc\">" + wc_gold + "</th>\n");
    sb3.append("<td class=\"wc\">" + wc_fuzzy_high + "</th>\n");
    sb3.append("<td class=\"wc\">" + wc_fuzzy_low + "</th>\n");
    sb3.append("<td class=\"wc\">" + wc_nomatch + "</th>\n");
    

    sb3.append("<td class=\"wc\">" + (wc_gold + wc_fuzzy_high + wc_fuzzy_low + wc_nomatch + wc_reps + wc_fuzzyreps) + "</th>\n");
    sb3.append("</tr>\n");
    sb3.append("</table>\n");
    
    sb.append("<div class=\"fproperties\">Number of Segments with Track Changes / Total Segments(in TXLF): &nbsp;&nbsp;<span class=\"fpropertiescontent\">" + tc_count + " / " + generalid + "</span></div>\n");
    sb.append("<br />\n");
    
    sb.append(sb3);
    sb.append("<br />\n");
    
    sb.append("<form>\n");
    sb.append("<input type=\"checkbox\" name=\"CKB\" value=\"SIT\"><span class=\"filter\">Show Segments in the TXLF Only</span>\n");
    sb.append("<br />\n");
    sb.append("<hr>\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"SA\" checked><span class=\"filter\">Show All</span>\n");
    sb.append("<br />\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"ST\"><span class=\"filter\">Show Changed Segments</span>\n");
    sb.append("<br />\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"STS\"><span class=\"filter\">Show Changed Sources</span>\n");
    sb.append("<br />\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"STT\" disabled><span class=\"filter\" style=\"color:#bbbbbb\">Show Changed Targets</span>\n");
    sb.append("<br />\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"STSNTT\" disabled><span class=\"filter\" style=\"color:#bbbbbb\">Show Changed Sources with Non-Changed Targets</span>\n");
    sb.append("<br />\n");
    sb.append("<input type=\"radio\" name=\"Filter\" value=\"SNTTS\" disabled><span class=\"filter\" style=\"color:#bbbbbb\">Show Non-Changed Sources with Changed Targets</span>\n");
    sb.append("</form>\n");
    
    sb.append(sb2);
    
    sb.append("<br />\n");
    
    sb.append("<form action=\"javascript:goToSegment();\">\n");
    sb.append("<input type=\"text\" id=\"search\" onfocus=\"searchfocus(this)\" onblur=\"searchnotfocus(this)\">\n");
    sb.append("</form>\n");
    
    sb.append("<a class=\"topref\" href=\"#TOP\" title=\"Back to Top\">TOP</a>\n");
    sb.append("<br />\n");
    sb.append("<hr>\n");
    sb.append("<br />\n");
    sb.append("<br />\n");
    
    sb.append("<script type=\"text/javascript\">\n");
    sb.append("$(document).ready(function(){\n");
    sb.append("var norms = $(document).find('.normal');\n");
    sb.append("for (var i = 0; i < norms.length; i++) {\n");
    sb.append("$(norms[i]).addClass(\"selected\");\n");
    sb.append("$(norms[i]).css(\"background-color\",\"black\");\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("\n");
    
    sb.append("function goToSegment() {\n");
    sb.append("var segid = document.getElementById(\"search\").value;\n");
    sb.append("location.href = \"#\" + segid;\n");
    sb.append("}\n");
    sb.append("\n");
    sb.append("function searchfocus(field) {\n");
    sb.append("field.style.background = \"red\";\n");
    sb.append("}\n");
    sb.append("\n");
    sb.append("function searchnotfocus(field) {\n");
    sb.append("field.style.background = \"#ccc\";\n");
    sb.append("}\n");
    sb.append("\n");
    
    sb.append("$(\".accept\").click(function() {\n");
    sb.append("if(!$(this).hasClass(\"selected\")){\n");
    sb.append("$(this).removeClass(\"unselected\")\n");
    sb.append("$(this).addClass(\"selected\")\n");
    sb.append("$(this).css(\"background-color\",\"1E90FF\");\n");
    sb.append("var rej = $(this).parent().find('.reject')[0];\n");
    sb.append("var nor = $(this).parent().find('.normal')[0];\n");
    sb.append("$(rej).removeClass(\"selected\");\n");
    sb.append("$(rej).addClass(\"unselected\");\n");
    sb.append("$(rej).css(\"background-color\",\"\");\n");
    sb.append("$(nor).removeClass(\"selected\");\n");
    sb.append("$(nor).addClass(\"unselected\");\n");
    sb.append("$(nor).css(\"background-color\",\"\");\n");
    sb.append("\n");
    sb.append("var accepts = $(this).parent().parent().find('ins');\n");
    sb.append("for (var i = 0; i < accepts.length; i++) {\n");
    sb.append("accepts[i].style.display = \"inline\";\n");
    sb.append("accepts[i].style.textDecoration = \"none\";\n");
    sb.append("}\n");
    sb.append("var rejects = $(this).parent().parent().find('del');\n");
    sb.append("for (var i = 0; i < rejects.length; i++) {\n");
    sb.append("rejects[i].style.display = \"none\";\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("\n");
    sb.append("$(\".reject\").click(function() {\n");
    sb.append("if(!$(this).hasClass(\"selected\")){\n");
    sb.append("$(this).removeClass(\"unselected\")\n");
    sb.append("$(this).addClass(\"selected\")\n");
    sb.append("$(this).css(\"background-color\",\"red\");\n");
    sb.append("var act = $(this).parent().find('.accept')[0];\n");
    sb.append("var nor = $(this).parent().find('.normal')[0];\n");
    sb.append("$(act).removeClass(\"selected\");\n");
    sb.append("$(act).addClass(\"unselected\");\n");
    sb.append("$(act).css(\"background-color\",\"\");\n");
    sb.append("$(nor).removeClass(\"selected\");\n");
    sb.append("$(nor).addClass(\"unselected\");\n");
    sb.append("$(nor).css(\"background-color\",\"\");\n");
    sb.append("\n");
    sb.append("var accepts = $(this).parent().parent().find('ins');\n");
    sb.append("for (var i = 0; i < accepts.length; i++) {\n");
    sb.append("accepts[i].style.display = \"none\";\n");
    sb.append("}\n");
    sb.append("var rejects = $(this).parent().parent().find('del');\n");
    sb.append("for (var i = 0; i < rejects.length; i++) {\n");
    sb.append("rejects[i].style.display = \"inline\";\n");
    sb.append("rejects[i].style.textDecoration = \"none\";\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("\n");
    sb.append("$(\".normal\").click(function() {\n");
    sb.append("if(!$(this).hasClass(\"selected\")){\n");
    sb.append("$(this).removeClass(\"unselected\")\n");
    sb.append("$(this).addClass(\"selected\")\n");
    sb.append("$(this).css(\"background-color\",\"black\");\n");
    sb.append("var act = $(this).parent().find('.accept')[0];\n");
    sb.append("var rej = $(this).parent().find('.reject')[0];\n");
    sb.append("$(act).removeClass(\"selected\");\n");
    sb.append("$(act).addClass(\"unselected\");\n");
    sb.append("$(act).css(\"background-color\",\"\");\n");
    sb.append("$(rej).removeClass(\"selected\");\n");
    sb.append("$(rej).addClass(\"unselected\");\n");
    sb.append("$(rej).css(\"background-color\",\"\");\n");
    sb.append("\n");
    sb.append("var accepts = $(this).parent().parent().find('ins');\n");
    sb.append("for (var i = 0; i < accepts.length; i++) {\n");
    sb.append("accepts[i].style.display = \"inline\";\n");
    sb.append("accepts[i].style.textDecoration = \"underline\";\n");
    sb.append("}\n");
    sb.append("var rejects = $(this).parent().parent().find('del');\n");
    sb.append("for (var i = 0; i < rejects.length; i++) {\n");
    sb.append("rejects[i].style.display = \"inline\";\n");
    sb.append("rejects[i].style.textDecoration = \"line-through\";\n");
    sb.append("}\n");
    sb.append("}\n");
    sb.append("});\n");
    sb.append("\n");
    
    sb.append("$(\"input[name='CKB']\").change(function(){");
    sb.append("if($(this).is(':checked')){");
    sb.append("$('.tpDELETION').hide();");
    sb.append("}else{");
    sb.append("if($(\"input[value='SA']\").is(':checked') || $(\"input[value='STS']\").is(':checked') || $(\"input[value='STSNTT']\").is(':checked')){");
    sb.append("$('.tpDELETION').show();");
    sb.append("}else{");
    sb.append("$('.tpDELETION').hide();");
    sb.append("}");
    sb.append("}");
    sb.append("});");
    sb.append("\n");
    sb.append("$(\"input[name='Filter']\").change(function(){");
    sb.append("var value = $(this).val();");
    sb.append("if(value == \"SA\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("$(tr).show();");
    sb.append("}else{");
    sb.append("if($(\"input[name='CKB']\").is(':checked')){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}");
    sb.append("}");
    sb.append("}else if(value == \"ST\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("if(!$(tr).hasClass(\"tpNONE\") || $(tr).hasClass(\"TrgTracked\")){");
    sb.append("$(tr).show();");
    sb.append("}else{");
    sb.append("$(tr).hide();");
    sb.append("}");
    sb.append("}else{");
    sb.append("if($(\"input[name='CKB']\").is(':checked')){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}");
    sb.append("}");
    sb.append("}else if(value == \"STS\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("if($(tr).hasClass(\"tpNONE\")){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}else{");
    sb.append("if($(\"input[name='CKB']\").is(':checked')){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}");
    sb.append("}");
    sb.append("}else if(value == \"STT\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("if($(tr).hasClass(\"TrgTracked\")){");
    sb.append("$(tr).show();");
    sb.append("}else{");
    sb.append("$(tr).hide();");
    sb.append("}");
    sb.append("}else{");
    sb.append("$(tr).hide();");
    sb.append("}");
    sb.append("}");
    sb.append("}else if(value == \"STSNTT\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("if($(tr).hasClass(\"tpNONE\") || $(tr).hasClass(\"TrgTracked\")){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}else{");
    sb.append("if($(\"input[name='CKB']\").is(':checked')){");
    sb.append("$(tr).hide();");
    sb.append("}else{");
    sb.append("$(tr).show();");
    sb.append("}");
    sb.append("}");
    sb.append("}");
    sb.append("}else if(value == \"SNTTS\"){");
    sb.append("var trs = document.getElementsByTagName(\"tr\");");
    sb.append("for(i=3;i<trs.length;i++){");
    sb.append("var tr = trs[i];");
    sb.append("if(!$(tr).hasClass(\"tpDELETION\")){");
    sb.append("if($(tr).hasClass(\"tpNONE\") && $(tr).hasClass(\"TrgTracked\")){");
    sb.append("$(tr).show();");
    sb.append("}else{");
    sb.append("$(tr).hide();");
    sb.append("}");
    sb.append("}else{");
    sb.append("$(tr).hide();");
    sb.append("}");
    sb.append("}");
    sb.append("}");
    sb.append("});");
    sb.append("\n");
    sb.append("</script>");
    
    sb.append("</body>\n");
    sb.append("</html>\n");
    
    this.htmlreportfortranslation = (new File(this.prjfolder) + File.separator + new File(this.sourcefile).getName().substring(0, new File(this.sourcefile).getName().lastIndexOf(".")) + "_aligned.html");
    this.htmlreportfortranslation_temp = (new File(this.prjfolder) + File.separator + new File(this.sourcefile).getName().substring(0, new File(this.sourcefile).getName().lastIndexOf(".")) + "_aligned.temp");
    if (new File(this.htmlreportfortranslation).exists()) {
      new File(this.htmlreportfortranslation).delete();
    }
    org.jsoup.nodes.Document doc = Jsoup.parse(sb.toString());
    Writer logWriter = new OutputStreamWriter(new FileOutputStream(this.htmlreportfortranslation), "UTF-8");
    logWriter.write(doc.toString().replace("&nbsp;", " "));
    
    Writer logWriter_temp = new OutputStreamWriter(new FileOutputStream(this.htmlreportfortranslation_temp), "UTF-8");
    logWriter_temp.write(doc.toString());
    
    logWriter.close();
    logWriter_temp.close();
  }
  
  public void collectFilesForProjectFile()
    throws Exception
  {
    this.exportfolder = (this.prjfolder + File.separator + "export");
    File export = new File(this.exportfolder);
    if (export.exists()) {
      FileUtils.cleanDirectory(export);
    } else {
      export.mkdirs();
    }
    FileUtils.copyFileToDirectory(new File(this.backupsourcefile), export);
    FileUtils.copyFileToDirectory(new File(this.targetfile), export);
    FileUtils.copyFileToDirectory(new File(this.alignedfile), export);
    FileUtils.copyFileToDirectory(new File(this.prjinfofile), export);
  }
  
  public void collectFilesForTranslationKit(String prjid)
    throws Exception
  {
    this.exportfolder = (this.prjfolder + File.separator + "export");
    File export = new File(this.exportfolder);
    if (export.exists()) {
      FileUtils.cleanDirectory(export);
    } else {
      export.mkdirs();
    }
    FileUtils.copyFileToDirectory(new File(createglpfile(prjid)), export);
    FileUtils.copyFileToDirectory(new File(this.htmlreportfortranslation), export);
  }
  
  private String createglpfile(String prjid)
    throws Exception
  {
    this.tempfolder = (this.prjfolder + File.separator + "temp");
    File temp = new File(this.tempfolder);
    if (temp.exists()) {
      FileUtils.cleanDirectory(temp);
    } else {
      temp.mkdirs();
    }
    InputStream contentfile = FileAligner.class.getClassLoader().getResourceAsStream("content.xml");
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
    org.w3c.dom.Document document = builder.parse(contentfile);
    org.w3c.dom.Element root = document.getDocumentElement();
    org.w3c.dom.Node sourcelang = root.getElementsByTagName("sourceLanguage").item(0);
    sourcelang.getAttributes().getNamedItem("localeCode").setTextContent(this.sourcelanguage);
    org.w3c.dom.Node targetlang = root.getElementsByTagName("targetLanguage").item(0);
    targetlang.getAttributes().getNamedItem("localeCode").setTextContent(this.targetlanguage);
    org.w3c.dom.Node xliffpath = root.getElementsByTagName("__xliffFile").item(0);
    xliffpath.getAttributes().getNamedItem("archivePath").setTextContent(this.targetlanguage + "/txlf/" + new File(this.populatedsourcetxlf).getName());
    org.w3c.dom.Node sourcepath = root.getElementsByTagName("__storable").item(0);
    sourcepath.getAttributes().getNamedItem("archivePath").setTextContent("source/" + new File(this.backupsourcefile).getName());
    org.w3c.dom.Node projectname = root.getElementsByTagName("projectName").item(0);
    projectname.setTextContent(prjid);
    
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer m = tf.newTransformer();
    DOMSource source = new DOMSource(document);
    File dest = new File(temp + File.separator + "content.xml");
    StreamResult result = new StreamResult(new FileOutputStream(dest));
    m.transform(source, result);
    
    File sourcefolder = new File(temp + File.separator + "source");
    sourcefolder.mkdir();
    FileUtils.copyFileToDirectory(new File(this.backupsourcefile), sourcefolder);
    
    File targetlangfolder = new File(temp + File.separator + this.targetlanguage);
    targetlangfolder.mkdir();
    File txlffolder = new File(targetlangfolder + File.separator + "txlf");
    txlffolder.mkdir();
    FileUtils.copyFileToDirectory(new File(this.populatedsourcetxlf), txlffolder);
    
    ZipFile zf = new ZipFile();
    String glpfile = this.tempfolder + File.separator + prjid + ".glp";
    zf.ZipIt(glpfile, this.tempfolder);
    
    contentfile.close();
    result.getOutputStream().close();
    
    return glpfile;
  }
  
  public void collectFilesForFinalReview()
    throws Exception
  {
    this.exportfolder = (this.prjfolder + File.separator + "export");
    File export = new File(this.exportfolder);
    if (export.exists()) {
      FileUtils.cleanDirectory(export);
    } else {
      export.mkdirs();
    }
    FileUtils.copyFileToDirectory(new File(this.aligneddoc), export);
    FileUtils.copyFileToDirectory(new File(this.translatedtxlf), export);
    FileUtils.copyFileToDirectory(new File(this.htmlreportforfinalreview), export);
    if (new File(this.comparedoc).exists()) {
      FileUtils.copyFileToDirectory(new File(this.comparedoc), export);
    }
    String reformattedtargetfolder = this.exportfolder + File.separator + "org_target";
    if (!new File(reformattedtargetfolder).exists()) {
      new File(reformattedtargetfolder).mkdir();
    }
    FileUtils.copyFileToDirectory(new File(this.reformattedtargetfile), new File(reformattedtargetfolder));
  }
  
  public void convertSourceToTxlf(boolean segmentParagraph)
    throws Exception
  {
    String orgtxlf = convertFileToTxlf(this.sourcefile, segmentParagraph, this.sourcelanguage);
    String newtxlf;
    if (segmentParagraph)
    {
      newtxlf = this.sourcefile + ".nonSeg.txlf";
      this.sourcetxlf_nonSeg = newtxlf;
    }
    else
    {
      newtxlf = this.sourcefile + ".Seg.txlf";
      this.sourcetxlf_seg = newtxlf;
    }
    if (new File(newtxlf).exists()) {
      new File(newtxlf).delete();
    }
    new File(orgtxlf).renameTo(new File(newtxlf));
    
    org.dom4j.Document document = XmlParser.parseXmlFile(newtxlf);
    org.dom4j.Element root = document.getRootElement();
    root.element("file").addAttribute("target-language", this.targetlanguage);
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(newtxlf)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public void convertReformattedSourceToTxlf(boolean segmentParagraph)
    throws Exception
  {
    String orgtxlf = convertFileToTxlf(this.reformattedsourcefile, segmentParagraph, this.sourcelanguage);
    String ext = orgtxlf.substring(orgtxlf.lastIndexOf('.'), orgtxlf.length());
    String newtxlf;
    if (segmentParagraph)
    {
      newtxlf = this.reformattedsourcefile + ".nonSeg.txlf";
      this.reformattedsourcetxlf_nonSeg = newtxlf;
    }
    else
    {
      newtxlf = this.reformattedsourcefile + ".Seg.txlf";
      this.reformattedsourcetxlf_seg = newtxlf;
    }
    if (new File(newtxlf).exists()) {
      new File(newtxlf).delete();
    }
    new File(orgtxlf).renameTo(new File(newtxlf));
    
    org.dom4j.Document document = XmlParser.parseXmlFile(newtxlf);
    org.dom4j.Element root = document.getRootElement();
    root.element("file").addAttribute("target-language", this.targetlanguage);
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(newtxlf)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public void convertTargetToTxlf(boolean segmentParagraph)
    throws Exception
  {
    String orgtxlf = convertFileToTxlf(this.targetfile, segmentParagraph, this.targetlanguage);
    String ext = orgtxlf.substring(orgtxlf.lastIndexOf('.'), orgtxlf.length());
    String newtxlf;
    if (segmentParagraph)
    {
      newtxlf = this.targetfile + ".nonSeg.txlf";
      this.targettxlf_nonSeg = newtxlf;
    }
    else
    {
      newtxlf = this.targetfile + ".Seg.txlf";
      this.targettxlf_seg = newtxlf;
    }
    if (new File(newtxlf).exists()) {
      new File(newtxlf).delete();
    }
    new File(orgtxlf).renameTo(new File(newtxlf));
    
    org.dom4j.Document document = XmlParser.parseXmlFile(newtxlf);
    org.dom4j.Element root = document.getRootElement();
    root.element("file").addAttribute("target-language", this.targetlanguage);
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(newtxlf)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public void convertReformattedTargetToTxlf(boolean segmentParagraph)
    throws Exception
  {
    String orgtxlf = convertFileToTxlf(this.reformattedtargetfile, segmentParagraph, this.targetlanguage);
    String ext = orgtxlf.substring(orgtxlf.lastIndexOf('.'), orgtxlf.length());
    String newtxlf;
    if (segmentParagraph)
    {
      newtxlf = this.reformattedtargetfile + ".nonSeg.txlf";
      this.reformattedtargettxlf_nonSeg = newtxlf;
    }
    else
    {
      newtxlf = this.reformattedtargetfile + ".seg.txlf";
      this.reformattedtargettxlf_seg = newtxlf;
    }
    if (new File(newtxlf).exists()) {
      new File(newtxlf).delete();
    }
    new File(orgtxlf).renameTo(new File(newtxlf));
    
    org.dom4j.Document document = XmlParser.parseXmlFile(newtxlf);
    org.dom4j.Element root = document.getRootElement();
    root.element("file").addAttribute("target-language", this.targetlanguage);
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(newtxlf)), "UTF8");
    document.write(writer);
    writer.close();
  }
  
  public String convertFileToTxlf(String filePath, boolean segmentParagraph, String sourcelanguage)
    throws Exception
  {
    com.aspose.words.Document doc = new com.aspose.words.Document(filePath);
    doc.joinRunsWithSameFormatting();
    doc.save(filePath);
    
    ArrayList<String> srcs = new ArrayList();
    srcs.add(filePath);
    
    String orgtxlfname = filePath + ".txlf";
    if (new File(orgtxlfname).exists()) {
      new File(orgtxlfname).delete();
    }
    Locale locale = Locale.makeLocale(sourcelanguage);
    Configuration config = new BaseConfiguration();
    config.setProperty("word.acceptTrackChanges", "true");
    config.setProperty("word.extractDropDownList", "false");
    config.setProperty("word.extractEquations", "false");
    config.setProperty("word.extractComments", "false");
    config.setProperty("extraction.tokens.extract", "all");
    config.setProperty("word.translateHyperlinkText", "true");
    config.setProperty("word.translateHyperlinkValue", "false");
    config.setProperty("word.ignoreBiLingualStyles", "true");
    ConvertDOC converter = new ConvertDOC();
    converter.setConfiguration(config);
    converter.setIgnoreSuccessfullConversion(true);
    
    converter.convert(srcs, locale);
    
    segmentTxlf(orgtxlfname, segmentParagraph, sourcelanguage);
    
    return orgtxlfname;
  }
  
  public boolean verifyParas()
    throws Exception
  {
    System.out.println("verifying paragraphs mapping....");
    boolean isValid = false;
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    org.dom4j.Document document_source = XmlParser.parseXmlFile(this.sourcetxlf_nonSeg);
    org.dom4j.Element root_source = document_source.getRootElement();
    
    org.dom4j.Document document_formatted = XmlParser.parseXmlFile(this.reformattedsourcetxlf_nonSeg);
    org.dom4j.Element root_formatted = document_formatted.getRootElement();
    
    List list_source = root_source.selectNodes("//*[name() = 'trans-unit']");
    int numberOfPara_source = list_source.size();
    
    List list_formatted = root_formatted.selectNodes("//*[name() = 'trans-unit']");
    int numberOfPara_formatted = list_formatted.size();
    
    List<org.dom4j.Element> text_source = new ArrayList();
    Iterator iter_source = list_source.iterator();
    while (iter_source.hasNext())
    {
      org.dom4j.Element source = ((org.dom4j.Element)iter_source.next()).element("source");
      text_source.add(source);
    }
    List<org.dom4j.Element> text_formatted = new ArrayList();
    Iterator iter_formatted = list_formatted.iterator();
    while (iter_formatted.hasNext())
    {
      org.dom4j.Element source = ((org.dom4j.Element)iter_formatted.next()).element("source");
      text_formatted.add(source);
    }
    Workbook wb = new Workbook();
    Worksheet ws = wb.getWorksheets().get(0);
    Cells cells = ws.getCells();
    int count = Math.max(text_source.size(), text_formatted.size());
    int t_count = 0;
    for (int i = 0; i < count; i++)
    {
      String t_src = "";
      String t_fom = "";
      if (i < text_source.size())
      {
        org.dom4j.Element src = (org.dom4j.Element)text_source.get(i);
        for (int j = 0; j < src.content().size(); j++) {
          if ((src.content().get(j) instanceof org.dom4j.Text))
          {
            t_src = t_src + ((org.dom4j.Text)src.content().get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
          }
          else if ((src.content().get(j) instanceof org.dom4j.Element))
          {
            org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
            if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab"))) {
              t_src = t_src + " ";
            } else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb"))) {
              t_src = t_src + "<br> ";
            }
          }
        }
      }
      cells.get(i, 0).setHtmlString("<html>" + t_src.trim().replace("<br> ", "&#8629;<br>") + "</html>");
      if (i < text_formatted.size())
      {
        org.dom4j.Element src = (org.dom4j.Element)text_formatted.get(i);
        
        ArrayList<String> node_ids = new ArrayList();
        for (int j = 0; j < src.content().size(); j++) {
          if ((src.content().get(j) instanceof org.dom4j.Text))
          {
            t_fom = t_fom + ((org.dom4j.Text)src.content().get(j)).getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
          }
          else if ((src.content().get(j) instanceof org.dom4j.Element))
          {
            org.dom4j.Element e = (org.dom4j.Element)src.content().get(j);
            if ((e.getName().equals("bx")) && (e.attribute("ctype").getValue().equals("x-strike-through")))
            {
              t_fom = t_fom + "<strike>";
              node_ids.add(e.attribute("rid").getValue());
            }
            else if (e.getName().equals("ex"))
            {
              if (node_ids.contains(e.attribute("rid").getValue()))
              {
                t_fom = t_fom + "</strike>";
                node_ids.remove(e.attribute("rid").getValue());
              }
            }
            else if ((e.getName().equals("bpt")) && (e.attribute("ctype").getValue().equals("x-underlined")) && (e.getText().contains("type=\"1\"")))
            {
              t_fom = t_fom + "<u>";
              node_ids.add(e.attribute("rid").getValue());
            }
            else if (e.getName().equals("ept"))
            {
              if (node_ids.contains(e.attribute("rid").getValue()))
              {
                t_fom = t_fom + "</u>";
                node_ids.remove(e.attribute("rid").getValue());
              }
            }
            else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("x-tab")))
            {
              t_fom = t_fom + " ";
            }
            else if ((e.getName().equals("x")) && (e.attribute("ctype").getValue().equals("lb")))
            {
              t_fom = t_fom + "<br> ";
            }
          }
        }
        if (!t_fom.contains("<u>"))
        {
          if (src.selectNodes("..//*[name() = 'it'][@ctype = 'x-underlined'][@pos = 'open']").size() != 0)
          {
            org.dom4j.Node node = (org.dom4j.Node)src.selectNodes("..//*[name() = 'it'][@ctype = 'x-underlined'][@pos = 'open']").get(0);
            if (node.getText().contains("type=\"1\"")) {
              t_fom = "<u>" + t_fom + "</u>";
            }
          }
        }
        else if ((!t_fom.contains("<strike>")) && 
          (src.selectNodes("..//*[name() = 'it'][@ctype = 'x-strike-through'][@pos = 'open']").size() != 0)) {
          t_fom = "<strike>" + t_fom + "</strike>";
        }
      }
      String accepted_t_fom = t_fom.replaceAll("(?s)<strike>.*?</strike>", "").replace("<u>", "").replace("</u>", "").replace("&amp;paradel;", "").replace("&amp;parains;", "").replace("&amp;hf;", "").replace("<br>", "");
      if (extractionSupportImpl.isExtractable(accepted_t_fom))
      {
        String input = "<html>" + t_fom.replace("&amp;hf;", "").replace("&amp;parains;", "").replace("<br> ", "&#8629;<br>").replace("&amp;paradel;", "<strike>&para;</strike><br>") + "</html>";
        cells.get(t_count, 1).setHtmlString(input);
        t_count++;
      }
    }
    wb.save(this.prjfolder + File.separator + "verifyParas.xlsx");
    if (numberOfPara_source == t_count)
    {
      System.out.println("result: TRUE source: " + numberOfPara_source + " formatted: " + t_count);
      isValid = true;
    }
    else
    {
      System.out.println("result: false source: " + numberOfPara_source + " formatted: " + t_count);
    }
    return isValid;
  }
  
  public void segmentTxlf(String txlf, boolean segmentParagraph, String srcLangcode)
    throws Exception
  {
    ArrayList<String> txlfs = new ArrayList();
    txlfs.add(txlf);
    Locale sourceLocale = Locale.makeLocale(srcLangcode);
    Configuration config = createConfigForSegmenter(segmentParagraph, srcLangcode);
    
    XliffSegmenter segmenter = new XliffSegmenter(sourceLocale);
    segmenter.setConfiguration(config);
    
    segmenter.segment(txlfs);
  }
  
  private Configuration createConfigForSegmenter(boolean segmentParagraph, String srcLangcode)
    throws Exception
  {
    Configuration config = new BaseConfiguration();
    config.setProperty("segmenter.default.paragraphsegmenter", Boolean.valueOf(segmentParagraph));
    config.setProperty("segmenter.strategy.trados.rule1enabled", "true");
    

    config.setProperty("segmenter.default.strategy", "trados");
    config.setProperty("segmenter.default.simplifier", "fontformat");
    config.setProperty("breakiterator.strategy.sentence.trados.esps", createESMString(Locale.makeLocale(srcLangcode)));
    

    String abbv = getAbbreviations(srcLangcode);
    if (!abbv.equals("")) {
      config.setProperty("abbreviations." + srcLangcode.split("-")[0], abbv);
    }
    return config;
  }
  
  private String getAbbreviations(String srcLangcode)
    throws Exception
  {
    String abbv_str = "";
    String key = "abbreviations." + srcLangcode.split("-")[0];
    InputStream langcodeFile = ProjectManager.class.getClassLoader().getResourceAsStream("abbreviations.properties");
    
    InputStreamReader inReader = new InputStreamReader(langcodeFile);
    BufferedReader readbuffer = new BufferedReader(inReader);
    String line;
    while ((line = readbuffer.readLine()) != null) {
      if (line.startsWith(key)) {
        abbv_str = line.replace(key, "");
      }
    }
    readbuffer.close();
    inReader.close();
    
    return abbv_str;
  }
  
  private int calculateSimilarityScore(String oldtext, String newtext)
  {
    DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1, 1, 1, 1);
    double metrics = dla.execute(oldtext, newtext);
    double maxlength = Math.max(oldtext.length(), newtext.length());
    int result = (int)((maxlength - metrics) / maxlength * 100.0D);
    




    return result;
  }
  
  public ArrayList<String[]> populateSourceTxlf()
    throws Exception
  {
    System.out.println("populating source txlf with aligned segments....");
    
    ArrayList<String[]> reportStates = new ArrayList();
    
    ExtractionSupportImpl extractionSupportImpl = new ExtractionSupportImpl(Locale.makeLocale(this.sourcelanguage), Locale.makeLocale(this.targetlanguage));
    Configuration config = new BaseConfiguration();
    config.setProperty("extraction.tokens.extract", "all");
    extractionSupportImpl.setConfiguration(config);
    
    Locale locale = Locale.makeLocale(this.sourcelanguage);
    TradosWordCounter wcounter = new TradosWordCounter(locale, config);
    
    org.dom4j.Document document_src = XmlParser.parseXmlFile(this.sourcetxlf_nonSeg);
    org.dom4j.Element root_src = document_src.getRootElement();
    
    org.dom4j.Document document_src_ingt = XmlParser.parseXmlFile(this.sourcetxlf_nonSeg);
    org.dom4j.Element root_src_ingt = document_src_ingt.getRootElement();
    
    org.dom4j.Document document_src_seg = XmlParser.parseXmlFile(this.sourcetxlf_seg);
    org.dom4j.Element root_src_seg = document_src_seg.getRootElement();
    
    List<com.aspose.words.Node> list_source = root_src.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    List<com.aspose.words.Node> list_source_ingt = root_src_ingt.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    List<com.aspose.words.Node> list_source_seg = root_src_seg.selectNodes("//*[name() = 'group'][@restype = 'x-paragraph']");
    int count = 0;
    int totalWC = 0;
    
    org.dom4j.Document document = XmlParser.parseXmlFile(this.alignedfile);
    List<org.dom4j.Element> groups = document.getRootElement().element("aligned").elements("group");
    for (int i = 0; i < groups.size(); i++)
    {
      org.dom4j.Element group = (org.dom4j.Element)groups.get(i);
      List<org.dom4j.Element> units = group.elements("unit");
      if (((org.dom4j.Element)units.get(0)).element("src_para") != null)
      {
        boolean isParaAllSegmented = true;
        for (int j = 0; j < units.size(); j++) {
          if (((org.dom4j.Element)units.get(j)).attributeValue("alignsegs").equals("false"))
          {
            isParaAllSegmented = false;
            break;
          }
        }
        String srcTextAccepted = group.elementText("text").replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "");
        if (!extractionSupportImpl.isExtractable(srcTextAccepted))
        {
          if (isParaAllSegmented)
          {
            for (int j = 0; j < units.size(); j++)
            {
              org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
              List<org.dom4j.Element> srcsegs = unit.element("src_para").element("segments").elements("src_seg");
              List<org.dom4j.Element> trgsegs = unit.element("trg_para").element("segments").elements("trg_seg");
              for (int x = 0; x < srcsegs.size(); x++)
              {
                String[] s = new String[7];
                s[0] = ((org.dom4j.Element)srcsegs.get(x)).getText();
                if (x >= trgsegs.size())
                {
                  s[1] = "";
                }
                else
                {
                  org.dom4j.Element trgseg = (org.dom4j.Element)trgsegs.get(x);
                  String id = trgseg.attributeValue("id");
                  if (id.startsWith("n - "))
                  {
                    s[1] = trgseg.getText();
                  }
                  else
                  {
                    List tmp_contents = new ArrayList();
                    if (id.contains(" - "))
                    {
                      int start = Integer.parseInt(id.split(" - ")[0]);
                      int end = Integer.parseInt(id.split(" - ")[1]);
                      tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(start)));
                      for (int su = start + 1; su <= end; su++)
                      {
                        boolean isprevendofpara = ((boolean[])this.txlftrgsewsmap.get(Integer.valueOf(su - 1)))[1];
                        boolean iscurrentstartofpara = ((boolean[])this.txlftrgsewsmap.get(Integer.valueOf(su)))[0];
                        if ((isprevendofpara) && (iscurrentstartofpara))
                        {
                          List prevseg = (List)this.txlftrgsegmap.get(Integer.valueOf(su - 1));
                          int previdx = -1;
                          for (int prev = 0; prev < prevseg.size(); prev++)
                          {
                            org.dom4j.Node prevnode = (org.dom4j.Node)prevseg.get(prev);
                            if (prevnode.getNodeType() == 1)
                            {
                              org.dom4j.Element prevnode_e = (org.dom4j.Element)prevnode;
                              if ((prevnode_e.getName().equals("ws")) && (prevnode_e.attributeValue("pos").equals("after"))) {
                                previdx = prevseg.size() - prev;
                              }
                            }
                          }
                          if (previdx != -1) {
                            tmp_contents.remove(tmp_contents.size() - previdx);
                          }
                          List currseg = (List)this.txlftrgsegmap.get(Integer.valueOf(su));
                          int curridx = -1;
                          for (int curr = 0; curr < currseg.size(); curr++)
                          {
                            org.dom4j.Node currnode = (org.dom4j.Node)currseg.get(curr);
                            if (currnode.getNodeType() == 1)
                            {
                              org.dom4j.Element currnode_e = (org.dom4j.Element)currnode;
                              if ((currnode_e.getName().equals("ws")) && (currnode_e.attributeValue("pos").equals("before"))) {
                                curridx = curr;
                              }
                            }
                          }
                          if (curridx != -1) {
                            currseg.remove(curridx);
                          }
                          if (Locale.makeLocale(this.targetlanguage).isFarEast())
                          {
                            tmp_contents.addAll(currseg);
                          }
                          else
                          {
                            tmp_contents.add(DocumentHelper.createText(" "));
                            tmp_contents.addAll(currseg);
                          }
                        }
                        else
                        {
                          tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(su)));
                        }
                      }
                    }
                    else
                    {
                      tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(Integer.parseInt(id))));
                    }
                    s[1] = trimText(assembleText(tmp_contents).replace("<br> ", "&#8629;<br>"), false)[0];
                  }
                }
                s[2] = "N/A";
                s[3] = "N/A";
                s[4] = ((org.dom4j.Element)srcsegs.get(x)).attributeValue("tctype");
                s[5] = "0";
                s[6] = "";
                reportStates.add(s);
              }
            }
          }
          else
          {
            String[] s = new String[7];
            for (int j = 0; j < units.size(); j++)
            {
              s[0] = ((org.dom4j.Element)units.get(j)).element("src_para").elementText("text");
              if (((org.dom4j.Element)units.get(j)).element("trg_para") != null) {
                s[1] = ((org.dom4j.Element)units.get(j)).element("trg_para").elementText("text");
              } else {
                s[1] = "";
              }
              s[2] = "N/A";
              s[3] = "N/A";
              s[4] = ((org.dom4j.Element)units.get(j)).element("src_para").attributeValue("tctype");
              s[5] = "0";
              s[6] = "";
              reportStates.add(s);
            }
          }
        }
        else
        {
          if (isParaAllSegmented)
          {
            org.dom4j.Element txlf_group = (org.dom4j.Element)list_source.get(count);
            org.dom4j.Element txlf_group_ingt = (org.dom4j.Element)list_source_ingt.get(count);
            org.dom4j.Element txlf_group_seg = (org.dom4j.Element)list_source_seg.get(count);
            txlf_group.setContent(txlf_group_seg.content());
            List transunits = txlf_group.elements("trans-unit");
            
            txlf_group_ingt.setContent(txlf_group_seg.content());
            List transunits_ingt = txlf_group_ingt.elements("trans-unit");
            
            ArrayList<String> mergedsegtext = new ArrayList();
            ArrayList<List> merged_trg_contents = new ArrayList();
            ArrayList<String> mergedsegtctypes = new ArrayList();
            
            ArrayList<String> keys = new ArrayList();
            ArrayList<String> key_left = new ArrayList();
            ArrayList<String> key_right = new ArrayList();
            ArrayList<String> org_keys = new ArrayList();
            ArrayList<String> trg_keys = new ArrayList();
            ArrayList<List> trg_contents = new ArrayList();
            ArrayList<String> src_tctypes = new ArrayList();
            ArrayList<String> src_review_stats = new ArrayList();
            ArrayList<String> src_ignore_stats = new ArrayList();
            ArrayList<Integer> edited_idx = new ArrayList();
            for (int j = 0; j < units.size(); j++)
            {
              org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
              org.dom4j.Element src_para = unit.element("src_para");
              org.dom4j.Element trg_para = unit.element("trg_para");
              List src_segs = src_para.element("segments").elements("src_seg");
              for (int z = 0; z < src_segs.size(); z++)
              {
                org.dom4j.Element src_seg = (org.dom4j.Element)src_segs.get(z);
                src_tctypes.add(src_seg.attributeValue("tctype"));
                src_review_stats.add(src_seg.attributeValue("needreview"));
                src_ignore_stats.add(src_seg.attributeValue("ignored"));
                keys.add(src_seg.getText().replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "").replace("<br>", "").trim());
                org_keys.add(src_seg.getText());
                if (trg_para != null)
                {
                  List trg_segs = trg_para.element("segments").elements("trg_seg");
                  if (((org.dom4j.Element)trg_segs.get(z)).attributeValue("edited").equals("true")) {
                    edited_idx.add(Integer.valueOf(trg_contents.size()));
                  }
                  if (trg_segs.size() > z)
                  {
                    trg_keys.add(((org.dom4j.Element)trg_segs.get(z)).getText());
                    String id = ((org.dom4j.Element)trg_segs.get(z)).attributeValue("id");
                    if (id.startsWith("n - "))
                    {
                      trg_contents.add(new ArrayList());
                    }
                    else
                    {
                      List tmp_contents = new ArrayList();
                      if (id.contains(" - "))
                      {
                        int start = Integer.parseInt(id.split(" - ")[0]);
                        int end = Integer.parseInt(id.split(" - ")[1]);
                        tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(start)));
                        for (int su = start + 1; su <= end; su++)
                        {
                          boolean isprevendofpara = ((boolean[])this.txlftrgsewsmap.get(Integer.valueOf(su - 1)))[1];
                          boolean iscurrentstartofpara = ((boolean[])this.txlftrgsewsmap.get(Integer.valueOf(su)))[0];
                          if ((isprevendofpara) && (iscurrentstartofpara))
                          {
                            List prevseg = (List)this.txlftrgsegmap.get(Integer.valueOf(su - 1));
                            int previdx = -1;
                            for (int prev = 0; prev < prevseg.size(); prev++)
                            {
                              org.dom4j.Node prevnode = (org.dom4j.Node)prevseg.get(prev);
                              if (prevnode.getNodeType() == 1)
                              {
                                org.dom4j.Element prevnode_e = (org.dom4j.Element)prevnode;
                                if ((prevnode_e.getName().equals("ws")) && (prevnode_e.attributeValue("pos").equals("after"))) {
                                  previdx = prevseg.size() - prev;
                                }
                              }
                            }
                            if (previdx != -1) {
                              tmp_contents.remove(tmp_contents.size() - previdx);
                            }
                            List currseg = (List)this.txlftrgsegmap.get(Integer.valueOf(su));
                            int curridx = -1;
                            for (int curr = 0; curr < currseg.size(); curr++)
                            {
                              org.dom4j.Node currnode = (org.dom4j.Node)currseg.get(curr);
                              if (currnode.getNodeType() == 1)
                              {
                                org.dom4j.Element currnode_e = (org.dom4j.Element)currnode;
                                if ((currnode_e.getName().equals("ws")) && (currnode_e.attributeValue("pos").equals("before"))) {
                                  curridx = curr;
                                }
                              }
                            }
                            if (curridx != -1) {
                              currseg.remove(curridx);
                            }
                            if (Locale.makeLocale(this.targetlanguage).isFarEast())
                            {
                              tmp_contents.addAll(currseg);
                            }
                            else
                            {
                              tmp_contents.add(DocumentHelper.createText(" "));
                              tmp_contents.addAll(currseg);
                            }
                          }
                          else
                          {
                            tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(su)));
                          }
                        }
                      }
                      else
                      {
                        tmp_contents.addAll((Collection)this.txlftrgsegmap.get(Integer.valueOf(Integer.parseInt(id))));
                      }
                      trg_contents.add(tmp_contents);
                    }
                  }
                  else
                  {
                    trg_keys.add("");
                    trg_contents.add(new ArrayList());
                  }
                }
                else
                {
                  trg_keys.add("");
                  trg_contents.add(new ArrayList());
                }
                if ((z == 0) && (z == src_segs.size() - 1))
                {
                  key_left.add(src_para.attributeValue("lefttrim"));
                  key_right.add(src_para.attributeValue("righttrim"));
                }
                else if (z == 0)
                {
                  key_left.add(src_para.attributeValue("lefttrim"));
                  key_right.add("true");
                }
                else if (z == src_segs.size() - 1)
                {
                  key_left.add("true");
                  key_right.add(src_para.attributeValue("righttrim"));
                }
                else
                {
                  key_left.add("true");
                  key_right.add("true");
                }
              }
            }
            SegmenterFactory factory = new SegmenterFactory();
            Configuration segconfig = createConfigForSegmenter(false, this.sourcelanguage);
            Segmenter segmenter = factory.getSegmenter("trados", Locale.makeLocale(this.sourcelanguage), segconfig);
            List<String> finsegs = segmenter.segment(group.elementText("text").replaceAll("(?s)<del>.*?</del>", "").replaceAll("<(/)*ins>", "").replace("<br>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"));
            ArrayList<ArrayList<Integer>> indices = new ArrayList();
            int key_start_index = 0;
            for (int k = 0; k < finsegs.size(); k++)
            {
              String finsegtext = ((String)finsegs.get(k)).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
              
              String combined_key = "";
              ArrayList<Integer> indice = new ArrayList();
              for (int x = key_start_index; x < keys.size(); x++)
              {
                combined_key = combined_key + (String)keys.get(x);
                



                indice.add(Integer.valueOf(x));
                if (combined_key.replace(" ", " ").trim().replaceAll("(\\s)+", "").equals(finsegtext.replace(" ", " ").trim().replaceAll("(\\s)+", "")))
                {
                  indices.add(indice);
                  key_start_index = x + 1;
                  break;
                }
              }
            }
            ArrayList<Integer> merged_edited_idx = new ArrayList();
            ArrayList<String[]> statss = new ArrayList();
            for (int m = 0; m < indices.size(); m++)
            {
              boolean iscontentsuseable = true;
              ArrayList<Integer> temp_indice = (ArrayList)indices.get(m);
              String temp_src = "";
              String temp_org_src = "";
              String temp_trg = "";
              List temp_trg_content = new ArrayList();
              int id = 1;int rid = 1;int bxrid = 1;int bptrid = 1;int bxid = 1;int bptid = 1;
              HashMap<String, String> map_rid = new HashMap();
              String temp_tctype = (String)src_tctypes.get(((Integer)temp_indice.get(0)).intValue());
              String temp_review_stats = (String)src_review_stats.get(((Integer)temp_indice.get(0)).intValue());
              for (Iterator localIterator = temp_indice.iterator(); localIterator.hasNext();)
              {
                int it = ((Integer)localIterator.next()).intValue();
                temp_tctype = temp_tctype.equals(src_tctypes.get(it)) ? temp_tctype : "MIX";
                temp_review_stats = ((String)src_review_stats.get(it)).equals("true") ? "true" : temp_review_stats.equals("true") ? "true" : "false";
                String temp_ignore_stats = (String)src_ignore_stats.get(it);
                if (edited_idx.contains(Integer.valueOf(it))) {
                  iscontentsuseable = false;
                }
                temp_src = temp_src + (String)keys.get(it);
                temp_org_src = temp_org_src + (String)org_keys.get(it);
                if (temp_ignore_stats.equals("true"))
                {
                  temp_trg = temp_trg + "[skipseg]";
                  temp_trg_content.add(DocumentHelper.createText("[skipseg]"));
                }
                else
                {
                  temp_trg = temp_trg + (String)trg_keys.get(it);
                  
                  List trg_content = (List)trg_contents.get(it);
                  for (int nc = 0; nc < trg_content.size(); nc++)
                  {
                    org.dom4j.Node raw = (org.dom4j.Node)trg_content.get(nc);
                    if (raw.getNodeType() == 3)
                    {
                      temp_trg_content.add(raw);
                    }
                    else if (raw.getNodeType() == 1)
                    {
                      org.dom4j.Element rawe = (org.dom4j.Element)raw;
                      if (rawe.getName().equals("source"))
                      {
                        for (int ncc = 0; ncc < rawe.content().size(); ncc++)
                        {
                          org.dom4j.Node node = (org.dom4j.Node)rawe.content().get(ncc);
                          if (node.getNodeType() == 3)
                          {
                            temp_trg_content.add(node);
                          }
                          else if (node.getNodeType() == 1)
                          {
                            org.dom4j.Element e = (org.dom4j.Element)node;
                            if (!e.getName().equals("x")) {
                              if (!e.getName().equals("ph")) {
                                if (e.getName().equals("bx"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != 0)) {
                                    continue;
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                else if (e.getName().equals("ex"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != temp_indice.size() - 1)) {
                                    continue;
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                else if (e.getName().equals("bpt"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != 0)) {
                                    continue;
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                else if (e.getName().equals("ept"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != temp_indice.size() - 1)) {
                                    continue;
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                              }
                            }
                            if (e.attribute("fake") != null) {
                              e.remove(e.attribute("fake"));
                            }
                            temp_trg_content.add(e);
                          }
                        }
                      }
                      else if (rawe.getName().equals("ws"))
                      {
                        String pos = rawe.attributeValue("pos");
                        if (pos.equals("before")) {
                          for (int ncc = 0; ncc < rawe.content().size(); ncc++)
                          {
                            org.dom4j.Node node = (org.dom4j.Node)rawe.content().get(ncc);
                            if (node.getNodeType() == 3)
                            {
                              temp_trg_content.add(0, node);
                            }
                            else if (node.getNodeType() == 1)
                            {
                              org.dom4j.Element e = (org.dom4j.Element)node;
                              if ((!e.getName().equals("x")) && 
                                (e.getName().equals("it")))
                              {
                                if (e.attributeValue("pos").equals("open"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != 0)) {
                                    continue;
                                  }
                                  if (e.getText().equals("")) {
                                    e.setName("bx");
                                  } else {
                                    e.setName("bpt");
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                else if (e.attributeValue("pos").equals("close"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != temp_indice.size() - 1)) {
                                    continue;
                                  }
                                  if (e.getText().equals("")) {
                                    e.setName("ex");
                                  } else {
                                    e.setName("ept");
                                  }
                                  e.remove(e.attribute("ctype"));
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                e.remove(e.attribute("pos"));
                              }
                              else
                              {
                                if (e.attribute("fake") != null) {
                                  e.remove(e.attribute("fake"));
                                }
                                temp_trg_content.add(0, e);
                              }
                            }
                          }
                        } else if (pos.equals("after")) {
                          for (int ncc = 0; ncc < rawe.content().size(); ncc++)
                          {
                            org.dom4j.Node node = (org.dom4j.Node)rawe.content().get(ncc);
                            if (node.getNodeType() == 3)
                            {
                              temp_trg_content.add(node);
                            }
                            else if (node.getNodeType() == 1)
                            {
                              org.dom4j.Element e = (org.dom4j.Element)node;
                              if ((!e.getName().equals("x")) && 
                                (e.getName().equals("it")))
                              {
                                if (e.attributeValue("pos").equals("open"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != 0)) {
                                    continue;
                                  }
                                  if (e.getText().equals("")) {
                                    e.setName("bx");
                                  } else {
                                    e.setName("bpt");
                                  }
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                else if (e.attributeValue("pos").equals("close"))
                                {
                                  if ((e.attribute("fake") != null) && (e.attributeValue("fake").equals("true")) && (temp_indice.indexOf(Integer.valueOf(it)) != temp_indice.size() - 1)) {
                                    continue;
                                  }
                                  if (e.getText().equals("")) {
                                    e.setName("ex");
                                  } else {
                                    e.setName("ept");
                                  }
                                  e.remove(e.attribute("ctype"));
                                  if ((e.attribute("fake") == null) || (!e.attributeValue("fake").equals("true"))) {}
                                }
                                e.remove(e.attribute("pos"));
                              }
                              else
                              {
                                if (e.attribute("fake") != null) {
                                  e.remove(e.attribute("fake"));
                                }
                                temp_trg_content.add(e);
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
              String[] stats = TrackChangeHelper.getTxlfTrgStatsFromTCType(temp_tctype, temp_trg);
              if ((stats[0].equals("1")) && (temp_review_stats.equals("true"))) {
                stats[2] = "fuzzy-match";
              }
              String[] s = new String[7];
              s[0] = temp_org_src.replace("<br> ", "&#8629;<br>");
              if (iscontentsuseable) {
                s[1] = trimText(assembleText(temp_trg_content).replace("<br> ", "&#8629;<br>"), false)[0];
              } else {
                s[1] = temp_trg.replace("<br> ", "&#8629;<br>");
              }
              if (s[1].contains("[skipseg]")) {
                if (s[1].replace("[skipseg]", "").trim().equals(""))
                {
                  s[1] = "";
                  temp_trg_content = new ArrayList();
                  temp_trg_content.add(DocumentHelper.createText(" "));
                  temp_trg = " ";
                  stats[0] = "1";
                  stats[1] = "translated";
                  stats[2] = "exact-match";
                }
                else
                {
                  s[1] = s[1].replace("[skipseg]", "");
                  temp_trg_content = replacetextinDomObj(temp_trg_content);
                  temp_trg = temp_trg.replace("[skipseg]", "");
                }
              }
              s[2] = stats[0];
              s[3] = stats[2];
              s[4] = temp_tctype;
              wcounter = new TradosWordCounter(locale, config);
              wcounter.countText(((org.dom4j.Element)transunits.get(m)).element("source").getText());
              s[5] = Integer.toString(wcounter.getWordCount());
              s[6] = "";
              totalWC += wcounter.getWordCount();
              reportStates.add(s);
              if (extractionSupportImpl.isExtractable(temp_src))
              {
                mergedsegtext.add(temp_trg);
                if (!iscontentsuseable) {
                  merged_edited_idx.add(Integer.valueOf(merged_trg_contents.size()));
                }
                merged_trg_contents.add(temp_trg_content);
                mergedsegtctypes.add(temp_tctype);
                statss.add(stats);
              }
            }
            for (int t = 0; t < transunits.size(); t++)
            {
              org.dom4j.Element trans_unit = (org.dom4j.Element)transunits.get(t);
              org.dom4j.Element trans_unit_ignt = (org.dom4j.Element)transunits_ingt.get(t);
              trans_unit.addAttribute("gs4tr:editStatus", "leveraged");
              org.dom4j.Element source = trans_unit.element("source");
              
              org.dom4j.Element target = trans_unit.addElement("target");
              trans_unit.elements().add(source.indexOf(source.getParent()) + 2, target.clone());
              trans_unit.remove(target);
              target = trans_unit.element("target");
              
              org.dom4j.Element target_ignt = trans_unit_ignt.addElement("target");
              trans_unit_ignt.elements().add(source.indexOf(source.getParent()) + 2, target_ignt.clone());
              trans_unit_ignt.remove(target_ignt);
              target_ignt = trans_unit_ignt.element("target");
              if (merged_edited_idx.contains(Integer.valueOf(t)))
              {
                target.setText(((String)mergedsegtext.get(t)).replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").trim());
                target_ignt.setText(((String)mergedsegtext.get(t)).replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").trim());
              }
              else
              {
                target.setContent(trimContents((List)merged_trg_contents.get(t)));
                target_ignt.setContent(trimContents((List)merged_trg_contents.get(t)));
                if (!((String[])statss.get(t))[0].equals("75"))
                {
                  org.dom4j.Element source_ingt = trans_unit_ignt.element("source");
                  source_ingt.setContent(trimContents((List)merged_trg_contents.get(t)));
                }
              }
              String[] stats = (String[])statss.get(t);
              if (stats[0].equals("1")) {
                trans_unit.addAttribute("gs4tr:locked", "true");
              }
              target.addAttribute("gs4tr:score", stats[0]);
              target.addAttribute("state", stats[1]);
              target.addAttribute("state-qualifier", stats[2]);
              if (stats[0].equals("0")) {
                trans_unit.remove(target);
              }
            }
          }
          else
          {
            String trgtext = "";
            if (((org.dom4j.Element)units.get(0)).element("trg_para") != null) {
              trgtext = ((org.dom4j.Element)units.get(0)).element("trg_para").elementText("text");
            }
            String temp_tctype = ((org.dom4j.Element)units.get(0)).element("src_para").attributeValue("tctype");
            for (int j = 1; j < units.size(); j++)
            {
              org.dom4j.Element prev_unit = (org.dom4j.Element)units.get(j - 1);
              org.dom4j.Element unit = (org.dom4j.Element)units.get(j);
              String src_tctype = unit.element("src_para").attributeValue("tctype");
              temp_tctype = temp_tctype.equals(src_tctype) ? temp_tctype : "MIX";
              if (unit.element("trg_para") != null)
              {
                String Rtrim = prev_unit.element("src_para").attributeValue("righttrim");
                String Ltrim = unit.element("src_para").attributeValue("lefttrim");
                if ((Rtrim.equals("true")) || (Ltrim.equals("true"))) {
                  trgtext = trgtext + " " + unit.element("trg_para").elementText("text");
                } else {
                  trgtext = trgtext + unit.element("trg_para").elementText("text");
                }
              }
            }
            org.dom4j.Element txlf_group = (org.dom4j.Element)list_source.get(count);
            org.dom4j.Element trans_unit = txlf_group.element("trans-unit");
            trans_unit.addAttribute("gs4tr:editStatus", "leveraged");
            org.dom4j.Element source = trans_unit.element("source");
            
            org.dom4j.Element target = trans_unit.addElement("target");
            trans_unit.elements().add(source.indexOf(source.getParent()) + 2, target.clone());
            trans_unit.remove(target);
            target = trans_unit.element("target");
            
            int lb_cnt = 0;
            String surfix = trgtext;
            while (surfix.indexOf("<br> ") != -1)
            {
              lb_cnt++;
              int pos = surfix.indexOf("<br> ");
              String prefix = surfix.substring(0, pos);
              target.addText(prefix.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"));
              org.dom4j.Element x = target.addElement("x");
              x.addAttribute("ctype", "lb");
              x.addAttribute("id", Integer.toString(lb_cnt));
              x.addAttribute("equiv-text", " ");
              surfix = surfix.substring(pos + 5, surfix.length());
            }
            target.addText(surfix.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"));
            String[] stats = TrackChangeHelper.getTxlfTrgStatsFromTCType(temp_tctype, trgtext);
            target.addAttribute("gs4tr:score", stats[0]);
            target.addAttribute("state", stats[1]);
            target.addAttribute("state-qualifier", stats[2]);
            
            String[] s = new String[7];
            s[0] = group.elementText("text").replace("<br> ", "&#8629;<br>");
            s[1] = trgtext.replace("<br> ", "&#8629;<br>");
            s[2] = stats[0];
            s[3] = stats[2];
            s[4] = temp_tctype;
            wcounter = new TradosWordCounter(locale, config);
            wcounter.countText(source.getText());
            s[5] = Integer.toString(wcounter.getWordCount());
            s[6] = "";
            totalWC += wcounter.getWordCount();
            reportStates.add(s);
          }
          count++;
        }
      }
    }
    root_src.element("file").addAttribute("gs4tr:wordcount", Integer.toString(totalWC));
    fixTxlfTrgTags(document_src);
    
    this.populatedsourcetxlf = (this.sourcefile + ".txlf");
    if (new File(this.populatedsourcetxlf).exists()) {
      new File(this.populatedsourcetxlf).delete();
    }
    OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.populatedsourcetxlf)), "UTF8");
    document_src.write(writer);
    writer.close();
    
    removeBlankLinesAndNameSpace(this.populatedsourcetxlf);
    

    root_src_ingt.element("file").addAttribute("gs4tr:wordcount", Integer.toString(totalWC));
    fixTxlfTrgTags(document_src_ingt);
    
    String ingtfile = this.sourcefile + ".ingt.txlf";
    if (new File(ingtfile).exists()) {
      new File(ingtfile).delete();
    }
    OutputStreamWriter writer_ingt = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(ingtfile)), "UTF8");
    document_src_ingt.write(writer_ingt);
    writer_ingt.close();
    
    removeBlankLinesAndNameSpace(ingtfile);
    
    return reportStates;
  }
  
  private void removeBlankLinesAndNameSpace(String file)
    throws Exception
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
    StringBuffer sb = new StringBuffer();
    String line;
    while ((line = in.readLine()) != null) {
      if (!line.trim().equals("")) {
        sb.append(line.replace(" xmlns=\"\"", ""));
      }
    }
    in.close();
    OutputStreamWriter writermp = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF8");
    writermp.write(sb.toString());
    writermp.close();
  }
  
  public void fixTxlfTrgTags(org.dom4j.Document doc)
    throws Exception
  {
    org.dom4j.Element root = doc.getRootElement();
    List<org.dom4j.Element> list_transunit = root.selectNodes("//*[name() = 'trans-unit']");
    for (org.dom4j.Element e : list_transunit)
    {
      org.dom4j.Element source = e.element("source");
      org.dom4j.Element target = e.element("target");
      if ((target != null) && (!target.getText().equals("")))
      {
        HashMap<String, List<Object>> src_map = new HashMap();
        boolean tagnodeappear = false;
        boolean textnodeappear = false;
        boolean sourcetagsurroundtext = true;
        int maxid = 1;
        for (int i = 0; i < source.content().size(); i++)
        {
          org.dom4j.Node node = (org.dom4j.Node)source.content().get(i);
          if (node.getNodeType() == 1)
          {
            org.dom4j.Element tag = (org.dom4j.Element)node;
            maxid = Math.max(maxid, Integer.parseInt(tag.attributeValue("id")));
          }
        }
        maxid++;
        for (int i = 0; i < source.content().size(); i++)
        {
          org.dom4j.Node node = (org.dom4j.Node)source.content().get(i);
          if (node.getNodeType() == 1)
          {
            if (textnodeappear) {
              tagnodeappear = true;
            }
            org.dom4j.Element tag = (org.dom4j.Element)node;
            String key = "";
            if ((tag.getName().equals("ex")) || (tag.getName().equals("ept"))) {
              key = tag.getName() + "#" + tag.attributeValue("rid");
            } else if (tag.getName().equals("x")) {
              key = tag.getName() + "#" + tag.attributeValue("ctype");
            } else {
              key = tag.getName();
            }
            if (src_map.containsKey(key))
            {
              ((List)src_map.get(key)).add(node.clone());
            }
            else
            {
              List<Object> list = new ArrayList();
              list.add(node.clone());
              src_map.put(key, list);
            }
          }
          else
          {
            if (tagnodeappear) {
              sourcetagsurroundtext = false;
            }
            textnodeappear = true;
          }
        }
        HashMap<String, String> rid_map = new HashMap();
        List trg_contents = new ArrayList();
        boolean targetnotag = true;
        for (int i = 0; i < target.content().size(); i++)
        {
          org.dom4j.Node node = (org.dom4j.Node)target.content().get(i);
          if (node.getNodeType() == 1)
          {
            org.dom4j.Element tag = (org.dom4j.Element)node;
            String rid = tag.attributeValue("rid");
            String key = "";
            if ((tag.getName().equals("ph")) || (tag.getName().equals("bpt")) || (tag.getName().equals("bx")))
            {
              key = tag.getName();
              if (src_map.containsKey(key))
              {
                List list = (List)src_map.get(key);
                trg_contents.add(list.get(0));
                targetnotag = false;
                rid_map.put(rid, ((org.dom4j.Element)list.get(0)).attributeValue("rid"));
                list.remove(0);
                if (list.size() == 0) {
                  src_map.remove(key);
                }
              }
            }
            else if (tag.getName().equals("x"))
            {
              key = tag.getName() + "#" + tag.attributeValue("ctype");
              if (src_map.containsKey(key))
              {
                List list = (List)src_map.get(key);
                trg_contents.add(list.get(0));
                targetnotag = false;
                list.remove(0);
                if (list.size() == 0) {
                  src_map.remove(key);
                }
              }
              else if ((tag.getName().equals("x")) && (tag.attributeValue("ctype").equals("x-tab")))
              {
                org.dom4j.Element tab = DocumentHelper.createElement("x");
                tab.addAttribute("ctype", "x-tab");
                tab.addAttribute("id", Integer.toString(maxid));
                maxid++;
                tab.addAttribute("equiv-text", " ");
                trg_contents.add(tab);
              }
              else if ((tag.getName().equals("x")) && (tag.attributeValue("ctype").equals("lb")))
              {
                org.dom4j.Element lb = DocumentHelper.createElement("x");
                lb.addAttribute("ctype", "lb");
                lb.addAttribute("id", Integer.toString(maxid));
                maxid++;
                lb.addAttribute("equiv-text", " ");
                trg_contents.add(lb);
              }
            }
            else if ((tag.getName().equals("ex")) || (tag.getName().equals("ept")))
            {
              String mapped_rid = (String)rid_map.get(tag.attributeValue("rid"));
              key = tag.getName() + "#" + mapped_rid;
              if (src_map.containsKey(key))
              {
                List list = (List)src_map.get(key);
                trg_contents.add(list.get(0));
                rid_map.put(rid, ((org.dom4j.Element)list.get(0)).attributeValue("rid"));
                list.remove(0);
                if (list.size() == 0) {
                  src_map.remove(key);
                }
              }
            }
          }
          else if (node.getNodeType() == 3)
          {
            String text = node.getText();
            if (text.contains("<br> "))
            {
              String key = "x#lb";
              String[] ss = text.split("<br> ");
              for (int s = 0; s < ss.length; s++)
              {
                trg_contents.add(DocumentHelper.createText(ss[s]));
                if (s < ss.length - 1) {
                  if (src_map.containsKey(key))
                  {
                    List list = (List)src_map.get(key);
                    trg_contents.add(list.get(0));
                    targetnotag = false;
                    list.remove(0);
                    if (list.size() == 0) {
                      src_map.remove(key);
                    }
                  }
                  else
                  {
                    org.dom4j.Element lb = DocumentHelper.createElement("x");
                    lb.addAttribute("ctype", "lb");
                    lb.addAttribute("id", Integer.toString(maxid));
                    maxid++;
                    lb.addAttribute("equiv-text", " ");
                    trg_contents.add(lb);
                  }
                }
              }
            }
            else if (text.contains("<br>"))
            {
              String key = "x#lb";
              String[] ss = text.split("<br> ");
              for (int s = 0; s < ss.length; s++)
              {
                trg_contents.add(DocumentHelper.createText(ss[s]));
                if (s < ss.length - 1) {
                  if (src_map.containsKey(key))
                  {
                    List list = (List)src_map.get(key);
                    trg_contents.add(list.get(0));
                    targetnotag = false;
                    list.remove(0);
                    if (list.size() == 0) {
                      src_map.remove(key);
                    }
                  }
                  else
                  {
                    org.dom4j.Element lb = DocumentHelper.createElement("x");
                    lb.addAttribute("ctype", "lb");
                    lb.addAttribute("id", Integer.toString(maxid));
                    maxid++;
                    lb.addAttribute("equiv-text", " ");
                    trg_contents.add(lb);
                  }
                }
              }
            }
            else
            {
              trg_contents.add(node);
            }
          }
          else
          {
            trg_contents.add(node);
          }
        }
        while (trg_contents.size() > 0)
        {
          org.dom4j.Node nd = (org.dom4j.Node)trg_contents.get(0);
          if (nd.getNodeType() == 3)
          {
            if (nd.getText().trim().equals(""))
            {
              trg_contents.remove(0);
            }
            else
            {
              nd.setText(nd.getText().replaceAll("^(\\s)+", ""));
              break;
            }
          }
          else if ((nd.getNodeType() == 1) && (nd.getName().equals("x")) && (((org.dom4j.Element)nd).attributeValue("ctype").equals("lb")))
          {
            trg_contents.remove(0);
          }
          else
          {
            if ((nd.getNodeType() != 1) || (!nd.getName().equals("x")) || (!((org.dom4j.Element)nd).attributeValue("ctype").equals("x-tab"))) {
              break;
            }
            trg_contents.remove(0);
          }
        }
        while (trg_contents.size() > 0)
        {
          org.dom4j.Node nd = (org.dom4j.Node)trg_contents.get(trg_contents.size() - 1);
          if (nd.getNodeType() == 3)
          {
            if (nd.getText().trim().equals(""))
            {
              trg_contents.remove(trg_contents.size() - 1);
            }
            else
            {
              nd.setText(nd.getText().replaceAll("(\\s)+$", ""));
              break;
            }
          }
          else if ((nd.getNodeType() == 1) && (nd.getName().equals("x")) && (((org.dom4j.Element)nd).attributeValue("ctype").equals("lb")))
          {
            trg_contents.remove(trg_contents.size() - 1);
          }
          else
          {
            if ((nd.getNodeType() != 1) || (!nd.getName().equals("x")) || (!((org.dom4j.Element)nd).attributeValue("ctype").equals("x-tab"))) {
              break;
            }
            trg_contents.remove(trg_contents.size() - 1);
          }
        }
        boolean isnotignoredsegment = true;
        if (trg_contents.size() == 1)
        {
          org.dom4j.Node node = (org.dom4j.Node)trg_contents.get(0);
          if ((node.getNodeType() == 3) && 
            (node.getText().replaceFirst(" ", "").equals(""))) {
            isnotignoredsegment = false;
          }
        }
        if ((targetnotag) && (sourcetagsurroundtext) && (isnotignoredsegment))
        {
          boolean isleadingtag = true;
          int index = 0;
          for (int i = 0; i < source.content().size(); i++)
          {
            org.dom4j.Node node = (org.dom4j.Node)source.content().get(i);
            if (node.getNodeType() == 1)
            {
              if (isleadingtag)
              {
                trg_contents.add(index, node);
                index++;
              }
              else
              {
                trg_contents.add(node);
              }
            }
            else
            {
              isleadingtag = false;
              index++;
            }
          }
        }
        target.setContent(trg_contents);
      }
    }
  }
  
  public int[] getRepsAndFuzzyReps()
    throws Exception
  {
    System.out.println("gathering reps and fuzzy reps count....");
    int[] fr_stats = new int[2];
    




































































































    return fr_stats;
  }
  
  private List<String[]> integrateNotesIntoSegments(String file) throws Exception {
	  
	  System.out.println("integrating comments....");
	  
	  List<String[]> notesinfo = new ArrayList<String[]>();
	  int id = 0;
	  org.dom4j.Document doc = XmlParser.parseXmlFile(file);
	  org.dom4j.Element root = doc.getRootElement();
	  List transunits = root.selectNodes("//*[name() = 'trans-unit']");
	  for(Object transunit_obj : transunits){
		  Element transunit = (Element)transunit_obj;
		  Element target = transunit.element("target");
		  if(target != null && !target.content().isEmpty()){
			  List<Element> notes = transunit.elements("note");
			  for(int i = notes.size()-1; i >= 0; i--){
				  Element note = notes.get(i);
				  List content = target.content();
				  content.add(0, DocumentHelper.createText("[%cmt_" + id + "_s%]"));
				  content.add(DocumentHelper.createText("[%cmt_" + id + "_e%]"));
				  target.setContent(content);
				  
				  String[] noteinfo = new String[3];
				  noteinfo[0] = note.attribute("from") != null?note.attributeValue("from"):"Unknown";
				  noteinfo[1] = note.attribute("timestamp") != null?note.attributeValue("timestamp"):"Unknown";
				  noteinfo[2] = note.getText();
				  notesinfo.add(noteinfo);
				  id++;
			  }
		  }
	  }
	  
	  OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF8");
	  doc.write(writer);
	  writer.close();
	    
	  return notesinfo;
  }
  
  public void createTargetFile()
    throws Exception
  {
    System.out.println("creating target file....");
    if (!isReplaceStyleAvailable(new com.aspose.words.Document(this.backupsourcefile))) {
      throw new Exception("cannot create target file, replace styles not available");
    }
    Configuration config = new BaseConfiguration();
    MergeDOC merger = new MergeDOC();
    merger.setConfiguration(config);
    merger.setOverwriteOriginalFile(false);
    XliffDocument xliffDocument = null;
    
    String tmp_translatedtxlf = this.translatedtxlf + ".tmp";
    FileUtils.copyFile(new File(this.translatedtxlf), new File(tmp_translatedtxlf), true);
    
    List<String[]> notesinfo = integrateNotesIntoSegments(tmp_translatedtxlf);
    
    xliffDocument = new XliffDocument(new File(tmp_translatedtxlf));
    String skeletonFile = this.sourcefile;
    com.aspose.words.Document document = new AsposeFactory().createDocumentInstance(new FileInputStream(skeletonFile));
    Configuration docConfig = xliffDocument.getConfiguration(new OfficeConfigurationConverterImpl());
    WordDocumentAligner aligner = new WordDocumentAligner(document, xliffDocument);
    aligner.addWordConfiguration(docConfig);
    aligner.align();
    String ext = this.sourcefile.substring(this.sourcefile.lastIndexOf('.'), this.sourcefile.length());
    String name = new File(this.sourcefile).getName();
    this.aligneddoc = (new File(this.translatedtxlf).getParent() + File.separator + name.substring(0, name.lastIndexOf(".")) + "_" + this.targetlanguage + ext);
    aligner.writeAlignedDocument(new File(this.aligneddoc));
    
    com.aspose.words.Document trg_doc = new com.aspose.words.Document(this.aligneddoc);
    if (this.replacestyles[1] == doublestrikethrough) {
      for (int i = 0; i < trg_doc.getChildNodes(21, true).getCount(); i++)
      {
        Run run = (Run)trg_doc.getChildNodes(21, true).get(i);
        if ((run.getFont().getUnderline() == this.replacestyles[0]) && (run.getFont().getDoubleStrikeThrough() == true))
        {
          run.getFont().setUnderline(1);
          run.getFont().setDoubleStrikeThrough(false);
          run.getFont().setStrikeThrough(true);
        }
        else if (run.getFont().getUnderline() == this.replacestyles[0])
        {
          run.getFont().setUnderline(1);
        }
        else if (run.getFont().getDoubleStrikeThrough() == true)
        {
          run.getFont().setDoubleStrikeThrough(false);
          run.getFont().setStrikeThrough(true);
        }
      }
    } else {
      for (int i = 0; i < trg_doc.getChildNodes(21, true).getCount(); i++)
      {
        Run run = (Run)trg_doc.getChildNodes(21, true).get(i);
        if (run.getFont().getUnderline() == this.replacestyles[2])
        {
          run.getFont().setUnderline(1);
          run.getFont().setStrikeThrough(true);
        }
        else if (run.getFont().getUnderline() == this.replacestyles[0])
        {
          run.getFont().setUnderline(1);
        }
        else if (run.getFont().getUnderline() == this.replacestyles[1])
        {
          run.getFont().setUnderline(0);
          run.getFont().setStrikeThrough(true);
        }
      }
    }
    removeIgnoredSegments(trg_doc, Locale.makeLocale(this.targetlanguage));
    removeIgnoredParagraphs(trg_doc);
    
    insertComments(trg_doc, notesinfo);
    
    trg_doc.save(this.aligneddoc);
    if (xliffDocument != null) {
      xliffDocument.dispose();
    }
    
    new File(tmp_translatedtxlf).delete();
  }
  
  private void insertComments(com.aspose.words.Document doc, List<String[]> notesinfo) throws Exception{
	  String reg = "\\[%cmt_(\\d)+_[se]%\\]";
	  Pattern regex = Pattern.compile(reg);
	  List<Run> collectedruns = new ArrayList<Run>();
	  doc.getRange().replace(regex, new ReplaceEvaluatorCommentsPlaceHolders(notesinfo, doc, collectedruns), false);
	  for(Run run : collectedruns){
		  String[] ss = run.getText().substring(2, run.getText().length()-2).split("_");
	      String[] noteinfo = notesinfo.get(Integer.parseInt(ss[1]));
	      if(ss[2].equals("s")){
	    	  Comment comment = new Comment(doc);
	    	  Paragraph para = new Paragraph(doc);
	    	  Run newrun = new Run(doc, noteinfo[2].replace("\n", ControlChar.LINE_BREAK));
	    	  para.getRuns().add(newrun);
	    	  comment.getParagraphs().add(para);
	    	  comment.setAuthor(noteinfo[0]);
	    	  SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
	    	  Date date = formatter.parse(noteinfo[1].replaceAll("[-:]", "").replaceAll("Z$", "+0000"));
	    	  comment.setDateTime(date);
	    	  CommentRangeStart start = new CommentRangeStart(doc, Integer.parseInt(ss[1]));
	    	  run.getParentNode().insertAfter(comment, run);
	    	  run.getParentNode().insertAfter(start, comment);
	      }else{
	    	  CommentRangeEnd end = new CommentRangeEnd(doc, Integer.parseInt(ss[1]));
	    	  run.getParentNode().insertAfter(end, run);
	      }
	      run.setText("");
	  }
  }
  
  public void createTargetCompareFile(String preservefmt)
    throws Exception
  {
    System.out.println("creating target compare file....");
    boolean iscompareerror = false;
    
    String ext = this.sourcefile.substring(this.sourcefile.lastIndexOf('.'), this.sourcefile.length());
    String name = new File(this.sourcefile).getName();
    this.comparedoc = (new File(this.translatedtxlf).getParent() + File.separator + name.substring(0, name.lastIndexOf(".")) + "_tracked" + ext);
    
    com.aspose.words.Document doc_old = new com.aspose.words.Document(this.reformattedtargetfile);
    String temp_old = new File(this.translatedtxlf).getParent() + File.separator + "old.docx";
    doc_old.save(temp_old);
    doc_old = new com.aspose.words.Document(temp_old);
    
    com.aspose.words.Document doc_new = new com.aspose.words.Document(this.aligneddoc);
    String temp_new = new File(this.translatedtxlf).getParent() + File.separator + "new.docx";
    doc_new.save(temp_new);
    doc_new = new com.aspose.words.Document(temp_new);
    normalizeSpaceAfterESM(doc_new, Locale.makeLocale(this.targetlanguage));
    //just want to delete comments
    DeleteCommentsAndShapeAltText(doc_new);
    try
    {
      doc_old.compare(doc_new, "user", new Date());
    }
    catch (Exception ex)
    {
      iscompareerror = true;
      ex.printStackTrace();
    }
    if (preservefmt.equals("true"))
    {
      new File(this.aligneddoc).delete();
      com.aspose.words.Document doc_clone = doc_old.deepClone();
      for (int i = 0; i < doc_clone.getRevisions().getCount(); i++)
      {
        Revision rev = doc_clone.getRevisions().get(i);
        if ((rev.getRevisionType() == 2) || (rev.getRevisionType() == 3))
        {
          rev.reject();
          i--;
        }
        else if ((rev.getParentNode().getText().equals(ControlChar.PARAGRAPH_BREAK)) || (rev.getParentNode().getText().equals("")))
        {
          rev.reject();
          i--;
        }
      }
      for (int i = 0; i < doc_clone.getRevisions().getCount(); i++)
      {
        Revision rev = doc_clone.getRevisions().get(i);
        rev.accept();
        i--;
      }
      doc_clone.save(this.aligneddoc);
      

      iscompareerror = false;
      doc_old = new com.aspose.words.Document(temp_old);
      try
      {
        doc_old.compare(doc_clone, "user", new Date());
      }
      catch (Exception ex)
      {
        iscompareerror = true;
        ex.printStackTrace();
      }
      if (!iscompareerror)
      {
        for (int i = 0; i < doc_old.getRevisions().getCount(); i++)
        {
          Revision rev = doc_old.getRevisions().get(i);
          if ((rev.getRevisionType() == 2) || (rev.getRevisionType() == 3))
          {
            rev.reject();
            i--;
          }
          else if ((rev.getParentNode().getText().equals(ControlChar.PARAGRAPH_BREAK)) || (rev.getParentNode().getText().equals("")))
          {
            rev.reject();
            i--;
          }
        }
        doc_old.save(this.comparedoc);
      }
    }
    else if (!iscompareerror)
    {
      for (int i = 0; i < doc_old.getRevisions().getCount(); i++)
      {
        Revision rev = doc_old.getRevisions().get(i);
        if ((rev.getRevisionType() == 2) || (rev.getRevisionType() == 3))
        {
          rev.accept();
          i--;
        }
      }
      doc_old.save(this.comparedoc);
    }
    new File(temp_old).delete();
    new File(temp_new).delete();
  }
  
  private static int getTagPenalty(org.dom4j.Element source)
  {
    int penalty = 0;
    penalty = (int)Math.round(source.elements("ut").size() * 0.5D);
    return penalty;
  }
  
  private static String wordToHtml(String paratext)
  {
    paratext = paratext.replace("<del></del>", "").replace("<ins></ins>", "").replace("</del><del>", "").replace("</ins><ins>", "");
    paratext = paratext.replaceAll("\t", "&#009;").replace(ControlChar.LINE_BREAK, "<cr>&#8629;</cr><br>").replace(String.valueOf('\036'), "&#8209;").replace(String.valueOf('\037'), "").replace(String.valueOf('\016'), "").replace(String.valueOf('\f'), "").replace(String.valueOf('\f'), "");
    return paratext;
  }
  
  private static String HtmlToPlain(String paratext)
  {
    paratext = paratext.replaceAll("&#009;", " ").replace("<cr>&#8629;</cr><br>", " ").replace("&#8209;", "-").replace("&lt;", "<").replace("&gt;", ">");
    return paratext;
  }
  
  private static void trimParaLeadingTrailingSpace(com.aspose.words.Document doc)
  {
    for (int i = 0; i < doc.getChildNodes(8, true).getCount(); i++)
    {
      Paragraph para = (Paragraph)doc.getChildNodes(8, true).get(i);
      if (para.getChildNodes(21, true).getCount() != 0)
      {
        Run firstrun = (Run)para.getChildNodes(21, true).get(0);
        if (firstrun.getText().trim().equals("")) {
          firstrun.remove();
        }
      }
      if (para.getChildNodes(21, true).getCount() != 0)
      {
        Run lastrun = (Run)para.getChildNodes(21, true).get(para.getChildNodes(21, true).getCount() - 1);
        if (lastrun.getText().trim().equals("")) {
          lastrun.remove();
        }
      }
    }
  }
  
  private static void UnlinkFields(com.aspose.words.Document doc)
  {
    for (int i = 0; i < doc.getChildNodes(22, true).getCount(); i++)
    {
      com.aspose.words.Node start = doc.getChildNodes(22, true).get(i);
      com.aspose.words.Node curNode = start;
      if (((FieldStart)curNode).getFieldType() != 13)
      {
        while ((curNode.getNodeType() != 23) && (curNode.getNodeType() != 24))
        {
          curNode = curNode.nextPreOrder(doc);
          if (curNode == null) {
            break;
          }
        }
        if (curNode != null) {
          RemoveSequence(start, curNode);
        }
        start.remove();
        i--;
      }
    }
  }
  
  private static void RemoveSequence(com.aspose.words.Node start, com.aspose.words.Node end)
  {
    com.aspose.words.Node curNode = start.nextPreOrder(start.getDocument());
    while ((curNode != null) && (!curNode.equals(end)))
    {
      com.aspose.words.Node nextNode = curNode.nextPreOrder(start.getDocument());
      if (curNode.isComposite())
      {
        if ((!((CompositeNode)curNode).getChildNodes(0, true).contains(end)) && (!((CompositeNode)curNode).getChildNodes(0, true).contains(start)))
        {
          nextNode = curNode.getNextSibling();
          curNode.remove();
        }
      }
      else {
        curNode.remove();
      }
      curNode = nextNode;
    }
  }
  
  private static String cumulateSegType(String oldtype, String newtype)
  {
    if (oldtype.equals("mix")) {
      return "mix";
    }
    if (oldtype.equals("insertion"))
    {
      if (newtype.equals("insertion")) {
        return "insertion";
      }
      return "mix";
    }
    if (oldtype.equals("deletion"))
    {
      if (newtype.equals("deletion")) {
        return "deletion";
      }
      return "mix";
    }
    if (newtype.equals("regular")) {
      return "regular";
    }
    return "mix";
  }
  
  private String getParaText(Paragraph para)
    throws Exception
  {
    String paratext = "";
    boolean isInField = false;
    for (int i = 0; i < para.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node nd = para.getChildNodes(0, true).get(i);
      if ((nd.getNodeType() == 21) && (!isInField))
      {
        Run run = (Run)nd;
        if (!run.getFont().getName().equals("Wingdings")) {
          paratext = paratext + run.getText();
        }
      }
      else if (nd.getNodeType() == 22)
      {
        isInField = true;
      }
      else if (nd.getNodeType() == 24)
      {
        isInField = false;
      }
    }
    return paratext.trim();
  }
  
  private String getParaRejectedText(Paragraph para)
    throws Exception
  {
    String paratext = "";
    boolean isInField = false;
    for (int i = 0; i < para.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node nd = para.getChildNodes(0, true).get(i);
      if ((nd.getNodeType() == 21) && (!isInField))
      {
        Run run = (Run)nd;
        if (!run.getFont().getName().equals("Wingdings")) {
          if ((!run.isInsertRevision()) || (run.isDeleteRevision())) {
            paratext = paratext + run.getText();
          }
        }
      }
      else if (nd.getNodeType() == 22)
      {
        isInField = true;
      }
      else if (nd.getNodeType() == 24)
      {
        isInField = false;
      }
    }
    return paratext.trim();
  }
  
  private String getParaAccpetedText(Paragraph para)
    throws Exception
  {
    String paratext = "";
    boolean isInField = false;
    for (int i = 0; i < para.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node nd = para.getChildNodes(0, true).get(i);
      if ((nd.getNodeType() == 21) && (!isInField))
      {
        Run run = (Run)nd;
        if (!run.getFont().getName().equals("Wingdings")) {
          if (!run.isDeleteRevision()) {
            paratext = paratext + run.getText();
          }
        }
      }
      else if (nd.getNodeType() == 22)
      {
        isInField = true;
      }
      else if (nd.getNodeType() == 24)
      {
        isInField = false;
      }
    }
    return paratext.trim();
  }
  
  private void DeleteCommentsAndShapeAltText(com.aspose.words.Document doc)
  {
    while (doc.getChildNodes(19, true).getCount() != 0) {
      doc.getChildNodes(19, true).get(0).remove();
    }
    for (int i = 0; i < doc.getChildNodes(0, true).getCount(); i++)
    {
      com.aspose.words.Node node = doc.getChildNodes(0, true).get(i);
      if (node.getNodeType() == 19)
      {
        node.remove();
        i--;
      }
      else if (node.getNodeType() == 18)
      {
        Shape shape = (Shape)node;
        shape.setAlternativeText("");
      }
    }
  }
  
  private void removeHyperlinks(com.aspose.words.Document doc)
    throws Exception
  {
    NodeCollection<FieldStart> starts = doc.getChildNodes(22, true);
    
    ArrayList<FieldStart> hyperlinkStarts = new ArrayList();
    for (FieldStart start : starts) {
      if (start.getFieldType() == 88)
      {
        hyperlinkStarts.add(start);
        
        com.aspose.words.Node currentNode = start.getNextSibling();
        com.aspose.words.Node fieldSeparator = null;
        while ((currentNode != null) && (currentNode.getNodeType() != 23))
        {
          currentNode = currentNode.getNextSibling();
          currentNode.getPreviousSibling().remove();
        }
        fieldSeparator = currentNode;
        while ((currentNode != null) && (currentNode.getNodeType() != 24))
        {
          if (currentNode.getNodeType() == 21) {
            ((Run)currentNode).getFont().setColor(Color.BLACK);
          }
          currentNode = currentNode.getNextSibling();
        }
        if (fieldSeparator != null) {
          fieldSeparator.remove();
        }
        if (currentNode != null) {
          currentNode.remove();
        }
      }
    }
    for (FieldStart start : hyperlinkStarts) {
      start.remove();
    }
  }
  
  private boolean isReplaceStyleAvailable(com.aspose.words.Document doc)
    throws Exception
  {
    List<Integer> availables = new ArrayList();
    
    //disable for now because WF5 does not display double strike through formatting correctly.
    //availables.add(Integer.valueOf(doublestrikethrough));
    for (int i = 0; i < Underline.getValues().length; i++)
    {
      int value = Underline.getValues()[i];
      if ((value != 0) && (value != 1)) {
        availables.add(Integer.valueOf(value));
      }
    }
    for (int i = 0; i < doc.getChildNodes(21, true).getCount(); i++)
    {
      Run run = (Run)doc.getChildNodes(21, true).get(i);
      if (run.getFont().getDoubleStrikeThrough()) {
        availables.remove(Integer.valueOf(doublestrikethrough));
      }
      if (availables.contains(Integer.valueOf(run.getFont().getUnderline()))) {
        availables.remove(Integer.valueOf(run.getFont().getUnderline()));
      }
    }
    if (availables.contains(Integer.valueOf(doublestrikethrough)))
    {
      if (availables.size() >= 1)
      {
        this.replacestyles[0] = ((Integer)availables.get(1)).intValue();
        this.replacestyles[1] = doublestrikethrough;
        return true;
      }
      return false;
    }
    if (availables.size() >= 3)
    {
      this.replacestyles[0] = ((Integer)availables.get(1)).intValue();
      this.replacestyles[1] = ((Integer)availables.get(2)).intValue();
      this.replacestyles[2] = ((Integer)availables.get(3)).intValue();
      return true;
    }
    return false;
  }
  
  private boolean isTrackedHidden(Revision rev)
    throws Exception
  {
    com.aspose.words.Node parent = rev.getParentNode();
    if ((parent != null) && (parent.isComposite()))
    {
      CompositeNode cn = (CompositeNode)parent;
      for (int i = 0; i < cn.getChildNodes(21, true).getCount(); i++)
      {
        Run run = (Run)cn.getChildNodes(21, true).get(i);
        if (run.getFont().getHidden()) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void normalizeSpaceAfterESM(com.aspose.words.Document doc, Locale locale)
    throws Exception
  {
    String[] esm = createESMString(locale).split(",");
    for (String sesm : esm)
    {
      String reg = "\\" + sesm + "[  ]+";
      Pattern regex = Pattern.compile(reg);
      String rep = sesm;
      if ((!locale.isFarEast()) || (locale.isKorean())) {
        rep = rep + " ";
      }
      doc.getRange().replace(regex, rep, new FindReplaceOptions());
    }
    for (int i = 0; i < doc.getChildNodes(8, true).getCount(); i++)
    {
      Paragraph para = (Paragraph)doc.getChildNodes(8, true).get(i);
      Run lastrun = null;
      for (int j = para.getChildNodes(21, true).getCount() - 1; j >= 0; j--)
      {
        Run run = (Run)para.getChildNodes(21, true).get(j);
        if (run != null)
        {
          lastrun = run;
          break;
        }
      }
      if (lastrun != null) {
        lastrun.setText(lastrun.getText().replaceAll("\\s+$", " "));
      }
    }
  }
  
  public void removeIgnoredSegments(com.aspose.words.Document doc, Locale locale)
    throws Exception
  {
    String[] esm = createESMString(locale).split(",");
    for (String sesm : esm)
    {
      String reg = "\\" + sesm;
      if ((!locale.isFarEast()) || (locale.isKorean())) {
        reg = reg + "  ";
      } else {
        reg = reg + " ";
      }
      Pattern regex = Pattern.compile(reg);
      String rep = sesm;
      
      doc.getRange().replace(regex, rep, new FindReplaceOptions());
    }
  }
  
  public void removeIgnoredParagraphs(com.aspose.words.Document doc)
    throws Exception
  {
    for (int i = 0; i < doc.getChildNodes(8, true).getCount(); i++)
    {
      Paragraph para = (Paragraph)doc.getChildNodes(8, true).get(i);
      if ((para.getText().contains(" ")) && (para.getText().replace(" ", "").replace(" ", "").equals(ControlChar.PARAGRAPH_BREAK)))
      {
        para.remove();
        i--;
      }
    }
  }
  
  private String createESMString(Locale locale)
    throws Exception
  {
    String ESM = "";
    for (int i = 0; i < this.esps.length; i++)
    {
      char c = this.esps[i];
      if (i == 0) {
        ESM = ESM + c;
      } else {
        ESM = ESM + "," + c;
      }
    }
    if ((locale.isJapanese()) || (locale.isKorean()) || (locale.isChinese()))
    {
      int i = 0;
      for (int max = this.esps.length; i < max; i++) {
        if (this.esps[i] == ':')
        {
          ESM = ESM + ",：";
        }
        else if (this.esps[i] == '.')
        {
          ESM = ESM + ",．";
          ESM = ESM + ",。";
          ESM = ESM + ",｡";
        }
        else if (this.esps[i] == '!')
        {
          ESM = ESM + ",！";
        }
        else if (this.esps[i] == '?')
        {
          ESM = ESM + ",？";
        }
      }
    }
    else if (locale.isHindi())
    {
      ESM = ESM + ",।";
    }
    if (locale.isArabic())
    {
      int i = 0;
      for (int max = this.esps.length; i < max; i++) {
        if (this.esps[i] == '.') {
          ESM = ESM + ",۔";
        } else if (this.esps[i] == '?') {
          ESM = ESM + ",؟";
        }
      }
    }
    return ESM;
  }
  
  private boolean isNormalTextExpansion(String source, String target)
  {
    if (source.length() > target.length() * textexpansionthreshold) {
      return false;
    }
    if (target.length() > source.length() * textexpansionthreshold) {
      return false;
    }
    return true;
  }
}
