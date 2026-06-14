class Solution {
    public int getCommon(int[] nums1, int[] nums2) {
        Set<Integer>set=new HashSet<>();
        for(int i=0;i<nums1.length;i++){
            set.add(nums1[i]);
        }
        for(int i=0;i<nums2.length;i++){
            if(set.contains(nums2[i]))return nums2[i];
        }
        return -1;
    }
}

optimal approach

class Solution {
    public int getCommon(int[] nums1, int[] nums2) {
        int i=0,j=0;
        while(i<nums1.length && j<nums2.length){
                if(nums1[i]==nums2[j])return nums1[i];
                if(nums1[i]<nums2[j]){
                    i++;
                }
                else{j++;}
        }
        return -1;
    }
}