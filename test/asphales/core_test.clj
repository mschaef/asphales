(ns asphales.core-test
  (:require [clojure.test :refer :all]
            [asphales.core :refer :all]
            [asphales.encode :as encode]
            [asphales.token :as token]))

(deftest memory-storage-get-and-put
  (testing "Stores and retrieves a value"
    (let [store (memory-storage)
          digest (put-data store {:x 3 :y 4})]
      (is (= {:x 3 :y 4} (get-data store digest)))
      (is (= nil (get-data store (token/token "missing-or-invalid-digest")))))))
