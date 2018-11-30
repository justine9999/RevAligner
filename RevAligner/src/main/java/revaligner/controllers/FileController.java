package revaligner.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.gs4tr.foundation.locale.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import revaligner.domain.RevProject;
import revaligner.service.CreationDateComparator;
import revaligner.service.ProjectManager;
import revaligner.service.SessionCollector;

@Controller
@Scope(value="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
public class FileController
  implements Serializable
{
  @Inject
  SessionCollector sessionCollector;
  @Autowired
  ProjectManager projectManager;
  private boolean ispropertyfilebeingused = false;
  public static final String UTF8_BOM = "\uFEFF﻿";
  public boolean isredirect = false;
  public boolean isaligning = false;
  public static Logger logger;
  
  @RequestMapping(value={"/upload/{type}"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, String> upload(MultipartHttpServletRequest request, HttpServletResponse response, @PathVariable String type)
    throws Exception
  {
    Iterator<String> itr = request.getFileNames();
    MultipartFile mpf = null;
    if (type.equals("pkg"))
    {
      try
      {
        if (itr.hasNext())
        {
          mpf = request.getFile((String)itr.next());
          File uplfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath() + File.separator + type);
          System.out.println(uplfolder.getAbsolutePath());
          if (!uplfolder.exists()) {
            uplfolder.mkdirs();
          } else {
            FileUtils.cleanDirectory(uplfolder);
          }
          String uploadfullpath = uplfolder + File.separator + mpf.getOriginalFilename();
          FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(uploadfullpath));
          if (this.projectManager.verifyprojectpackage(uploadfullpath))
          {
            System.out.println("valid project file");
            FileUtils.cleanDirectory(uplfolder);
            
            HashMap<String, String> prjinfo = new HashMap();
            FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
            Properties props = new Properties();
            props.load(in);
            
            HashMap<String, String> langcodemapr = ProjectManager.getLangeuageCodeMapReverse();
            
            prjinfo.put("raprojectsourcelanguagecode", langcodemapr.get(props.getProperty("raprojectsourcelanguagecode")));
            prjinfo.put("raprojecttargetlanguagecode", langcodemapr.get(props.getProperty("raprojecttargetlanguagecode")));
            prjinfo.put("rasourcefilename", props.getProperty("rasourcefilename"));
            prjinfo.put("ratargetfilename", props.getProperty("ratargetfilename"));
            prjinfo.put("rasourcefilesize", props.getProperty("rasourcefilesize"));
            prjinfo.put("ratargetfilesize", props.getProperty("ratargetfilesize"));
            prjinfo.put("isaligned", props.getProperty("isaligned"));
            prjinfo.put("isoutfortrans", props.getProperty("isoutfortrans"));
            
            return prjinfo;
          }
          new File(this.projectManager.getRevProject().getPrjWorkingPath()).delete();
          throw new Exception("invalid project file.");
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        response.sendError(500, e.getMessage());
        response.setStatus(500);
      }
    }
    else
    {
      long tStart = System.currentTimeMillis();
      while (this.ispropertyfilebeingused)
      {
        Thread.sleep(500L);
        double elapsedSeconds = (System.currentTimeMillis() - tStart) / 1000.0D;
        if (elapsedSeconds > 10.0D) {
          throw new Exception("project info file is being used all the time.");
        }
      }
      this.ispropertyfilebeingused = true;
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      FileOutputStream out = null;
      try
      {
        while (itr.hasNext())
        {
          mpf = request.getFile((String)itr.next());
          if ((type.equals("source")) || (type.equals("target")) || (type.equals("merge")))
          {
            File uplfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath() + File.separator + type);
            if (!uplfolder.exists()) {
              uplfolder.mkdirs();
            } else {
              FileUtils.cleanDirectory(uplfolder);
            }
            String uploadfullpath = uplfolder + File.separator + mpf.getOriginalFilename();
            FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(uploadfullpath));
            
            props.setProperty("ra" + type + "filename", mpf.getOriginalFilename());
            props.setProperty("ra" + type + "filesize", Long.toString(mpf.getSize()) + " K");
            if (type.equals("source")) {
              this.projectManager.setSourcePath(uploadfullpath);
            } else if (type.equals("target")) {
              this.projectManager.setTargetPath(uploadfullpath);
            } else if (type.equals("merge")) {
              this.projectManager.setTranslateTxlfPath(uploadfullpath);
            }
          }
        }
        out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
        props.store(out, "RA PROJECT INFOMATION");
      }
      catch (Exception e)
      {
        System.err.println("failed uploading " + type + " file, please check log");
        logger.error("PROJECT: " + this.projectManager.getRevProject().getPrjNumber() + " - error uploading " + type + " file", e);
        this.ispropertyfilebeingused = false;
        response.sendError(500, e.getMessage());
        response.setStatus(500);
      }
      finally
      {
        if (out != null) {
          out.close();
        }
        in.close();
        props.clear();
        this.ispropertyfilebeingused = false;
      }
      this.ispropertyfilebeingused = false;
    }
    return new HashMap();
  }
  
  @RequestMapping(value={"/del/{type}"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void delete(HttpServletResponse response, @PathVariable String type)
    throws Exception
  {
    long tStart = System.currentTimeMillis();
    while (this.ispropertyfilebeingused)
    {
      Thread.sleep(500L);
      double elapsedSeconds = (System.currentTimeMillis() - tStart) / 1000.0D;
      if (elapsedSeconds > 10.0D) {
        throw new Exception("project info file is being used all the time.");
      }
    }
    this.ispropertyfilebeingused = true;
    FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
    Properties props = new Properties();
    props.load(in);
    FileOutputStream out = null;
    
    props.remove("ra" + type + "filename");
    props.remove("ra" + type + "filesize");
    
    out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
    props.store(out, "RA PROJECT INFOMATION");
    

    File uplfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath() + File.separator + type);
    try
    {
      if (uplfolder.exists()) {
        FileUtils.cleanDirectory(uplfolder);
      } else {
        throw new IOException("Invalid File Path!");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    finally
    {
      this.ispropertyfilebeingused = false;
    }
    this.ispropertyfilebeingused = false;
  }
  
  @RequestMapping(value={"/align"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void align(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    if (this.isaligning) {
      return;
    }
    this.isaligning = true;
    

    System.out.println("aligning files...");
    String prjid = this.projectManager.getRevProject().getPrjNumber();
    String aligntype = request.getParameter("aligntype");
    System.out.println("alignment type: " + aligntype);
    if ((!aligntype.equals("sequential")) && (!aligntype.equals("auto"))) {
      throw new Exception("unknow alignment type");
    }
    this.projectManager.setAlignType(aligntype);
    this.projectManager.setAlignProgress(0, prjid);
    try
    {
      Thread.sleep(2000L);
      this.projectManager.iniprj(this.projectManager.getRevProject().getPrjNumber(), "false", "false");
      this.projectManager.alignFiles(aligntype);
      
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      props.setProperty("isaligned", "true");
      props.setProperty("alignertype", aligntype);
      FileOutputStream out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
      props.store(out, "RA PROJECT INFOMATION");
      out.close();
      in.close();
      this.projectManager.setAlignProgress(100, prjid);
      System.out.println("files aligned");
    }
    catch (Exception e)
    {
      this.projectManager.setAlignProgress(-1, prjid);
      System.out.println("files failed to be aligned");
      e.printStackTrace();
      response.getWriter().write(this.projectManager.getErrorMessage());
      
      response.setStatus(500);
    }
    finally
    {
      this.isaligning = false;
    }
  }
  
  @RequestMapping(value={"/readnbalignmentresult"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public HashMap<String, String> readnbalignmentresult(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    System.out.println("reading nb alignment result...");
    
    HashMap<String, String> results = new HashMap();
    try
    {
      String[] res = this.projectManager.readnbalignmentreport();
      if (res[0].trim().equals("")) {
        results.put("ratio", "N/A");
      } else {
        results.put("ratio", res[0]);
      }
      System.out.println("result read");
    }
    catch (Exception e)
    {
      results.put("ratio", "N/A");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return results;
  }
  
  @RequestMapping(value={"/searchprj"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, String> searchproject(HttpServletResponse response, HttpServletRequest request, HttpSession httpSession)
    throws Exception
  {
    System.out.println("searching project...");
    
    HashMap<String, String> prjinfo = new HashMap();   
    
    String clue = request.getParameter("prjid");
    String prjid = "";
    if(clue.startsWith("RA#")){
    	prjid = clue;
    }else{
    	prjid = this.projectManager.findProjectIdBySubmissionName(clue);
    	if(prjid.equals("")){
    		System.out.println("target project not found by the submission name");
            
            response.sendError(500, "target project not found by the submission name");
            response.setStatus(500);
            return prjinfo;
    	}
    }
    prjinfo.put("raprojectnumber", prjid);
    
    try
    {
      Thread.sleep(2000L);
      String token = request.getParameter("token");
      System.out.println("token: " + token);
      if(isAdmin())
      {
        System.out.println("searching project as admin");
        String username = this.projectManager.searchProjectFolderAsAdminUser(prjid);
        if (!username.equals(""))
        {
          System.out.println("target project folder found");
          this.projectManager.getRevProject().setPrjNumber(prjid);
          this.projectManager.getRevProject().setUsername(username);
          File prjfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath());
          this.projectManager.setPrjFolder(prjfolder.getAbsolutePath());
          if (this.projectManager.searchProjectInfoFile(prjid))
          {
            System.out.println("target project info file found");
            

            FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
            Properties props = new Properties();
            props.load(in);
            
            String isaln = (props.getProperty("isaligned") != null) && (props.getProperty("isaligned").equals("true")) ? "true" : "false";
            String isout = (props.getProperty("isoutfortrans") != null) && (props.getProperty("isoutfortrans").equals("true")) ? "true" : "false";
            if (ProjectManager.checkProjectAccessable(prjid, this.sessionCollector, username))
            {
              this.projectManager.iniprj(prjid, isaln, isout);
              
              prjinfo.put("raprojectsourcelanguagecode", "");
              prjinfo.put("raprojecttargetlanguagecode", "");
              prjinfo.put("rasourcefilename", "");
              prjinfo.put("ratargetfilename", "");
              prjinfo.put("rasourcefilesize", "");
              prjinfo.put("ratargetfilesize", "");
              prjinfo.put("isaligned", "false");
              prjinfo.put("isoutfortrans", "false");
              String isautosaved = new File(this.projectManager.getRevProject().getAutoSavedAlignedXmlPath()).exists() ? "true" : "false";
              prjinfo.put("isautosavedfileexist", isautosaved);
              prjinfo.put("raprojectsubmissionname", "");
              
              this.projectManager.getRevProject().setPrjCreationDate(props.getProperty("raprojectcreationdate"));
              HashMap<String, String> langcodemapr = ProjectManager.getLangeuageCodeMapReverse();
              
              prjinfo.put("raprojectsourcelanguagecode", langcodemapr.get(props.getProperty("raprojectsourcelanguagecode")));
              prjinfo.put("raprojecttargetlanguagecode", langcodemapr.get(props.getProperty("raprojecttargetlanguagecode")));
              prjinfo.put("rasourcefilename", props.getProperty("rasourcefilename"));
              prjinfo.put("ratargetfilename", props.getProperty("ratargetfilename"));
              prjinfo.put("rasourcefilesize", props.getProperty("rasourcefilesize"));
              prjinfo.put("ratargetfilesize", props.getProperty("ratargetfilesize"));
              prjinfo.put("isaligned", props.getProperty("isaligned"));
              prjinfo.put("isoutfortrans", props.getProperty("isoutfortrans"));
              System.out.println("aligner base type: " + props.getProperty("alignerbasetype"));
              if (props.getProperty("alignerbasetype") != null) {
                prjinfo.put("alignerbasetype", props.getProperty("alignerbasetype"));
              }
              System.out.println("aligner type: " + props.getProperty("alignertype"));
              if (props.getProperty("alignertype") != null)
              {
                prjinfo.put("alignertype", props.getProperty("alignertype"));
                this.projectManager.setAlignType(props.getProperty("alignertype"));
              }
              if (props.getProperty("raprojectsubmissionname") != null) {
                prjinfo.put("raprojectsubmissionname", props.getProperty("raprojectsubmissionname"));
              }
              this.sessionCollector.getSessionMap().put(request.getSession(), username + "_" + prjid);
              System.out.println("prjid: " + prjid);
              System.out.println("new RA session created");
              System.out.println("prj collected: " + request.getSession().getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
              
              if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
            	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
              }
            }
            else
            {
              System.out.println("target project is not accessable now");
              
              response.sendError(500, "project is not accessable");
              response.setStatus(500);
            }
          }
          else
          {
            System.out.println("target project info file not found");
            
            response.sendError(500, "missing project info file");
            response.setStatus(500);
          }
        }
        else
        {
          System.out.println("target project not found");
          
          response.sendError(500, "target project not found");
          response.setStatus(500);
        }
      }
      else if ((token == null) || (token.equals("")))
      {
        String username = getUserName();
        this.projectManager.getRevProject().setUsername(username);
        if (this.projectManager.searchProjectFolder(prjid))
        {
          System.out.println("target project folder found");
          this.projectManager.getRevProject().setPrjNumber(prjid);
          File prjfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath());
          this.projectManager.setPrjFolder(prjfolder.getAbsolutePath());
          if (this.projectManager.searchProjectInfoFile(prjid))
          {
            System.out.println("target project info file found");
            

            FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
            Properties props = new Properties();
            props.load(in);
            
            String isaln = (props.getProperty("isaligned") != null) && (props.getProperty("isaligned").equals("true")) ? "true" : "false";
            String isout = (props.getProperty("isoutfortrans") != null) && (props.getProperty("isoutfortrans").equals("true")) ? "true" : "false";
            if (ProjectManager.checkProjectAccessable(prjid, this.sessionCollector, getUserName()))
            {
              this.projectManager.iniprj(prjid, isaln, isout);
              
              prjinfo.put("raprojectsourcelanguagecode", "");
              prjinfo.put("raprojecttargetlanguagecode", "");
              prjinfo.put("rasourcefilename", "");
              prjinfo.put("ratargetfilename", "");
              prjinfo.put("rasourcefilesize", "");
              prjinfo.put("ratargetfilesize", "");
              prjinfo.put("isaligned", "false");
              prjinfo.put("isoutfortrans", "false");
              String isautosaved = new File(this.projectManager.getRevProject().getAutoSavedAlignedXmlPath()).exists() ? "true" : "false";
              prjinfo.put("isautosavedfileexist", isautosaved);
              prjinfo.put("raprojectsubmissionname", "");
              
              this.projectManager.getRevProject().setPrjCreationDate(props.getProperty("raprojectcreationdate"));
              HashMap<String, String> langcodemapr = ProjectManager.getLangeuageCodeMapReverse();
              
              prjinfo.put("raprojectsourcelanguagecode", langcodemapr.get(props.getProperty("raprojectsourcelanguagecode")));
              prjinfo.put("raprojecttargetlanguagecode", langcodemapr.get(props.getProperty("raprojecttargetlanguagecode")));
              prjinfo.put("rasourcefilename", props.getProperty("rasourcefilename"));
              prjinfo.put("ratargetfilename", props.getProperty("ratargetfilename"));
              prjinfo.put("rasourcefilesize", props.getProperty("rasourcefilesize"));
              prjinfo.put("ratargetfilesize", props.getProperty("ratargetfilesize"));
              prjinfo.put("isaligned", props.getProperty("isaligned"));
              prjinfo.put("isoutfortrans", props.getProperty("isoutfortrans"));
              System.out.println("aligner base type: " + props.getProperty("alignerbasetype"));
              if (props.getProperty("alignerbasetype") != null) {
                prjinfo.put("alignerbasetype", props.getProperty("alignerbasetype"));
              }
              System.out.println("aligner type: " + props.getProperty("alignertype"));
              if (props.getProperty("alignertype") != null)
              {
                prjinfo.put("alignertype", props.getProperty("alignertype"));
                this.projectManager.setAlignType(props.getProperty("alignertype"));
              }
              if (props.getProperty("raprojectsubmissionname") != null) {
                prjinfo.put("raprojectsubmissionname", props.getProperty("raprojectsubmissionname"));
              }
              this.sessionCollector.getSessionMap().put(request.getSession(), getUserName() + "_" + prjid);
              System.out.println("prjid: " + prjid);
              System.out.println("new RA session created");
              System.out.println("prj collected: " + request.getSession().getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
              
              if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
            	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
              }
            }
            else
            {
              System.out.println("target project is not accessable now");
              
              response.sendError(500, "project is not accessable");
              response.setStatus(500);
            }
          }
          else
          {
            System.out.println("target project info file not found");
            
            response.sendError(500, "missing project info file");
            response.setStatus(500);
          }
        }
        else
        {
          System.out.println("target project not found");
          
          response.sendError(500, "target project not found");
          response.setStatus(500);
        }
      }
      else
      {
        System.out.println("searching project with token");
        String[] ss = this.projectManager.parseAccessToken(token);
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        
        String timeStamp = df.format(cal.getTime());
        long currenttime = Long.parseLong(timeStamp);
        if ((ss.length == 4) && (ss[2].equals(prjid)) && (currenttime <= Long.parseLong(ss[0])))
        {
          String username = ss[1];
          if (this.projectManager.searchProjectFolderFromOtherUser(prjid, username))
          {
            System.out.println("target project folder found");
            this.projectManager.getRevProject().setPrjNumber(prjid);
            this.projectManager.getRevProject().setUsername(username);
            File prjfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath());
            this.projectManager.setPrjFolder(prjfolder.getAbsolutePath());
            if (this.projectManager.searchProjectInfoFile(prjid))
            {
              System.out.println("target project info file found");
              

              FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
              Properties props = new Properties();
              props.load(in);
              
              String isaln = (props.getProperty("isaligned") != null) && (props.getProperty("isaligned").equals("true")) ? "true" : "false";
              String isout = (props.getProperty("isoutfortrans") != null) && (props.getProperty("isoutfortrans").equals("true")) ? "true" : "false";
              if (ProjectManager.checkProjectAccessable(prjid, this.sessionCollector, username))
              {
                this.projectManager.iniprj(prjid, isaln, isout);
                
                prjinfo.put("raprojectsourcelanguagecode", "");
                prjinfo.put("raprojecttargetlanguagecode", "");
                prjinfo.put("rasourcefilename", "");
                prjinfo.put("ratargetfilename", "");
                prjinfo.put("rasourcefilesize", "");
                prjinfo.put("ratargetfilesize", "");
                prjinfo.put("isaligned", "false");
                prjinfo.put("isoutfortrans", "false");
                String isautosaved = new File(this.projectManager.getRevProject().getAutoSavedAlignedXmlPath()).exists() ? "true" : "false";
                prjinfo.put("isautosavedfileexist", isautosaved);
                prjinfo.put("raprojectsubmissionname", "");
                
                this.projectManager.getRevProject().setPrjCreationDate(props.getProperty("raprojectcreationdate"));
                HashMap<String, String> langcodemapr = ProjectManager.getLangeuageCodeMapReverse();
                
                prjinfo.put("raprojectsourcelanguagecode", langcodemapr.get(props.getProperty("raprojectsourcelanguagecode")));
                prjinfo.put("raprojecttargetlanguagecode", langcodemapr.get(props.getProperty("raprojecttargetlanguagecode")));
                prjinfo.put("rasourcefilename", props.getProperty("rasourcefilename"));
                prjinfo.put("ratargetfilename", props.getProperty("ratargetfilename"));
                prjinfo.put("rasourcefilesize", props.getProperty("rasourcefilesize"));
                prjinfo.put("ratargetfilesize", props.getProperty("ratargetfilesize"));
                prjinfo.put("isaligned", props.getProperty("isaligned"));
                prjinfo.put("isoutfortrans", props.getProperty("isoutfortrans"));
                System.out.println("aligner base type: " + props.getProperty("alignerbasetype"));
                if (props.getProperty("alignerbasetype") != null) {
                  prjinfo.put("alignerbasetype", props.getProperty("alignerbasetype"));
                }
                System.out.println("aligner type: " + props.getProperty("alignertype"));
                if (props.getProperty("alignertype") != null)
                {
                  prjinfo.put("alignertype", props.getProperty("alignertype"));
                  this.projectManager.setAlignType(props.getProperty("alignertype"));
                }
                if (props.getProperty("raprojectsubmissionname") != null) {
                  prjinfo.put("raprojectsubmissionname", props.getProperty("raprojectsubmissionname"));
                }
                this.sessionCollector.getSessionMap().put(request.getSession(), username + "_" + prjid);
                System.out.println("prjid: " + prjid);
                System.out.println("new RA session created");
                System.out.println("prj collected: " + request.getSession().getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
                
                if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
              	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
                }
              }
              else
              {
                System.out.println("target project is not accessable now");
                
                response.sendError(500, "project is not accessable");
                response.setStatus(500);
              }
            }
            else
            {
              System.out.println("target project info file not found");
              
              response.sendError(500, "missing project info file");
              response.setStatus(500);
            }
          }
          else
          {
            System.out.println("target project not found");
            
            response.sendError(500, "target project not found");
            response.setStatus(500);
          }
        }
        else
        {
          System.out.println("invalid access token");
          
          response.sendError(500, "invalid access token");
          response.setStatus(500);
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("target project not found");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return prjinfo;
  }
  
  @RequestMapping(value={"/getprojectlist"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public HashMap<String, List<String[]>> getprojectlist(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    System.out.println("getting project list...");
    HashMap<String, List<String[]>> prjlist = new HashMap();
    List<String[]> infos = new ArrayList();
    
    Comparator<String[]> comparator = new CreationDateComparator();
    PriorityQueue<String[]> queue = new PriorityQueue(30, comparator);
    List<String> ongoingprjs = new ArrayList();
    Iterator it = this.sessionCollector.getSessionMap().entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry<HttpSession, String> entry = (Map.Entry)it.next();
      ongoingprjs.add(entry.getValue());
    }
    try
    {
      Thread.sleep(2000L);
      if (isAdmin())
      {
        String searchfolder = this.projectManager.getRevProject().getBaseWorkingPath();
        File[] arrayOfFile1 = new File(searchfolder).listFiles();int i = arrayOfFile1.length;
        for (File file: arrayOfFile1)
        {
          if (file.isDirectory())
          {
            String user = file.getName();
            for (File sfile : file.listFiles()) {
              if ((sfile.isDirectory()) && (sfile.getName().startsWith("RA#")))
              {
                String name = sfile.getName();
                String infofile = sfile.getAbsolutePath() + File.separator + "_.info";
                if (new File(infofile).exists())
                {
                  FileInputStream in = new FileInputStream(infofile);
                  Properties props = new Properties();
                  props.load(in);
                  String date = props.getProperty("raprojectcreationdate");
                  String subname = props.getProperty("raprojectsubmissionname") == null ? "N/A" : props.getProperty("raprojectsubmissionname");
                  String[] prjinfo = new String[6];
                  prjinfo[0] = name;
                  prjinfo[1] = date;
                  prjinfo[2] = user;
                  if (ongoingprjs.contains(user + "_" + name)) {
                    prjinfo[3] = "true";
                  } else {
                    prjinfo[3] = "false";
                  }
                  String progress = props.getProperty("raprojectprogress");
                  if ((progress == null) || (progress.equals("")))
                  {
                    String alignedxml = sfile.getAbsolutePath() + File.separator + "rev_aligned.xml";
                    if (new File(alignedxml).exists()) {
                      progress = this.projectManager.readAlignedFileForRatio(alignedxml);
                    } else {
                      progress = "0";
                    }
                    props.setProperty("raprojectprogress", progress);
                    FileOutputStream out = new FileOutputStream(infofile);
                    props.store(out, "RA PROJECT INFOMATION");
                  }
                  prjinfo[4] = progress;
                  prjinfo[5] = subname;
                  if ((date != null) && (!name.equals("")) && (!date.equals("")) && (!user.equals(""))) {
                    queue.add(prjinfo);
                  }
                }
              }
            }
          }
        }
      }
      else
      {
        String user = getUserName();
        String userfolder = this.projectManager.getRevProject().getBaseWorkingPath() + File.separator + user;
        File[] arrayOfFile2 = new File(userfolder).listFiles();
        for (File file: arrayOfFile2)
        {
          if ((file.isDirectory()) && (file.getName().startsWith("RA#")))
          {
            String name = file.getName();
            String infofile = file.getAbsolutePath() + File.separator + "_.info";
            if (new File(infofile).exists())
            {
              FileInputStream in = new FileInputStream(infofile);
              Properties props = new Properties();
              props.load(in);
              String date = props.getProperty("raprojectcreationdate");
              String subname = props.getProperty("raprojectsubmissionname");
              String[] prjinfo = new String[6];
              prjinfo[0] = name;
              prjinfo[1] = date;
              prjinfo[2] = user;
              prjinfo[3] = "false";
              
              String progress = props.getProperty("raprojectprogress");
              if ((progress == null) || (progress.equals("")))
              {
                String alignedxml = file.getAbsolutePath() + File.separator + "rev_aligned.xml";
                if (new File(alignedxml).exists()) {
                  progress = this.projectManager.readAlignedFileForRatio(alignedxml);
                } else {
                  progress = "0";
                }
                props.setProperty("raprojectprogress", progress);
              }
              prjinfo[4] = progress;
              prjinfo[5] = subname;
              if ((date != null) && (!name.equals("")) && (!date.equals("")) && (!user.equals(""))) {
                queue.add(prjinfo);
              }
            }
          }
        }
      }
      while (!queue.isEmpty()) {
        infos.add(queue.poll());
      }
      prjlist.put("prjinfo", infos);
    }
    catch (Exception e)
    {
      System.out.println("error getting project list");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return prjlist;
  }
  
  @RequestMapping(value={"/checksessionexistence"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void checkprjexists(HttpServletResponse response, HttpServletRequest request, HttpSession httpSession)
    throws Exception
  {
    System.out.println("checking if session exists in pool...");
    try
    {
      Thread.sleep(2000L);
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        System.out.println("session exists");
      }
      else
      {
        System.out.println("session not exist");
        response.sendError(500, "session not exist");
        response.setStatus(500);
      }
    }
    catch (Exception e)
    {
      System.out.println("session not exist or internal error");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/readappbaseconfiguration"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, String> readAppBaseConfiguration(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    System.out.println("reading app configuration...");
    HashMap<String, String> appConfig = new HashMap();
    try
    {
      logger = this.projectManager.getRevProject().getMainLogger();
      appConfig.put("timeoutcheckinterval", Double.toString(this.projectManager.getRevProject().getTimeOutCheckInterval()));
      appConfig.put("autosaveinterval", Double.toString(this.projectManager.getRevProject().getAutoSaveInterval()));
    }
    catch (Exception e)
    {
      System.out.println("error reading app configuration");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return appConfig;
  }
  
  @RequestMapping(value={"/generateaccesstoken"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, String> generateAccessToken(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    System.out.println("generating access token...");
    HashMap<String, String> data = new HashMap();
    try
    {
      Thread.sleep(1000L);
      if ((!this.projectManager.getRevProject().getUsername().equals(getUserName())) && (!isAdmin())) {
        throw new Exception("you are not the owner of the project");
      }
      String prjid = this.projectManager.getRevProject().getPrjNumber();
      String username = getUserName();
      DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      
      Calendar cal = Calendar.getInstance();
      cal.add(5, 30);
      String date = cal.getTime().toString();
      String timeStamp = df.format(cal.getTime());
      
      String token = this.projectManager.generateAccessToken(prjid, username, timeStamp);
      data.put("tokenstring", token);
      data.put("expiretime", date);
    }
    catch (Exception e)
    {
      System.out.println("error generating access token");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return data;
  }
  
  @RequestMapping(value={"/fetchsegs"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, List<String>> fetchsegments(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("retrieving segments...");
    HashMap<String, List<String>> stsegs = new HashMap();
    try
    {
      String[] seqs = arr.replace("\"", "").replace("[", "").replace("]", "").split(",", -1);
      System.out.println("source id: " + seqs[0]);
      System.out.println("target id: " + seqs[1]);
      
      String src_segid = seqs[0];
      String trg_segid = seqs[1];
      
      stsegs = this.projectManager.getSegmentsFromParagraph(src_segid, trg_segid);
      System.out.println("source seg 1: " + (String)((List)stsegs.get("srcsegs")).get(0));
      System.out.println("target seg 1: " + (String)((List)stsegs.get("trgsegs")).get(0));
    }
    catch (Exception e)
    {
      System.out.println("failed to fetch segments");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return stsegs;
  }
  
  @RequestMapping(value={"/Aligner"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView readAndDisplayAlignedParagraphs(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    System.out.println("reading aligned xml file...");
    
    ModelAndView model = null;
    if (!this.sessionCollector.getSessionMap().containsKey(httpSession))
    {
      model = new ModelAndView("redirect:/rac/entry");
      return model;
    }
    try
    {
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      props.setProperty("alignerbasetype", "paragraph");
      FileOutputStream out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
      props.store(out, "RA PROJECT INFOMATION");
      out.close();
      in.close();
      
      model = new ModelAndView("manualalign");
      this.projectManager.readAlignedParagraphs();
      LinkedHashMap<String, List<String>> map_srcs = this.projectManager.getAlignedSrcParas();
      LinkedHashMap<String, List<String>> map_trgs = this.projectManager.getAlignedTrgParas();
      LinkedHashMap<String, List<String>> map_missing = this.projectManager.getMissingTrgParas();
      LinkedHashMap<String, List<String>> map_src_segs = this.projectManager.getSrcSegments();
      LinkedHashMap<String, List<String>> map_trg_segs = this.projectManager.getTrgSegments();
      LinkedHashMap<String, String> map_lock_para_seqs = this.projectManager.getLockedParaSeqs();
      LinkedHashMap<String, String> map_aligned_para_seqs = this.projectManager.getAlignedParaSeqs();
      model.addObject("prjnum", this.projectManager.getRevProject().getPrjNumber());
      model.addObject("srclang", this.projectManager.getRevProject().getSrcLang());
      model.addObject("trglang", this.projectManager.getRevProject().getTrgLang());
      model.addObject("s_map", map_srcs);
      model.addObject("t_map", map_trgs);
      model.addObject("m_map", map_missing);
      model.addObject("src_segs_map", map_src_segs);
      model.addObject("trg_segs_map", map_trg_segs);
      model.addObject("lock_para_seq_map", map_lock_para_seqs);
      model.addObject("aligned_para_seq_map", map_aligned_para_seqs);
      System.out.println("displaying aligned file");
    }
    catch (Exception e)
    {
      System.out.println("aligned files failed to be displayed");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/SegAligner"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView readAndDisplayAlignedSegments(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    System.out.println("reading aligned xml file...");
    
    String aligntype = this.projectManager.getAlignType();
    ModelAndView model = null;
    if (!this.sessionCollector.getSessionMap().containsKey(httpSession))
    {
      model = new ModelAndView("redirect:/rac/entry");
      return model;
    }
    try
    {
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      props.setProperty("alignerbasetype", "sentence");
      FileOutputStream out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
      props.store(out, "RA PROJECT INFOMATION");
      out.close();
      in.close();
      
      model = new ModelAndView("manualalign_seg");
      this.projectManager.readAlignedSegments(aligntype, false);
      LinkedHashMap<String, List<String>> map_srcs = this.projectManager.getSrcSegments();
      LinkedHashMap<String, List<String>> map_trgs = this.projectManager.getTrgSegments();
      LinkedHashMap<String, List<String>> map_missing = this.projectManager.getMissingTrgSegs();
      LinkedHashMap<String, String> map_lock_para_seqs = this.projectManager.getLockedSegSeqs();
      LinkedHashMap<String, String> map_review_para_seqs = this.projectManager.getReviewSegSeqs();
      LinkedHashMap<String, String> map_ignore_para_seqs = this.projectManager.getIgnoreSegSeqs();
      int nullcnt = this.projectManager.getNullCnt();
      model.addObject("prjnum", this.projectManager.getRevProject().getPrjNumber());
      model.addObject("srclang", this.projectManager.getRevProject().getSrcLang());
      model.addObject("trglang", this.projectManager.getRevProject().getTrgLang());
      model.addObject("isfareast", Boolean.toString(Locale.makeLocale(this.projectManager.getRevProject().getTrgLang()).isFarEast()));
      model.addObject("s_map", map_srcs);
      model.addObject("t_map", map_trgs);
      model.addObject("m_map", map_missing);
      model.addObject("lock_para_seq_map", map_lock_para_seqs);
      model.addObject("review_para_seq_map", map_review_para_seqs);
      model.addObject("ignore_para_seq_map", map_ignore_para_seqs);
      model.addObject("nullcnt", Integer.valueOf(nullcnt));
      System.out.println("displaying aligned file");
    }
    catch (Exception e)
    {
      System.out.println("aligned files failed to be displayed");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/SegAlignerAuto"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView readAndDisplayAutoSavedAlignedSegments(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    System.out.println("reading auto-saved aligned xml file...");
    
    String aligntype = this.projectManager.getAlignType();
    ModelAndView model = null;
    if (!this.sessionCollector.getSessionMap().containsKey(httpSession))
    {
      model = new ModelAndView("redirect:/rac/entry");
      return model;
    }
    try
    {
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      props.setProperty("alignerbasetype", "sentence");
      FileOutputStream out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
      props.store(out, "RA PROJECT INFOMATION");
      out.close();
      in.close();
      
      model = new ModelAndView("manualalign_seg");
      this.projectManager.readAlignedSegments(aligntype, true);
      LinkedHashMap<String, List<String>> map_srcs = this.projectManager.getSrcSegments();
      LinkedHashMap<String, List<String>> map_trgs = this.projectManager.getTrgSegments();
      LinkedHashMap<String, List<String>> map_missing = this.projectManager.getMissingTrgSegs();
      LinkedHashMap<String, String> map_lock_para_seqs = this.projectManager.getLockedSegSeqs();
      LinkedHashMap<String, String> map_review_para_seqs = this.projectManager.getReviewSegSeqs();
      LinkedHashMap<String, String> map_ignore_para_seqs = this.projectManager.getIgnoreSegSeqs();
      int nullcnt = this.projectManager.getNullCnt();
      model.addObject("prjnum", this.projectManager.getRevProject().getPrjNumber());
      model.addObject("srclang", this.projectManager.getRevProject().getSrcLang());
      model.addObject("trglang", this.projectManager.getRevProject().getTrgLang());
      model.addObject("isfareast", Boolean.toString(Locale.makeLocale(this.projectManager.getRevProject().getTrgLang()).isFarEast()));
      model.addObject("s_map", map_srcs);
      model.addObject("t_map", map_trgs);
      model.addObject("m_map", map_missing);
      model.addObject("lock_para_seq_map", map_lock_para_seqs);
      model.addObject("review_para_seq_map", map_review_para_seqs);
      model.addObject("ignore_para_seq_map", map_ignore_para_seqs);
      model.addObject("nullcnt", Integer.valueOf(nullcnt));
      System.out.println("displaying aligned file");
    }
    catch (Exception e)
    {
      System.out.println("auto-saved aligned files failed to be displayed");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/sessiontimesout"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView sessionTimesOut(HttpServletResponse response)
    throws Exception
  {
    System.out.println("saving data for timed out project...");
    
    ModelAndView model = null;
    try
    {
      this.isredirect = true;
      model = new ModelAndView("redirect:/rac/sessiontimesout");
    }
    catch (Exception e)
    {
      System.out.println("failed to display time out page");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/sessiontimesout"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView sessionTimesOutGet(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      if (this.isredirect)
      {
        System.out.println("reading time out page");
        
        model = new ModelAndView("error_message");
        model.addObject("message", "Session Times Out ....");
        this.isredirect = false;
        
        System.out.println("displaying time out page");
      }
      else
      {
        model = new ModelAndView("redirect:/rac/entry");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/project"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView backToMainPage(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      model = new ModelAndView("project");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/project"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView backToMainPageGet(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      model = new ModelAndView("redirect:/rac/entry");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/entry"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView backToIndexPage(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      model = new ModelAndView("redirect:/");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/errorloadingaligner"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView errorLoadingAligner(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      this.isredirect = true;
      model = new ModelAndView("redirect:/rac/errorloadingaligner");
    }
    catch (Exception e)
    {
      System.out.println("failed to display error page");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/errorloadingaligner"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain;charset=UTF-8"})
  @ResponseBody
  public ModelAndView errorLoadingAlignerGet(HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      if (this.isredirect)
      {
        System.out.println("reading error page");
        
        model = new ModelAndView("error_message");
        model.addObject("message", "Error loading the Aligner page ....");
        this.isredirect = false;
        
        System.out.println("displaying error page");
      }
      else
      {
        model = new ModelAndView("redirect:/rac/entry");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/save"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void save(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("saving file...");
    try
    {
      Thread.sleep(2000L);
      String decodedString = URLDecoder.decode(arr, "UTF-8");
      JSONObject json = new JSONObject(decodedString);
      

      JSONArray seqs = (JSONArray)json.get("arr1");
      JSONArray missings = (JSONArray)json.get("arr2");
      JSONArray locks = (JSONArray)json.get("arr3");
      JSONArray segaligned = (JSONArray)json.get("arr4");
      JSONArray targets = (JSONArray)json.get("arr5");
      JSONArray missing_targets = (JSONArray)json.get("arr6");
      int nullcnt = json.getInt("nullcnt");
      
      this.projectManager.updateAlignedFile(seqs, missings, locks, segaligned, targets, missing_targets, nullcnt);
      System.out.println("files saved");
    }
    catch (Exception e)
    {
      System.out.println("files failed to be saved");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/save_seg"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void save_seg(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("saving file...");
    try
    {
      Thread.sleep(2000L);
      String decodedString = URLDecoder.decode(arr, "UTF-8");
      JSONObject json = new JSONObject(decodedString);
      

      JSONArray targets = (JSONArray)json.get("arr1");
      JSONArray trg_seqs = (JSONArray)json.get("arr2");
      JSONArray missing_targets = (JSONArray)json.get("arr3");
      JSONArray missing_trg_seqs = (JSONArray)json.get("arr4");
      JSONArray locks = (JSONArray)json.get("arr5");
      int nullcnt = json.getInt("nullcnt");
      JSONArray edited = (JSONArray)json.get("arr6");
      JSONArray review = (JSONArray)json.get("arr7");
      JSONArray ignore = (JSONArray)json.get("arr8");
      
      this.projectManager.updateAlignedFile_seg(targets, trg_seqs, missing_targets, missing_trg_seqs, locks, nullcnt, edited, review, ignore);
      System.out.println("files saved");
    }
    catch (Exception e)
    {
      System.out.println("files failed to be saved");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    System.out.println("updating project progress info..");
    try
    {
      String infofile = this.projectManager.getRevProject().getProjectInfoFile();
      if (!new File(infofile).exists())
      {
        System.out.println("failed to update project info: info file not found");
        return;
      }
      FileInputStream in = new FileInputStream(infofile);
      Properties props = new Properties();
      props.load(in);
      
      String progress = "";
      String alignedxml = this.projectManager.getRevProject().getAlignedXmlPath();
      if (new File(alignedxml).exists()) {
        progress = this.projectManager.readAlignedFileForRatio(alignedxml);
      }
      if (!progress.equals(""))
      {
        props.setProperty("raprojectprogress", progress);
        FileOutputStream out = new FileOutputStream(infofile);
        props.store(out, "RA PROJECT INFOMATION");
      }
      else
      {
        System.out.println("failed to update project info: failed to calculate project progress");
        return;
      }
    }
    catch (Exception e)
    {
      System.out.println("failed to update project info");
      e.printStackTrace();
    }
  }
  
  @RequestMapping(value={"/auto_save_seg"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void auto_save_seg(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("auto-saving file...");
    try
    {
      String decodedString = URLDecoder.decode(arr, "UTF-8");
      JSONObject json = new JSONObject(decodedString);
      

      JSONArray targets = (JSONArray)json.get("arr1");
      JSONArray trg_seqs = (JSONArray)json.get("arr2");
      JSONArray missing_targets = (JSONArray)json.get("arr3");
      JSONArray missing_trg_seqs = (JSONArray)json.get("arr4");
      JSONArray locks = (JSONArray)json.get("arr5");
      int nullcnt = json.getInt("nullcnt");
      JSONArray edited = (JSONArray)json.get("arr6");
      JSONArray review = (JSONArray)json.get("arr7");
      JSONArray ignore = (JSONArray)json.get("arr8");
      
      this.projectManager.auto_updateAlignedFile_seg(targets, trg_seqs, missing_targets, missing_trg_seqs, locks, nullcnt, edited, review, ignore);
      System.out.println("files copy saved");
    }
    catch (Exception e)
    {
      System.out.println("files copy failed to be saved");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/cancel_session_and_auto_save_seg"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void cancel_session_and_auto_save_seg(HttpServletResponse response, HttpSession httpSession, @RequestBody String arr)
    throws Exception
  {
    System.out.println("auto-saving file...");
    try
    {
      String decodedString = URLDecoder.decode(arr, "UTF-8");
      JSONObject json = new JSONObject(decodedString);
      

      JSONArray targets = (JSONArray)json.get("arr1");
      JSONArray trg_seqs = (JSONArray)json.get("arr2");
      JSONArray missing_targets = (JSONArray)json.get("arr3");
      JSONArray missing_trg_seqs = (JSONArray)json.get("arr4");
      JSONArray locks = (JSONArray)json.get("arr5");
      int nullcnt = json.getInt("nullcnt");
      JSONArray edited = (JSONArray)json.get("arr6");
      JSONArray review = (JSONArray)json.get("arr7");
      JSONArray ignore = (JSONArray)json.get("arr8");
      
      this.projectManager.auto_updateAlignedFile_seg(targets, trg_seqs, missing_targets, missing_trg_seqs, locks, nullcnt, edited, review, ignore);
      System.out.println("files copy saved");
    }
    catch (Exception e)
    {
      System.out.println("files copy failed to be saved");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    System.out.println("cancel current session...");
    try
    {
      this.projectManager.cancelexcution();
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        httpSession.invalidate();
        this.sessionCollector.removeSession(httpSession);
        System.out.println("current session cancelled");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
    }
    catch (Exception e)
    {
      System.out.println("current session failed to be cancelled");
      System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/sendprjinfo"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void getprjinfo(HttpServletResponse response, @RequestBody String arr, HttpSession httpSession)
    throws Exception
  {
    System.out.println("saving project info...");
    try
    {
      arr = URLDecoder.decode(arr, "UTF-8");
      JSONArray seqs = new JSONArray(arr);
      this.projectManager.getRevProject().setPrjNumber(seqs.getString(0));
      

      File prjfolder = new File(this.projectManager.getRevProject().getPrjWorkingPath());
      this.projectManager.setPrjFolder(prjfolder.getAbsolutePath());
      if (prjfolder.exists()) {
        prjfolder.delete();
      } else {
        prjfolder.mkdir();
      }
      this.projectManager.getRevProject().setPrjCreationDate(seqs.getString(1));
      
      HashMap<String, String> langcodemap = ProjectManager.getLangeuageCodeMap();
      
      this.projectManager.getRevProject().setSrcLang((String)langcodemap.get(seqs.getString(2).trim()));
      this.projectManager.getRevProject().setTrgLang((String)langcodemap.get(seqs.getString(3).trim()));
      


      Properties properties = new Properties();
      if (new File(this.projectManager.getRevProject().getProjectInfoFile()).exists())
      {
        FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
        properties.load(in);
      }
      properties.setProperty("raprojectnumber", seqs.getString(0));
      properties.setProperty("raprojectcreationdate", seqs.getString(1));
      properties.setProperty("raprojectsourcelanguagecode", (String)langcodemap.get(seqs.getString(2).trim()));
      properties.setProperty("raprojecttargetlanguagecode", (String)langcodemap.get(seqs.getString(3).trim()));
      properties.setProperty("raprojectsubmissionname", seqs.getString(4));
      
      File file = new File(this.projectManager.getRevProject().getPrjWorkingPath() + File.separator + "_.info");
      FileOutputStream fileOut = new FileOutputStream(file);
      properties.store(fileOut, "RA PROJECT INFOMATION");
      fileOut.close();
      
      this.projectManager.setPrjInfoFile(file.getAbsolutePath());
      System.out.println("project info saved");
      
      if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
    	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
      }
    }
    catch (Exception e)
    {
      System.out.println("project info failed to be saved");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/cloneproject"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void cloneproject(HttpServletResponse response, @RequestBody String arr, HttpSession httpSession)
    throws Exception
  {
    System.out.println("cloning project...");
    try
    {
      Thread.sleep(2000L);
      arr = URLDecoder.decode(arr, "UTF-8");
      JSONArray seqs = new JSONArray(arr);
      System.out.println(seqs);
      String trgprjnum = seqs.getString(0);
      String trguser = seqs.getString(1);
      String timestamp = seqs.getString(2);
      String currentuser = getUserName();
      
      this.projectManager.cloneProject(trgprjnum, trguser, timestamp, currentuser);
      
      System.out.println("project cloned");
    }
    catch (Exception e)
    {
      System.out.println("project failed to be cloned");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/export"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void saveandexport(HttpServletResponse response)
    throws Exception
  {
    System.out.println("exporting file...");
    try
    {
      Thread.sleep(2000L);
      String exportfile = this.projectManager.zipPorject();
      response.setContentType("application/zip");
      response.setHeader("Content-disposition", "attachment; filename=\"" + new File(exportfile).getName() + "\"");
      FileCopyUtils.copy(Files.readAllBytes(Paths.get(exportfile, new String[0])), response.getOutputStream());
      new File(this.projectManager.getRevProject().getExportZipFolderPath()).delete();
      System.out.println("files exported");
    }
    catch (Exception e)
    {
      System.out.println("files failed to be exported");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/gettranslationkit"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void getrasnlationkit(HttpServletResponse response)
    throws Exception
  {
    System.out.println("exporting translation kit...");
    try
    {
      Thread.sleep(2000L);
      this.projectManager.createTranslationKit();
      
      String exportfile = this.projectManager.zipTranslationKit();
      response.setContentType("application/zip");
      response.setHeader("Content-disposition", "attachment; filename=\"" + new File(exportfile).getName() + "\"");
      OutputStream ro = response.getOutputStream();
      FileCopyUtils.copy(Files.readAllBytes(Paths.get(exportfile, new String[0])), ro);
      new File(this.projectManager.getRevProject().getExportZipFolderPath()).delete();
      
      FileInputStream in = new FileInputStream(this.projectManager.getRevProject().getProjectInfoFile());
      Properties props = new Properties();
      props.load(in);
      props.setProperty("isoutfortrans", "true");
      FileOutputStream out = new FileOutputStream(this.projectManager.getRevProject().getProjectInfoFile());
      props.store(out, "RA PROJECT INFOMATION");
      out.close();
      in.close();
      ro.close();
      
      System.out.println("translation kit exported");
    }
    catch (Exception e)
    {
      System.out.println("translation kit failed to be exported");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/gettmnotreviewed"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void gettmnotreviewed(HttpServletResponse response)
    throws Exception
  {
    System.out.println("exporting translation memory not reviewed...");
    try
    {
      Thread.sleep(2000L);
      String tm = this.projectManager.createTranslationMemory(this.projectManager.getRevProject().getPrjNumber(), false);
      
      response.setContentType("text/plain");
      response.setHeader("Content-disposition", "attachment; filename=\"" + new File(tm).getName() + ".txt\"");
      FileCopyUtils.copy(Files.readAllBytes(Paths.get(tm, new String[0])), response.getOutputStream());
      
      System.out.println("translation memory not reviewed exported");
    }
    catch (Exception e)
    {
      System.out.println("translation memory not reviewed failed to be exported");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/gettmreviewed"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void gettmreviewed(HttpServletResponse response)
    throws Exception
  {
    System.out.println("exporting translation memory reviewed...");
    try
    {
      Thread.sleep(2000L);
      String tm = this.projectManager.createTranslationMemory(this.projectManager.getRevProject().getPrjNumber(), true);
      
      response.setContentType("text/plain");
      response.setHeader("Content-disposition", "attachment; filename=\"" + new File(tm).getName() + ".txt\"");
      FileCopyUtils.copy(Files.readAllBytes(Paths.get(tm, new String[0])), response.getOutputStream());
      
      System.out.println("translation memory reviewed exported");
    }
    catch (Exception e)
    {
      System.out.println("translation memory reviewed failed to be exported");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/merge"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void merge(HttpServletResponse response, HttpServletRequest request)
    throws Exception
  {
    System.out.println("merging to target file...");
    try
    {
      Thread.sleep(2000L);
      String preservefmt = request.getParameter("preservefmt");
      this.projectManager.createTargetPackage(preservefmt);
      
      System.out.println("target file and review log created");
    }
    catch (Exception e)
    {
      System.out.println("target file and review log failed to be merged");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/startnewsession"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void startNewSession(HttpServletResponse response, HttpSession httpSession, HttpServletRequest request)
    throws Exception
  {
    System.out.println("start new session...");
    
    String prjid = request.getParameter("prjid");
    try
    {
      if (!this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        this.sessionCollector.getSessionMap().put(httpSession, getUserName() + "_" + prjid);
        if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
      	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
        }
        System.out.println("new RA session created");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
      else
      {
        System.out.println("prjid: " + prjid);
        System.out.println("active project found");
        response.sendError(500, "active project found");
        response.setStatus(500);
      }
    }
    catch (Exception e)
    {
      System.out.println("new session failed to be created");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/backtomain"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void backtomain(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    try
    {
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
    	this.sessionCollector.removeSession(httpSession);
        System.out.println("prj removed: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
    }
    catch (Exception e)
    {
      System.out.println("back to main failed");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/checkactiveprj"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void checkactiveprj(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    System.out.println("start new session...");
    try
    {
      if (!this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        System.out.println("no active project, can continue");
      }
      else
      {
        System.out.println("active project found, cannot continue");
        response.sendError(500, "active project found, cannot continue");
        response.setStatus(500);
      }
    }
    catch (Exception e)
    {
      System.out.println("failed continue");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/cancelsession"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void cancelSession(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    System.out.println("cancel current session...");
    try
    {
      this.projectManager.cancelexcution();
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        httpSession.invalidate();
        this.sessionCollector.removeSession(httpSession);
        System.out.println("current session cancelled");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
    }
    catch (Exception e)
    {
      System.out.println("current session failed to be cancelled");
      System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/gettarget"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void gettargetfile(HttpServletResponse response)
    throws Exception
  {
    System.out.println("exporting target package...");
    try
    {
      Thread.sleep(2000L);
      String exportfile = this.projectManager.zipForTargetPackage();
      response.setContentType("application/zip");
      response.setHeader("Content-disposition", "attachment; filename=\"" + new File(exportfile).getName() + "\"");
      FileCopyUtils.copy(Files.readAllBytes(Paths.get(exportfile, new String[0])), response.getOutputStream());
      new File(this.projectManager.getRevProject().getExportZipFolderPath()).delete();
      
      System.out.println("target file and review log exported");
    }
    catch (Exception e)
    {
      System.out.println("target file and review log failed to be exported");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/updateparagraph"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, List<String>> updateparagraph(HttpServletResponse response, @RequestBody String arr)
    throws Exception
  {
    System.out.println("updating paragraph...");
    HashMap<String, List<String>> segs = new HashMap();
    try
    {
      arr = URLDecoder.decode(arr, "UTF-8");
      
      segs = this.projectManager.updateParagraph(arr);
      
      System.out.println("paragraph updated...");
    }
    catch (Exception e)
    {
      System.out.println("failed to update paragraph");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return segs;
  }
  
  @RequestMapping(value={"/checkifsessionvalid"}, method={org.springframework.web.bind.annotation.RequestMethod.GET}, produces={"text/plain"})
  @ResponseBody
  public void delete(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    try
    {
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        if (this.projectManager.isSessionTimesOut(httpSession))
        {
          System.out.println("session is about to time out");
          
          response.sendError(500, "session not valid");
          response.setStatus(500);
        }
      }
      else
      {
        response.sendError(500, "session not found");
        response.setStatus(500);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/updatelastaccesstime"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void updateLastAccessTime(HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    try
    {
      if(this.sessionCollector.getSessionMap().containsKey(httpSession)){
    	  this.sessionCollector.getAccessTimeMap().put(httpSession, System.nanoTime());
      }
    }
    catch (Exception e)
    {
      System.out.println("last access time failed to be updated");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/login"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public ModelAndView loginPage(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      model = new ModelAndView("loginPage");
    }
    catch (Exception e)
    {
      System.out.println("failed to load login page");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/logout"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public ModelAndView logoutPage(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        this.sessionCollector.removeSession(httpSession);
        System.out.println("current session cancelled");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null) {
        new SecurityContextLogoutHandler().logout(request, response, auth);
      }
      model = new ModelAndView("redirect:/rac/entry");
    }
    catch (Exception e)
    {
      System.out.println("failed to logout");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/logoutwithoutredirect"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public void logoutMute(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    try
    {
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        this.sessionCollector.removeSession(httpSession);
        System.out.println("current session cancelled");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
      httpSession.invalidate();
    }
    catch (Exception e)
    {
      System.out.println("failed to logout");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  @RequestMapping(value={"/closeproject"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public ModelAndView closeproject(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession)
    throws Exception
  {
    ModelAndView model = null;
    try
    {
      if (this.sessionCollector.getSessionMap().containsKey(httpSession))
      {
        this.sessionCollector.removeSession(httpSession);
        System.out.println("current session cancelled");
        System.out.println("prj collected: " + httpSession.getId() + " pool size: " + this.sessionCollector.getSessionMap().size());
      }
      model = new ModelAndView("redirect:/rac/entry");
    }
    catch (Exception e)
    {
      System.out.println("failed to logout");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return model;
  }
  
  @RequestMapping(value={"/setupuser"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public HashMap<String, String> getUserInfo(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    System.out.println("reading user infomations...");
    HashMap<String, String> userinfo = new HashMap();
    try
    {
      String fake_username = this.projectManager.getRevProject().getUsername();
      String real_username = getUserName();
      String username = real_username;
      if (fake_username != null) {
        username = fake_username;
      } else {
        this.projectManager.getRevProject().setUsername(real_username);
      }
      String userfolder = this.projectManager.getRevProject().getBaseWorkingPath() + File.separator + username;
      if (!new File(userfolder).exists()) {
        new File(userfolder).mkdir();
      }
      userinfo.put("username", real_username);
      userinfo.put("isadmin", Boolean.toString(isAdmin()));
    }
    catch (Exception e)
    {
      System.out.println("failed to fetch userinfo");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
    return userinfo;
  }
  
  @RequestMapping(value={"/reliefproject"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public void reliefproject(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    System.out.println("reliefing project...");
    try
    {
      Thread.sleep(2000L);
      String value = request.getParameter("value");
      HttpSession httpsession = null;
      Iterator it = this.sessionCollector.getSessionMap().entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry<HttpSession, String> entry = (Map.Entry)it.next();
        if (((String)entry.getValue()).equals(value)) {
          httpsession = (HttpSession)entry.getKey();
        }
      }
      if (httpsession != null)
      {
        this.sessionCollector.removeSession(httpsession);
        System.out.println("project reliefed...");
      }
      else
      {
        System.out.println("active project not found...");
      }
    }
    catch (Exception e)
    {
      System.out.println("failed to relief project");
      e.printStackTrace();
      response.sendError(500, e.getMessage());
      response.setStatus(500);
    }
  }
  
  private String getUserName()
    throws Exception
  {
    String userName = "";
    try
    {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if ((principal instanceof UserDetails)) {
        userName = ((UserDetails)principal).getUsername();
      } else {
        userName = principal.toString();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return userName;
  }
  
  private boolean isAdmin()
    throws Exception
  {
    boolean isadmin = false;
    try
    {
      Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
      for (GrantedAuthority authority : authorities)
      {
        System.out.println("role: " + authority.getAuthority());
        if (authority.getAuthority().equals("ROLE_ADMIN"))
        {
          isadmin = true;
          break;
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return isadmin;
  }
}
