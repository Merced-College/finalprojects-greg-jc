import java.util.LinkedList;

/**
 * DataProcessor holds GPS waypoints in a linked list
 * and sorts them using Quick Sort when requested.
 */
public class DataProcessor {

    // 1) Our data structure: a linked list to store GPS waypoints.
    //    We add to the tail in O(1) time as new coordinates arrive.
    private LinkedList<Coordinate> coords;

    /** Constructor initializes the linked list. */
    public DataProcessor() {
        coords = new LinkedList<>();
    }

    /**
     * Add a new GPS coordinate to our list.
     * @param coord the Coordinate object to append
     */
    public void addCoordinate(Coordinate coord) {
        coords.add(coord);
        System.out.println("Added Coordinate: " + coord);
    }

    /**
     * Returns a sorted array of all coordinates by latitude.
     * This method:
     *  1. Converts the linked list to an array.
     *  2. Sorts the array in place with Quick Sort.
     *  3. Returns the sorted array.
     *
     * @return a new array of Coordinate sorted by latitude
     */
    public Coordinate[] getSortedCoordinates() {
        // Convert the linked list to an array
        Coordinate[] arr = coords.toArray(new Coordinate[0]);
        // Sort the array with Quick Sort
        quickSortCoordinates(arr, 0, arr.length - 1);
        return arr;
    }

    /**
     * Quick Sort algorithm: recursively sorts arr[low..high] by latitude.
     */
    private void quickSortCoordinates(Coordinate[] arr, int low, int high) {
        if (low < high) {
            // Partition the array and get the pivot index
            int pivotIndex = partition(arr, low, high);
            // Sort elements before pivot
            quickSortCoordinates(arr, low, pivotIndex - 1);
            // Sort elements after pivot
            quickSortCoordinates(arr, pivotIndex + 1, high);
        }
    }

    /**
     * Partition step for Quick Sort.
     * Picks the last element as pivot, then rearranges elements
     * so that those with latitude < pivot go left, others go right.
     *
     * @return the final index of the pivot element
     */
    private int partition(Coordinate[] arr, int low, int high) {
        Coordinate pivot = arr[high];
        int i = low - 1;  // i tracks the end of the "less than pivot" section
        for (int j = low; j < high; j++) {
            if (arr[j].getLatitude() < pivot.getLatitude()) {
                i++;
                // Swap arr[i] and arr[j]
                Coordinate temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // Place pivot just after the last smaller element
        Coordinate temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }
}
