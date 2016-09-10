(ns dameon.thalamus.core
  (:require [dameon.face.core :as face]
            [dameon.eyes.core :as eyes]
            [dameon.visual-cortex.core :as visual-cortex]
            [dameon.brochas-area.core :as brochas-area]
            [clojure.core.async :as async])) 

(defn init-face []
  (face/create))

(defn init-eyes []
  (eyes/see visual-cortex/tree))

(defn init-brochas-area []
  (brochas-area/launch-sphinx))


(defn wake-up []
  (async/go (init-face))
  (async/go (init-eyes))
  (async/go (init-brochas-area)))

(wake-up)
