import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Scanner;

// ==================== MAIN CLASS ====================
public class LibrarySystem {

    // ---------- Data Stores using our custom structures ----------
    private HashTable<Integer, Book> booksByIsbn;          // CO4
    private HashTable<Integer, User> usersById;            // CO4
    private LinkedList<BorrowRecord> borrowedRecords;      // CO2
    private Stack<String> actionStack;                     // CO3
    private Queue<Integer> finePaymentQueue;               // CO3
    private PriorityQueue<HoldRequest> holdRequests;       // CO3

    private User currentUser;
    private Scanner scanner;

    // ---------- Inner Classes for Data ----------
    static class Book {
        int id; String title, author, isbn, genre; int year, copies;
        
        Book(int id, String title, String author, String isbn, int year, String genre, int copies) {
            this.id = id; this.title = title; this.author = author; this.isbn = isbn;
            this.year = year; this.genre = genre; this.copies = copies;
        }
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getIsbn() { return isbn; }
        public int getYear() { return year; }
        public String getGenre() { return genre; }
        public int getCopies() { return copies; }
        public void setCopies(int copies) { this.copies = copies; }
        public String toString() {
            return String.format("ID:%d | %s by %s | ISBN:%s | %d | %s | Copies:%d",
                    id, title, author, isbn, year, genre, copies);
        }
    }

    static class User {
        int id; String name; String role;
        User(int id, String name, String role) { this.id = id; this.name = name; this.role = role; }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String toString() { return String.format("ID:%d | %s (%s)", id, name, role); }
    }

    static class BorrowRecord {
        int bookId, userId; LocalDate borrowDate, dueDate; double fine;
        BorrowRecord(int bookId, int userId, LocalDate borrowDate, LocalDate dueDate) {
            this.bookId = bookId; this.userId = userId; this.borrowDate = borrowDate; this.dueDate = dueDate; this.fine = 0.0;
        }
        public int getBookId() { return bookId; }
        public int getUserId() { return userId; }
        public LocalDate getBorrowDate() { return borrowDate; }
        public LocalDate getDueDate() { return dueDate; }
        public double getFine() { return fine; }
        public void setFine(double fine) { this.fine = fine; }
    }

    static class HoldRequest implements Comparable<HoldRequest> {
        int bookId; int requestCount;
        HoldRequest(int bookId, int count) { this.bookId = bookId; this.requestCount = count; }
        public int compareTo(HoldRequest other) { return Integer.compare(this.requestCount, other.requestCount); }
    }

    // ==================== CUSTOM DATA STRUCTURES ====================

    // ---------- CO2: Singly Linked List (generic) ----------
    static class LinkedList<T> {
        private class Node { T data; Node next; Node(T data) { this.data = data; } }
        private Node head; private int size;

        public void add(T data) {
            Node newNode = new Node(data);
            if (head == null) head = newNode;
            else { Node curr = head; while (curr.next != null) curr = curr.next; curr.next = newNode; }
            size++;
        }

        public boolean remove(T data) {
            if (head == null) return false;
            if (head.data.equals(data)) { head = head.next; size--; return true; }
            Node curr = head;
            while (curr.next != null && !curr.next.data.equals(data)) curr = curr.next;
            if (curr.next != null) { curr.next = curr.next.next; size--; return true; }
            return false;
        }

        public boolean contains(T data) {
            Node curr = head;
            while (curr != null) { if (curr.data.equals(data)) return true; curr = curr.next; }
            return false;
        }

        public void reverse() {
            Node prev = null, curr = head, next = null;
            while (curr != null) { next = curr.next; curr.next = prev; prev = curr; curr = next; }
            head = prev;
        }

        public int size() { return size; }
        public boolean isEmpty() { return head == null; }

        public java.util.Iterator<T> iterator() {
            return new java.util.Iterator<T>() {
                Node curr = head;
                public boolean hasNext() { return curr != null; }
                public T next() { T data = curr.data; curr = curr.next; return data; }
            };
        }
    }

