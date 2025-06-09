(ns asphales.transaction-store-test
  (:require [clojure.test :refer :all]
            [asphales.coin :refer :all]
            [asphales.transaction-store :refer :all]))

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
