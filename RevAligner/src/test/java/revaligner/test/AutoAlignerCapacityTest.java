package revaligner.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import revaligner.service.FileAligner;
import revaligner.service.SessionCollector;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FileAligner.class)
public class AutoAlignerCapacityTest {
	
	private List<String[]> fileSets = new ArrayList<>();
	private String sourcelanguage = "en";
	private String targetlanguage = "es";
	private SessionCollector sessionCollector = new SessionCollector();
	
	@Before
	public void init() {
		System.out.println("initializing test data sets");
		int capacity = 10;
		String testbase = "C:\\Program Files (x86)\\pa\\paprjs\\testAutoAligner";
		String sourceprj = "C:\\Program Files (x86)\\pa\\paprjs\\testprj23";
		File sourcefiletocopy = new File(sourceprj + File.separator + "source" + File.separator + "EN.doc");
		File targetfiletocopy = new File(sourceprj + File.separator + "target" + File.separator + "ES.doc");
		try {
			FileUtils.cleanDirectory(new File(testbase));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 1; i <= capacity; i++){
			String prjfolder = testbase + File.separator + "testprj" + Integer.toString(i);
			new File(prjfolder).mkdirs();
			String sourcefolder = prjfolder + File.separator + "source";
			new File(sourcefolder).mkdirs();
			String targetfolder = prjfolder + File.separator + "target";
			new File(targetfolder).mkdirs();
			try {
				FileUtils.copyFileToDirectory(sourcefiletocopy, new File(sourcefolder));
				FileUtils.copyFileToDirectory(targetfiletocopy, new File(targetfolder));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] fileSet = new String[4];
			fileSet[0] = sourcefolder + File.separator + sourcefiletocopy.getName();
			fileSet[1] = targetfolder + File.separator + targetfiletocopy.getName();
			fileSet[2] = prjfolder;
			fileSet[3] = new File(prjfolder).getName();
			
			fileSets.add(fileSet);
		}
	}
	
	@Test
	public void testAutoAlignerCapacity() {
		
		ExecutorService executor = Executors.newFixedThreadPool(fileSets.size());
		
		int idx = 1;
		for(String[] fileSet : fileSets) {
			Runnable worker = new AutoAlignerJobWorker(fileSet, sourcelanguage, targetlanguage, sessionCollector, Integer.toString(idx++));
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
		for(String[] fileSet : fileSets) {
			String auto_aligner_output = fileSet[2] + File.separator + "auto-aligner_output.txt";
			assertTrue(new File(auto_aligner_output).exists());
		}
	}
}
