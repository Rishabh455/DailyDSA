```java
import java.util.*;

public class GroupAnagrams {

    public static List<List<String>> groupAnagrams(String[] strs) {

        // unordered_map<string, vector<string>>
        Map<String, List<String>> map = new HashMap<>();

        int n = strs.length;

        // for(int i = 0; i < n; i++)
        for (int i = 0; i < n; i++) {

            String currStr = strs[i];

            // sort(currStr.begin(), currStr.end())
            char[] chars = currStr.toCharArray();
            Arrays.sort(chars);

            // sorted string becomes key
            String sortedStr = new String(chars);

            // myMap[currStr].push_back(strs[i])
            map.putIfAbsent(sortedStr, new ArrayList<>());
            map.get(sortedStr).add(strs[i]);
        }

        // final answer
        List<List<String>> ans = new ArrayList<>();

        // for(auto pair : myMap)
        for (Map.Entry<String, List<String>> pair : map.entrySet()) {

            List<String> temp = new ArrayList<>();

            // for(auto word : pair.second)
            for (String word : pair.getValue()) {
                temp.add(word);
            }

            ans.add(temp);
        }

        return ans;
    }

    public static void main(String[] args) {

        String[] strs = {"eat", "tea", "tan", "ate", "nat", "bat"};

        List<List<String>> result = groupAnagrams(strs);

        System.out.println(result);
    }
}
```

### Time Complexity

* Sorting each string → `O(K log K)`
* For `N` strings → `O(N * K log K)`

Where:

* `N` = number of strings
* `K` = average length of each string

If we use character frequency count instead of sorting, then it can become:

```text
O(N * K)
```

because we avoid sorting.
