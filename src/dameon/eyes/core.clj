(ns dameon.eyes.core
  (require [dameon.visual-cortex.stream :as vis-stream])
  (use [clojure.core.async :only (go)]))


(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size]
        '[org.opencv.imgcodecs Imgcodecs]
        '[org.opencv.imgproc Imgproc]
        '[org.opencv.videoio VideoCapture Videoio]
        '[org.opencv.objdetect CascadeClassifier])

(import 'java.nio.ByteBuffer)
(import 'java.nio.ByteOrder)
(import 'java.util.concurrent.locks.ReentrantReadWriteLock)

(def gc-freq-in-mins (* 60 2))

(def subscribers (ref []))
(def video-feed (VideoCapture. 0))
(def current-frame (ref {:buf (Mat.) :lock (ReentrantReadWriteLock.)}))
(def continue-seeing (ref false))
(def cur-see-thread (ref (Thread. (fn []))))

(defn get-field-of-vision []
  [(int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
   (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))])


(defn stop-seeing []
  (dosync (ref-set continue-seeing false)))

(defn get-max-fps
  "Warning! This will kill the current eyes stream"
  []
  (stop-seeing)
  (let [start-time
        (. System currentTimeMillis)

        buf
        (Mat.
         (int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
         (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))
         CvType/CV_8UC3)]

    (doall (for [x (range 20)]
       (.read video-feed buf)))
    (float (/ 20 (/ (- (. System currentTimeMillis) start-time) 1000)))))


(defn get-current-frame []
  (let [current-frame @current-frame]
   (.lock (.readLock (:lock current-frame)))
   (try
     (.clone (:mat current-frame))
     (finally (.unlock (.readLock (:lock current-frame)))))))




(defn see []
  (if (or @continue-seeing (.isAlive @cur-see-thread))
    (throw (Exception. "Dameon is already seeing")))
  (dosync (ref-set continue-seeing true))
  (let [time-since-last-gc (ref (. System currentTimeMillis))

        thread
        (Thread.
          (fn []
            (try
             (while @continue-seeing
               (let [buf ;the buffer must be reinstanciated every frame due to it being a Java class
                     (Mat.
                      (int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
                      (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))
                      CvType/CV_8UC3)]

                 (.read video-feed buf)
                 (let [cur-frame @current-frame]
                   (go
                     (.lock (.writeLock (:lock cur-frame)))
                     (try
                       (.release (:mat cur-frame))
                       (finally (.unlock (.writeLock (:lock cur-frame)))))))
                 (dosync (ref-set current-frame {:mat buf :lock (ReentrantReadWriteLock.)}))
                 (doall (map #(send %1 vis-stream/update-frame (get-current-frame)) @subscribers))
                 (if (> (. System currentTimeMillis) (+ @time-since-last-gc (* 1000 60 gc-freq-in-mins)))
                   (do (. System gc)
                       (dosync (ref-set time-since-last-gc (. System currentTimeMillis)))))))
             (catch Exception e (do (println (str e)) (stop-seeing))))))]
    (.start thread)
    thread))

(defn add-subscriber [subscriber]
  (dosync (ref-set subscribers (cons subscriber @subscribers))))


(defn remove-subscriber [subscriber]
  (dosync (ref-set subscribers (remove #(= subscriber %) @subscribers))))





