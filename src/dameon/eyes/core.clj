(ns dameon.eyes.core)

(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size]
        '[org.opencv.imgcodecs Imgcodecs]
        '[org.opencv.imgproc Imgproc]
        '[org.opencv.videoio VideoCapture Videoio]
        '[org.opencv.objdetect CascadeClassifier])

(import 'java.nio.ByteBuffer)
(import 'java.nio.ByteOrder)

(def video-feed (VideoCapture. 0))
(def current-frame (ref (Mat.)))
(def continue-seeing (ref false))
(def cur-see-thread (ref (Thread. (fn []))))

(defn get-field-of-vision []
  [(int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
   (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))])

(defn get-max-fps []
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

(defn see []
  (if (or @continue-seeing (.isAlive @cur-see-thread))
    (throw (Exception. "Dameon is already seeing")))
  (dosync (ref-set continue-seeing true))
  (let [gc-time (ref (. System currentTimeMillis))

        thread
        (Thread.
          (fn []
            (while @continue-seeing
              (let [buf ;the buffer must be reinstanciated every frame due to it being a Java class
                    (Mat.
                     (int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
                     (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))
                     CvType/CV_8UC3)]

                (.read video-feed buf)
                (dosync
                 (.release @current-frame)
                 (ref-set current-frame buf))))))]
    (.start thread)
    thread))

(defn stop-seeing []
  (dosync (ref-set continue-seeing false)))

(defn get-current-frame []
  @current-frame)










