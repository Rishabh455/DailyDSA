class Solution {
    public int[] findThePrefixCommonArray(int[] A, int[] B) {
        int n=A.length;
        int max = 0;
        int []res=new int[n];
      

        // Frequency array
        int[] freq = new int[n + 1];

        // Store frequency
       
       int ans=0;
        for(int i=0;i<n;i++){
           
           freq[A[i]]++;
           if(freq[A[i]]==2){
             ans++;
           }
           freq[B[i]]++;
            if(freq[B[i]]==2){
             ans++;
           }
          
           res[i]=ans;
        }
        return res;
    }
}