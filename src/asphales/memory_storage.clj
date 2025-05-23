(ns asphales.memory-storage
  (:require [asphales.storage :as storage]
            [asphales.token :as token]))


(deftype MemoryStorage [store]
  storage/Storage

  (put-data [self data]
    (let [encoded (storage/encode-binary data)
          data-digest (storage/digest encoded)]
      (swap! store assoc data-digest encoded)
      (token/token data-digest)))

  (get-data [self data-token]
    (when-let [bytes (get @store (token/token-digest data-token))]
      (storage/decode-binary bytes))))

(defn memory-storage []
  (MemoryStorage. (atom {})))
