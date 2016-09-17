(ns dameon.visual-cortex.emotion-recognition
  (:require [org.httpkit.client :as http]))

(defn mat-to-mem-jpg [mat]
  (let [options
        {:headers {"Authorization" (str "Bearer " (get-access-token))
                   "" "04d8887cbcc24bbe8009b36de7a32a72"
                   "Content-type" "image/jpeg"}}])
  )



















