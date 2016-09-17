(ns dameon.face.core
  (require
   [dameon.face.emotion-animation :as animation]
   [dameon.settings :as settings]
   [dameon.smart-atom :as smart-atom]
   [quil.core :as q]
   [quil.middleware :as m]
   [clojure.core.async :as async :refer [go chan <! >!]]
   [clojure.java.shell :as shell]))

(import '[org.opencv.core MatOfInt MatOfByte MatOfRect Mat CvType Size]
        '[org.opencv.imgcodecs Imgcodecs]
        '[org.opencv.imgproc Imgproc])


(import 'java.nio.ByteBuffer)
(import 'java.nio.ByteOrder)

(def animation-frame-rate 2)
(def transitioner (ref nil))
(def frame-rate (ref 2))
(def draw-mat? (ref false))
(def smart-mat-to-draw (ref (smart-atom/create (Mat. settings/width settings/height CvType/CV_8UC3))))
(def in-main-loop? (ref false))
(def cur-emotion (ref nil))

(defn has-extension [the-str ext]
  (> (.indexOf the-str (str "." ext)) -1))

(defn get-ext-filenames [folder ext]
  (filter
   #(has-extension % ext)
   (map
    #(.getName %)
    (filter
     #(.isFile %)
     (.listFiles (clojure.java.io/file folder))))))

(defn turn-gifs-in-folder-into-sprites [folder]
  (let [emotion-names (map
                       #(first (clojure.string/split % #".gif"))
                       (get-ext-filenames folder "gif"))]
    (doall (map #(shell/sh "mkdir" (str folder "/" % )) emotion-names))
    (doall (map #(shell/sh
            "convert"
            "-coalesce"
            (str folder "/" % ".gif")
            (str folder "/" (str % "/%02d.png")))
          emotion-names))))

(def available-emotion-keys 
  (map
   #(keyword (.getName %))
    (filter
     #(not (.isFile %))
     ;;all folders and files
     (.listFiles (clojure.java.io/file settings/face-animation-folder)))))

(defn load-emotions []
  (apply merge (map
    (fn [emot] {emot (animation/new emot)})
    available-emotion-keys)))

(defn setup []
  (q/frame-rate animation-frame-rate)
  (q/background 255)
  (let [animations (load-emotions)]
    (dosync (ref-set cur-emotion :happy))
    {:emotions animations :cur-emotion :happy}))

(defn update-state [state]
  (q/frame-rate @frame-rate)
  (if (not (nil? @transitioner))
    ;;transition
    (let [transition-emotion @transitioner]
      (do
       (dosync (ref-set transitioner nil))
       (-> state
           (assoc-in [:emotions transition-emotion]
                     (animation/reset (get-in state [:emotions transition-emotion])))
           (assoc :cur-emotion transition-emotion))))
    ;;or just get the next state
    (assoc-in state
              [:emotions (state :cur-emotion)]
              (animation/next-frame (get-in state [:emotions (state :cur-emotion)])))))

(defn mat-to-p-image [mat]
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

(defn draw-mat [mat]
  (q/image (mat-to-p-image mat) 0 0 ))

(defn update-mat-to-display [smart-mat]
  (try
    (let [new-smart-mat-to-draw
         (if (= (.size (smart-atom/deref smart-mat))
                (Size. settings/width settings/height))
           ;return a copy of the smart mat if no changes need to be made to the mat
           smart-mat
           ;;if changes do need to be made, the smart mat must be deep-cloned to get a brand new matrix
           ;;this is because other processes have access to the matrix and we don't want to alter it
           (let [new-smart-mat (smart-atom/deep-clone smart-mat)]
             (. Imgproc resize
                (smart-atom/deref new-smart-mat)
                (smart-atom/deref new-smart-mat)
                (Size. settings/width settings/height))
             new-smart-mat))]
      (smart-atom/delete smart-mat)
      (let [old-smart-mat @smart-mat-to-draw]
        ;;set the new smart-mat-to-draw
        (dosync (ref-set smart-mat-to-draw new-smart-mat-to-draw))
        ;;since the smart-mat-to-draw reference has changed, we need to smart-mat/delete the old smart-mat
        (smart-atom/delete old-smart-mat)))
    (catch Exception e (println (str (.getMessage e))))))

(defn activate-mat-display []
  (dosync (ref-set draw-mat? true)
          (ref-set frame-rate 30)))

(defn deactivate-mat-display []
  (dosync (ref-set draw-mat? false)
          (ref-set frame-rate animation-frame-rate)))


(defn draw [state]
  (q/background 0)
  (if @draw-mat?
    ;;increment the copy counter so the mat doesn't get released mid display
    (do
      (smart-atom/copy @smart-mat-to-draw)
      (draw-mat (smart-atom/deref @smart-mat-to-draw))
      ;;decrement the copy counter so the mat can get released if the smart-mat-to-draw has been updated
      (smart-atom/delete @smart-mat-to-draw))
    (q/image (animation/get-cur-frame (get-in state [:emotions (state :cur-emotion)]))  -139 -25))
  (q/fill 255))
  
;(go (>! "Hello there"))

(defn create []
 (q/defsketch dameon-face
   :size [600 450]
   :setup setup
   :update update-state
   :draw draw
   :middleware [m/fun-mode]))


(defn change-emotion
  "Function used to change the emotion displayed of the face. 
   If keyword argument :block is set true, then the function will wait until the animation is in the :emotion-loop to return"
  [emotion & {:keys [block] :or {block false}}]
  (dosync (ref-set in-main-loop? false))
  (if (> (.indexOf available-emotion-keys emotion) -1)
    (dosync (ref-set transitioner emotion)
            (ref-set cur-emotion emotion))
    (throw (Exception. "This emotion is not available")))
  (if block
    (while (not @in-main-loop?))))

(defn get-cur-emotion []
  @cur-emotion)
