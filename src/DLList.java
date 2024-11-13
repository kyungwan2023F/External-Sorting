import java.util.Iterator;
import java.util.NoSuchElementException;

// -------------------------------------------------------------------------
/**
 * This class provides an iterator for the DLList data structure specifically
 * for `Run` objects.
 * 
 * @author Kyungwan Do, Jaeyoung Shin
 * @version Nov 12, 2024
 */
public class DLList {

    // -------------------------------------------------------------------------
    /**
     * This class provides an iterator for the DLList data structure
     * specifically for `Run` objects.
     */
    public class DLListRunIterator implements Iterator<Run> {
        private Node<Run> next; // Next node
        private boolean calledNext; // If next is called

        /**
         * Creates a new DLListRunIterator.
         */
        public DLListRunIterator() {
            next = head.next();
            calledNext = false;
        }


        /**
         * Checks if there are more elements in the list.
         *
         * @return true if there are more elements in the list.
         */
        @Override
        public boolean hasNext() {
            return next != tail;
        }


        /**
         * Gets the next value in the list.
         *
         * @return the next value.
         * @throws NoSuchElementException
         *             if there are no nodes left in the list.
         */
        @Override
        public Run next() {
            if (!hasNext()) {
                throw new NoSuchElementException(
                    "No more elements in the list");
            }
            Run data = next.getData();
            next = next.next();
            calledNext = true;
            return data;
        }
    }

    /**
     * Iterator method creates Iterator object.
     *
     * @return new Iterator object.
     */
    public Iterator<Run> iterator() {
        return new DLListRunIterator();
    }

    /**
     * This represents a node in a doubly linked list for storing `Run` objects.
     * This node stores data, a pointer to the node before it, and a pointer to
     * the node after it.
     */
    private static class Node<T> {
        private Node<T> next;
        private Node<T> previous;
        private T data;

        /**
         * Creates a new node with the given data.
         *
         * @param d
         *            the data to put inside the node.
         */
        public Node(T d) {
            data = d;
        }


        /**
         * Sets the node after this node.
         *
         * @param n
         *            the node after this one.
         */
        public void setNext(Node<T> n) {
            next = n;
        }


        /**
         * Sets the node before this one.
         *
         * @param n
         *            the node before this one.
         */
        public void setPrevious(Node<T> n) {
            previous = n;
        }


        /**
         * Gets the next node.
         *
         * @return the next node.
         */
        public Node<T> next() {
            return next;
        }


        /**
         * Gets the node before this one.
         *
         * @return the node before this one.
         */
        public Node<T> previous() {
            return previous;
        }


        /**
         * Gets the data in the node.
         *
         * @return the data in the node.
         */
        public T getData() {
            return data;
        }
    }

    /**
     * How many nodes are in the list.
     */
    private int size;

    /**
     * The first node in the list. THIS IS A SENTINEL NODE AND AS SUCH DOES NOT
     * HOLD ANY DATA. REFER TO init()
     */
    private Node<Run> head;

    /**
     * The last node in the list. THIS IS A SENTINEL NODE AND AS SUCH DOES NOT
     * HOLD ANY DATA. REFER TO init()
     */
    private Node<Run> tail;

    /**
     * Create a new DLListRun object.
     */
    public DLList() {
        init();
    }


