(ns dameon.visual-cortex.stream-tree)

(defn create []
  {:paths {} :structure []})

(defn get-in-
  "get-in which assumes structure stored in :upstreams key so that path can be a vec of indicies"
  [upstreams path]
  (let [remaining (rest path)
        cur-stream (if (nil? (first path))
                    {:up-streams upstreams}
                    (get upstreams (first path) nil))]
    (if (or (empty? remaining) (nil? cur-stream))
      cur-stream
      (get-in- (get cur-stream :up-streams) remaining))))

(defn add
  "Add stream to tree at index"
  [stream tree & [parent-name]]
  (let [path
        (if (nil? parent-name)
          []
          (get-in tree [:paths parent-name]))

        parent-upstreams
        (get (get-in- (tree :structure) path) :up-streams)]

    (if (nil? path) (throw (Exception. "This parent doesn't exist")))
    (if (nil? (get stream :name)) (throw (Exception. "This stream doesn't have a name")))
   (-> tree

       (assoc-in
        (concat [:structure] (interleave path (repeat :up-streams)))
        (conj parent-upstreams stream))

       (assoc-in
        [:paths (get stream :name)] 
        (conj path (count parent-upstreams))))))




(if (nil? parent-name)















