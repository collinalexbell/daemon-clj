(ns dameon.visual-cortex.stream
  (require [dameon.eyes.core :as eyes]))

(eyes/see)


(defprotocol Stream
  "Unit of Visual Processing"
  (get-next-frame [rec]))

(extend-type
 nil
 Stream
 (get-next-frame [rec] (eyes/get-current-frame)))


