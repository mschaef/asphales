(ns asphales.util)

(defn fail [& args]
  (throw (RuntimeException. (apply str args))))

