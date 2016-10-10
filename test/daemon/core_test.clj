(ns daemon.core-test
  (:require [clojure.test :refer :all]
            [daemon.core :refer :all]
            [daemon.brochas-area.core :as brochas-area]
            [daemon.voice.core :as voice]
            [daemon.temporal-lobe.core :as temporal-lobe]))


;;Features to test
;;;Voice recognition
(deftest interpret-speech []
  (is
   (= "This is a test."
    (brochas-area/get-words-from-api-result
     @(brochas-area/interpret-speech (brochas-area/slurp-bytes "resources/test/test-interpret-speech.wav"))))))


(deftest meditation []
  (voice/speak "Testing meditations. You should hear 2 test meditations with 10 seconds in between.")
  (Thread/sleep 5000)
  (temporal-lobe/meditate "30" ["the beauty of clojure" "the beauty of nature"]))

(interpret-speech)

(meditation)

















