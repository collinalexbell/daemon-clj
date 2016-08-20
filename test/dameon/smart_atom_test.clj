(ns dameon.smart-atom-test
  (:require [clojure.test :refer :all]
            [dameon.smart-atom :as smart-atom]))

(import '[org.opencv.core Mat]
        '[org.opencv.imgcodecs Imgcodecs])


(deftest smart-atom
  (let [a (smart-atom/create (Imgcodecs/imread "ramen.jpg"))]
    (is (not (.empty (smart-atom/deref a))))
    (let [b (smart-atom/copy a)]
      (smart-atom/delete a)
      (is (not (.empty (smart-atom/deref b))))
      (smart-atom/delete b)
      (is (.empty (smart-atom/deref b))))))

(run-tests)

















