(ns dameon.voice.core
  (require [dameon.settings :as settings]
           [dameon.voice.watson-speak :as watson])
  (use [clojure.java.shell :only [sh]]))


(defn cerevoice-speak [words]
  (spit "speak" words)
  (sh "./cerevoice/examples/python/tts_callback.py" "-p" "-Vcerevoice/cerevoice_heather_3.2.0_48k.voice" "-Lcerevoice/license.lic" "speak")
  :done)

(defn mac-speak [words]
  (sh "say" "-vVicki" words)
  :done)

(defn is-speaking []
  (case settings/voice-engine
    :watson (not @watson/finished)
    :mac true
    :default true))

(defn speak [words]
  (case settings/voice-engine
    :watson    (watson/speak words)
    :cerevoice (cerevoice-speak words)
    :mac       (mac-speak words)
  (throw (Exception. "No voice engine enabled"))))




















