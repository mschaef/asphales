(ns asphales.memory-storage
  (:require [asphales.storage :as storage]
            [asphales.token :as token]))


(deftype MemoryStorage [root store]
  storage/Storage

  (-put-bytes [self data]
    (let [data-digest (storage/digest data)]
      (swap! store assoc data-digest data)
      (token/token data-digest)))

  (-get-bytes [self tok]
    (get @store (token/token-digest tok)))

  (-get-root [self]
    @root)

  (-update-root [self current-root new-root]
    (compare-and-set! root current-root new-root)))

(defn memory-storage []
  (MemoryStorage. (atom nil) (atom {})))
