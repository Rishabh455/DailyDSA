how to declate a queue in java\
Queue<Integer> queue= new LinkedList<>();
queue.add(2);
queue.poll(); this will pop the elmemtn as well as display the emement./


how to sort character of string 
 sort(currStr.begin(), currStr.end())
            char[] chars = currStr.toCharArray();
            Arrays.sort(chars);

            // sorted string becomes key
            String sortedStr = new String(chars);

            // myMap[currStr].push_back(strs[i])
            map.putIfAbsent(sortedStr, new ArrayList<>());
            map.get(sortedStr).add(strs[i]);


 if String [] str="dsfhaksdf";
then we can do this Arrays.sort(str);
but if we have String ab="jskdfljsa";
then we have to convert it in char [] ary then o0nly we can short it.          

 char [] ss=s.toCharArray();
        Arrays.sort(ss);
        char[] tt=t.toCharArray();
        Arrays.sort(tt);

        String sorteds=Arrays.toString(ss);
        String sortedt=Arrays.toString(tt);
        ya phir aisa likha ek hi baat hai
        String sorteds=new String(ss);
        String sortedt=new String(tt);
         System.out.println(ss +"::"+ tt);  // ye aisa output kuyu de rha [C@87aac27::[C@3e3abc88 why not this  aaagmnr::aaagmnr
       
        return sorteds.equals(sortedt);
        return Arrays.toString(ss).equals(Arrays.toString(tt));
