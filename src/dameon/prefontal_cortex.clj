(ns dameon.prefrontal-cortex)

(def possible-actions (atom {}))

(def critic-fns (atom {}))

(defn critique [cur-state goal possible-actions]
  ((get @critic-fns goal) cur-state goal possible-actions))

(defn add-possible-actions [goal action]
  (swap! possible-actions (fn [a] (assoc a goal (conj (get a goal) action)))))

(defn update-critic-fn [goal critic-fn]
  (swap! critic-fns (fn [c] (assoc c goal critic-fn))))

  (get possible-actions goal))

(defn do-best-action [cur-state goal]
  ((critique cur-state goal (get @possible-actions goal))))


;; The dameon has seen my face, it wants to please me, so it decides it is a good idea to greet me










