(ns dameon.temporal-lobe.core
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.brochas-area.core :as brochas-area]
   [clojure.core.async :as async]))

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
  (voice/speak (str "Good evening " name ", how are you doing?"))
  (while (voice/is-speaking) :default)
  (face/change-emotion :happy)
  (set-cur-conversation :status)
  (anticipate-vocal-input 5000))
 














