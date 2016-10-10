(ns daemon.core-test
  (:require [clojure.test :refer :all]
            [daemon.core :refer :all]
            [daemon.brochas-area.core :as brochas-area]))


;;Features to test
;;;Voice recognition
(deftest interpret-speech []
  (is
   (= "This is a test."
    (brochas-area/get-words-from-api-result
     @(brochas-area/interpret-speech (brochas-area/slurp-bytes "resources/test/test-interpret-speech.wav"))))))


(interpret-speech)

















