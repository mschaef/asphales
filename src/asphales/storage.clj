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
  (put-data [self data])
  (get-data [self hash])

  (get-root [self])
  (update-root [self current-root new-root]))
