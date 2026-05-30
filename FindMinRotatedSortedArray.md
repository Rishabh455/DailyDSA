package daily.dsa.binarysearch;

/**
 * ============================================================
 *  LeetCode 153 — Find Minimum in Rotated Sorted Array
 *  Difficulty : Medium
 *  Topic      : Binary Search
 *  Date       : 15 May 2026
 * ============================================================
 *
 * PROBLEM STATEMENT
 * -----------------
 * A sorted array of unique elements has been rotated between 1 and n times.
 * Return the minimum element in O(log n) time.
 *
 * EXAMPLES
 * --------
 *   Input : [3, 4, 5, 1, 2]   →  Output : 1
 *   Input : [4, 5, 6, 7, 0, 1, 2]  →  Output : 0
 *   Input : [11, 13, 15, 17]   →  Output : 11  (no rotation)
 *
 * INTUITION
 * ---------
 *  The array is made of TWO sorted halves joined at a pivot (the minimum).
 *
 *    [ 4  5  6  7 | 0  1  2 ]
 *                   ^
 *                 pivot = minimum
 *
 *  Binary Search decision rule (compare mid with high):
 *
 *   • nums[mid] > nums[high]
 *       → mid is in the LEFT (larger) half
 *       → minimum must be to the RIGHT  →  l = mid + 1
 *
 *   • nums[mid] <= nums[high]
 *       → mid is in the RIGHT (smaller) half, or array is fully sorted
 *       → minimum could be at mid or further LEFT  →  h = mid - 1
 *       → but track nums[mid] as a candidate answer
 *
 * WHY compare with HIGH and not LOW?
 *   Because nums[low] can be deceiving — after a rotation the left edge
 *   can be larger than the minimum. The right edge (high) always tells us
 *   which side of the pivot we are on.
 *
 * TIME  : O(log n)
 * SPACE : O(1)
 */
public class FindMinRotatedSortedArray {

    public int findMin(int[] nums) {
        int l = 0, h = nums.length - 1;
        int ans = Integer.MAX_VALUE;

        while (l <= h) {
            int mid = l + (h - l) / 2;   // overflow-safe mid

            if (nums[mid] >= nums[h]) {
                // mid is on the LEFT sorted half → go right
                ans = Math.min(ans, nums[mid]);
                l = mid + 1;
            } else {
                // mid is on the RIGHT sorted half → go left
                ans = Math.min(ans, nums[mid]);
                h = mid - 1;
            }
        }
        return ans;
    }

    // ── Cleaner alternative (no separate ans variable) ──────────────────────
    /**
     * Slightly cleaner variant: when nums[mid] <= nums[high], mid itself is a
     * valid candidate, so we keep it by doing h = mid (not mid - 1).
     * The loop ends when l == h, and that element IS the minimum.
     */
    public int findMinClean(int[] nums) {
        int l = 0, h = nums.length - 1;

        while (l < h) {
            int mid = l + (h - l) / 2;

            if (nums[mid] > nums[h]) {
                l = mid + 1;          // minimum is strictly to the right
            } else {
                h = mid;              // mid could be the answer, don't skip it
            }
        }
        return nums[l];               // l == h, both point to minimum
    }

    // ── Driver ───────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        FindMinRotatedSortedArray sol = new FindMinRotatedSortedArray();

        int[][] tests  = { {3,4,5,1,2}, {4,5,6,7,0,1,2}, {11,13,15,17}, {1} };
        int[]   expect = { 1, 0, 11, 1 };

        for (int i = 0; i < tests.length; i++) {
            int result = sol.findMinClean(tests[i]);
            System.out.printf("Input: %-20s → Output: %2d  [%s]%n",
                java.util.Arrays.toString(tests[i]),
                result,
                result == expect[i] ? "PASS ✓" : "FAIL ✗");
        }
    }
}