class Solution {
    public int[][] merge(int[][] intervals) {
        //need to declare 2d interval for ans
        int n=intervals.length;
         Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        //  ArrayList<ArrayList<Integer>ans=new ArrayList<>();
          //ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
          ArrayList<ArrayList<Integer>> ans=new ArrayList<>();
          ArrayList<Integer> row1 = new ArrayList<>();
           row1.add(intervals[0][0]);
           row1.add(intervals[0][1]);
           ans.add(row1);
        for(int i=1;i<n;i++){
            //jo bhi data structure hai us me sa latest wala inteal yha par dekhan hoga
          //  ArrayList<Integer> last = ans.get(ans.size()-1);
          int nn=ans.size()-1;
          int a = ans.get(nn).get(0);
          int b = ans.get(nn).get(1);
          int c=intervals[i][0];
          int d=intervals[i][1];
            
            if(b>=c && b<d){
                ans.get(nn).set(1,d);
            }
            else if(b>=c && b>=d){
               ans.get(nn).set(1,b);
            }
            else{
                ArrayList<Integer> row2 = new ArrayList<>();
           row2.add(c);
           row2.add(d);
             ans.add(row2);
            }


        }
        int[][] result = new int[ans.size()][2];

for(int i = 0; i < ans.size(); i++) {
    result[i][0] = ans.get(i).get(0);
    result[i][1] = ans.get(i).get(1);
}
return result;
    }
}


// willl optimize the syntx more
class Solution {

    public int[][] merge(int[][] intervals) {

        int n = intervals.length;

        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);

        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();

        // first interval add
        ArrayList<Integer> first = new ArrayList<>();
        first.add(intervals[0][0]);
        first.add(intervals[0][1]);

        ans.add(first);

        for (int i = 1; i < n; i++) {

            ArrayList<Integer> last = ans.get(ans.size() - 1);

            int lastEnd = last.get(1);

            int currStart = intervals[i][0];
            int currEnd = intervals[i][1];

            // overlap
            if (lastEnd >= currStart) {

                last.set(1, Math.max(lastEnd, currEnd));

            } else {

                // new interval
                ArrayList<Integer> newInterval = new ArrayList<>();

                newInterval.add(currStart);
                newInterval.add(currEnd);

                ans.add(newInterval);
            }
        }

        // convert to int[][]
        int[][] result = new int[ans.size()][2];

        for (int i = 0; i < ans.size(); i++) {

            result[i][0] = ans.get(i).get(0);
            result[i][1] = ans.get(i).get(1);
        }

        return result;
    }
}