(ns dameon.core
  (require [dameon.face.core :as face]
           [dameon.voice.core :as voice]
           [dameon.brochas-area.core :as brochas-area]
           [dameon.prefrontal-cortex.core :as prefrontal-cortex]
           [clj-time.core :as t]
           [clj-time.coerce :as c]
           [overtone.at-at :as at-at]
           [dameon.visual-cortex.youtube-player :as youtube-player]
           [dameon.visual-cortex.core :as visual-cortex]
           [dameon.eyes.core :as eyes]
           [clojure.core.async :as async]
           [dameon.smart-atom :as smart-atom]
           [dameon.keyboard-listener :as keyboard-listener]))

(import '[java.util.Timer])

(def user "Collin")

(defn init []
  (prefrontal-cortex/add-default-actions)
  (prefrontal-cortex/add-stop-listening)
  (eyes/see visual-cortex/tree)
  (keyboard-listener/start prefrontal-cortex/input))

(init)


(defn greet [name]
  (face/change-emotion :urgent)
  (voice/speak (str "Good evening " name ", how are you doing?"))
  (while (voice/is-speaking) :default)
  (face/change-emotion :happy)
  (prefrontal-cortex/anticipate-vocal-input 10000))


(defn start-greeter []
  (prefrontal-cortex/clear-possible-actions)
  (prefrontal-cortex/add-default-actions)
  (prefrontal-cortex/add-stop-listening)
  (visual-cortex/stop-display-basic-vision)
  (prefrontal-cortex/add-possible-actions
   :greet
   (fn [cur-state] (greet user)))
  (prefrontal-cortex/add-face-watcher))




(def alarm-not-fired? (atom true))

(defn fire-alarm? [time]
  (if (and (> (. System currentTimeMillis) time) @alarm-not-fired?)
    true
    false))

(def my-pool (at-at/mk-pool))

(defn set-alarm [time]
   (at-at/at (c/to-long (apply t/date-time time))  
             #(do (voice/speak "Hello. It is time to wake up!")
                  (. Thread sleep 3000)
                  (voice/speak "Hello. It is time to wake up!")
                  (. Thread sleep 3000)
                  (voice/speak "Hello. It is time to wake up!")
                  (swap! alarm-not-fired? (fn [x] (identity false))))
             my-pool))










