(ns dameon.eyes.core
  (require [quil.core :as q]))

(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size]
        '[org.opencv.imgcodecs Imgcodecs]
        '[org.opencv.imgproc Imgproc]
        '[org.opencv.videoio VideoCapture Videoio]
        '[org.opencv.objdetect CascadeClassifier])

(import 'java.nio.ByteBuffer)
(import 'java.nio.ByteOrder)

(def video-feed (VideoCapture. 0))

(int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))

(def frame-ref (ref (Mat.)))
(def img (ref nil))


(defn toPImage [mat]
  (let [w (.width mat)
        h (.height mat)
        mat2 (Mat.)
        image (q/create-image w h :rgb)
        data8 (make-array Byte/TYPE (* w h 4))
        data32 (int-array (* w h))]
    (. Imgproc cvtColor mat mat2 Imgproc/COLOR_RGB2RGBA)
    (.loadPixels image)
    (.get mat2 0 0 data8)
    (.get (.asIntBuffer (.order (. ByteBuffer wrap data8) ByteOrder/LITTLE_ENDIAN)) data32)
    ;(set! (.-pixels image) data32)
    (System/arraycopy data32 0 (.-pixels image) 0 (count data32))
    (.updatePixels image)
    image))

(def face-classifier (CascadeClassifier. "/Users/collinbell/opt/opencv/data/haarcascades/haarcascade_frontalface_default.xml"))

(defn detect-face [img]
  (let [grey (Mat.)
        faces (MatOfRect.)]
    (. Imgproc cvtColor img grey Imgproc/COLOR_RGB2GRAY)
    (.detectMultiScale face-classifier grey faces)
    (filter #(if (> (.-width %1) 150) true false) (.toArray faces))))

(defn setup []
  (q/frame-rate 4))

(defn draw  []
  (q/background 0)
  (let
      [new-frame
       (Mat.
        (int (.get video-feed (. Videoio CAP_PROP_FRAME_WIDTH)))
        (int (.get video-feed (. Videoio CAP_PROP_FRAME_HEIGHT)))
        CvType/CV_8UC3)]
    (q/text (str new-frame) 20 600)
    (q/text (str "Num Channels: " (.channels new-frame)) 20 630)
       (.read video-feed new-frame)
       (Imgcodecs/imwrite "derp.jpg" new-frame)
       (q/image (toPImage new-frame) 0 0 )
       (q/fill 0 0 0 0)
       (q/stroke-weight 3)
       (doall (map #(q/rect (.-x %1) (.-y %1) (.-width %1) (.-height %1)) (detect-face new-frame)))))




(q/defsketch example                  ;; Define a new sketch named example
  :title "Rouge Eyes"                 ;; Set the title of the sketch
  :settings #(q/smooth 2)             ;; Turn on anti-aliasing
  :setup setup                        ;; Specify the setup fn
  :draw draw                          ;; Specify the draw fn
  :size [ 1280  720 ])        










