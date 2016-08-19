(ns dameon.visual-cortex.core
  (require [dameon.eyes.core :as eyes]
           [dameon.face.core :as face]
           [dameon.visual-cortex.stream :as stream]
           [clojure.core.async :as async]))


(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size Scalar Rect]
        '[org.opencv.imgproc Imgproc]
        '[org.opencv.objdetect CascadeClassifier])



(eyes/see) ;start the eyes.

(def stream-on-face-running (ref false))
(def sees-person (ref false))



(def face-classifier (CascadeClassifier. "/Users/collinbell/opt/opencv/data/haarcascades/haarcascade_frontalface_default.xml"))

(defn detect-face [stream]
  (let [grey (Mat.)
        faces (MatOfRect.)
        scale 4]
    ;;(. Imgproc cvtColor (get-current-frame stream) grey Imgproc/COLOR_RGB2GRAY)
    (. Imgproc resize grey grey (Size. (int (/ (.width grey) scale)) (int (/ (.height grey) scale))))
    (.detectMultiScale face-classifier grey faces)
    (let [rv
          (filter #(if (> (.-width %1) (/ 150 scale)) true false)
                  (map
                   #(Rect. (* scale (.x %)) (* scale (.y %)) (* scale (.width %)) (* scale (.height %)))
                   (.toArray faces)))]
      (if (> (count rv) 0)
        (dosync (ref-set sees-person true))
        (dosync (ref-set sees-person false)))
      rv)))


(defn show-stream-on-face []
  (if @stream-on-face-running (throw (Exception. "Stream is already running")))
  (dosync (ref-set stream-on-face-running true))
  (face/activate-mat-display)
  (let [base-stream
        (agent (stream/->Base-stream []))
        thread
        (Thread.
         (fn []
           (eyes/add-subscriber base-stream)
           (while @stream-on-face-running
             (let [start-time (. System currentTimeMillis)]
               (face/update-mat-to-display (get @base-stream :cur-frame))
               (let [elapsed-time 
                     (- (. System currentTimeMillis) start-time)

                     remaining-time
                     (- (* 1000 (/ 1 30)) elapsed-time)]
                 
                 (if (> remaining-time 0)
                   (. Thread sleep remaining-time)))))))]
    (.start thread)
    thread))

(defn run-face-detection []
  (show-stream-on-face {:type :rect :rect-array-generator detect-face :parent {:type :eye}}))

(defn remove-stream-on-face []
  (dosync (ref-set stream-on-face-running false) (ref-set eyes/subscribers []))
  (face/deactivate-mat-display))


(defn sees-person? []
  @sees-person)

















