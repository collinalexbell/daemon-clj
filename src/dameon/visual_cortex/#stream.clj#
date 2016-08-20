(ns dameon.visual-cortex.stream)

(defn cleanup-stream [stream])


(defprotocol Stream
  "Unit of Visual Processing"
  (update-frame [rec parent]))


(defn subscribe [stream]
  (swap! (stream :subscribers) assoc stream))

(defn no-one-holding? [frame])

(defn release-old-mat-if-neccisary [stream]
  ;;We know that a frame is "dead" if:
  ;;1) a new update-frame call has been made
  ;;2) there were no subscribers on the last update-frame call
  ;;3) deref does not have a reference to it before it is finished cloning it
  (if (not (get stream :subscribers-on-last-update?))
   (go
     (.lock (.writeLock (get stream :lock)))
     (try
       (.release (get stream :cur-frame))
       (finally (.unlock (.writeLock (get stream :lock)))))))) 

(defn get-current-frame [stream]
   (.lock (.readLock (get stream :lock))))
   (try
     (.clone (get stream :cur-frame))
     (finally (.unlock (.readLock (get stream :lock))))))

(defn deref [stream-agent]
  (assoc stream-agent :cur-frame (.clone (get stream-agent :cur-frame))))

(defrecord Base-stream [subscribers]
  Stream
  (update-frame [this parent]
    (let [new-stream (assoc this :cur-frame (get parent :cur-frame))]
      (release-old-mat-if-neccisary new-stream)
      (doall
       (map #(send %1 update-frame new-stream) subscribers))
      ;;side-effects
      new-stream))) 

















