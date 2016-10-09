(ns daemon.temporal-lobe.interrupt
  (:require
   [clojure.core.async :as async]
   [overtone.at-at :as at-at]
   [daemon.voice.core :as voice]
   [clj-time.coerce :as c]
   [clj-time.format :as f]))

(def list-limit 5)
(def interrupts (at-at/mk-pool))



(defn parse-time-string-into-ms [time-string]
  ;;Count the colons, that will tell you what the first numbers should be
  (apply
   +
   (map
    #(* (Integer/parseInt %1)  %2)
    (reverse (clojure.string/split time-string #":")) 
    [1000 (* 1000 60) (* 1000 60 60) (* 1000 60 60 24)])))

(defn add-interrupt
  [interrupt-time desc & actions]
  (at-at/after
   (if (string? interrupt-time)
     (parse-time-string-into-ms interrupt-time)
     interrupt-time)
   #(doall
     (map
      (fn [action] (action))
      actions))
   interrupts
   :desc desc))

(defmacro add-interrupt*
  [& stuff])


;;;example call
;;;(add-interupt
;;; in "2:00" ; ..at "3:00" (in means timer and at means alarm)
;;; in-category :awesome
;;; that-says "Hello World" 
;;; then-waits "2" 
;;; then-says "Goodby World")


;;;example speech
;;; "set timer for 2 minutes in category awesome that says hello world then waits 2 seconds and then says goodbye world"

(defn k->s [k]
  (name k))

(defn to-str [i]
  (str
   "<speak version=\"1.0\">"
   "interrupt i.d. " (get i :id) 
   (if-let [name (get-in i [:desc :name])]
     (str " <break strength=\"x-weak\"></break>with name " (k->s name)))
   " <break strength=\"x-weak\"></break> firing at "
   (f/unparse (f/formatters :hour-minute)
              (c/from-long (+ (get i :created-at) (get i :initial-delay))))
   "</speak>"))

(defn get-interrupts-by-id-set [id-set]
  (filter
   #(contains? id-set (get % :id))
   (at-at/scheduled-jobs interrupts)))

(defn is-are [count]
  (if (= count 1)
    "is"
    "are"))

(defn plural-or-not [count]
  (if (= count 1)
    ""
    "s"))

(defn gen-interupt-speak-intro [cnt]
  (str "There "
       (is-are cnt)
       " "
       cnt
       " interrupt" (plural-or-not cnt) ":"))

(defn speak-interrupts [id-set]
  {:pre  [(set? id-set)]}
  (let [filtered-interrupts (get-interrupts-by-id-set id-set)]
   (println
    (reduce
     #(str (if (not (= (gen-interupt-speak-intro (count filtered-interrupts)) %1))
             (str %1 " and ") %1)
           (to-str %2) " ")
     (gen-interupt-speak-intro (count filtered-interrupts))
     filtered-interrupts))))

(defmacro speak-interupts*
  [& stuff])

;;example
;;(list-active-interupts after "1:00 " before "2:00" in-category :sexy limit-to 5)

(defn delete-interupts [id-set]
  (run!
   #(at-at/kill %)
   (get-interrupts-by-id-set id-set)))

(defmacro delete-interupts* [& stuff])

;;;example calls
;;;(delete-interupts
;;; in-category :awesome
;;; with-ids [0 1 2]
;;; after "2:00"
;;; before "4:00")

















