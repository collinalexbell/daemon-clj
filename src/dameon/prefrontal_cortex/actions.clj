(ns dameon.prefrontal-cortex.actions
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.brochas-area.core :as brochas-area]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.visual-cortex.youtube-player :as youtube-player]
   [dameon.temporal-lobe.calendar :as cal]
   [dameon.visual-cortex.core :as visual-cortex]
   [dameon.temporal-lobe.core :as temporal-lobe]
   [dameon.temporal-lobe.twitter :as twitter]))


(def action-map
  {:listen-intently
    (fn [cur-state]
      (face/set-emot-buffer (face/get-cur-emotion))
      (face/change-emotion :listen)
      (if (not (@temporal-lobe/state :anticipate-vocal-input))
        (voice/speak "I am listening"))
      (if (= (cur-state :event) :combo-pressed)
        ;;-1 activates manual stoppage
        (brochas-area/record-and-interpret-speech -1 pfc/input)
        (brochas-area/record-and-interpret-speech 5000 pfc/input)))

    :act-on-speech
    (fn [cur-state]
      (println "acting on speech")
      (let [cur-conversation (@temporal-lobe/state :cur-conversation)]
        (temporal-lobe/clear-cur-conversation)
       (if (= cur-conversation :status)
         (temporal-lobe/update-user-status (cur-state :data)))
       (if (and (= cur-conversation :tweet?) (> (.indexOf (:data cur-state) "yes") -1))
         (do (twitter/tweet (@temporal-lobe/state :user-status))
             (voice/speak "I sent the tweet. Is there anything else I can do for you?")))
       (if (> (.indexOf (:data cur-state) "pushup") -1)
         (pfc/do-best-action {:num-pushups 5} :count-pushups))
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
            (clojure.string/replace (:data cur-state) #"play" ""))))
       ))

    :tell-me-todays-events
    (fn [cur-state]
      (voice/speak (cal/event-list-to-string (cal/get-todays-events) "today")))

    :stop-listening
    (fn [cur-state]
      (@brochas-area/stop-listening))

    :face-detection
    (fn [cur-state]
      (if (> (get-in cur-state [:data :conseq-face-frames]) 1)
        (do (swap! visual-cortex/detect-face (constantly nil)) 
            (pfc/do-best-action cur-state :greet))))

   :update-user-status
   temporal-lobe/update-user-status

   :count-pushups
   (fn [cur-state]
     (voice/speak "Ok.")
     (visual-cortex/display-pushup-counter (cur-state :num-pushups)))})

(defn add-all []
  (doall
   (map
    #(apply pfc/add-possible-actions %)
    action-map)))


(defn reload-action [action]
  (pfc/remove-possible-action action)
  (pfc/add-possible-actions action (action-map action)))


















