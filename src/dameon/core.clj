(ns dameon.core
  (require [dameon.voice.core :as voice]
           [dameon.face.core :as face]
           [dameon.visual-cortex.core :as visual-cortex]
           [clojure.core.async :as async]))

(defn greet-person-if-seen []
  (async/go
    (loop [time-since-last-saw-person (. System currentTimeMillis)] ;in seconds 
      (Thread/sleep 1000)
      (println time-since-last-saw-person)
      (let [sees-person (visual-cortex/sees-person?)
            elapsed-time-since-last-saw-person (/ (- (. System currentTimeMillis) time-since-last-saw-person) 1000)]
        (if (and (> elapsed-time-since-last-saw-person 20) sees-person)
          (voice/speak "Hello There"))
        (recur (if sees-person (. System currentTimeMillis) time-since-last-saw-person))))))

(def greet-process (greet-person-if-seen))

(defn greet [name]
  (face/change-emotion :confused :block true)
  (voice/speak (str "Hello " name ))
  (voice/speak (str "How are you?"))
  (face/change-emotion :exuberant))













