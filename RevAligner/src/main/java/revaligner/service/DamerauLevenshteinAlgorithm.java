package revaligner.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DamerauLevenshteinAlgorithm
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private final int deleteCost;
  private final int insertCost;
  private final int replaceCost;
  private final int swapCost;
  
  public DamerauLevenshteinAlgorithm(int deleteCost, int insertCost, int replaceCost, int swapCost)
  {
    if (2 * swapCost < insertCost + deleteCost) {
      throw new IllegalArgumentException("Unsupported cost assignment");
    }
    this.deleteCost = deleteCost;
    this.insertCost = insertCost;
    this.replaceCost = replaceCost;
    this.swapCost = swapCost;
  }
  
  public int execute(String source, String target)
  {
    if (source.length() == 0) {
      return target.length() * this.insertCost;
    }
    if (target.length() == 0) {
      return source.length() * this.deleteCost;
    }
    int[][] table = new int[source.length()][target.length()];
    Map<Character, Integer> sourceIndexByCharacter = new HashMap<Character, Integer>();
    if (source.charAt(0) != target.charAt(0)) {
      table[0][0] = Math.min(this.replaceCost, this.deleteCost + this.insertCost);
    }
    sourceIndexByCharacter.put(Character.valueOf(source.charAt(0)), Integer.valueOf(0));
    for (int i = 1; i < source.length(); i++)
    {
      int deleteDistance = table[(i - 1)][0] + this.deleteCost;
      int insertDistance = (i + 1) * this.deleteCost + this.insertCost;
      int matchDistance = i * this.deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : this.replaceCost);
      table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
    }
    for (int j = 1; j < target.length(); j++)
    {
      int deleteDistance = (j + 1) * this.insertCost + this.deleteCost;
      int insertDistance = table[0][(j - 1)] + this.insertCost;
      int matchDistance = j * this.insertCost + (source.charAt(0) == target.charAt(j) ? 0 : this.replaceCost);
      table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
    }
    for (int i = 1; i < source.length(); i++)
    {
      int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0 : -1;
      for (int j = 1; j < target.length(); j++)
      {
        Integer candidateSwapIndex = (Integer)sourceIndexByCharacter.get(Character.valueOf(target.charAt(j)));
        int jSwap = maxSourceLetterMatchIndex;
        int deleteDistance = table[(i - 1)][j] + this.deleteCost;
        int insertDistance = table[i][(j - 1)] + this.insertCost;
        int matchDistance = table[(i - 1)][(j - 1)];
        if (source.charAt(i) != target.charAt(j)) {
          matchDistance += this.replaceCost;
        } else {
          maxSourceLetterMatchIndex = j;
        }
        int swapDistance;
        if ((candidateSwapIndex != null) && (jSwap != -1))
        {
          int iSwap = candidateSwapIndex.intValue();
          int preSwapCost;
          if ((iSwap == 0) && (jSwap == 0)) {
            preSwapCost = 0;
          } else {
            preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
          }
          swapDistance = preSwapCost + (i - iSwap - 1) * this.deleteCost + (j - jSwap - 1) * this.insertCost + this.swapCost;
        }
        else
        {
          swapDistance = 2147483647;
        }
        table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
      }
      sourceIndexByCharacter.put(Character.valueOf(source.charAt(i)), Integer.valueOf(i));
    }
    return table[(source.length() - 1)][(target.length() - 1)];
  }
}
