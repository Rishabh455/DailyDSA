# Code
```java []
class Solution {
    public void moveZeroes(int[] nums) {
        int n=nums.length;
        int countZero=0;
        int []ans=new int[n];
        int j=0;
        for(int i=0;i<n;i++){
            if(nums[i]==0){
                countZero++;
            }
            else{
                ans[j]=nums[i];
                j++;
            }
        }
        for(int i=0;i<countZero;i++){
            ans[j]=0;
            j++;
        }
         for(int i=0;i<n;i++){
          nums[i]=ans[i];
          
        }
       // nums=ans;
    }
}

first approach   O(n)

class Solution {
    public void moveZeroes(int[] nums) {
        int n=nums.length;
        int countZero=0;
        //int []ans=new int[n];
        int j=1,i=0;
        while(j<n)
        {
           if(nums[i]!=0){i++;}
           if(nums[j]!=0){
            int tmp=nums[i];
            nums[i]=nums[j];
            nums[j]=tmp;
           }
           j++;
        }
        
    }
}
