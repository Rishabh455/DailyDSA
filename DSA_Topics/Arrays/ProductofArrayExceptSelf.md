whatch vide3o of mik link:
https://www.youtube.com/watch?v=ku4oITayEmk&t=31s
approach one 
class Solution {
    public int[] productExceptSelf(int[] nums) {
        int n=nums.length;
        int[]res=new int[n];
        int []right=new int [n];
         left[0]=1;
         right[n-1]=1;
        for(int i=1;i<n;i++){
            left[i]=left[i-1]*nums[i-1];
        }
        for(int i=n-2;i>=0;i--){
            right[i]=right[i+1]*nums[i+1];
        }
        for(int i=0;i<n;i++){
            nums[i]=left[i]*right[i];
        }
        return nums;
    }
}

ab hum ye do array bana ye usko ek hi array bhi result array me bhar denge

class Solution {
    public int[] productExceptSelf(int[] nums) {
        int [] res=new int[nums.length];
           int n=nums.length;
           res[0]=1;
           for(int i=1;i<n;i++){
               res[i]=res[i-1]*nums[i-1];  ye to same hi hai bus
           }
           int right=1;
           for(int i=n-1;i>=0;i--){
            res[i]=res[i]*right;
            right=right*nums[i]; // ye line aa gyi agalg se 
           }
           return res;
    }
}

