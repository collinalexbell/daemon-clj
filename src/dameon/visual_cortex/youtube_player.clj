(ns dameon.visual-cortex.youtube-player
  (:import [javafx.application Application]
           [javafx.scene Scene]
           [javafx.scene.web WebView]
           [javafx.stage StageBuilder]))


(defonce force-toolkit-init (javafx.embed.swing.JFXPanel.))
 
(defn run-later*
[f]
(javafx.application.Platform/runLater f))
 
(defmacro run-later
[& body]
`(run-later* (fn [] ~@body)))
 
(defn run-now*
[f]
(let [result (promise)]
(run-later
(deliver result (try (f) (catch Throwable e e))))
@result))
 
(defmacro run-now
[& body]
`(run-now* (fn [] ~@body)))

(defn get-url [media]
  (if (= media :chopin)
    "https://www.youtube.com/watch?v=9E6b3swbnWg"
    media))

(defn play [media]
  (let [video-url (get-url media)
        stage
        (run-now
         (let [inner-stage (.build (StageBuilder/create))
               web-view (WebView.)]
           (-> web-view 
               (.getEngine)
               (.load video-url))
           (.setPrefSize web-view 640 390)
           (.setScene inner-stage (Scene. web-view))
           inner-stage))]
    (run-now (.show stage))))
















