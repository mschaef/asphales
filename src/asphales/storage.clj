(ns asphales.storage
  (:require [clojure.edn :as edn]
            [clj-commons.digest :as digest]
            [asphales.encode :as encode]
            [asphales.token :as token]))

(defn encode-binary [value]
  (.getBytes (encode/encode value) "UTF-8"))

(defn decode-binary [bytes]
  (edn/read-string {:readers {'token token/token}}
                   (String. bytes "UTF-8")))

(defn digest [bytes]
  (digest/sha256 bytes))

(defprotocol Storage
  (-put-bytes [self data])
  (-get-bytes [self tok])

  (get-root [self])
  (update-root [self current-root new-root]))

(defn put-bytes [store data]
  (-put-bytes store data))

(defn get-bytes [store tok]
  (-get-bytes store tok))

(defn put-edn [store edn]
  (-put-bytes store (encode-binary edn)))

(defn get-edn [store tok]
  (when-let [bytes (-get-bytes store tok)]
    (decode-binary bytes)))
