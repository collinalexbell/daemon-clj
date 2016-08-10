(ns dameon.face.emotion-animation
  (require [quil.core :as q]
           [dameon.settings :as settings]))

(def width settings/width)
(def height settings/height)

(def animation-folder "face_animations/")

(def emotion-keyword-to-folder-name
  {:happy      "Happy/"
   :sad        "Sad/"
   :determined "Determined/"
   :confused   "Confused/"
   :exuberant  "Exuberant/"})

(defn get-full-file-paths
  "Generate the required file paths for this animation dir. 
  The path must be a path to a folder that contains: 
  'emotion.png', 'neutral_to_emotion.png', and 'emotion_to_neutral.png'"
  [sprite-file-dir]
    (let [corrected-base-path
        (if (not (= (last sprite-file-dir) \/))
          (str sprite-file-dir "/")
          sprite-file-dir)]

    {:emotion-loop
     (str corrected-base-path "emotion.png") 

     :neutral-to-emotion
     (str corrected-base-path "neutral_to_emotion.png")

     :emotion-to-neutral
     (str corrected-base-path "emotion_to_neutral.png")}))

(defn load-sprite-files
  "Returns a map with the loaded sprite files that were loaded using quill.core/image-load"
  [emotion-name]
  (if-let [emotion-folder
           (emotion-keyword-to-folder-name emotion-name)]
    (apply merge
     (map
        (fn [l]
          (let [img (q/load-image (last l))]
            (q/resize
             img
             (* (/ (.width img) 2160) width)
             height)
            {(first l) img}))
        (get-full-file-paths
         (str animation-folder emotion-folder))))
      (throw (Exception. "Incorrect emotion"))))

(defn split-image-into-frames [image]
  (let [num-frames (/ (.width image) width)]
   (doall (map
     (fn [index]
       (let [dest (q/create-image width height :rgb)]
         (q/copy image dest [(* width index) 0 width height] [0 0 width height])
         dest))
     (range num-frames)))))

(defn split-images-into-frames [images]
  (apply merge
   (map
    (fn [item]
      {(first item)
       (split-image-into-frames (second item))})
    images)))


(defn transition-into
  "Resets emotion-animation such that cur-frame returns the first frame of neutral-to-emotion"
  [emotion-animation]
  (assoc emotion-animation
         :cur-frame-no 0

         :cur-animation
         :neutral-to-emotion

         :finished
         false))

(defn transition-out-of
  [emotion-animation]
  (assoc emotion-animation
         :cur-frame-no 0

         :cur-animation
         :emotion-to-neutral))

(defn next-frame
  [emotion-animation]
  (case (emotion-animation :cur-animation)
    :neutral-to-emotion
    (if (<
         (emotion-animation :cur-frame-no)
         (- (count (get-in emotion-animation [:frames :neutral-to-emotion])) 1))
      (assoc emotion-animation
             :cur-frame-no (+ 1 (emotion-animation :cur-frame-no)))
      (assoc emotion-animation
             :cur-animation :emotion-loop
             :cur-frame-no  0))

    :emotion-loop
    (assoc emotion-animation
           :cur-frame-no
           (mod
            (+ 1 (emotion-animation :cur-frame-no))
            (count (get-in emotion-animation [:frames :emotion-loop]))))

    :emotion-to-neutral
    (if (< (emotion-animation :cur-frame-no) (- (count (get-in emotion-animation [:frames :emotion-to-neutral])) 1))
      (assoc emotion-animation
             :cur-frame-no (+ 1 (emotion-animation :cur-frame-no)))
      (assoc emotion-animation :finished true))))

(defn get-cur-frame [emotion-animation]
  (nth
   (get-in emotion-animation [:frames (emotion-animation :cur-animation)])
   (emotion-animation :cur-frame-no)))

(defn new [emotion-name]
  {:frames
   (split-images-into-frames
    (load-sprite-files emotion-name))
   :cur-frame-no 0
   :cur-animation :neutral-to-emotion
   :finished true})




















