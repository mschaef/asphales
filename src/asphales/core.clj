(ns asphales.core
  (:require [asphales.encode :as encode]
            [clj-commons.digest :as digest]
            [clojure.edn :as edn]))

(def test-values [nil 1 "hello" [] [1] [1 2 3] () '(1 2 3)
                  {:type :point
                   :location {:x 3 :y 4}
                   :observers #{"alice" "bob"}}])

(defn encode-binary [value]
  (.getBytes (encode/encode value) "UTF-8"))

(defn decode-binary [bytes]
  (edn/read-string (String. bytes "UTF-8")))

(defprotocol Storage
  (put-data [self data])
  (get-data [self hash]))

(deftype MemoryStorage [store]
  Storage

  (put-data [self data]
    (let [encoded (encode-binary data)
          data-digest (digest/sha256 encoded)]
      (swap! store assoc data-digest encoded)
      data-digest))

  (get-data [self data-digest]
    (when-let [bytes (get @store data-digest)]
      (decode-binary bytes))))

(defn memory-storage []
  (MemoryStorage. (atom {})))


(defn -main
  "I don't do a whole lot."
  []
  (doseq [value test-values]
    (println (str value " -> " (digest/sha256 (encode-binary value))))))
