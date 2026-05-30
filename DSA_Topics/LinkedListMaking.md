class MyLinkedList {

    // ================= NODE CLASS =================
    class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    // head of linked list
    Node head;

    // =====================================================
    // 1. ADD AT END
    // =====================================================

    public void addLast(int data) {

        Node newNode = new Node(data);

        // if list empty
        if (head == null) {
            head = newNode;
            return;
        }

        Node temp = head;

        while (temp.next != null) {
            temp = temp.next;
        }

        temp.next = newNode;
    }

    // =====================================================
    // 2. ADD AT BEGINNING
    // =====================================================

    public void addFirst(int data) {

        Node newNode = new Node(data);

        newNode.next = head;

        head = newNode;
    }

    // =====================================================
    // 3. DELETE FIRST NODE
    // =====================================================

    public void deleteFirst() {

        if (head == null) {
            System.out.println("List is empty");
            return;
        }

        head = head.next;
    }

    // =====================================================
    // 4. DELETE LAST NODE
    // =====================================================

    public void deleteLast() {

        if (head == null) {
            System.out.println("List is empty");
            return;
        }

        // single node
        if (head.next == null) {
            head = null;
            return;
        }

        Node secondLast = head;
        Node lastNode = head.next;

        while (lastNode.next != null) {

            secondLast = secondLast.next;
            lastNode = lastNode.next;
        }

        secondLast.next = null;
    }

    // =====================================================
    // 5. PRINT LINKED LIST
    // =====================================================

    public void printList() {

        if (head == null) {
            System.out.println("List is empty");
            return;
        }

        Node temp = head;

        while (temp != null) {

            System.out.print(temp.data + " -> ");

            temp = temp.next;
        }

        System.out.println("null");
    }

    // =====================================================
    // 6. SIZE OF LINKED LIST
    // =====================================================

    public int size() {

        int count = 0;

        Node temp = head;

        while (temp != null) {

            count++;

            temp = temp.next;
        }

        return count;
    }

    // =====================================================
    // 7. SEARCH ELEMENT
    // =====================================================

    public boolean search(int key) {

        Node temp = head;

        while (temp != null) {

            if (temp.data == key) {
                return true;
            }

            temp = temp.next;
        }

        return false;
    }

    // =====================================================
    // 8. INSERT AT INDEX
    // =====================================================

    public void insertAtIndex(int index, int data) {

        if (index < 0 || index > size()) {
            System.out.println("Invalid index");
            return;
        }

        if (index == 0) {
            addFirst(data);
            return;
        }

        Node newNode = new Node(data);

        Node temp = head;

        for (int i = 0; i < index - 1; i++) {
            temp = temp.next;
        }

        newNode.next = temp.next;

        temp.next = newNode;
    }

    // =====================================================
    // 9. DELETE AT INDEX
    // =====================================================

    public void deleteAtIndex(int index) {

        if (index < 0 || index >= size()) {
            System.out.println("Invalid index");
            return;
        }

        if (index == 0) {
            deleteFirst();
            return;
        }

        Node temp = head;

        for (int i = 0; i < index - 1; i++) {
            temp = temp.next;
        }

        temp.next = temp.next.next;
    }

    // =====================================================
    // 10. REVERSE LINKED LIST
    // =====================================================

    public void reverse() {

        Node prev = null;
        Node curr = head;

        while (curr != null) {

            Node nextNode = curr.next;

            curr.next = prev;

            prev = curr;

            curr = nextNode;
        }

        head = prev;
    }

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {

        MyLinkedList list = new MyLinkedList();

        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        list.addFirst(5);

        list.printList();

        list.deleteFirst();

        list.printList();

        list.deleteLast();

        list.printList();

        list.insertAtIndex(1, 100);

        list.printList();

        list.deleteAtIndex(1);

        list.printList();

        System.out.println("Size: " + list.size());

        System.out.println("Search 20: " + list.search(20));

        list.reverse();

        list.printList();
    }
}