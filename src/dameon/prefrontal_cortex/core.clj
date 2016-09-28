(ns dameon.prefrontal-cortex.core
  (:require [dameon.voice.core :as voice]
            [dameon.face.core :as face]
            [dameon.visual-cortex.core :as visual-cortex]
            [dameon.brochas-area.core :as brochas-area]
            [dameon.visual-cortex.youtube-player :as youtube-player]
            [dameon.temporal-lobe.calendar :as cal]
            [dameon.eyes.core :as eyes]
            [clojure.core.async :as async]))

(def temporal-state (atom {}))
(def action-memory (atom []))
(def input-memory (atom []))
(def auditory-state (atom {}))
(def face-buffer (atom nil))
(def possible-actions (atom {}))
(defn clear-possible-actions []
  (def possible-actions (atom {})))

(defn add-possible-actions [goal action]
  (swap! possible-actions (fn [a] (assoc a goal (conj (get a goal) action)))))

(defn remove-possible-action [goal]
  (swap! possible-actions dissoc goal))


;;This is limiting. Sometimes there isn't a best actions
;;Sometimes there are 2 mutually exclusive actions that should be taken at once
(defn do-best-action [cur-state goal]
  (swap! action-memory conj goal)
  (try
    ((last (get @possible-actions goal)) cur-state)
    (catch Exception e)))


;;;Comine input with state and make it into new state
;;;Then pass the new state to pfc/handle-state-change
(defn input [the-input]
  (println "input recieved")
  (case (get the-input :event)
    :words
    (case (get the-input :from)
      :text (do-best-action (assoc the-input :last-action (peek @action-memory)) :act-on-speech)
      :sphinx (do-best-action the-input :listen-intently)
      :api    (do (println the-input) (do-best-action the-input :act-on-speech)))
    :face-detection
    (do-best-action the-input :face-detection)
    :combo-pressed
    (do-best-action (merge the-input @auditory-state) :listen-intently)
    :combo-released
    (do-best-action the-input :stop-listening))
  (swap! input-memory conj the-input))

;;Sphinx is shit. It doesn't work. Sorry.
;(brochas-area/launch-sphinx input)

;;(twitter/notify-me-if :collinalexbell :messages-me)

(defn init []
  (eyes/see visual-cortex/tree))


(defn anticipate-vocal-input [time]
  (swap! auditory-state assoc :anticipate-vocal-input true)
  ;;Stop the anticipation after time
  (async/go (do (Thread/sleep time)
                (swap! auditory-state assoc :anticipate-vocal-input false))))

















