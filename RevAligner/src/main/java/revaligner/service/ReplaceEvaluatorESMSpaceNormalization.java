package revaligner.service;

import com.aspose.words.CompositeNode;
import com.aspose.words.IReplacingCallback;
import com.aspose.words.Node;
import com.aspose.words.ReplacingArgs;
import com.aspose.words.Run;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class ReplaceEvaluatorESMSpaceNormalization
  implements IReplacingCallback
{
  private String rep;
  
  public ReplaceEvaluatorESMSpaceNormalization(String rep)
  {
    this.rep = rep;
  }
  
  public int replacing(ReplacingArgs e)
    throws Exception
  {
    Node currentNode = e.getMatchNode();
    if (e.getMatchOffset() > 0) {
      currentNode = splitRun((Run)currentNode, e.getMatchOffset());
    }
    ArrayList runs = new ArrayList();
    
    int remainingLength = e.getMatch().group().length();
    while ((remainingLength > 0) && (currentNode != null))
    {
      if (currentNode.getText().length() > remainingLength) {
        break;
      }
      runs.add(currentNode);
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
      runs.add(currentNode);
    }
    for (Object orun : runs) {
      Run run = (Run)orun;
      run.setText(this.rep);
    }
    return 1;
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
