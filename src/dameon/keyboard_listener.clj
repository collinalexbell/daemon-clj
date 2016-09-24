(ns dameon.keyboard-listener
  (:require
   [clojure.core.async :as async]
   [clojure.java.shell :as shell]
   [dameon.voice.core :as voice]))



(defn start-hotkey-dameon []
  (async/go (shell/sh "libs/hotkey/bin/hotkey.sh")))


(defn gen-hotkey-listener [callback]
  (fn [line]
    (println "hotkey event")
    (let [data (read-string line)]
        (async/go (callback data)))))

(defn readFile [file listener]
  (with-open [rdr (clojure.java.io/reader file)]
    (doseq [line (line-seq rdr)]
      (listener line))))

(defn launch-hotkey-listener [callback]
  (async/go
    (readFile
    (str (System/getProperty "user.dir") "/libs/hotkey/pipe")
    (gen-hotkey-listener callback))))


(defn kill-dameon []
  (doall
   (map #(shell/sh "kill" "-9" %)
        (clojure.string/split
         (get (shell/sh "libs/hotkey/bin/get_pid.sh") :out)
         #"\n"))))



(defn start [callback]
  (start-hotkey-dameon)
  (launch-hotkey-listener callback))















