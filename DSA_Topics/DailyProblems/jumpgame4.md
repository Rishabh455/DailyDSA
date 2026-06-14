class Solution {
    public int minJumps(int[] arr) {
        //get me the cdoe
       Map<Integer, List<Integer>> idxMap = new HashMap<>();
        int n=arr.length;
       for (int i = 0; i < n; i++) {

    idxMap.putIfAbsent(arr[i], new ArrayList<>());

    idxMap.get(arr[i]).add(i);
}
        boolean[]visited=new boolean[n];
        Queue<Integer>que=new LinkedList<>();
        que.add(0);
        int steps=0;
        visited[0]=true;
        while (!que.isEmpty()) {

            int size = que.size();

            while (size-- > 0) {

                int curr = que.poll();

                if (curr == n - 1) {
                    return steps;
                }

                // curr + 1
                if (curr + 1 < n && !visited[curr + 1]) {
                    visited[curr + 1] = true;
                    que.add(curr + 1);
                }

                // curr - 1
                if (curr - 1 >= 0 && !visited[curr - 1]) {
                    visited[curr - 1] = true;
                    que.add(curr - 1);
                }

                // same value jumps
                for (int idx : idxMap.get(arr[curr])) {

                    if (!visited[idx]) {
                        visited[idx] = true;
                        que.add(idx);
                    }
                }

                // IMPORTANT: avoid revisiting same-value list
                idxMap.get(arr[curr]).clear();
            }

            steps++;
        }
        return steps;
    }
}