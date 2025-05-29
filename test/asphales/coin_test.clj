(ns asphales.coin-test
  (:require [clojure.test :refer :all]
            [asphales.coin :refer :all]
            [asphales.transaction-store :as txs]))

(deftest coin-minting
  (testing "Single coin captures properties set at minting"
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
      (txs/with-transaction s
        (let [coin (first (active-coins "alice"))]
          (is (= tok (:id coin)))
          (is (= "alice" (:owner (:body coin))))
          (is (= 100 (:amount (:body coin))))))))

  (testing "It is possible to mint two identical coins"
    (let [s (txs/init-transaction-store)]
      (txs/with-transaction s
        (mint-coin! 100 "alice")
        (mint-coin! 100 "alice")

        (is (= 2 (count (active-coins "alice")))))))

  (testing "Active coins are filtered by party name"
    (let [s (txs/init-transaction-store)]
      (txs/with-transaction s
        (mint-coin! 100 "alice")
        (mint-coin! 100 "alice")
        (mint-coin! 100 "bob")

        (is (= 3 (count (active-coins))))
        (is (= 2 (count (active-coins "alice"))))
        (is (= 1 (count (active-coins "bob"))))
        (is (= 0 (count (active-coins "charlie")))))))

  (testing "Coins can be retracted once only"
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
      (txs/with-transaction s
        (is (= 1 (count (active-coins "alice"))))
        (txs/retract-state! tok)
        (is (= 0 (count (active-coins "alice"))))
        (is (thrown? RuntimeException (txs/retract-state! tok))))

      (txs/with-transaction s
        (is (= 0 (count (active-coins "alice")))))))

  (testing "Exceptions within transactions cause transactions to be aborted."
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
      (is (thrown? RuntimeException
                   (txs/with-transaction s
                     (is (= 1 (count (active-coins "alice"))))
                     (txs/retract-state! tok)
                     (is (= 0 (count (active-coins "alice"))))
                     (txs/retract-state! tok))))
      (txs/with-transaction s
        (is (= 1 (count (active-coins "alice")))))))

  (testing "Coins can be transferred from one owner to another"
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
       (txs/with-transaction s
         (transfer-coin! tok "bob"))

       (txs/with-transaction s
         (is (= 0 (count (active-coins "alice"))))
         (let [coin (first (active-coins))]
           (is (= "bob" (:owner (:body coin))))
           (is (= 100 (:amount (:body coin))))))))

  (testing "Coins can be merged if they are of the same owner"
    (let [s (txs/init-transaction-store)]
      (txs/with-transaction s
        (mint-coin! 100 "alice")
        (mint-coin! 150 "alice"))
      (txs/with-transaction s
        (is (= 2 (count (active-coins))))
        (merge-coins! (map :id (active-coins)))
        (is (= 1 (count (active-coins)))))
      (txs/with-transaction s
        (let [coin (first (active-coins "alice"))]
          (is (= "alice" (:owner (:body coin))))
          (is (= 250 (:amount (:body coin))))))))

  (testing "Coins cannot be merged if they are of different owners"
    (let [s (txs/init-transaction-store)]
       (txs/with-transaction s
         (mint-coin! 100 "alice")
         (mint-coin! 150 "bob"))
       (txs/with-transaction s
         (is (thrown? RuntimeException (merge-coins! (map :id (active-coins))))))))

  (testing "Coins can be split into two"
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
       (txs/with-transaction s
         (is (= 1 (count (active-coins))))
         (let [tok (split-coin! tok 12)
               coin (txs/fetch-state tok)]
           (is (= 2 (count (active-coins))))
           (is (= "alice" (:owner (:body coin))))
           (is (= 12 (:amount (:body coin))))
           (transfer-coin! tok "bob")))

      (txs/with-transaction s
        (let [coin (first (active-coins "alice"))]
          (is (= "alice" (:owner (:body coin))))
          (is (= 88 (:amount (:body coin))))))))

  (testing "Coins cannot be split into two if the split amount is greater than the total value"
    (let [s (txs/init-transaction-store)
          tok (txs/with-transaction s
                (mint-coin! 100 "alice"))]
       (txs/with-transaction s
         (is (thrown? RuntimeException (split-coin! tok 120)))))))
