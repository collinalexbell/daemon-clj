(ns dameon.prefrontal-cortex.input
  (require [dameon.prefrontal-cortex.core :as pfc]))

(defn input [the-input]
  (if (= (get the-input :event) :words)
    (pfc/do-best-action the-input :listen-intently)))

(input {:event :words
        :from  :sphinx
        :data  "ok dagny"})






