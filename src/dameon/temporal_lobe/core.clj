(ns temporal-lobe.core
  (:require
   [dameon.face.core :as face]
   [dameon.voice.core :as voice]
   [dameon.prefrontal-cortex.core :as pfc]
   [dameon.brochas-area.core :as brochas-area]))

  (defn greet [name]
    (face/change-emotion :urgent)
    (voice/speak (str "Good evening " name ", how are you doing?"))
    (while (voice/is-speaking) :default)
    (face/change-emotion :happy)
    (brochas-area/anticipate-vocal-input 10000))
  










