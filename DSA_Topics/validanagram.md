 char [] ss=s.toCharArray();
        Arrays.sort(ss);
        char[] tt=t.toCharArray();
        Arrays.sort(tt);

        String sorteds=Arrays.toString(ss);
        String sortedt=Arrays.toString(tt);
         System.out.println(ss +"::"+ tt);  // ye aisa output kuyu de rha [C@87aac27::[C@3e3abc88 why not this  aaagmnr::aaagmnr
       
        return sorteds.equals(sortedt);
        return Arrays.toString(ss).equals(Arrays.toString(tt));


        then we have different way to solve the same

        class Solution {
    public boolean isAnagram(String s, String t) {
       int[] f1=new int [26];
       int[]f2=new int [26];
       if(s.length()!=t.length())return false;
       for(int i=0;i<s.length();i++){
            char sa=s.charAt(i);
            char st=t.charAt(i);
            f1[sa-'a']++;
            f2[st-'a']++;
       }
       for(int i=0;i<26;i++){
        if(f1[i]!=f2[i])return false;
       }
       return true;
    }
}