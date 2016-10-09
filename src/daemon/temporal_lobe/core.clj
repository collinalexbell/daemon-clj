(ns daemon.temporal-lobe.core
  (:require
   [daemon.face.core :as face]
   [daemon.voice.core :as voice]
   [daemon.prefrontal-cortex.core :as pfc]
   [daemon.brochas-area.core :as brochas-area]
   [clojure.core.async :as async]
   [overtone.at-at :as at-at]
   [daemon.prefrontal-cortex.core :as pfc]
   [daemon.temporal-lobe.twitter :as twitter]
   [daemon.visual-cortex.youtube-player :as youtube-player]
   [daemon.temporal-lobe.wiki-search :as wiki]
   [daemon.temporal-lobe.interrupt :as interrupt]
   [daemon.utils.time :as util-time]
   [daemon.utils.coll :as util-coll]))

(def state (atom {}))
(def my-pool nil)
(defn init []
  (def my-pool (at-at/mk-pool)))



(defn- set-cur-conversation
  "Sets a piece of state that keeps track of last topic talked about"
  [cur-conversation]
  (swap! state assoc :cur-conversation cur-conversation))

(defn- clear-cur-conversation
  "Wipes state about cur conversation"
  []
  (swap! state assoc :cur-conversation nil))

(defn update-user-status
  "Sets a piece of state that keeps track of my status 
  and then asks if I want to tweet my status"
  [status]
  (swap! state assoc :user-status status)
  (voice/speak "Would you like me to tweet that for you?")
  (set-cur-conversation :tweet?))

(defn anticipate-vocal-input
  "Sets a piece of state that the prefrontal-cortex uses to know when to not be 
  as indicitive of when the daemon is listening (...doesn't say 'i am listening')"
  [time]
  (swap! state assoc :anticipate-vocal-input true)
  ;;Stop the anticipation after time
  (async/go (do (Thread/sleep time)
                (swap! state assoc :anticipate-vocal-input false))))

(defn greet
  "Greets a person and provokes a status update"
  [name]
  (face/change-emotion :urgent)
  (voice/speak (str "Good morning " name ", what have you been doing?"))
  (while (voice/is-speaking) :default)
  (face/change-emotion :happy)
  (set-cur-conversation :status)
  (anticipate-vocal-input 5000))
 
(defmacro if-in-str
  "Will run through a series of clauses testing if the a string is present in the haystack and then executing code if it is or isnt
  
  Example usage:
(if-in-str
     \"bar\"
     (\"foo\"
       (println \"has foo\")
        (println \"does not have foo\"))
     (\"bar\"
      (println \"has bar\")"

  [haystack & clauses]
  (cons
   'do
   (map
    (fn [clause]
      (let [needle (first clause)
            is-present-form (second clause)
            is-not-present-form (nth clause 2 nil)]
        `(if (> (.indexOf ~haystack ~needle) -1)
           ~is-present-form
           ~is-not-present-form)))
    clauses)))

(defn pre-process-speech
  "Right now it just strips out periods. It will probably change in the future"
  [speech]
  (clojure.string/replace speech #"\." ""))


(def meditations
  (clojure.string/split (slurp "resources/meditation.edn") #"\n"))

(defn meditate
  "Takes meditations as lines and total time to meditate in form ss or mm:ss or hh:mm:ss
  Daemon will read each meditation mantra at evenly spaced times to take up the total meditation tim"
  [total-time-to-meditate meditations]
  (run!
   #(interrupt/fire-interrupt*
     in
     (str (int
           (/ (int
               (* (+ 1 (util-coll/index-of % meditations))
                  (/
                   (util-time/parse-time-string-into-ms total-time-to-meditate)
                   (+ 1 (count meditations))))) 1000)))
    that-says (str "Meditate about " %))
   meditations)
  (interrupt/fire-interrupt*
   in total-time-to-meditate
   that-says "You have finished your meditation!")
  (voice/speak "Starting meditation"))

(defn act-on-speech
  "Recieves raw speech, preprocesses it, and then passes signals on accordingly (or calls funcitons)
  Speech is stored in (cur-state :data)"
  [cur-state]
  (println "acting on speech")
  (let [cur-conversation (@state :cur-conversation)
        speech (pre-process-speech (:data cur-state))]
    (clear-cur-conversation)
    (if (= cur-conversation :status)
      (update-user-status (cur-state :data)))
    (if (and (= cur-conversation :tweet?) (> (.indexOf (:data cur-state) "yes") -1))
      (do (twitter/tweet (@state :user-status))
          (voice/speak "I sent the tweet. Is there anything else I can do for you?")))
    (if-in-str
     speech
     ("update my status"
      (do
        (set-cur-conversation :status)
        (voice/speak "Ok. What is up?")))
     ("pushup"
      (pfc/do-best-action {:num-pushups 5} :count-pushups))
     ("calendar"
      (pfc/do-best-action nil :tell-me-todays-events))
     ("change emotion"
      (if-in-str speech ("happy" (face/change-emotion :happy))))
     ("restore"
      (face/restore face/daemon-face))
     ("maximize"
      (face/maximize face/daemon-face))
     ("play"
      (if-in-str speech ("stop pla"
                         (youtube-player/stop-player)
                         (youtube-player/play-most-popular-by-search-term
                          (clojure.string/replace (:data cur-state) #"play" "")))))
     ("set alarm"
      :pass)
     ("meditate for"
      (-> speech
          (clojure.string/replace #"meditate for" "")
          (clojure.string/replace #"minutes" "")
          (clojure.string/trim)
          (str ":00")
          (meditate meditations)))
     ("search wikipedia for"
      (wiki/search
       (clojure.string/replace
        speech #"search wikipedia for" ""))))))