    // ---------- CO4: Hash Table (separate chaining) ----------
    static class HashTable<K, V> {
        private static class Entry<K, V> { K key; V value; Entry<K,V> next; Entry(K k, V v) { key = k; value = v; } }
        private Entry<K, V>[] buckets; private int capacity = 16; private int size = 0;
        @SuppressWarnings("unchecked")
        public HashTable() { buckets = new Entry[capacity]; }
        private int hash(K key) { return Math.abs(key.hashCode()) % capacity; }
        public void put(K key, V value) {
            int idx = hash(key); Entry<K,V> newEntry = new Entry<>(key, value);
            if (buckets[idx] == null) buckets[idx] = newEntry;
            else {
                Entry<K,V> curr = buckets[idx];
                while (curr.next != null) { if (curr.key.equals(key)) { curr.value = value; return; } curr = curr.next; }
                if (curr.key.equals(key)) curr.value = value;
                else curr.next = newEntry;
            }
            size++;
        }
        public V get(K key) {
            Entry<K,V> curr = buckets[hash(key)];
            while (curr != null) { if (curr.key.equals(key)) return curr.value; curr = curr.next; }
            return null;
        }
        public V remove(K key) {
            int idx = hash(key); Entry<K,V> curr = buckets[idx], prev = null;
            while (curr != null) {
                if (curr.key.equals(key)) {
                    if (prev == null) buckets[idx] = curr.next;
                    else prev.next = curr.next;
                    size--; return curr.value;
                }
                prev = curr; curr = curr.next;
            }
            return null;
        }
        public int size() { return size; }
        public boolean isEmpty() { return size == 0; }
    }

    // ---------- CO3: Stack (array) ----------
    static class Stack<T> {
        private T[] arr; private int top; private int capacity;
        @SuppressWarnings("unchecked")
        public Stack(int cap) { capacity = cap; arr = (T[]) new Object[cap]; top = -1; }
        public void push(T item) { if (top == capacity-1) throw new RuntimeException("Stack overflow"); arr[++top] = item; }
        public T pop() { if (isEmpty()) throw new RuntimeException("Stack underflow"); return arr[top--]; }
        public T peek() { return isEmpty() ? null : arr[top]; }
        public boolean isEmpty() { return top == -1; }
    }

    // ---------- CO3: Circular Queue ----------
    static class Queue<T> {
        private T[] arr; private int front, rear, size, capacity;
        @SuppressWarnings("unchecked")
        public Queue(int cap) { capacity = cap; arr = (T[]) new Object[cap]; front = 0; rear = -1; size = 0; }
        public void enqueue(T item) { if (size == capacity) throw new RuntimeException("Queue full"); rear = (rear+1)%capacity; arr[rear] = item; size++; }
        public T dequeue() { if (isEmpty()) throw new RuntimeException("Queue empty"); T item = arr[front]; front = (front+1)%capacity; size--; return item; }
        public boolean isEmpty() { return size == 0; }
    }

    // ---------- CO3: Priority Queue (max-heap) ----------
    static class PriorityQueue<T extends Comparable<T>> {
        private T[] heap; private int size; private int capacity;
        @SuppressWarnings("unchecked")
        public PriorityQueue(int cap) { capacity = cap; heap = (T[]) new Comparable[cap+1]; size = 0; }
        public void insert(T item) { if (size == capacity) throw new RuntimeException("Heap full"); heap[++size] = item; swim(size); }
        public T deleteMax() { if (size == 0) return null; T max = heap[1]; swap(1, size--); sink(1); heap[size+1] = null; return max; }
        private void swim(int k) { while (k > 1 && heap[k/2].compareTo(heap[k]) < 0) { swap(k, k/2); k = k/2; } }
        private void sink(int k) { while (2*k <= size) { int j = 2*k; if (j < size && heap[j].compareTo(heap[j+1]) < 0) j++; if (heap[k].compareTo(heap[j]) >= 0) break; swap(k, j); k = j; } }
        private void swap(int i, int j) { T tmp = heap[i]; heap[i] = heap[j]; heap[j] = tmp; }
        public boolean isEmpty() { return size == 0; }
    }

