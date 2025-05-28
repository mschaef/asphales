(ns asphales.core
  (:use asphales.util)
  (:require [asphales.transactions :as transactions]))

(defn mint-coin! [amount owner]
  (transactions/assert-state! {:amount amount :owner owner}))

(defn active-coins
  ([]
   (transactions/active-states))

  ([owner]
   (filter #(= owner (:owner (:body %))) (active-coins))))

(defn merge-coins! [toks]
  (when (> (count toks) 0)
    (let [all-coins (map transactions/fetch-state toks)
          owners (set (map #(:owner (:body %)) all-coins))]
      (when (> (count owners) 1)
        (fail "Cannot merge coins from muliple owners: " owners))
      (doseq [tok toks]
        (transactions/retract-state! tok))
      (mint-coin! (apply + (map #(:amount (:body %)) all-coins))
                  (first owners)))))

(defn split-coin! [tok split-amount]
  (let [coin (transactions/fetch-state tok)
        owner (:owner (:body coin))
        total-amount (:amount (:body coin))]
    (when (> split-amount total-amount)
      (fail "Cannot split more coin (" split-amount ") than available: " total-amount))
    (transactions/retract-state! tok)

    (mint-coin! split-amount owner)
    (mint-coin! (- total-amount split-amount) owner)))

(defn transfer-coin! [tok new-owner]
  (let [coin (transactions/fetch-state tok)]
    (transactions/retract-state! tok)
    (mint-coin! (:amount (:body coin)) new-owner)))

(defn -main
  "I don't do a whole lot."
  []
  (let [s (transactions/init-transaction-store)]
    (transactions/with-transaction s
      (mint-coin! 100 "alice"))
    (transactions/with-transaction s
      (mint-coin! 100 "bob"))
    (transactions/with-transaction s
      (mint-coin! 100 "alice")
      (mint-coin! 100 "charlie"))
    (transactions/with-transaction s
      (merge-coins! (map :id (active-coins "alice"))))
    (transactions/with-transaction s
      (split-coin! (:id (first (active-coins "charlie"))) 12))
    (transactions/with-transaction s
      (transfer-coin! (:id (first (active-coins "bob"))) "david"))
    (transactions/with-transaction s
      (transactions/show-transactions)))
  (println "end run."))
