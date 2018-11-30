package revaligner.service;

import com.aspose.words.Comment;
import com.aspose.words.CommentRangeEnd;
import com.aspose.words.CommentRangeStart;
import com.aspose.words.CompositeNode;
import com.aspose.words.IReplacingCallback;
import com.aspose.words.Node;
import com.aspose.words.Paragraph;
import com.aspose.words.ReplaceAction;
import com.aspose.words.ReplacingArgs;
import com.aspose.words.Run;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

public class ReplaceEvaluatorCommentsPlaceHolders
  implements IReplacingCallback
{
  private List<String[]> notesinfo;
  private com.aspose.words.Document doc;
  private List<Run> collectedruns;
  
  public ReplaceEvaluatorCommentsPlaceHolders(List<String[]> notesinfo, com.aspose.words.Document doc, List<Run> collectedruns)
  {
    this.notesinfo = notesinfo;
    this.doc = doc;
    this.collectedruns = collectedruns;
  }
  
  public int replacing(ReplacingArgs e)
    throws Exception
  {
    Node currentNode = e.getMatchNode();
    if (e.getMatchOffset() > 0) {
      currentNode = splitRun((Run)currentNode, e.getMatchOffset());
    }
    //ArrayList runs = new ArrayList();
    
    int remainingLength = e.getMatch().group().length();
    while ((remainingLength > 0) && (currentNode != null))
    {
      if (currentNode.getText().length() > remainingLength) {
        break;
      }
      //runs.add(currentNode);
      collectedruns.add(0,(Run)currentNode);
      System.out.println("1: " + currentNode.getText());
      remainingLength -= currentNode.getText().length();
      do
      {
        currentNode = currentNode.getNextSibling();
        if (currentNode == null) {
          break;
        }
      } while (currentNode.getNodeType() != 21);
    }
    if ((currentNode != null) && (remainingLength > 0))
    {
      splitRun((Run)currentNode, remainingLength);
      //runs.add(currentNode);
      collectedruns.add(0,(Run)currentNode);
      System.out.println("2: " + currentNode.getText());
    }
    //for (Object orun : runs) {
      //Run run = (Run)orun;
      //String[] ss = run.getText().substring(2, run.getText().length()-2).split("_");
      //String[] noteinfo = this.notesinfo.get(Integer.parseInt(ss[1]));
      //System.out.println(ss[1] + " " + noteinfo[2] + " " + run.getText());
      /*if(ss[2].equals("s")){
    	  Comment comment = new Comment(doc);
    	  Paragraph para = new Paragraph(doc);
    	  Run newrun = new Run(doc, noteinfo[2]);
    	  para.getRuns().add(newrun);
    	  comment.getParagraphs().add(para);
    	  comment.setAuthor(noteinfo[0]);
    	  SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
    	  Date date = formatter.parse(noteinfo[1].replaceAll("Z$", "+0000"));
    	  comment.setDateTime(date);
    	  CommentRangeStart start = new CommentRangeStart(doc, Integer.parseInt(ss[1]));
    	  run.getParentNode().insertAfter(comment, run);
    	  run.getParentNode().insertAfter(start, comment);
      }else{
    	  CommentRangeEnd end = new CommentRangeEnd(doc, Integer.parseInt(ss[1]));
    	  run.getParentNode().insertAfter(end, run);
      }
      run.setText("");*/
    //}
    return ReplaceAction.SKIP;
  }
  
  private static Run splitRun(Run run, int position)
    throws Exception
  {
    Run afterRun = (Run)run.deepClone(true);
    afterRun.setText(run.getText().substring(position));
    run.setText(run.getText().substring(0, 0 + position));
    run.getParentNode().insertAfter(afterRun, run);
    return afterRun;
  }
}
