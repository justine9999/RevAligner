package revaligner.test;

import revaligner.service.FileAligner;
import revaligner.service.SessionCollector;

public class AutoAlignerJobWorker implements Runnable {
	
	private String[] fileSet;
	private String sourcelanguage;
	private String targetlanguage;
	private SessionCollector sessionCollector;
	private String name;
	
	public AutoAlignerJobWorker(String[] fileSet, String sourcelanguage, String targetlanguage, SessionCollector sessionCollector, String name){
		this.fileSet = fileSet;
		this.sourcelanguage = sourcelanguage;
		this.targetlanguage = targetlanguage;
		this.sessionCollector = sessionCollector;
		this.name = name;
	}
	
	@Override
	public void run() {
		
		FileAligner fa = new FileAligner();
		fa.setSourceFile(fileSet[0]);
	    fa.setTargetFile(fileSet[1]);
	    fa.setPrjFolder(fileSet[2]);
	    fa.setSourceLanguage(sourcelanguage);
	    fa.setTargetLanguage(targetlanguage);
	    
		try{
			fa.createReformattedDocument("auto");
			
			fa.convertSourceToTxlf(true);
		    fa.convertSourceToTxlf(false);
		    fa.convertReformattedSourceToTxlf(true);
		    fa.convertReformattedTargetToTxlf(true); 
		    fa.convertReformattedTargetToTxlf(false);
		    
		    if (fa.verifyParas())
		    {
		        fa.createAlignedXML_auto(fileSet[3], sessionCollector);
		    }else{
		    	throw new Exception("cannot align " + name);
		    }
		    System.out.println("done " + name + "!");
		}catch(Exception ex){
			
		}
	}
}
