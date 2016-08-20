(ns dameon.smart-atom
  (require [dameon.smart-object :as smart-object]))


(defn- error-check [a]
    (if (not (nil? (a :deleted)))
        (throw (Exception. "This ref has been deleted"))))

(defn create
  "Creates a ref-to-smart-obj that can not be derefed easily (because it is in a map)"
  [obj]

  {:smart-object-atom (atom (smart-object/create obj))})

(defn deref
  "Returns the object"
  [a] 

  (error-check a)
  (smart-object/get-object @(a :data)))


(defn copy
  "Returns a copy of the atom that can be used in a seperate thread.
  Warning: This smart-atom MUST be copied if it will be used concurently. 
  Each thread needs its own copy."
  [a]
  
  (error-check a)
  (swap! (a :smart-obj-atom) smart-object/pointer-copied)
  a)

(defn delete
  "Makes this copy of the atom inactive.
  When all copies of the atom are inactive the object will be deallocated from native memory."
  [a]
  (error-check a)
  (swap! (a :smart-obj-atom) smart-object/pointer-deleted)
  (assoc a :deleted true))



















