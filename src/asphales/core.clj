(ns asphales.core
  (:require [asphales.storage :as storage]
            [asphales.memory-storage :as memory-storage]))

(defn fail [& args]
  (throw (RuntimeException. (apply str args))))

(defn genesis []
  {:active-states #{}
   :previous nil})

(defn init-transaction-store []
  (let [store (memory-storage/memory-storage)
        genesis-tok (storage/put-edn store (genesis))]
    (storage/update-root store nil genesis-tok)
    store))

(def ^:dynamic *tx-store* nil)
(def ^:dynamic *tx-state* nil)

(defn call-with-transaction [store tx-fn]
  (when *tx-store*
    (fail "Transactions cannot be nested." *tx-store*))
  (let [initial-state-tok (storage/get-root store)
        initial-state (storage/get-edn store initial-state-tok)]
    (binding [*tx-store* store
              *tx-state* (atom initial-state)]
      (let [retval (tx-fn)]
        (if (storage/update-root store initial-state-tok (storage/put-edn store (assoc @*tx-state* :previous initial-state-tok)))
          retval
          (fail "Version conflict committing transation."))))))

(defmacro with-transaction [store & body]
  `(call-with-transaction ~store (fn [] ~@body)))

(defn assert-state! [body]
  (let [tok (storage/put-edn *tx-store* {:body body})]
    (swap! *tx-state*
           (fn [state tok]
             (update-in state [:active-states] conj tok))
           tok)
    tok))

(defn retract-state! [tok]
  (if (get-in *tx-state* [:active-states tok])
    (swap! *tx-state*
           (fn [state tok]
             (update-in state [:active-states] disj tok))
           tok)
    (fail "State not active: " tok)))

(defn transaction-seq
  ([store ]
   (transaction-seq store (storage/get-root store)))

  ([store tok]
   (if tok
     (let [transaction (storage/get-edn store tok)]
       (cons (assoc transaction :token tok) (lazy-seq (transaction-seq store (:previous transaction))))))))

(defn show-transactions [ store ]
  (doseq [txn (transaction-seq store)]
    (println (:token txn))
    (doseq [s (:active-states txn)]
      (println "   " (storage/get-edn store s)))
    (println)))


(defn mint-coins [amount owner]
  (assert-state! {:amount amount :owner owner}))

(defn -main
  "I don't do a whole lot."
  []
  (let [s (init-transaction-store)]
    (with-transaction s
      (mint-coins 100 "alice"))
    (with-transaction s
      (mint-coins 100 "bob"))
    (with-transaction s
      (mint-coins 100 "charlie"))
    (show-transactions s))
  (println "end run."))
