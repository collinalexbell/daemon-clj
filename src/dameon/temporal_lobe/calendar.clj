(ns dameon.temporal-lobe.calendar
  (:require [clojure.edn :as edn :only [read-string]]
            [clojure.pprint :as pprint]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l])

  (:use [google-apps-clj.google-calendar]))

(def creds (edn/read-string (slurp "config/google-creds.edn")))


(def google-datetime-formatter (f/formatter "yyyy-MM-dd'T'hh:mm:ssZZ"))

(list-events creds
             (f/unparse google-datetime-formatter (t/today-at 00 00))
             (f/unparse google-datetime-formatter (t/plus (t/today-at 23 59) (t/days 1))))


(map #(println %) (list-events creds "2016-07-01T00:00:00-02:00" "2016-09-04T00:00:00-02:00"))



















