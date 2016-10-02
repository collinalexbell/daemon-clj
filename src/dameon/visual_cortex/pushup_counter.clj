(ns dameon.visual-cortex.pushup-counter
  (:require
   [dameon.smart-atom :as smart-atom])
  (:import org.httpkit.BytesInputStream
           [org.opencv.core MatOfByte Core Mat Point Scalar]
           [org.opencv.imgcodecs Imgcodecs]
           [org.opencv.imgproc Imgproc]
           [org.opencv.video Video])
  (:use [incanter core stats charts io]))

(def plot (time-series-plot [] []))
(view plot)

(def last-frames (atom []))
(def last-frame (atom nil))
(def frame-history-size 3)
(def test-frame (atom nil))
(def test-exception (atom nil))
(def fgbg  (Video/createBackgroundSubtractorMOG2))
(.setHistory fgbg 50)

(defn add-frame [frame]
  (try (if (= (count @last-frames) frame-history-size)
     (do
       (smart-atom/delete (peek @last-frames))
       (swap! last-frames pop)))
       (catch Exception e (def except (.getMessage e))))
  (swap! last-frames (fn [lf] (into [] (cons frame lf)))))

(defn get-centroid [mat]
  (let [moments (Imgproc/moments mat)]
    {:x (/ (.get_m10 moments) (.get_m00 moments))
     :y (/ (.get_m01 moments) (.get_m00 moments))}))


(defn get-motion [new-frame]
  (swap! last-frame (constantly (smart-atom/deref new-frame)))
  (let [rv (Mat.)]
    (try
      (.apply fgbg (smart-atom/deref new-frame) rv)
      (catch Exception e (do (println "exception")
                             (swap! test-exception (constantly (.getMessage e))))))
    (let [centroid (get-centroid rv)]
      (try
        (Imgproc/rectangle rv (Point. 20 20) (Point. 120 60) (Scalar. 255.0) -1)
        (Imgproc/putText rv (str "y: "
                                 (centroid :y))
                         (Point. 25 35)
                         Core/FONT_HERSHEY_PLAIN 1 (Scalar. 0.0) 2)

        (add-points plot [(System/currentTimeMillis)] (centroid :y))
        (catch Exception e (println (.getMessage e))))
      {:smart-mat (smart-atom/create rv)})))




















