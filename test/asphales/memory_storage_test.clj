(ns asphales.memory-storage-test
  (:require [clojure.test :refer :all]
            [asphales.core :refer :all]
            [asphales.token :as token]
            [asphales.storage :as storage]
            [asphales.memory-storage :as memory-storage]))

(deftest memory-storage-get-and-put
  (testing "Stores and retrieves a value"
    (let [store (memory-storage/memory-storage)
          digest (storage/put-edn store {:x 3 :y 4})]
      (is (= {:x 3 :y 4} (storage/get-edn store digest)))
      (is (= nil (storage/get-edn store (token/token "missing-or-invalid-digest")))))))

(deftest memory-storge-root-controls
  (testing "Get and update root"
    (let [store (memory-storage/memory-storage)
          d-1 (storage/put-edn store {:x 3 :y 4})
          d-2 (storage/put-edn store {:x 6 :y 8})]
      (is (= nil (storage/get-root store)))
      (is (= false (storage/update-root store :not-right 12)))
      (is (= true (storage/update-root store nil d-1)))
      (is (= d-1 (storage/get-root store)))

      (is (= false (storage/update-root store :not-right d-2)))
      (is (= true (storage/update-root store d-1 d-2)))
      (is (= d-2 (storage/get-root store))))))
