(ns dameon.visual-cortex.stream)


(defprotocol Stream
  "Unit of Visual Processing"
  (update-frame [rec new-frame]))


(defn subscribe [stream]
  (swap! (stream :subscribers) assoc stream))


(defrecord Base-stream [subscribers]
  Stream
  (update-frame [this new-frame]
    (doall
     (map #(send %1 update-frame new-frame) subscribers))
    (assoc this :cur-frame new-frame)))

