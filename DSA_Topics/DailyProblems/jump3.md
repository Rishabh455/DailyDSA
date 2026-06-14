class Solution {
    public boolean canReach(int[] arr, int start) {

                    // through DFS
                    // if(start<0 ||start>=arr.length ||arr[start]<0)return false;
                    // if(arr[start]==0){return true;}

                    // arr[start]*=-1;
                    // boolean a=canReach(arr, start+arr[start]);
                    // boolean b=canReach(arr, start-arr[start]);
                    // return a||b;

                    //through BSF
                    Queue<Integer>queue=new LinkedList<>();
                    queue.add(start);
                    while(!queue.isEmpty()){
                        int curr=queue.poll();
                        
                        if(arr[curr]==0)return true;
                        if(arr[curr]<0){
                            continue;
                        }
                        if(curr+arr[curr]<arr.length){
                            queue.add(curr+arr[curr]);
                        }
                        if(curr-arr[curr]>=0){
                            queue.add(curr-arr[curr]);
                        }
                        arr[curr]*=-1;
                    }
                    return false;
                    

    }
}

https://www.youtube.com/watch?v=FVkYM-GjiQQ