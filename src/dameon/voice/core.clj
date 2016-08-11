(ns dameon.voice.core
  (require [dameon.settings :as settings])
  (use [clojure.java.shell :only [sh]]))

(def engine settings/voice-engine)

(defn cerevoice-speak [words]
  (spit "speak" words)
  (sh "./cerevoice/examples/python/tts_callback.py" "-p" "-Vcerevoice/cerevoice_heather_3.2.0_48k.voice" "-Lcerevoice/license.lic" "speak")
  :done)

(defn mac-speak [words]
  (sh "say" "-vVicki" words)
  :done)

(defn speak [words]
  (case engine
    :cerevoice (cerevoice-speak words)
    :mac       (mac-speak words)
  (throw (Exception. "No voice engine enabled"))))




















