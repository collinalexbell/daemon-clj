(ns dameon.temporal-lobe.core
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.brochas-area.core :as brochas-area]
   [clojure.core.async :as async]
   [overtone.at-at :as at-at]))

(def state (atom {}))

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

(def my-pool (at-at/mk-pool))

(defn set-alarm [time-string & actions]
  (at-at/after
   (parse-time-string-into-ms time-string)
   #(doall
     (map
      (fn [action] (action))
      actions))
   my-pool))












