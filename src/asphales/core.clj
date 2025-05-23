(ns asphales.core
  (:require [clojure.edn :as edn]
            [asphales.encode :as encode]
            [asphales.token :as token]
            [clj-commons.digest :as digest]))

(def test-values [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)
                  {:type :point
                   :location {:x 3 :y 4}
                   :observers #{"alice" "bob"}}])

(defn encode-binary [value]
  (.getBytes (encode/encode value) "UTF-8"))

(defn decode-binary [bytes]
  (edn/read-string {:readers {'token token/token}}
                   (String. bytes "UTF-8")))

(defprotocol Storage
  (put-data [self data])
  (get-data [self hash]))

(deftype MemoryStorage [store]
  Storage

  (put-data [self data]
    (let [encoded (encode-binary data)
          data-digest (digest/sha256 encoded)]
      (swap! store assoc data-digest encoded)
      (token/token data-digest)))

  (get-data [self data-token]
    (when-let [bytes (get @store (token/token-digest data-token))]
      (decode-binary bytes))))

(defn memory-storage []
  (MemoryStorage. (atom {})))


(defn -main
  "I don't do a whole lot."
  []
  (doseq [value test-values]
    (println (str value " -> " (digest/sha256 (encode-binary value))))))
