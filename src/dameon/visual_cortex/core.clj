(ns dameon.visual-cortex.core
  (require ;;[dameon.eyes.core :as eyes]
           [dameon.face.core :as face]
           [dameon.visual-cortex.stream :as stream]
           [dameon.visual-cortex.stream-tree :as stree]
           [clojure.core.async :as async]))


(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size Scalar Rect]
        '[org.opencv.imgproc Imgproc]
        '[org.opencv.imgcodecs Imgcodecs]
        '[org.opencv.objdetect CascadeClassifier])


;;start the eyes.
(def tree (atom (stree/create)))

(def eye-thread (eyes/see tree))

(def stream-on-face-running (ref false))

(defn gen-face-update-loop [the-stream]
  (fn []
    (while @stream-on-face-running
      (let [start-time (. System currentTimeMillis)]
        (face/update-mat-to-display (get (stream/deref the-stream) :cur-frame))
        (let [elapsed-time 
              (- (. System currentTimeMillis) start-time)

              remaining-time
              (- (* 1000 (/ 1 10)) elapsed-time)]

          (if (> remaining-time 0)
            (. Thread sleep remaining-time)))))))

(defn show-stream-on-face [stream]
  (if @stream-on-face-running (throw (Exception. "Stream is already running")))
  (dosync (ref-set stream-on-face-running true))
  (face/activate-mat-display)
  (let [thread
        (Thread. (gen-face-update-loop stream))]
    (.start thread)
    thread))

(defn remove-stream-on-face []
  (dosync (ref-set stream-on-face-running false))
  (face/deactivate-mat-display))


(defn display-basic-vision []
  (face/activate-mat-display)
  (swap! tree stree/add
         (stream/->Base-stream []
                               [#(do (face/update-mat-to-display) (% :smart-mat)
                                     (spit "display-basic-vision.log" (str % "\n")))]
                               :base-stream)))


(defn stop-display-basic-vision []
  (face/deactivate-mat-display)
  (def tree (atom (stree/create))))
