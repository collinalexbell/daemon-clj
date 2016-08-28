(ns dameon.visual-cortex.stream-tree-test
  (:require
   [dameon.visual-cortex.stream-tree :as t]
   [dameon.visual-cortex.stream :as s])
  (:use
   [clojure.test]))

(deftest add
  (let [tree (t/create)
        s1   (s/->Base-stream [] [])]
    ;;Exception if stream doesn't have a name
    (is (thrown? Exception (t/add s1 tree)))
    (let [s1 (assoc s1 :name :foo)]
     ;;Exception if the parant with parent-name doesn't exist
      (is (thrown? Exception (t/add s1 tree :bar)))
     ;;Test t/add when t is empty
      (is (= (t/add s1 tree) {:paths {:foo [0]} :structure [s1]}))
      ;;Test t/add 2x when t is empty
      (let [s2 (assoc (s/->Base-stream [] []) :name :bar)]
       (is (= (t/add s2 (t/add s1 tree)) {:paths {:foo [0] :bar [1]} :structure [s1 s2]})))
      (let [s-w-upstream (assoc (s/->Base-stream [(assoc s1 :name :bar)] []) :name :foo)]
        (is (= (t/add (assoc (s/->Base-stream [] []) :name :bar) (t/add s1 tree) :foo)
               {:paths {:foo [0] :bar [0 0]} :structure [s-w-upstream]} ))))))


(run-tests)

















