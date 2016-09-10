(ns dameon.visual-cortex.core
  (require [dameon.eyes.core :as eyes]
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

(defn remove-stream-on-face []
  (dosync (ref-set stream-on-face-running false))
  (face/deactivate-mat-display))


(defn display-basic-vision []
  (face/activate-mat-display)
  (swap! tree stree/add
         (->> (stream/->Base-stream []
                                    [#(do
                                        (spit "display-basic-vision.log" (str % "\n") :append true)
                                        (face/update-mat-to-display (% :smart-mat)))]
                                    :base-stream)
          (stream/set-fps 2))))

(defn display-face-detect []
  (face/activate-mat-display)
  (swap! tree stree/add
         (->> (stream/->FaceDetectionStream []
                                    [#(do
                                        (spit "display-basic-vision.log" (str % "\n") :append true)
                                        (face/update-mat-to-display (% :smart-mat)))]
                                    :base-stream)
          (stream/set-fps 1))))


(defn stop-display-basic-vision []
  (face/deactivate-mat-display)
  (def tree (atom (stree/create))))

(stop-display-basic-vision)
(display-face-detect)
(eyes/see tree)
(eyes/stop-seeing)
