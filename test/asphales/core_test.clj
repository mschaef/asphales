(ns asphales.core-test
  (:require [clojure.test :refer :all]
            [asphales.core :refer :all]
            [asphales.token :as token]
            [asphales.storage :as storage]
            [asphales.memory-storage :as memory-storage]))

(deftest memory-storage-get-and-put
  (testing "Stores and retrieves a value"
    (let [store (memory-storage/memory-storage)
          digest (storage/put-data store {:x 3 :y 4})]
      (is (= {:x 3 :y 4} (storage/get-data store digest)))
      (is (= nil (storage/get-data store (token/token "missing-or-invalid-digest")))))))
