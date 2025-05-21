(ns asphales.core
  (:require [asphales.encode :as encode]
            [clj-commons.digest :as digest]
            [clojure.edn :as edn]))

(def test-values [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)
                  {:type :point
                   :location {:x 3 :y 4}
                   :observers #{"alice" "bob"}}])

(deftype Token [digest])

(defn token [digest]
  (Token. digest))

(defn token-digest [token]
  (.digest token))

(defmethod print-method Token [token writer]
  (doto writer
    (.write "#token \"")
    (.write (token-digest token))
    (.write "\"")))

(defn encode-binary [value]
  (.getBytes (encode/encode value) "UTF-8"))

(defn decode-binary [bytes]
  (edn/read-string {:readers {'token token}}
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
      (token data-digest)))

  (get-data [self data-token]
    (when-let [bytes (get @store (token-digest data-token))]
      (decode-binary bytes))))

(defn memory-storage []
  (MemoryStorage. (atom {})))


(defn -main
  "I don't do a whole lot."
  []
  (doseq [value test-values]
    (println (str value " -> " (digest/sha256 (encode-binary value))))))
