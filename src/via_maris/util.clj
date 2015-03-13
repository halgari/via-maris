(ns via-maris.util
  (:refer-clojure :exclude [conj!]))

(defn conj!
  ([] (transient []))
  ([acc] (persistent! []))
  ([acc item] (clojure.core/conj! acc item)))
