(ns dameon.visual-cortex.stream)

(defn cleanup-stream [stream])


(defprotocol Stream
  "Unit of Visual Processing"
  (update-frame [rec parent]))


(defn subscribe [stream]
  (swap! (stream :subscribers) assoc stream))

(defn no-one-holding? [frame])

(defn release-old-mat-if-neccisary [this]
  (if (= 0 (count (get this :subscribers)))
    (.release (get this :cur-frame))))

(defn deref [stream-agent]
  (assoc stream-agent :cur-frame (.clone (get stream-agent :cur-frame))))

(defrecord Base-stream [subscribers]
  Stream
  (update-frame [this parent]
    (doall
     (map #(send %1 update-frame (assoc this :cur-frame (get parent :cur-frame))) subscribers))
    (release-old-mat-if-neccisary this)
    this)) 










