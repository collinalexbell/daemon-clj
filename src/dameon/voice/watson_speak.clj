(ns dameon.voice.watson-speak
  (:require [clojure.edn :as edn :only [read-string]]
            [org.httpkit.client :as http]
            [clojure.java.io :as io])
  (:import [com.ibm.watson.developer_cloud.text_to_speech.v1 TextToSpeech]
           [com.ibm.watson.developer_cloud.text_to_speech.v1.util WaveUtils]
           [javax.sound.sampled AudioSystem AudioFormat]
           [java.io BufferedInputStream InputStream]))

(def creds (get (edn/read-string (slurp "config/creds.edn")) :ibm-creds))

(defn get-synthesis [text]
  (let [options
        {:basic-auth [(creds :username) (creds :password)]
         :query-params {:voice "en-US_AllisonVoice"
                        :accept "audio/wav"
                        :text text}}]
    (WaveUtils/reWriteWaveHeader (@(http/get (str (creds :url) "/v1/synthesize") options) :body))))



(defn play-wav [stream]
  (let [clip (AudioSystem/getClip)]
      (.open clip (AudioSystem/getAudioInputStream stream))
      (.start clip)))


(defn speak [text]
  (play-wav (get-synthesis text)))
















