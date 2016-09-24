(ns dameon.prefrontal-cortex.core
  (:require [dameon.voice.core :as voice]
            [dameon.face.core :as face]
            [dameon.visual-cortex.core :as visual-cortex]
            [dameon.brochas-area.core :as brochas-area]
            [dameon.visual-cortex.youtube-player :as youtube-player]
            [dameon.temporal-lobe.calendar :as cal]
            [dameon.eyes.core :as eyes]))



(def face-buffer (atom nil))
(def possible-actions (atom {}))

(defn add-possible-actions [goal action]
  (swap! possible-actions (fn [a] (assoc a goal (conj (get a goal) action)))))


;;This is limiting. Sometimes there isn't a best actions
;;Sometimes there are 2 mutually exclusive actions that should be taken at once
(defn do-best-action [cur-state goal]
  (try
    ((first (get @possible-actions goal)) cur-state)
    (catch Exception e)))



;;;Comine input with state and make it into new state
;;;Then pass the new state to pfc/handle-state-change
(defn input [the-input]
  (println "input recieved")
  (case (get the-input :event)
    :words
    (case (get the-input :from)
      :text (do-best-action the-input :act-on-speech)
      :sphinx (do-best-action the-input :listen-intently)
      :api    (do (println the-input) (do-best-action the-input :act-on-speech)))
    :face-detection
    (do-best-action the-input :face-detection)
    :combo-pressed
    (do-best-action the-input :listen-intently)
    :combo-released
    (do-best-action the-input :stop-listening)))

;;Sphinx is shit. It doesn't work. Sorry.
;(brochas-area/launch-sphinx input)

;;(twitter/notify-me-if :collinalexbell :messages-me)

(defn init []
  (eyes/see visual-cortex/tree))

(defn add-default-actions []
  (add-possible-actions
   :listen-intently
   (fn [cur-state]
     (face/set-emot-buffer (face/get-cur-emotion))
     (face/change-emotion :listen)
     (voice/speak "I am listening")
     (if (= (cur-state :event) :combo-pressed)
       ;;-1 activates manual stoppage
       (brochas-area/record-and-interpret-speech -1 input)
       (brochas-area/record-and-interpret-speech 5000 input))))
  (add-possible-actions
   :stop-listening
   (fn [cur-state]
     (brochas-area/stop-listening)))
  (add-possible-actions
   :act-on-speech
   (fn [cur-state]
     (println "acting on speech")
     (if (> (.indexOf (:data cur-state) "calendar") -1)
       (do-best-action nil :tell-me-todays-events))
     (if (> (.indexOf (:data cur-state) "play") -1)
       (if (> (.indexOf (:data cur-state) "stop pla") -1)
         (youtube-player/stop-player)
         (youtube-player/play-most-popular-by-search-term
          (clojure.string/replace (:data cur-state) #"play" ""))))))
  (add-possible-actions
   :tell-me-todays-events
   (fn [cur-state]
     (voice/speak (cal/event-list-to-string (cal/get-todays-events) "today")))))

(defn add-stop-listening []
  (add-possible-actions
   :stop-listening
   (fn [cur-state]
     (@brochas-area/stop-listening))))


(defn add-face-watcher []
  (visual-cortex/start-face-detect input)
  (add-possible-actions
   :face-detection
   (fn [cur-state]
     (voice/speak "I see you"))))

(add-default-actions)
(add-stop-listening)

;(init)

;(add-face-watcher)
















