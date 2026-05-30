class Solution {
    public boolean isValid(String s) {
        int n=s.length();
        Stack<Character>st=new Stack<>();
        for(int i=0;i<n;i++){
        char curr=s.charAt(i);
        if(curr=='(' || curr=='['|| curr=='{'){
            st.add(curr);
        }
         else{
         if(st.size()==0){return false;}  // ye rlo ki agar stack khali ho gya mtlb invalid case hai to return fasle
         char m=st.peek();
          if(m=='('&& curr==')'||
             m=='['&& curr==']'||
             m=='{'&& curr=='}'){
                 st.pop();
             }
             else return false;
         }
        }
        return st.size()==0;
    }
}