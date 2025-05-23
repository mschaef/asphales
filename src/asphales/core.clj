(ns asphales.core
  (:require [asphales.encode :as encode]
            [asphales.token :as token]
            [clj-commons.digest :as digest]))

(def test-values [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)
                  {:type :point
                   :location {:x 3 :y 4}
                   :observers #{"alice" "bob"}}])

(defprotocol Storage
  (put-data [self data])
  (get-data [self hash]))

(deftype MemoryStorage [store]
  Storage

  (put-data [self data]
    (let [encoded (encode/encode-binary data)
          data-digest (digest/sha256 encoded)]
      (swap! store assoc data-digest encoded)
      (token/token data-digest)))

  (get-data [self data-token]
    (when-let [bytes (get @store (token/token-digest data-token))]
      (encode/decode-binary bytes))))

(defn memory-storage []
  (MemoryStorage. (atom {})))


(defn -main
  "I don't do a whole lot."
  []
  (doseq [value test-values]
    (println (str value " -> " (digest/sha256 (encode/encode-binary value))))))
