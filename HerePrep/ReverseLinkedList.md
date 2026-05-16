206. Reverse Linked List
Companies
Given the head of a singly linked list, reverse the list, and return the reversed list.
class Solution {
    public ListNode reverseList(ListNode head) {
        ListNode curr=head;
        ListNode prev=null;
        while(curr!=null){
            ListNode tmp=curr.next;
            curr.next=prev;
            prev=curr;
            curr=tmp;;
        }
        return prev;
    }
}