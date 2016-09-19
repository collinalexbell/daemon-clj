(ns dameon.face.emotion-animation
  (require [quil.core :as q]
           [dameon.settings :as settings]))


(defn load-sprite-files
  "Returns a map with the loaded sprite files that were loaded using quill.core/image-load"
  [emotion-key]
  ;;If the emotion exists
  (if-let [emotion-folder (str settings/face-animation-folder "/" (name emotion-key))]
    (map 
     #(let [img (q/load-image (str emotion-folder "/" %))]
        (q/resize img (int (/ 1024 2.8)) (int (/ 600 2.8)))
        img)
     ;;vector of image paths in emotion-folder
     (map #(.getName %) (.listFiles (clojure.java.io/file emotion-folder))))
    ;;if emotion doesn't exist 
    (throw (Exception. "Incorrect emotion"))))


(defn next-frame
  [emotion-animation]
    (assoc emotion-animation
           :cur-frame-no
           (mod
            (+ 1 (emotion-animation :cur-frame-no))
            (count (get emotion-animation :frames)))))

(defn get-cur-frame [emotion-animation]
  (nth
   (get emotion-animation :frames)
   (emotion-animation :cur-frame-no)))

(defn reset [e]
  (assoc e :cur-frame-no 0))

(defn new [emotion-key]
  {:frames
    (load-sprite-files emotion-key)
   :cur-frame-no 0})




















