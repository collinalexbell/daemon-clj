(ns dameon.prefrontal-cortex.core
  (:require [dameon.voice.core :as voice]
            [dameon.brochas-area.core :as brochas-area]
            [dameon.temporal-lobe.calendar :as cal]
            [dameon.temporal-lobe.twitter :as twitter]))


(def possible-actions (atom {}))

(defn add-possible-actions [goal action]
  (swap! possible-actions (fn [a] (assoc a goal (conj (get a goal) action)))))


;;This is limiting. Sometimes there isn't a best actions
;;Sometimes there are 2 mutually exclusive actions that should be taken at once
(defn do-best-action [cur-state goal]
  ((first (get @possible-actions goal)) cur-state))



;;;Comine input with state and make it into new state
;;;Then pass the new state to pfc/handle-state-change
(defn input [the-input]
  (if (= (get the-input :event) :words)
    (case (get the-input :from)
      :sphinx (do-best-action the-input :listen-intently)
      :api    (do (println the-input) (do-best-action the-input :act-on-speech)))))

(brochas-area/launch-sphinx input)

(twitter/notify-me-if :collinalexbell :messages-me)

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


















