(ns dameon.prefrontal-cortex.core
  (:require [dameon.voice.core :as voice]
            [dameon.brochas-area.core :as brochas-area]
            [dameon.temporal-lobe.calendar :as cal]))

(def possible-actions (atom {}))

(defn add-possible-actions [goal action]
  (swap! possible-actions (fn [a] (assoc a goal (conj (get a goal) action)))))


(defn do-best-action [cur-state goal]
  ((first (get @possible-actions goal)) cur-state))

(defn input [the-input]
  (if (= (get the-input :event) :words)
    (do-best-action the-input :listen-intently)))
;; The dameon has seen my face, it wants to please me, so it decides it is a good idea to greet me


(defn init []
  (add-possible-actions
   :listen-intently
   (fn [cur-state]
     (voice/speak "I am listening")
     (brochas-area/record-and-interpret-speech 5000)))
  (add-possible-actions
   :act-on-speech
   (fn [cur-state]
     (if (> (.indexOf (:data cur-state) "calendar") -1)
       (do-best-action nil :tell-me-todays-events))))
  (add-possible-actions
   :tell-me-todays-events
   (fn [cur-state]
     (voice/speak (cal/event-list-to-string (cal/get-todays-events) "today")))))

(init)















