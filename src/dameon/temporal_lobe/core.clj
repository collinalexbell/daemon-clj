(ns dameon.temporal-lobe.core
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.brochas-area.core :as brochas-area]
   [clojure.core.async :as async]
   [overtone.at-at :as at-at]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.temporal-lobe.twitter :as twitter]))

(def state (atom {}))
(def my-pool nil)
(defn init []
  (def my-pool (at-at/mk-pool)))



(defn set-cur-conversation [cur-conversation]
  (swap! state assoc :cur-conversation cur-conversation))

(defn clear-cur-conversation []
  (swap! state assoc :cur-conversation nil))

(defn update-user-status [status]
  (swap! state assoc :user-status status)
  (voice/speak "Would you like me to tweet that for you?")
  (set-cur-conversation :tweet?))

(defn anticipate-vocal-input [time]
  (swap! state assoc :anticipate-vocal-input true)
  ;;Stop the anticipation after time
  (async/go (do (Thread/sleep time)
                (swap! state assoc :anticipate-vocal-input false))))

(defn greet [name]
  (face/change-emotion :urgent)
  (voice/speak (str "Good morning " name ", what have you been doing?"))
  (while (voice/is-speaking) :default)
  (face/change-emotion :happy)
  (set-cur-conversation :status)
  (anticipate-vocal-input 5000))
 


(defn parse-time-string-into-ms [time-string]
  ;;Count the colons, that will tell you what the first numbers should be
  (apply
   +
   (map
    #(* (Integer/parseInt %1)  %2)
    (reverse (clojure.string/split time-string #":")) 
    [1000 (* 1000 60) (* 1000 60 60) (* 1000 60 60 24)])))

(defn set-alarm [time-string & actions]
  (at-at/after
   (parse-time-string-into-ms time-string)
   #(doall
     (map
      (fn [action] (action))
      actions))
   my-pool))

(defmacro if-in-str [needle haystack is-present-form is-not-present-form]
  (if (> (.indexOf haystack needle) -1)
    true
    false))

(defn act-on-speech [cur-state]
  (println "acting on speech")
  (let [cur-conversation (@state :cur-conversation)]
    (clear-cur-conversation)
    (if (= cur-conversation :status)
      (update-user-status (cur-state :data)))
    (if (and (= cur-conversation :tweet?) (> (.indexOf (:data cur-state) "yes") -1))
      (do (twitter/tweet (@state :user-status))
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
         (clojure.string/replace (:data cur-state) #"play" ""))))))