    // ---------- CO1: Sorting Algorithms (generic with Comparator) ----------
    static class SortingAlgorithms {
        public static <T> void bubbleSort(T[] arr, Comparator<T> comp) {
            int n = arr.length;
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++)
                    if (comp.compare(arr[j], arr[j+1]) > 0) swap(arr, j, j+1);
        }
        public static <T> void selectionSort(T[] arr, Comparator<T> comp) {
            int n = arr.length;
            for (int i = 0; i < n-1; i++) {
                int minIdx = i;
                for (int j = i+1; j < n; j++)
                    if (comp.compare(arr[j], arr[minIdx]) < 0) minIdx = j;
                swap(arr, i, minIdx);
            }
        }
        public static <T> void insertionSort(T[] arr, Comparator<T> comp) {
            int n = arr.length;
            for (int i = 1; i < n; i++) {
                T key = arr[i]; int j = i-1;
                while (j >= 0 && comp.compare(arr[j], key) > 0) { arr[j+1] = arr[j]; j--; }
                arr[j+1] = key;
            }
        }
        public static <T> void mergeSort(T[] arr, Comparator<T> comp) {
            if (arr.length > 1) {
                int mid = arr.length / 2;
                @SuppressWarnings("unchecked")
                T[] left = (T[]) new Object[mid];
                @SuppressWarnings("unchecked")
                T[] right = (T[]) new Object[arr.length - mid];
                System.arraycopy(arr, 0, left, 0, left.length);
                System.arraycopy(arr, mid, right, 0, right.length);
                mergeSort(left, comp); mergeSort(right, comp);
                merge(arr, left, right, comp);
            }
        }
        private static <T> void merge(T[] result, T[] left, T[] right, Comparator<T> comp) {
            int i = 0, j = 0, k = 0;
            while (i < left.length && j < right.length)
                result[k++] = comp.compare(left[i], right[j]) <= 0 ? left[i++] : right[j++];
            while (i < left.length) result[k++] = left[i++];
            while (j < right.length) result[k++] = right[j++];
        }
        public static <T> void quickSort(T[] arr, Comparator<T> comp, int low, int high) {
            if (low < high) { int pi = partition(arr, comp, low, high); quickSort(arr, comp, low, pi-1); quickSort(arr, comp, pi+1, high); }
        }
        private static <T> int partition(T[] arr, Comparator<T> comp, int low, int high) {
            T pivot = arr[high]; int i = low-1;
            for (int j = low; j < high; j++)
                if (comp.compare(arr[j], pivot) <= 0) { i++; swap(arr, i, j); }
            swap(arr, i+1, high); return i+1;
        }
        private static <T> void swap(T[] arr, int i, int j) { T tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp; }
    }

    // ==================== LIBRARY SYSTEM ====================
    public LibrarySystem() {
        booksByIsbn = new HashTable<>();
        usersById = new HashTable<>();
        borrowedRecords = new LinkedList<>();
        actionStack = new Stack<>(100);
        finePaymentQueue = new Queue<>(100);
        holdRequests = new PriorityQueue<>(100);
        loadDemoData();
    }

    private void loadDemoData() {
        usersById.put(1, new User(1, "Alice", "student"));
        usersById.put(2, new User(2, "Bob", "student"));
        usersById.put(3, new User(3, "Charlie", "student"));
        usersById.put(4, new User(4, "Librarian", "librarian"));

        booksByIsbn.put(1001, new Book(1001, "The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", 1925, "Fiction", 3));
        booksByIsbn.put(1002, new Book(1002, "1984", "George Orwell", "9780451524935", 1949, "Dystopian", 5));
        booksByIsbn.put(1003, new Book(1003, "To Kill a Mockingbird", "Harper Lee", "9780061120084", 1960, "Classic", 2));

        LocalDate today = LocalDate.now();
        borrowedRecords.add(new BorrowRecord(1001, 1, today.minusDays(5), today.plusDays(9)));
        borrowedRecords.add(new BorrowRecord(1002, 2, today.minusDays(10), today.minusDays(3))); // overdue
    }

    public void start() {
        scanner = new Scanner(System.in);
        while (true) {
            if (currentUser == null) showLoginMenu();
            else showMainMenu();
        }
    }

    private void showLoginMenu() {
        System.out.println("\n=== LOGIN ===");
        System.out.println("1. Alice (student)");
        System.out.println("2. Bob (student)");
        System.out.println("3. Charlie (student)");
        System.out.println("4. Librarian");
        System.out.println("5. Exit");
        System.out.print("Choice: ");
        int ch = scanner.nextInt(); scanner.nextLine();
        switch (ch) {
            case 1: currentUser = usersById.get(1); break;
            case 2: currentUser = usersById.get(2); break;
            case 3: currentUser = usersById.get(3); break;
            case 4: currentUser = usersById.get(4); break;
            case 5: System.exit(0);
            default: System.out.println("Invalid.");
        }
        if (currentUser != null) System.out.println("Logged in as " + currentUser.getName());
    }

    private void showMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. View Inventory");
        System.out.println("2. Search Book (Linear/Binary)");      // CO1
        System.out.println("3. Sort Books (by title)");            // CO1
        if (currentUser.getRole().equals("student")) {
            System.out.println("4. Borrow Book");
            System.out.println("5. Return Book");
            System.out.println("6. View My Borrowed Books");
            System.out.println("7. Pay Fine");
            System.out.println("8. Request Hold (Priority Queue)"); // CO3
        } else {
            System.out.println("4. Add Book");
            System.out.println("5. Remove Book");
            System.out.println("6. View All Borrowed Records");
        }
        System.out.println("9. Undo Last Action (Stack)");          // CO3
        System.out.println("10. Process Fine Payments (Queue)");    // CO3
        System.out.println("11. Logout");
        System.out.print("Choice: ");
        int ch = scanner.nextInt(); scanner.nextLine();

        switch (ch) {
            case 1: viewInventory(); break;
            case 2: searchBook(); break;
            case 3: sortBooks(); break;
            case 4: if (currentUser.getRole().equals("student")) borrowBook(); else addBook(); break;
            case 5: if (currentUser.getRole().equals("student")) returnBook(); else removeBook(); break;
            case 6: if (currentUser.getRole().equals("student")) viewMyBorrowed(); else viewAllBorrowed(); break;
            case 7: if (currentUser.getRole().equals("student")) payFine(); break;
            case 8: if (currentUser.getRole().equals("student")) requestHold(); break;
            case 9: undo(); break;
            case 10: processPayments(); break;
            case 11: currentUser = null; break;
            default: System.out.println("Invalid.");
        }
    }

    // ---------- Inventory & Search/Sort (CO1) ----------
    private void viewInventory() {
        System.out.println("\n--- Inventory ---");
        // Iterate over known keys (since hash table doesn't have keySet, we use fixed keys for demo)
        int[] keys = {1001,1002,1003};
        for (int isbn : keys) {
            Book b = booksByIsbn.get(isbn);
            if (b != null) System.out.println(b);
        }
    }

    private void searchBook() {
        System.out.print("Enter title to search: ");
        String title = scanner.nextLine();
        // Linear search
        System.out.println("--- Linear Search ---");
        boolean found = false;
        int[] keys = {1001,1002,1003};
        for (int isbn : keys) {
            Book b = booksByIsbn.get(isbn);
            if (b != null && b.getTitle().equalsIgnoreCase(title)) {
                System.out.println("Found: " + b);
                found = true; break;
            }
        }
        if (!found) System.out.println("Not found.");

        // Binary search (requires sorted array)
        System.out.println("--- Binary Search (after sorting) ---");
        Book[] books = new Book[booksByIsbn.size()];
        int idx = 0;
        for (int isbn : keys) { Book b = booksByIsbn.get(isbn); if (b != null) books[idx++] = b; }
        SortingAlgorithms.insertionSort(books, Comparator.comparing(Book::getTitle));
        int pos = binarySearch(books, title);
        if (pos >= 0) System.out.println("Found via binary: " + books[pos]);
        else System.out.println("Not found via binary.");
    }

    private int binarySearch(Book[] arr, String title) {
        int low = 0, high = arr.length-1;
        while (low <= high) {
            int mid = (low+high)/2;
            int cmp = arr[mid].getTitle().compareToIgnoreCase(title);
            if (cmp == 0) return mid;
            else if (cmp < 0) low = mid+1;
            else high = mid-1;
        }
        return -1;
    }

    private void sortBooks() {
        Book[] books = new Book[booksByIsbn.size()];
        int idx = 0;
        int[] keys = {1001,1002,1003};
        for (int isbn : keys) { Book b = booksByIsbn.get(isbn); if (b != null) books[idx++] = b; }
        System.out.println("Choose algorithm: 1.Bubble 2.Selection 3.Insertion 4.Merge 5.Quick");
        int algo = scanner.nextInt(); scanner.nextLine();
        Comparator<Book> byTitle = Comparator.comparing(Book::getTitle);
        long start = System.nanoTime();
        switch (algo) {
            case 1: SortingAlgorithms.bubbleSort(books, byTitle); break;
            case 2: SortingAlgorithms.selectionSort(books, byTitle); break;
            case 3: SortingAlgorithms.insertionSort(books, byTitle); break;
            case 4: SortingAlgorithms.mergeSort(books, byTitle); break;
            case 5: SortingAlgorithms.quickSort(books, byTitle, 0, books.length-1); break;
            default: System.out.println("Invalid"); return;
        }
        long end = System.nanoTime();
        System.out.println("Sorted books:");
        for (Book b : books) System.out.println(b);
        System.out.println("Time: " + (end-start) + " ns");
    }

    // ---------- Student Actions ----------
    private void borrowBook() {
        System.out.print("Enter ISBN of book to borrow: ");
        int isbn = scanner.nextInt(); scanner.nextLine();
        Book book = booksByIsbn.get(isbn);
        if (book == null) { System.out.println("Not found."); return; }
        if (book.getCopies() <= 0) { System.out.println("No copies."); return; }
        book.setCopies(book.getCopies()-1);
        LocalDate today = LocalDate.now();
        BorrowRecord rec = new BorrowRecord(book.getId(), currentUser.getId(), today, today.plusDays(14));
        borrowedRecords.add(rec);
        actionStack.push("Borrowed " + book.getTitle());
        System.out.println("Borrowed. Due: " + rec.getDueDate());
    }

    private void returnBook() {
        LinkedList<BorrowRecord> userRecs = new LinkedList<>();
        for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            if (rec.getUserId() == currentUser.getId()) userRecs.add(rec);
        }
        if (userRecs.isEmpty()) { System.out.println("No books borrowed."); return; }
        System.out.println("Your borrowed books:");
        int i = 1;
        for (java.util.Iterator<BorrowRecord> it = userRecs.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            Book b = booksByIsbn.get(rec.getBookId());
            System.out.println(i++ + ". " + b.getTitle() + " (Due: " + rec.getDueDate() + ")");
        }
        System.out.print("Choose book to return: ");
        int choice = scanner.nextInt(); scanner.nextLine();
        BorrowRecord toReturn = null;
        int cnt = 1;
        for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            if (rec.getUserId() == currentUser.getId()) {
                if (cnt == choice) { toReturn = rec; break; }
                cnt++;
            }
        }
        if (toReturn != null) {
            borrowedRecords.remove(toReturn);
            Book book = booksByIsbn.get(toReturn.getBookId());
            book.setCopies(book.getCopies()+1);
            actionStack.push("Returned " + book.getTitle());
            System.out.println("Returned.");
        }
    }

    private void viewMyBorrowed() {
        System.out.println("\n--- My Borrowed Books ---");
        boolean any = false;
        for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            if (rec.getUserId() == currentUser.getId()) {
                Book b = booksByIsbn.get(rec.getBookId());
                long overdue = ChronoUnit.DAYS.between(rec.getDueDate(), LocalDate.now());
                double fine = overdue > 0 ? overdue * 5.0 : 0.0;
                rec.setFine(fine);
                System.out.println(b.getTitle() + " | Due: " + rec.getDueDate() + " | Fine: ₹" + fine);
                any = true;
            }
        }
        if (!any) System.out.println("None.");
    }

    private void payFine() {
        double total = 0;
        for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            if (rec.getUserId() == currentUser.getId() && rec.getFine() > 0) total += rec.getFine();
        }
        if (total == 0) { System.out.println("No fines."); return; }
        System.out.println("Total fine: ₹" + total);
        System.out.print("Pay now? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            finePaymentQueue.enqueue(currentUser.getId());
            // Clear fines (demo)
            for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
                BorrowRecord rec = it.next();
                if (rec.getUserId() == currentUser.getId()) rec.setFine(0);
            }
            actionStack.push("Paid fine ₹" + total);
            System.out.println("Payment queued.");
        }
    }

    private void requestHold() {
        System.out.print("Enter ISBN of book to hold: ");
        int isbn = scanner.nextInt(); scanner.nextLine();
        Book book = booksByIsbn.get(isbn);
        if (book == null) { System.out.println("Not found."); return; }
        holdRequests.insert(new HoldRequest(book.getId(), 1));
        System.out.println("Hold request placed (priority queue).");
    }

    // ---------- Librarian Actions ----------
    private void addBook() {
        System.out.print("ISBN: "); int isbn = scanner.nextInt(); scanner.nextLine();
        System.out.print("Title: "); String title = scanner.nextLine();
        System.out.print("Author: "); String author = scanner.nextLine();
        System.out.print("Year: "); int year = scanner.nextInt(); scanner.nextLine();
        System.out.print("Genre: "); String genre = scanner.nextLine();
        System.out.print("Copies: "); int copies = scanner.nextInt(); scanner.nextLine();
        Book book = new Book(isbn, title, author, String.valueOf(isbn), year, genre, copies);
        booksByIsbn.put(isbn, book);
        actionStack.push("Added " + title);
        System.out.println("Added.");
    }

    private void removeBook() {
        System.out.print("ISBN of book to remove: ");
        int isbn = scanner.nextInt(); scanner.nextLine();
        Book removed = booksByIsbn.remove(isbn);
        if (removed != null) { actionStack.push("Removed " + removed.getTitle()); System.out.println("Removed."); }
        else System.out.println("Not found.");
    }

    private void viewAllBorrowed() {
        System.out.println("\n--- All Borrowed Records ---");
        for (java.util.Iterator<BorrowRecord> it = borrowedRecords.iterator(); it.hasNext(); ) {
            BorrowRecord rec = it.next();
            Book b = booksByIsbn.get(rec.getBookId());
            User u = usersById.get(rec.getUserId());
            System.out.println(u.getName() + " borrowed " + b.getTitle() + " due " + rec.getDueDate());
        }
    }

    // ---------- Stack Undo ----------
    private void undo() {
        if (actionStack.isEmpty()) { System.out.println("Nothing to undo."); return; }
        String last = actionStack.pop();
        System.out.println("Undo: " + last + " (simulated)");
    }

    // ---------- Queue Processing ----------
    private void processPayments() {
        if (finePaymentQueue.isEmpty()) { System.out.println("No pending payments."); return; }
        System.out.println("Processing payments:");
        while (!finePaymentQueue.isEmpty()) {
            int uid = finePaymentQueue.dequeue();
            User u = usersById.get(uid);
            System.out.println("Payment processed for " + u.getName());
        }
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        new LibrarySystem().start();
    }
}