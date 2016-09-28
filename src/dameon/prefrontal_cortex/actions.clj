(ns dameon.prefrontal-cortex.actions
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.brochas-area.core :as brochas-area]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.visual-cortex.youtube-player :as youtube-player]
   [dameon.temporal-lobe.calendar :as cal]
   [dameon.visual-cortex.core :as visual-cortex]))




(def action-map
  {:listen-intently
    (fn [cur-state]
      (face/set-emot-buffer (face/get-cur-emotion))
      (face/change-emotion :listen)
      (if (not (cur-state :anticipate-vocal-input))
        (voice/speak "I am listening"))
      (if (= (cur-state :event) :combo-pressed)
        ;;-1 activates manual stoppage
        (brochas-area/record-and-interpret-speech -1 pfc/input)
        (brochas-area/record-and-interpret-speech 5000 pfc/input)))

    :act-on-speech
    (fn [cur-state]
      (println "acting on speech")
      (if (> (.indexOf (:data cur-state) "calendar") -1)
        (pfc/do-best-action nil :tell-me-todays-events))
      (if (> (.indexOf (:data cur-state) "change emotion"))
        (if (> (.indexOf (:data cur-state) "happy") -1)
          (face/change-emotion :happy)))
      (if (> (.indexOf (:data cur-state) "maximize") -1)
        (face/maximize face/dameon-face))
      (if (> (.indexOf (:data cur-state) "restore") -1)
        (face/restore face/dameon-face))
      (if (> (.indexOf (:data cur-state) "play") -1)
        (if (> (.indexOf (:data cur-state) "stop pla") -1)
          (youtube-player/stop-player)
          (youtube-player/play-most-popular-by-search-term
           (clojure.string/replace (:data cur-state) #"play" "")))))

    :tell-me-todays-events
    (fn [cur-state]
      (voice/speak (cal/event-list-to-string (cal/get-todays-events) "today")))

    :stop-listening
    (fn [cur-state]
      (@brochas-area/stop-listening))

    :face-detection
    (fn [cur-state]
      (if (> (get-in cur-state [:data :conseq-face-frames]) 0)
        (do (swap! visual-cortex/detect-face (constantly nil)) 
            (pfc/do-best-action cur-state :greet))))})

(defn add-all []
  (doall
   (map
    #(apply pfc/add-possible-actions %)
    action-map)))
