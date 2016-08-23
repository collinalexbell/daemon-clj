(ns dameon.visual-cortex.stream
  (require [dameon.smart-atom :as smart-atom]
           [clojure.spec :as s]
           [clojure.core.async :refer [go]]))

(defn foo [a] (go [] a))
(foo)

(defprotocol Stream
  "Unit of Visual Processing"
  (gen-new-data [stream smart-mat]))

(defn update-mat
  "Will generate new stream data and pass that data to this streams subscribers and call any terminus functions"
  [stream smart-mat]
  (let [data (gen-new-data stream smart-mat)]
   (doall
    (map #(go (% (assoc data :smart-mat (smart-atom/copy (data :smart-mat)))))
         (get stream :termini)))
   (doall
    (map
     #(let [copied-mat-smart-atom (smart-atom/copy (data :smart-mat))]
        (go (update-mat % copied-mat-smart-atom)))
      (get stream :up-streams)))
   ;;clean up the new-new frame reference
   (smart-atom/delete (data :smart-mat)))
  stream)

(defrecord Base-stream [up-streams termini]
  Stream
  (gen-new-data [this smart-mat]
    {:smart-mat smart-mat})) 



















