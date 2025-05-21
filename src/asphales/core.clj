(ns asphales.core
  (:use [asphales.encode :as encode]))

(defn -main
  "I don't do a whole lot."
  []
  (doseq [x [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)]]
    (println (str x " -> " (encode/encode x)))))
