(ns asphales.core
  (:use asphales.util)
  (:require [asphales.transaction-store :as txs]
            [asphales.coin :as coin]))

(defn -main
  "I don't do a whole lot."
  []
  (let [s (txs/init-transaction-store)]
    (txs/with-transaction s
      (coin/mint-coin! 100 "alice"))
    (txs/with-transaction s
      (coin/mint-coin! 100 "bob"))
    (txs/with-transaction s
      (coin/mint-coin! 100 "alice")
      (coin/mint-coin! 100 "charlie"))
    (txs/with-transaction s
      (coin/merge-coins! (map :id (coin/active-coins "alice"))))
    (txs/with-transaction s
      (coin/split-coin! (:id (first (coin/active-coins "charlie"))) 12))
    (txs/with-transaction s
      (coin/transfer-coin! (:id (first (coin/active-coins "bob"))) "david"))
    (txs/with-transaction s
      (txs/show-transactions)))
  (println "end run."))