    /**
     * Initializes the object to have the head and tail nodes.
     */
    private void init() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.setNext(tail);
        tail.setPrevious(head);
        size = 0;
    }


    /**
     * Checks if the list is empty.
     *
     * @return true if the list is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }


    /**
     * Gets the number of elements in the list.
     *
     * @return the number of elements.
     */
    public int size() {
        return size;
    }


    /**
     * Removes all of the elements from the list.
     */
    public void clear() {
        init();
    }


    /**
     * Checks if the list contains the given `Run` object.
     *
     * @param run
     *            the `Run` object to check for.
     * @return true if it contains the object.
     */
    public boolean contains(Run run) {
        return lastIndexOf(run) != -1;
    }


    /**
     * Gets the `Run` object at the given position.
     *
     * @param index
     *            where the object is located.
     * @return The `Run` object at the given position.
     * @throws IndexOutOfBoundsException
     *             if there no node at the given index.
     */
    public Run get(int index) {
        return getNodeAtIndex(index).getData();
    }


    /**
     * Adds a `Run` object to the end of the list.
     *
     * @param newRun
     *            the `Run` object to add to the end.
     */
    public void add(Run newRun) {
        add(size(), newRun);
    }


    /**
     * Adds the `Run` object at the specified position in the list.
     *
     * @param index
     *            where to add the object.
     * @param run
     *            the `Run` object to add.
     * @throws IndexOutOfBoundsException
     *             if index is less than zero or greater than size.
     * @throws IllegalArgumentException
     *             if run is null.
     */
    public void add(int index, Run run) {
        if (index < 0 || size < index) {
            throw new IndexOutOfBoundsException();
        }
        if (run == null) {
            throw new IllegalArgumentException(
                "Cannot add null objects to a list");
        }

        Node<Run> nodeAfter = (index == size) ? tail : getNodeAtIndex(index);

        Node<Run> addition = new Node<>(run);
        addition.setPrevious(nodeAfter.previous());
        addition.setNext(nodeAfter);
        nodeAfter.previous().setNext(addition);
        nodeAfter.setPrevious(addition);
        size++;
    }


    /**
     * Gets the node at the given index.
     *
     * @param index
     *            the index to retrieve the node.
     * @return node at index.
     */
    private Node<Run> getNodeAtIndex(int index) {
        if (index < 0 || size() <= index) {
            throw new IndexOutOfBoundsException("No element exists at "
                + index);
        }
        Node<Run> current = head.next();
        for (int i = 0; i < index; i++) {
            current = current.next();
        }
        return current;
    }


    /**
     * Gets the last position of the given `Run` object in the list.
     *
     * @param run
     *            the `Run` object to look for.
     * @return the last position of it, or -1 if it is not in the list.
     */
    public int lastIndexOf(Run run) {
        Node<Run> current = tail.previous();
        for (int i = size() - 1; i >= 0; i--) {
            if (current.getData().equals(run)) {
                return i;
            }
            current = current.previous();
        }
        return -1;
    }


    /**
     * Removes the element at the specified index from the list.
     *
     * @param index
     *            the index where the object is located.
     * @return true if successful.
     */
    public boolean remove(int index) {
        Node<Run> nodeToBeRemoved = getNodeAtIndex(index);
        nodeToBeRemoved.previous().setNext(nodeToBeRemoved.next());
        nodeToBeRemoved.next().setPrevious(nodeToBeRemoved.previous());
        size--;
        return true;
    }


    /**
     * Removes the first `Run` object in the list that `.equals(run)`.
     *
     * @param runNumber
     *            remove run with runNumber
     * @return true if the object was found and removed.
     */
    public boolean removeRun(int runNumber) {
        Node<Run> current = head.next();
        while (current != tail) {
            if (current.getData().getRunNum() == runNumber) {
                // Found the node with the specified runNumber, remove it.
                current.previous().setNext(current.next());
                current.next().setPrevious(current.previous());
                size--;
                return true;
            }
            current = current.next();
        }
        // If no node with the specified runNumber was found, return false.
        return false;
    }


    /**
     * Retrieves the `Run` object in the list that has the given runNumber.
     *
     * @param runNumber
     *            the run number to find.
     * @return the `Run` object with the specified runNumber, or `null` if not
     *         found.
     */
    public Run getRunByNumber(int runNumber) {
        Node<Run> current = head.next();
        while (current != tail) {
            if (current.getData().getRunNum() == runNumber) {
                // Found the node with the specified runNumber, return the Run
                // object.
                return current.getData();
            }
            current = current.next();
        }
        // If no node with the specified runNumber was found, return null.
        return null;
    }


    /**
     * Returns a string representation of the list.
     *
     * @return a string representing the list.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (!isEmpty()) {
            Node<Run> currNode = head.next();
            while (currNode != tail) {
                Run element = currNode.getData();
                builder.append(element.toString());
                if (currNode.next != tail) {
                    builder.append(", ");
                }
                currNode = currNode.next();
            }
        }
        builder.append("}");
        return builder.toString();
    }
}
