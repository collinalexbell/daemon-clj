(ns dameon.voice.watson-speak
  (:require [clojure.edn :as edn :only [read-string]]
            [org.httpkit.client :as http]
            [clojure.java.io :as io]
            [clojure.core.async :as async])
  (:import [com.ibm.watson.developer_cloud.text_to_speech.v1 TextToSpeech]
           [com.ibm.watson.developer_cloud.text_to_speech.v1.util WaveUtils]
           [javax.sound.sampled AudioSystem AudioFormat]
           [java.io BufferedInputStream InputStream]))

(def creds (get (edn/read-string (slurp "config/creds.edn")) :ibm-creds))
(def finished (atom true))

(defn get-synthesis [text]
  (let [options
        {:basic-auth [(creds :username) (creds :password)]
         :query-params {:voice "en-US_AllisonVoice"
                        :accept "audio/wav"
                        :text text}}]
    (WaveUtils/reWriteWaveHeader (@(http/get (str (creds :url) "/v1/synthesize") options) :body))))



(defn open-wav [stream]
  (let [clip (AudioSystem/getClip)]
      (.open clip (AudioSystem/getAudioInputStream stream))
      clip))

(defn play-wav [clip]
  (.start clip)
  (Thread/sleep 50)
  (while (.isRunning clip) :pass)
  (swap! finished (constantly true)))

(defn speak [text]
    (let [audio-clip
          (open-wav (get-synthesis text))]
      (swap! finished (constantly false))
      (async/go (play-wav audio-clip))))

@finished












