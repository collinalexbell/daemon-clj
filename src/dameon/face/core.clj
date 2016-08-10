(ns dameon.face.core
  (require
   [dameon.face.emotion-animation :as animation]
   [dameon.settings :as settings]
   [quil.core :as q]
   [quil.middleware :as m]
   [clojure.core.async :as async :refer [go chan <! >!]]))


(def transitioner (ref nil))

(def width settings/width)
(def height settings/height)

(def available-emotions
  [:happy :sad :determined :confused :exuberant])

(defn load-emotions []
  (apply merge (map
    (fn [emot] {emot (animation/new emot)})
    available-emotions)))

(defn setup []
  (q/frame-rate 1)
  (q/background 255)
  (let [anim (q/load-image "face_animations/Happy/neutral_to_emotion.png")]
    (q/resize anim  (* 4 width) height)
    {:x 1 :animation anim :emotions (load-emotions) :cur-index 0 :transition-emotion :happy :cur-emotion :happy}))

(defn update-state [state]
  (q/frame-rate 7)
  (if (not (nil? @transitioner))
    (let [transition-emotion @transitioner]
      (do
       (dosync (ref-set transitioner nil))
       (assoc
        (assoc-in state
                  [:emotions (state :cur-emotion)]
                  (animation/transition-out-of (get-in state [:emotions (state :cur-emotion)])))
        :transition-emotion transition-emotion)))
    (if (not ((get-in state [:emotions (state :cur-emotion)]) :finished))
     (assoc-in state
             [:emotions (state :cur-emotion)]
             (animation/next-frame (get-in state [:emotions (state :cur-emotion)])))
     (assoc
      (assoc-in state
                [:emotions (state :transition-emotion)]
                (animation/transition-into (get-in state [:emotions (state :transition-emotion)])))
       :cur-emotion
       (state :transition-emotion)))))


(defn draw [state]
  ;(go (q/text (<! c) 20 40))
  (q/background 0)
  (q/image (animation/get-cur-frame (get-in state [:emotions (state :cur-emotion)]))  0 0 200 200)
  (q/fill 255)
  ;(q/text (str state) 20 20)
  )
  
;(go (>! "Hello there"))

(q/defsketch dameon-face
  :size [width height]
  :setup setup
  :update update-state
  :draw draw
  :middleware [m/fun-mode])



(defn change-emotion [emotion]
  (if (> (.indexOf available-emotions emotion) -1)
    (dosync (ref-set transitioner emotion))
    (throw (Exception. "This emotion is not available"))))









