(ns asphales.transaction-store-test
  (:require [clojure.test :refer :all]
            [asphales.coin :refer :all]
            [asphales.transaction-store :refer :all]
            [asphales.storage :as storage]))

(deftest initial-state
  (testing "Initial transaction store has only the genesis block."
    (let [s (init-transaction-store)]
      (is (= 1 (count (transaction-seq s))))

      (let [tx (first (transaction-seq s))]
        (is (= nil (:previous tx)))
        (is (= #{} (:active-states tx)))))))


(deftest assert-and-fetch
  (testing "A state can be asserted and retrieved within the same transaction."
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [tok (assert-state! {:magic-word "xyzzy"})]
          (is (= "xyzzy" (:magic-word (:body (fetch-state tok)))))))))

  (testing "A state can be asserted in one transaction and retrieved within a subsequent transaction."
    (let [s (init-transaction-store)
          tok (with-transaction s
                (assert-state! {:magic-word "xyzzy"}))]
      (with-transaction s
        (is (= "xyzzy" (:magic-word (:body (fetch-state tok))))))))

  (testing "Multiple states with the same value can be stored"
    (let [s (init-transaction-store)
          [tok-1 tok-2] (with-transaction s
                          [(assert-state! {:magic-word "xyzzy"})
                           (assert-state! {:magic-word "xyzzy"})])]
      (is (not (= tok-1 tok-2))))))

(deftest retract-state
  (testing "A state that has been retracted cannot be retrieved within the same transaction."
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [tok (assert-state! {:magic-word "xyzzy"})]
          (retract-state! tok)
          (is (thrown? RuntimeException (fetch-state tok)))))))

  (testing "A state that has been retracted cannot be retracted again within the same transaction."
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [tok (assert-state! {:magic-word "xyzzy"})]
          (retract-state! tok)
          (is (thrown? RuntimeException (retract-state! tok)))))))

  (testing "A state that has been retracted cannot be retrieved within a subsequent transaction."
    (let [s (init-transaction-store)
          tok (with-transaction s
                (assert-state! {:magic-word "xyzzy"}))]
      (with-transaction s
        (retract-state! tok))
      (with-transaction s
        (is (thrown? RuntimeException (fetch-state tok))))))

  (testing "A state that has been retracted cannot be retracte again within a subsequent transaction."
    (let [s (init-transaction-store)
          tok (with-transaction s
                (assert-state! {:magic-word "xyzzy"}))]
      (with-transaction s
        (retract-state! tok))
      (with-transaction s
        (is (thrown? RuntimeException (retract-state! tok)))))))


(deftest active-state-inspection-by-id
  (testing "A ledger starts out without any active state"
    (let [s (init-transaction-store)]
      (with-transaction s
        (is (= #{} (active-state-ids))))))

  (testing "Once asserted, a state is listed by active-state-ids"
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [tok (assert-state! {:magic-word "xyzzy"})]
          (println tok)
          (is (= #{tok} (active-state-ids)))))))

  (testing "Once asserted, mulitple states are listed by active-state-ids"
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [[tok-1 tok-2 tok-3] [(assert-state! {:n 1})
                                   (assert-state! {:n 2})
                                   (assert-state! {:n 3})]]
          (is (= #{tok-1 tok-2 tok-3} (active-state-ids)))))))

  (testing "After being asserted and retracted within a transaction, a state is not listed by active-state-id"
    (let [s (init-transaction-store)]
      (with-transaction s
        (let [[tok-1 tok-2 tok-3] [(assert-state! {:n 1})
                                   (assert-state! {:n 2})
                                   (assert-state! {:n 3})]]
          (retract-state! tok-2)
          (is (= #{tok-1 tok-3} (active-state-ids)))
          (retract-state! tok-1)
          (retract-state! tok-3)
          (is (= #{} (active-state-ids)))))))

  (testing "After being asserted and retracted, a state is not listed by active-state-id"
    (let [s (init-transaction-store)
          [tok-1 tok-2 tok-3] (with-transaction s
                                [(assert-state! {:n 1})
                                 (assert-state! {:n 2})
                                 (assert-state! {:n 3})])]
      (with-transaction s
        (retract-state! tok-2))
      (with-transaction s
        (is (= #{tok-1 tok-3} (active-state-ids))))
      (with-transaction s
        (retract-state! tok-1)
        (retract-state! tok-3))
      (with-transaction s
        (is (= #{} (active-state-ids)))))))

(deftest version-conflict-detection
  (testing "Transactions fail if there is a version conflict"
    (let [s (init-transaction-store)
          genesis-transaction-tok (storage/get-root s)]
      (with-transaction s
        (assert-state! {:n 1}))
      (is (thrown? RuntimeException
                   (with-transaction s
                     ;; Force a version conflict by updating the
                     ;; storage root to point to some other token.
                     (storage/update-root s (storage/get-root s) genesis-transaction-tok)))))))

(deftest nested-transaction-detection
  (testing "Nested transactions are not supported and fail if attempted."
    (let [s (init-transaction-store)]
      (with-transaction s
        (is (thrown? RuntimeException
                     (with-transaction s
                       (assert-state! {:n 1}))))))))
