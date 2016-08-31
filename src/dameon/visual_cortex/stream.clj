(ns dameon.visual-cortex.stream
  (require [dameon.smart-atom :as smart-atom]
           [dameon.visual-cortex.face-recognition]
           [clojure.spec :as s]
           [clojure.core.async :refer [go]]))


(defprotocol Stream
  "Unit of Visual Processing"
  (gen-new-data [stream smart-mat]))

(defn ready-to-update? [stream the-time]
  (if (and (not (nil? the-time)) (not (nil? (get stream :fps))))
    (if (>=
         the-time
         (+ ;calculate next time to fire
          @(get stream :last-time) ;in ms
          ;;fps-> milliseconds per frame
          (/ 1 (/ (get stream :fps) 1000))))
      (do (swap! (get stream :last-time) (fn [x] the-time))
          stream)
      false)
    stream))

(defn update-mat
  "Will generate new stream data and pass that data to this streams subscribers and call any terminus functions"
  [stream smart-mat & [the-time]]
  (if-let [stream (ready-to-update? stream the-time)]
    (let [data (gen-new-data stream smart-mat)]
      (doall
       (map (fn [the-fn]
              (let [smart-atom-copy (smart-atom/copy (data :smart-mat))]
                (go 
                  (the-fn (assoc data :smart-mat smart-atom-copy)))))
            (get stream :termini)))
      (doall
       (map
        #(let [copied-mat-smart-atom (smart-atom/copy (data :smart-mat))]
           ;;(println  (str "IN UPSTREAM LOOP: " copied-mat-smart-atom))
           (update-mat % copied-mat-smart-atom))
        (get stream :up-streams)))
      ;;clean up the new-new frame reference
      (smart-atom/delete (data :smart-mat)))
    ;;clean up the frame reference even if new data wasn't generated
    ;;the two cleanup calls can not be unified,
    ;;because gen-new-data may create a new smartmat that needs cleaning
    (smart-atom/delete smart-mat))
  stream)

(s/def ::fps (s/and #(<= % 30) #(>= % 0.01)))
(defn set-fps [fps stream]
  (if (s/valid? ::fps fps)
    (assoc stream :fps fps :last-time (atom 0))
    (throw (Exception. (s/explain-str fps)))))

(defrecord Base-stream [up-streams termini name]
  Stream
  (gen-new-data [this smart-mat]
    {:smart-mat smart-mat})) 


(defrecord FaceDetectionStream [up-treams termini name]
  
  )









