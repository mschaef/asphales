(ns asphales.coin
  (:use asphales.util)
  (:require [asphales.transaction-store :as txs]))

(defn mint-coin! [amount owner]
  (txs/assert-state! {:amount amount :owner owner}))

(defn active-coins
  ([]
   (txs/active-states))

  ([owner]
   (filter #(= owner (:owner (:body %))) (active-coins))))

 (defn merge-coins! [toks]
  (when (> (count toks) 0)
    (let [all-coins (map txs/fetch-state toks)
          owners (set (map #(:owner (:body %)) all-coins))]
      (when (> (count owners) 1)
        (fail "Cannot merge coins from muliple owners: " owners))
      (doseq [tok toks]
        (txs/retract-state! tok))
      (mint-coin! (apply + (map #(:amount (:body %)) all-coins))
                  (first owners)))))

(defn split-coin! [tok split-amount]
  (let [coin (txs/fetch-state tok)
        owner (:owner (:body coin))
        total-amount (:amount (:body coin))]
    (when (> split-amount total-amount)
      (fail "Cannot split more coin (" split-amount ") than available: " total-amount))
    (txs/retract-state! tok)

    (mint-coin! (- total-amount split-amount) owner)
    (mint-coin! split-amount owner)))

(defn transfer-coin! [tok new-owner]
  (let [coin (txs/fetch-state tok)]
    (txs/retract-state! tok)
    (mint-coin! (:amount (:body coin)) new-owner)))
